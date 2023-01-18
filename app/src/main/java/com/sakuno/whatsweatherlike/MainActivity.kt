package com.sakuno.whatsweatherlike

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.baidu.location.LocationClient
import com.google.gson.Gson
import com.sakuno.whatsweatherlike.customwidgets.*
import com.sakuno.whatsweatherlike.utils.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt

class MainActivity : Activity() {

    var caiyunWeatherKey = "mkhvpq9w0AsN6gjl"

//    private var baiduAK = "7G00KgUlyZW6DnNI2lM0Xr4NNcP0sqWk"

    private var locationClient: LocationClient? = null

    private var nightMode = false

    private var addCityDialog: Dialog? = null

    private var weatherInfoPreferences: SharedPreferences? = null

    private var citiesPreferences: SharedPreferences? = null

    private var customCityList: CustomCities? = null

    private var cityListAvailable = false

    private var cityList: CityList? = null

    private var mainCardView: ViewPager2? = null

    private var fleshDefaultIndex: Int = 0

    private var mutableCardView: MutableMap<Int, View> = mutableMapOf()

    private var refreshLayout: BetterSwipeRefreshLayout? = null

    private var customCitiesFromPreferences: CustomCities
        get() = Gson().fromJson(
            citiesPreferences!!.getString("cities", "") ?: "", CustomCities::class.java
        ) ?: CustomCities(listOf())
        set(value) {
            val editor = citiesPreferences?.edit()
            editor?.putString("cities", Gson().toJson(value, CustomCities::class.java))
            editor?.apply()
        }

    private var favoriteCity: City
        get() = Gson().fromJson(
            citiesPreferences!!.getString("favorite_city", "") ?: "", City::class.java
        )
        set(value) {
            val editor = citiesPreferences?.edit()
            editor?.putString("favorite_city", Gson().toJson(value, City::class.java))
            editor?.apply()
        }

    private var favoriteCityIndex: Int
        get() = try {
            customCityList!!.cities.indexOf(favoriteCity)
                .takeIf { it in customCityList!!.cities.indices } ?: 0
        } catch (_: Exception) {
            0
        }
        set(value) {
            favoriteCity = customCityList!!.cities[value]
        }

    private val permissionRequestCode = 1000

    private val permissionList = listOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_WIFI_STATE,
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.CHANGE_WIFI_STATE,
        android.Manifest.permission.INTERNET
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAndGetPermissions()

        window.statusBarColor = getColor(R.color.black_translation)

        nightMode =
            (this@MainActivity.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager).nightMode == UiModeManager.MODE_NIGHT_YES

        try {
            weatherInfoPreferences = getSharedPreferences("weather", Context.MODE_PRIVATE)
            citiesPreferences = getSharedPreferences("cities", Context.MODE_PRIVATE)
            customCityList = customCitiesFromPreferences
            if (customCityList!!.check()) customCitiesFromPreferences = customCityList!!
        } catch (_: Exception) {
            Toast.makeText(this, "原始应用数据已损坏\n正在重新创建数据", Toast.LENGTH_LONG).show()
            customCityList = CustomCities(listOf())
            customCityList!!.check()
            customCitiesFromPreferences = customCityList!!
        }

        LocationClient.setAgreePrivacy(true)

        CityWeatherModel.intervalOfCheckInformationAcquisition = 1000

        try {
            locationClient = LocationClient(this)
            locationClient!!.registerLocationListener(CityWeatherModel.localPositionListener)
        } catch (_: Exception) {
            Log.d("BaiduLocation", "请同意百度隐私合规接口")
        }

        Thread { initCityList() }.start()

        findViewById<ImageButton>(R.id.add_city_btn).setOnClickListener {
            showAddCityDialog()
        }

        findViewById<ImageButton>(R.id.menu_btn).setOnClickListener {
            startActivityForResult(
                Intent(this@MainActivity, CardManageActivity::class.java), 3
            )
        }

        mainCardView = findViewById(R.id.vp_cardsView)

        fleshDefaultIndex = favoriteCityIndex

        refreshLayout = findViewById(R.id.rfl_fresh_layout)

        refreshLayout!!.run {
            setOnRefreshListener {
                fleshDefaultIndex = mainCardView!!.currentItem
                fleshData()
            }
            isRefreshing = true
            fleshData()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            3 -> {
                if(resultCode != 1){
                    customCityList = customCitiesFromPreferences
                    fleshDefaultIndex = favoriteCityIndex
                    fleshData()
                }
            }
        }
    }

    private fun initCityList() {

        Log.d("ASSETS_READER", "START_GET_ASSETS")

        val stringBuilder = StringBuilder()
        try {
            val bf =
                BufferedReader(InputStreamReader(resources.assets.open("BaiduMap_cityCenter.json")))
            while (true) {
                val line: String = bf.readLine() ?: break
                stringBuilder.append(line)
            }

        } catch (_: IOException) {
        }

        val res = stringBuilder.toString()

        Log.d("ASSETS_READER", res)

        try {
            cityList = CityList.fromJsonString(res)
            cityListAvailable = true
        } catch (_: Exception) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "城市列表已损坏", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndGetPermissions() {

        val packageManager = packageManager
        var permissionInfo: PermissionInfo? = null

        if (!getEnoughPermissions()) {
            AlertDialog.Builder(this).setTitle("提示").setMessage(
                "本应用需要授予以下权限以获取到较为准确的天气预测\n" + "\n" + "    · 精确位置\n" + "    · 读取与修改 WIFI 状态\n" + "    · 访问互联网\n" + "\n" + "请在按下确定按钮之后同意权限请求"
            ).setPositiveButton("确定") { _, _ ->
                for (it in permissionList) {
                    try {
                        permissionInfo = packageManager.getPermissionInfo(it, 0)
                    } catch (e: PackageManager.NameNotFoundException) {
                        e.printStackTrace()
                    }
                    if (ContextCompat.checkSelfPermission(
                            this, it
                        ) != PackageManager.PERMISSION_GRANTED
                    ) ActivityCompat.requestPermissions(
                        this, permissionList.toTypedArray(), permissionRequestCode
                    )
                    else Log.d(
                        "Permission",
                        "Permission [${permissionInfo?.loadLabel(packageManager) ?: "NULL"}] Has Been Obtained"
                    )
                }
            }.create().show()

            Thread {
                Log.d("Permission", "Trying Get Permission...")
                while (!getEnoughPermissions()) Thread.sleep(500)
                Log.d("Permission", "Permission Got Enough!")
                runOnUiThread { fleshData() }
            }.start()
        }
    }

    private fun getEnoughPermissions(): Boolean {
        for (it in permissionList) {
            if (ContextCompat.checkSelfPermission(
                    this, it
                ) != PackageManager.PERMISSION_GRANTED
            ) return false
        }
        return true
    }

    private fun fleshData() {
        if (!getEnoughPermissions()) return

        Log.d("BaiduLocation", "Start Locate")
        locationClient?.start()

        mainCardView!!.adapter = this.UserCitiesAdapter(
            (customCityList ?: CustomCities(listOf())).cities
        )

        refreshLayout?.isRefreshing = false

        mainCardView!!.startAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_show))

        mainCardView!!.currentItem = fleshDefaultIndex
        fleshDefaultIndex = favoriteCityIndex
    }

    private fun addCity(city: City) {
        customCityList!!.cities = customCityList!!.cities.toMutableList().run {
            add(city)
            toList()
        }
        customCityList!!.check()
        customCitiesFromPreferences = customCityList!!
    }

    private fun removeCity(index: Int) {
        customCityList!!.cities = customCityList!!.cities.toMutableList().run {
            if (index in 1 until size) removeAt(index)
            toList()
        }
        customCityList!!.check()
        customCitiesFromPreferences = customCityList!!
    }

    private fun showAddCityDialog() =
        (addCityDialog.takeIf { it != null } ?: initAddCityDialog())?.show()

    private fun initAddCityDialog(): Dialog? {

        if (!cityListAvailable) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "城市列表已损坏", Toast.LENGTH_SHORT).show()
            }
            return addCityDialog
        }

        addCityDialog = Dialog(this@MainActivity, R.style.dialog_bottom_full)
        addCityDialog!!.setCanceledOnTouchOutside(true)
        addCityDialog!!.setCancelable(true)

        val window = addCityDialog!!.window!!
        window.setGravity(Gravity.BOTTOM)
        window.setWindowAnimations(R.style.share_animation)

        View.inflate(this@MainActivity, R.layout.add_city_dialog, null).run {
            findViewById<ImageButton>(R.id.addcity_btn_back).setOnClickListener {
                addCityDialog!!.hide()
            }
            initAddCityView(this)
            window.setContentView(this)
        }
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT
        )

        return addCityDialog
    }

    private fun initAddCityView(view: View) {

        view.findViewById<LinearLayout>(R.id.addcity_ll_background).background = getDrawable(
            this, if (nightMode) R.drawable.night_mode_half_radius_rectangle
            else R.drawable.day_mode_half_radius_rectangle
        )

        val searchResultLayout = view.findViewById<LinearLayout>(R.id.addcity_ll_search_result)

        view.findViewById<EditText>(R.id.addcity_et_city_name)
            .addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(textView: Editable?) {
                    val searchKey = textView.toString()

                    if (searchKey.isEmpty()) {
                        return
                    }

                    searchResultLayout.removeAllViews()

                    val searchRes = cityList!!.searchCity(searchKey)

                    if (searchRes.isEmpty()) {
                        return
                    }

                    for (each in searchRes) View.inflate(
                        this@MainActivity, R.layout.add_city_result_item, null
                    ).run {
                        isClickable = true
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, dp2px(64)
                        ).run {
                            setMargins(0, dp2px(4), 0, dp2px(4))
                            this
                        }
                        findViewById<TextView>(R.id.addcityres_tv_result_name).text = each.showName
                        findViewById<TextView>(R.id.addcityres_tv_longitude).text =
                            each.longitude.toString()
                        findViewById<TextView>(R.id.addcityres_tv_latitude).text =
                            each.latitude.toString()

                        setOnClickListener {
                            val sameCityIndex = customCityList!!.cities.indexOf(each)
                            if (sameCityIndex >= 0) {
                                AlertDialog.Builder(this@MainActivity).setTitle("提示")
                                    .setMessage("该城市已存在，详见第 ${sameCityIndex + 1} 张卡片")
                                    .setPositiveButton("确定") { _, _ ->
                                        fleshDefaultIndex = sameCityIndex
                                        fleshData()
                                        addCityDialog!!.hide()
                                    }.show()
                            } else {
                                addCity(each)
                                fleshDefaultIndex = customCityList!!.cities.size - 1
                                fleshData()
                                addCityDialog!!.hide()
                            }
                        }
                        searchResultLayout.addView(this)
                    }

                    return
                }
            })

        view.findViewById<EditText>(R.id.addcity_et_city_name)
            .setOnEditorActionListener { textView, _, _ ->

                val searchKey = textView.text.toString()

                if (searchKey.isEmpty()) {
                    Toast.makeText(this@MainActivity, "城市名不能为空", Toast.LENGTH_SHORT).show()
                    return@setOnEditorActionListener true
                }

                searchResultLayout.removeAllViews()

                val searchRes = cityList!!.searchCity(searchKey)

                if (searchRes.isEmpty()) {
                    Toast.makeText(this@MainActivity, "搜索结果为空", Toast.LENGTH_SHORT).show()
                    return@setOnEditorActionListener true
                }

                for (each in searchRes) View.inflate(
                    this@MainActivity, R.layout.add_city_result_item, null
                ).run {
                    isClickable = true
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp2px(64)
                    ).run {
                        setMargins(0, dp2px(4), 0, dp2px(4))
                        this
                    }
                    findViewById<TextView>(R.id.addcityres_tv_result_name).text = each.showName
                    findViewById<TextView>(R.id.addcityres_tv_longitude).text =
                        each.longitude.toString()
                    findViewById<TextView>(R.id.addcityres_tv_latitude).text =
                        each.latitude.toString()

                    setOnClickListener {
                        val sameCityIndex = customCityList!!.cities.indexOf(each)
                        if (sameCityIndex >= 0) {
                            AlertDialog.Builder(this@MainActivity).setTitle("提示")
                                .setMessage("该城市已存在，详见第 ${sameCityIndex + 1} 张卡片")
                                .setPositiveButton("确定") { _, _ ->
                                    fleshDefaultIndex = sameCityIndex
                                    fleshData()
                                    addCityDialog!!.hide()
                                }.show()
                        } else {
                            addCity(each)
                            fleshDefaultIndex = customCityList!!.cities.size - 1
                            fleshData()
                            addCityDialog!!.hide()
                        }
                    }
                    searchResultLayout.addView(this)
                }

                return@setOnEditorActionListener false
            }
    }

    @SuppressLint("SetTextI18n")
    private fun initDetailInfo(model: CityWeatherModel, view: View) {
        view.findViewById<LinearLayout>(R.id.detail_ll_background).background =
            getDrawable(this@MainActivity,
                R.drawable.night_mode_half_radius_rectangle.takeIf { nightMode }
                    ?: R.drawable.day_mode_half_radius_rectangle)

        val weather = model.weatherInfo?.result
        if (weather != null) runOnUiThread {

            view.findViewById<TextView>(R.id.detail_tv_city_title).text = model.cityName

            // init AQI card

            view.findViewById<AqiScaler>(R.id.detail_card_aqi).apply {
                systemNightMode = nightMode
                availableValue = weather.realtime.airQuality.aqi.chn.toFloat()
                scaleGroup = arrayOf(AqiScaler.ScaleData(0f, "优") {
                    if (it) getColor(R.color.aqi_excellent_night) else getColor(R.color.aqi_excellent_day)
                }, AqiScaler.ScaleData(
                    50f, "良"
                ) {
                    if (it) getColor(R.color.aqi_great_night) else getColor(R.color.aqi_great_day)
                }, AqiScaler.ScaleData(
                    100f, "轻"
                ) {
                    if (it) getColor(R.color.aqi_mild_night) else getColor(R.color.aqi_mild_day)
                }, AqiScaler.ScaleData(
                    150f, "中"
                ) {
                    if (it) getColor(R.color.aqi_moderate_night) else getColor(R.color.aqi_moderate_day)
                }, AqiScaler.ScaleData(
                    200f, "重"
                ) {
                    if (it) getColor(R.color.aqi_heavy_night) else getColor(R.color.aqi_heavy_day)
                }, AqiScaler.ScaleData(
                    300f, "严"
                ) {
                    if (it) getColor(R.color.aqi_severe_night) else getColor(R.color.aqi_severe_day)
                })
                applyChanges()
            }

            val iaqiDataMap = AqiCalculator(
                weather.realtime.airQuality.pm25,
                weather.realtime.airQuality.pm10,
                weather.realtime.airQuality.o3,
                weather.realtime.airQuality.so2,
                weather.realtime.airQuality.no2,
                weather.realtime.airQuality.co
            ).toMap()

            for (it in arrayOf<IaqiCard>(
                view.findViewById(R.id.detail_ic_pm25),
                view.findViewById(R.id.detail_ic_pm10),
                view.findViewById(R.id.detail_ic_so2),
                view.findViewById(R.id.detail_ic_co),
                view.findViewById(R.id.detail_ic_no2),
                view.findViewById(R.id.detail_ic_o3)
            )) it.apply {
                dataResource = when (it.id) {
                    R.id.detail_ic_pm25 -> IaqiCard.IaqiDataResource(
                        drawableToBitmap(
                            getDrawable(
                                this@MainActivity, R.drawable.logo_pm25
                            )!!
                        ),
                        getStringResource("PM25"),
                        iaqiDataMap[AqiCalculator.GasType.PM25]!!.second.toString()
                            .run { if (length > 4) split('.')[0] else this },
                        iaqiDataMap[AqiCalculator.GasType.PM25]!!.third.toInt().toString(),
                        iaqiDataMap[AqiCalculator.GasType.PM25]!!.first
                    )
                    R.id.detail_ic_pm10 -> IaqiCard.IaqiDataResource(
                        drawableToBitmap(
                            getDrawable(
                                this@MainActivity, R.drawable.logo_pm10
                            )!!
                        ),
                        getStringResource("PM10"),
                        iaqiDataMap[AqiCalculator.GasType.PM10]!!.second.toString()
                            .run { if (length > 4) split('.')[0] else this },
                        iaqiDataMap[AqiCalculator.GasType.PM10]!!.third.toInt().toString(),
                        iaqiDataMap[AqiCalculator.GasType.PM10]!!.first
                    )
                    R.id.detail_ic_o3 -> IaqiCard.IaqiDataResource(
                        drawableToBitmap(getDrawable(this@MainActivity, R.drawable.logo_o3)!!),
                        getStringResource("O3"),
                        iaqiDataMap[AqiCalculator.GasType.O3]!!.second.toString()
                            .run { if (length > 4) split('.')[0] else this },
                        iaqiDataMap[AqiCalculator.GasType.O3]!!.third.toInt().toString(),
                        iaqiDataMap[AqiCalculator.GasType.O3]!!.first
                    )
                    R.id.detail_ic_so2 -> IaqiCard.IaqiDataResource(
                        drawableToBitmap(getDrawable(this@MainActivity, R.drawable.logo_so2)!!),
                        getStringResource("SO2"),
                        iaqiDataMap[AqiCalculator.GasType.SO2]!!.second.toString()
                            .run { if (length > 4) split('.')[0] else this },
                        iaqiDataMap[AqiCalculator.GasType.SO2]!!.third.toInt().toString(),
                        iaqiDataMap[AqiCalculator.GasType.SO2]!!.first
                    )
                    R.id.detail_ic_no2 -> IaqiCard.IaqiDataResource(
                        drawableToBitmap(getDrawable(this@MainActivity, R.drawable.logo_no2)!!),
                        getStringResource("NO2"),
                        iaqiDataMap[AqiCalculator.GasType.NO2]!!.second.toString()
                            .run { if (length > 4) split('.')[0] else this },
                        iaqiDataMap[AqiCalculator.GasType.NO2]!!.third.toInt().toString(),
                        iaqiDataMap[AqiCalculator.GasType.NO2]!!.first
                    )
                    R.id.detail_ic_co -> IaqiCard.IaqiDataResource(
                        drawableToBitmap(getDrawable(this@MainActivity, R.drawable.logo_co)!!),
                        getStringResource("CO"),
                        iaqiDataMap[AqiCalculator.GasType.CO]!!.second.toString()
                            .run { if (length > 4) split('.')[0] else this },
                        iaqiDataMap[AqiCalculator.GasType.CO]!!.third.toInt().toString(),
                        iaqiDataMap[AqiCalculator.GasType.CO]!!.first
                    )
                    else -> null
                }
                systemNightMode = nightMode
                night_mode_main_bgColor = getColor(R.color.iaqi_main_bg_night)
                day_mode_main_bgColor = getColor(R.color.iaqi_main_bg_day)
                applyChanges()
            }


            // init daily weather forecast card

            val dailyWeatherInfoList = mutableListOf<LineChartForDailyWeather.ResourceInfo>()

            val numberOfDailyWeatherData = arrayOf(
                weather.daily.temperature.size,
                weather.daily.skycon08h20h.size,
                weather.daily.skycon20h32h.size
            ).min()

            Log.d("Daily Forecast", "numberOfData: $numberOfDailyWeatherData")

            for (i in 0 until numberOfDailyWeatherData) {

                val cal = Calendar.getInstance()
                val dateName: String
                val isToday: (Calendar) -> Boolean = {
                    val today = Calendar.getInstance()
                    it.get(Calendar.YEAR) == today.get(Calendar.YEAR) && it.get(Calendar.MONTH) == today.get(
                        Calendar.MONTH
                    ) && it.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
                }

                MyTime.MyDateTimeAuxiliary.fromCaiyunDatetimeString(weather.daily.temperature[i].date)
                    .run {
                        cal.set(year, month - 1, day)
                        dateName = toDateString()
                    }

                dailyWeatherInfoList += LineChartForDailyWeather.ResourceInfo(
                    dayOfWeek = if (isToday(cal)) "今天" else getStringResource(
                        "dayOfWeek${
                            cal.get(
                                Calendar.DAY_OF_WEEK
                            ) - 1
                        }"
                    ),
                    dayOfMonth = dateName,
                    skyCondition_08_20 = getStringResource(weather.daily.skycon08h20h[i].value),
                    skyCondition_20_32 = getStringResource(weather.daily.skycon20h32h[i].value),
                    weatherBitmap_08_20 = drawableToBitmap(
                        getDrawable(
                            this,
                            CityWeatherModel.toWeatherIcon(weather.daily.skycon08h20h[i].value)
                        )!!
                    ),
                    weatherBitmap_20_32 = drawableToBitmap(
                        getDrawable(
                            this,
                            CityWeatherModel.toWeatherIcon(weather.daily.skycon20h32h[i].value)
                        )!!
                    ),
                    maxTemperature = weather.daily.temperature[i].max.toFloat(),
                    minTemperature = weather.daily.temperature[i].min.toFloat()
                )
            }

            view.findViewById<LineChartForDailyWeather>(R.id.detail_lc_daily_weather_forecast)
                .apply {
                    weatherInfoResourceArray = dailyWeatherInfoList.toTypedArray()
                    lineColor =
                        if (nightMode) getColor(R.color.light_green) else getColor(R.color.deep_green)
                    lineBackgroundColor =
                        if (nightMode) getColor(R.color.deep_green) else getColor(R.color.light_green)
                    textColor = if (nightMode) Color.WHITE else Color.BLACK
                    applyChanges()
                }


            // init hour weather forecast card

            val hourlyWeatherInfoList = mutableListOf<LineChartForHourWeather.ResourceInfo>()
            val numberOfHourlyWeatherData =
                min(weather.hourly.skycon.size, weather.hourly.temperature.size)
            Log.d("Hour Forecast", "numberOfData: $numberOfHourlyWeatherData")
            for (i in 0 until numberOfHourlyWeatherData) hourlyWeatherInfoList += LineChartForHourWeather.ResourceInfo(
                MyTime.fromCaiyunDateTimeString(
                    weather.hourly.skycon[i].datetime
                ).toHourTime().toString(),
                weather.hourly.temperature[i].value.toFloat(),
                drawableToBitmap(
                    getDrawable(
                        this, CityWeatherModel.toWeatherIcon(weather.hourly.skycon[i].value)
                    )!!
                ),
                getStringResource(weather.hourly.skycon[i].value)
            )

            view.findViewById<LineChartForHourWeather>(R.id.detail_lc_hour_forecast).apply {
                weatherInfoResourceArray = hourlyWeatherInfoList.toTypedArray()
                lineColor =
                    if (nightMode) getColor(R.color.light_green) else getColor(R.color.deep_green)
                lineBackgroundColor =
                    if (nightMode) getColor(R.color.deep_green) else getColor(R.color.light_green)
                textColor = if (nightMode) Color.WHITE else Color.BLACK
                applyChanges()
            }


            // init precipitation forecast card

            val dataArray = weather.minutely.precipitation2h.toDoubleArray()
            val currentTime = MyTime.fromString(model.updateTime)
            var maxPrecipitation: Float
            var startIndexes: IntArray
            var endIndexes: IntArray
            var isRaining: Boolean

            view.findViewById<LineChartForPrecipitationForecast>(R.id.detail_lc_realtime_precipitation)
                .apply {
                    setDataArray(dataArray)
                    lineColor =
                        if (nightMode) getColor(R.color.light_blue) else getColor(R.color.deep_blue)
                    lineBackgroundColor =
                        if (nightMode) getColor(R.color.deep_blue) else getColor(R.color.light_blue)
                    startTime = currentTime
                    textColor = if (nightMode) Color.WHITE else Color.BLACK
                    maxPrecipitation = maxData
                    startIndexes = startIndex.toIntArray()
                    endIndexes = endIndex.toIntArray()
                    isRaining = notZeroAtBeginning
                    applyChanges()
                }

            view.findViewById<TextView>(R.id.detail_tv_max_precipitation).text =
                if (maxPrecipitation != 0f) maxPrecipitation.toString() else ""

            view.findViewById<TextView>(R.id.detail_tv_precipitation_desc).text = if (isRaining) {
                if (endIndexes.isEmpty()) {
                    "雨将会持续超过两个小时"
                } else {
                    if (startIndexes.isEmpty()) {
                        "雨将会在${endIndexes[0]}分钟后渐停"
                    } else {
                        "雨将会在${endIndexes[0]}分钟后渐停，随后${startIndexes[0] - endIndexes[0]}分钟后又开始下雨"
                    }
                }
            } else if (startIndexes.isEmpty()) {
                "两小时内不会下雨"
            } else {
                "${startIndexes[0]}分钟后将会有雨"
            }


            // init alert card

            weather.alert.content.run {
                if (this.isEmpty()) {
                    view.findViewById<TextView>(R.id.detail_tv_alert_title).text =
                        getStringResource("no_alert")
                    view.findViewById<TextView>(R.id.detail_tv_alert_desc).text =
                        getStringResource("no_alert_desc")
                } else {
                    view.findViewById<TextView>(R.id.detail_tv_alert_title).text = "${
                        getStringResource(
                            "EWN" + get(0).code.substring(0, 2)
                        )
                    } ${
                        getStringResource(
                            "EWG" + get(0).code.substring(2, 4)
                        )
                    }预警${
                        if (size > 1) " (+${size - 1})"
                        else ""
                    }"
                    view.findViewById<TextView>(R.id.detail_tv_alert_desc).text = get(0).description
                }
            }


            // init sun rise fall card

            weather.daily.astro.getOrNull(0)?.run {
                view.findViewById<TextView>(R.id.detail_tv_sun_date).text =
                    MyTime.MyDateTimeAuxiliary.fromCaiyunDatetimeString(date).toDateString()
                view.findViewById<TextView>(R.id.detail_tv_sun_rise_time).text = sunrise.time
                view.findViewById<TextView>(R.id.detail_tv_sun_fall_time).text = sunset.time
            }

            // init wind forecast card

            weather.realtime.wind.run {
                view.findViewById<TextView>(R.id.detail_tv_now_wind_info).text = "${
                    getStringResource(
                        "WD${
                            CityWeatherModel.windDirectionIndicator(direction)
                        }"
                    )
                } ${
                    getStringResource(
                        "WS${
                            CityWeatherModel.windStrengthIndicator(speed)
                        }"
                    )
                } $speed m/s"
            }

            val windStrengthDataList = mutableListOf<Double>()
            for (each in weather.hourly.wind) {
                windStrengthDataList += each.speed
                Log.d("WindLineChart", "insertData: $each")
            }

            view.findViewById<LineChartForWindForecast>(R.id.detail_lc_wind_forecast).run {
                setDataArray(windStrengthDataList.toDoubleArray())
                lineColor = getColor(R.color.wind_line)
                lineBackgroundColor = getColor(R.color.wind_line_bg)
                applyChanges()
            }


            // init detail info card

            view.findViewById<TextView>(R.id.detail_tv_ultraviolet).text =
                weather.realtime.lifeIndex.ultraviolet.run {
                    if (index == 0) "当前无紫外线"
                    else "紫外线强度$desc"
                }

            view.findViewById<TextView>(R.id.detail_tv_pressure).text =
                "气压为${(weather.realtime.pressure / 10).roundToInt().toDouble() / 100}kPa"

            view.findViewById<TextView>(R.id.detail_tv_humidity).text =
                "相对湿度为 ${(weather.realtime.humidity * 100).roundToInt()}%"
        }
    }

    inner class UserCitiesAdapter(
        private var list: List<City>,
    ) : RecyclerView.Adapter<UserCitiesAdapter.UserCitiesViewHolder>() {

        inner class UserCitiesViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

            private var updateTime: MyTime? = null

            private var detailDialog: Dialog? = null

            private val isLocalIV: ImageView = view.findViewById(R.id.card_iv_is_local)

            private val isFavoriteIV: ImageView = view.findViewById(R.id.card_iv_favorite)

            private val cityNameTV: TextView = view.findViewById(R.id.tv_cityTitle)

            private val updateTimeTV: TextView = view.findViewById(R.id.tv_updateTime)

            private val nowTemperTV: TextView = view.findViewById(R.id.tv_nowTemper)

            private val nowAqiTV: TextView = view.findViewById(R.id.tv_aqi)

            private val nowAqiGradeIV: ImageView = view.findViewById(R.id.iv_aqiGrade)

            private val nowWeatherTV: TextView = view.findViewById(R.id.tv_nowWeather)

            private val nowWindDirectionTV: TextView = view.findViewById(R.id.tv_windDirection)

            private val nowWindStrengthTV: TextView = view.findViewById(R.id.tv_windStrength)

            private val nowWeatherBackground: ImageView =
                view.findViewById(R.id.iv_cardWeatherBackground)

            private val todayWeatherTV: TextView = view.findViewById(R.id.tv_todayWeather)

            private val todayTemperTV: TextView = view.findViewById(R.id.tv_todayTemper)

            private val todayWeatherIV: ImageView = view.findViewById(R.id.iv_todayWeather)

            private val tomorrowWeatherTV: TextView = view.findViewById(R.id.tv_tomorrowWeather)

            private val tomorrowTemperTV: TextView = view.findViewById(R.id.tv_tomorrowTemper)

            private val tomorrowWeatherIV: ImageView = view.findViewById(R.id.iv_tomorrowWeather)

            private val backgroundLL: LinearLayout =
                view.findViewById(R.id.card_ll_daily_weather_background)

            private val weekDayOfDayAfterTomorrowTV: TextView =
                view.findViewById(R.id.tv_weekDayOfDayAfterTomorrow)

            private val dayAfterTomorrowWeatherTV: TextView =
                view.findViewById(R.id.tv_dayAfterTomorrowWeather)

            private val dayAfterTomorrowTemperTV: TextView =
                view.findViewById(R.id.tv_dayAfterTomorrowTemper)

            private val dayAfterTomorrowWeatherIV: ImageView =
                view.findViewById(R.id.iv_dayAfterTomorrowWeather)

            private val detailBtn: View = view.findViewById(R.id.btn_to_detail)

            private fun initDetailDynamicElement(index: Int, view: View) {

                runOnUiThread {
                    view.findViewById<ImageButton>(R.id.detail_ib_favorite)?.run {
                        if (favoriteCityIndex == index) setImageResource(R.drawable.icon_star_fill)
                        else {
                            setImageResource(R.drawable.icon_star)
                            setOnClickListener {
                                setImageResource(R.drawable.icon_star_fill)
                                favoriteCityIndex = index
                                val mainIndex = favoriteCityIndex
                                for ((key, value) in mutableCardView) value.findViewById<ImageView>(
                                    R.id.card_iv_favorite
                                ).visibility =
                                    if (key == mainIndex) View.VISIBLE else View.INVISIBLE
                            }
                        }
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            fun build(position: City, index: Int) {

                mutableCardView[index] = view

                Thread {

                    val model = CityWeatherModel()

                    val editor = weatherInfoPreferences!!.edit()

                    runOnUiThread {
                        detailBtn.setOnLongClickListener {
                            if (index == 0) Toast.makeText(
                                this@MainActivity, "不能删除本地城市！", Toast.LENGTH_SHORT
                            ).show()
                            else AlertDialog.Builder(this@MainActivity).setTitle("提示")
                                .setMessage("确认删除该城市？").setPositiveButton("删除") { _, _ ->
                                    removeCity(index)
                                    fleshDefaultIndex = favoriteCityIndex
                                    fleshData()
                                }.setNegativeButton("取消") { _, _ ->

                                }.create().show()
                            true
                        }
                    }

                    if (model.updateWithCity(
                            caiyunWeatherKey, position
                        ).available
                    ) {
                        editor.putString(position.hashCode().toString(), model.dataBody)
                        editor.apply()
                    } else {
                        Log.d("Data", "数据获取失败，正在尝试获取先前成功的数据")
                        weatherInfoPreferences!!.getString(position.hashCode().toString(), "")
                            .takeIf {
                                it?.isNotBlank() ?: false
                            }.run {
                                if (!model.updateWithDataString(
                                        this ?: "", position.showName
                                    ).available
                                ) {
                                    runOnUiThread {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "数据获取失败，没有近期成功的数据可展示",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    return@Thread
                                } else {
                                    runOnUiThread {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "数据获取失败，正在展示的是最后一次成功的数据",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                    }

                    updateTime = MyTime.fromString(model.updateTime)

                    val isLocal = if (index == 0) View.VISIBLE else View.INVISIBLE

                    val isFavorite =
                        if (index == favoriteCityIndex) View.VISIBLE else View.INVISIBLE

                    val weather = model.weatherInfo!!.result

                    val preResWD = getStringResource(
                        "WD${
                            CityWeatherModel.windDirectionIndicator(
                                weather.realtime.wind.direction
                            )
                        }"
                    )

                    val preResWS = weather.realtime.wind.speed.run {
                        "${
                            getStringResource(
                                "WS${
                                    CityWeatherModel.windStrengthIndicator(this)
                                }"
                            )
                        } ${
                            getStringResource(
                                "WSDesc${
                                    CityWeatherModel.windStrengthIndicator(this)
                                }"
                            )
                        }"
                    }

                    val preResBackgroundAnim = AnimationUtils.loadAnimation(
                        this@MainActivity, R.anim.focusing_show
                    )

                    val preResBackground =
                        CityWeatherModel.toWeatherBackground(weather.realtime.skycon)(
                            CityWeatherModel.TIME_NORMAL
                        )

                    val preNowAqiGrade =
                        CityWeatherModel.toAqiIcon(weather.realtime.airQuality.aqi.chn)
                    val preNowTemper = weather.realtime.temperature.roundToInt().toString()
                    val preNowAqi = weather.realtime.airQuality.aqi.chn.toString()

                    val preResTodaySkyCon = getStringResource(weather.daily.skycon[0].value)
                    val preResTodayTemper =
                        "${weather.daily.temperature[0].max.toInt()} / ${weather.daily.temperature[0].min.toInt()} ℃"
                    val preResTodayImage =
                        CityWeatherModel.toWeatherIcon(weather.daily.skycon[0].value)

                    val preResTomorrowSkyCon = getStringResource(weather.daily.skycon[1].value)
                    val preResTomorrowTemper =
                        "${weather.daily.temperature[1].max.toInt()} / ${weather.daily.temperature[1].min.toInt()} ℃"
                    val preResTomorrowImage = CityWeatherModel.toWeatherIcon(
                        weather.daily.skycon[1].value
                    )

                    val preResDayAfterTomorrowWeekDay =
                        getStringResource("dayOfWeek${(model.todayWeekDay + 2) % 7}")
                    val preResDayAfterTomorrowSkyCon =
                        getStringResource(weather.daily.skycon[2].value)
                    val preResDayAfterTomorrowTemper =
                        "${weather.daily.temperature[2].max.toInt()} / ${weather.daily.temperature[2].min.toInt()} ℃"
                    val preResDayAfterTomorrowImage = CityWeatherModel.toWeatherIcon(
                        weather.daily.skycon[2].value
                    )

                    val backgroundDrawable = getDrawable(
                        this@MainActivity, if (nightMode) R.drawable.night_mode_radius_rectangle
                        else R.drawable.day_mode_radius_rectangle
                    )

                    runOnUiThread {
                        backgroundLL.background = backgroundDrawable
                        cityNameTV.text = model.cityName
                        isLocalIV.visibility = isLocal
                        isFavoriteIV.visibility = isFavorite
                        updateTimeTV.text = model.updateTime
                        nowTemperTV.text = preNowTemper
                        nowAqiTV.text = preNowAqi
                        nowAqiGradeIV.setImageResource(preNowAqiGrade)
                        nowWeatherTV.text = getStringResource(weather.realtime.skycon)
                        nowWindDirectionTV.text = preResWD
                        nowWindStrengthTV.text = preResWS
                        nowWeatherBackground.startAnimation(preResBackgroundAnim)
                        nowWeatherBackground.setImageResource(preResBackground)
                        todayWeatherTV.text = preResTodaySkyCon
                        todayTemperTV.text = preResTodayTemper
                        todayWeatherIV.setImageResource(preResTodayImage)
                        tomorrowWeatherTV.text = preResTomorrowSkyCon
                        tomorrowTemperTV.text = preResTomorrowTemper
                        tomorrowWeatherIV.setImageResource(preResTomorrowImage)
                        weekDayOfDayAfterTomorrowTV.text = preResDayAfterTomorrowWeekDay
                        dayAfterTomorrowWeatherTV.text = preResDayAfterTomorrowSkyCon
                        dayAfterTomorrowTemperTV.text = preResDayAfterTomorrowTemper
                        dayAfterTomorrowWeatherIV.setImageResource(preResDayAfterTomorrowImage)

                        val dialogInitResult = initDetailDialog(model)

                        if (dialogInitResult.first) {
                            detailBtn.setOnClickListener {
                                initDetailDynamicElement(index, dialogInitResult.second)
                                detailDialog?.show()
                            }
                        }
                    }
                }.start()
            }

            private fun initDetailDialog(model: CityWeatherModel): Pair<Boolean, View> {

                var available = true

                detailDialog = Dialog(this@MainActivity, R.style.dialog_bottom_full)

                val view = View.inflate(this@MainActivity, R.layout.detailed_info, null)

                try {
                    initDetailInfo(model, view)
                } catch (_: Exception) {
                    available = false
                }

                view.findViewById<ImageButton>(R.id.detail_ib_back)
                    .setOnClickListener { detailDialog!!.hide() }

                detailDialog!!.setCanceledOnTouchOutside(true)
                detailDialog!!.setCancelable(true)

                val window = detailDialog!!.window!!
                window.setGravity(Gravity.BOTTOM)
                window.setWindowAnimations(R.style.share_animation)

                window.setContentView(view)
                window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
                )

                return Pair(available, view)

            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
        ): UserCitiesViewHolder = UserCitiesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.weather_card, parent, false)
        )

        override fun onBindViewHolder(holder: UserCitiesViewHolder, position: Int) =
            holder.build(list[position], position)


        override fun getItemCount(): Int = list.size

    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap = drawable.run {
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        drawable.draw(Canvas(bitmap))
        bitmap
    }

    fun getStringResource(imageName: String): String =
        resources.getIdentifier(imageName, "string", packageName).takeIf { it != 0 }
            ?.run(::getString) ?: ""

    private fun dp2px(v: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics
    ).toInt()

}