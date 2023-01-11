package com.sakuno.whatsweatherlike

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.sakuno.whatsweatherlike.customwidgets.LineChart
import com.sakuno.whatsweatherlike.utils.MyTime


class MainActivity : Activity() {

    var caiyunWeatherKey = "mkhvpq9w0AsN6gjl"

    var baiduAK = "7G00KgUlyZW6DnNI2lM0Xr4NNcP0sqWk"

    var detailDialog: Dialog? = null

    fun getStringResource(imageName: String): String =
        resources.getIdentifier(imageName, "string", packageName).takeIf { it != 0 }
            ?.run(::getString) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ViewPager2>(R.id.vp_cardsView).adapter = this.UserCitiesAdapter(
            arrayListOf(
                Pair(113.1257, 22.4219), Pair(112.1257, 32.4219), Pair(113.5000, 24.3000)
            )
        )
    }

    fun showDetailInfoDialog(model: CityWeatherModel) = run { initDetailInfoDialog(model).show() }

    fun initDetailInfoDialog(model: CityWeatherModel): Dialog {

        detailDialog = Dialog(this, R.style.dialog_bottom_full)

        val view = View.inflate(this, R.layout.detailed_info, null)
        val weather = model.weatherInfo?.result

        detailDialog!!.setCanceledOnTouchOutside(true)
        detailDialog!!.setCancelable(true)

        val window = detailDialog!!.window!!
        window.setGravity(Gravity.BOTTOM)
        window.setWindowAnimations(R.style.share_animation)

        window.setContentView(view)
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )

        if (weather != null) runOnUiThread {
            view.findViewById<ProgressBar>(R.id.detail_pb_aqiGrade).progress =
                weather.realtime.airQuality.aqi.chn
            view.findViewById<TextView>(R.id.detail_tv_aqiNum).text =
                weather.realtime.airQuality.aqi.chn.toString()
            view.findViewById<TextView>(R.id.detail_tv_aqiGrade).text =
                getStringResource(CityWeatherModel.toAqiGradeStringResourceName(weather.realtime.airQuality.aqi.chn))


            val dataArray = weather.minutely.precipitation2h.toDoubleArray()
            val currentTime = MyTime.fromString(model.updateTime)
            var maxPrecipitation: Float
            var startIndexes: IntArray
            var endIndexes: IntArray
            var isRaining: Boolean
            var precipitationDesc: String

            view.findViewById<LineChart>(R.id.detail_lc_realtime_precipitation).apply {
                setDataArray(dataArray)
                lineColor = getColor(R.color.deep_blue)
                lineBackgroundColor = getColor(R.color.light_blue)
                showBottomScale = true
                bottomScaleType = LineChart.ScaleType.TIME
                bottomScaleDisplayType = LineChart.ScaleDisplayType.START_AND_END
                startTime = currentTime
                stepTime = MyTime.fromMinute(1)
                maxPrecipitation = maxData
                startIndexes = startIndex.toIntArray()
                endIndexes = endIndex.toIntArray()
                isRaining = notZeroAtBeginning
                applyChanges()
            }
            view.findViewById<TextView>(R.id.detail_tv_max_precipitation).text =
                if (maxPrecipitation != 0f) maxPrecipitation.toString() else ""

            if (isRaining) {
                if (endIndexes.isEmpty()) {
                    precipitationDesc = "雨将会"
                }
            }
        }

        return detailDialog!!
    }

    inner class UserCitiesAdapter(
        private var list: ArrayList<Pair<Double, Double>>,
    ) : RecyclerView.Adapter<UserCitiesAdapter.UserCitiesViewHolder>() {


        inner class UserCitiesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            private val cityNameTV: TextView = view.findViewById(R.id.tv_cityTitle)

            private val updateTimeTV: TextView = view.findViewById(R.id.tv_updateTime)

            private val nowTemperTV: TextView = view.findViewById(R.id.tv_nowTemper)
            private val nowAqiTV: TextView = view.findViewById(R.id.tv_aqi)
            private val newAqiGradeIV: ImageView = view.findViewById(R.id.iv_aqiGrade)
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

            private val weekDayOfDayAfterTomorrowTV: TextView =
                view.findViewById(R.id.tv_weekDayOfDayAfterTomorrow)
            private val dayAfterTomorrowWeatherTV: TextView =
                view.findViewById(R.id.tv_dayAfterTomorrowWeather)
            private val dayAfterTomorrowTemperTV: TextView =
                view.findViewById(R.id.tv_dayAfterTomorrowTemper)
            private val dayAfterTomorrowWeatherIV: ImageView =
                view.findViewById(R.id.iv_dayAfterTomorrowWeather)

            private val detailBtn: View = view.findViewById(R.id.btn_to_detail)

            @SuppressLint("SetTextI18n")
            fun build(position: Pair<Double, Double>) {

                Thread {

                    val model = CityWeatherModel()

                    if (model.updateWithAreaID(
                            caiyunWeatherKey, position.first, position.second
                        ).weatherInfo == null
                    ) {
                        return@Thread
                    }

                    val weather = model.weatherInfo!!.result

                    val cityNameBuilder = StringBuilder()

                    for (it in weather.alert.adcodes) cityNameBuilder.append(it.name)

                    runOnUiThread {
                        cityNameTV.text = cityNameBuilder.toString()
                        updateTimeTV.text = model.updateTime
                        nowTemperTV.text = weather.realtime.temperature.toString()
                        nowAqiTV.text = weather.realtime.airQuality.aqi.chn.toString()
                        newAqiGradeIV.setImageResource(CityWeatherModel.toAqiIcon(weather.realtime.airQuality.aqi.chn))
                        nowWeatherTV.text = getStringResource(weather.realtime.skycon)
                        nowWindDirectionTV.text = getStringResource(
                            "WD${
                                CityWeatherModel.windDirectionIndicator(
                                    weather.realtime.wind.direction
                                )
                            }"
                        )
                        nowWindStrengthTV.text = getStringResource(
                            "WS${
                                CityWeatherModel.windStrengthIndicator(
                                    weather.realtime.wind.speed
                                )
                            }"
                        )

                        nowWeatherBackground.startAnimation(
                            AnimationUtils.loadAnimation(
                                this@MainActivity, R.anim.alpha_show
                            )
                        )

                        nowWeatherBackground.setImageResource(
                            CityWeatherModel.toWeatherBackground(weather.realtime.skycon)(
                                CityWeatherModel.TIME_NORMAL
                            )
                        )


                        todayWeatherTV.text = getStringResource(weather.daily.skycon[0].value)

                        todayTemperTV.text =
                            "${weather.daily.temperature[0].max.toInt()} / ${weather.daily.temperature[0].min.toInt()} ℃"

                        todayWeatherIV.setImageResource(CityWeatherModel.toWeatherIcon(weather.daily.skycon[0].value))

                        tomorrowWeatherTV.text = getStringResource(weather.daily.skycon[1].value)

                        tomorrowTemperTV.text =
                            "${weather.daily.temperature[1].max.toInt()} / ${weather.daily.temperature[1].min.toInt()} ℃"

                        tomorrowWeatherIV.setImageResource(
                            CityWeatherModel.toWeatherIcon(
                                weather.daily.skycon[1].value
                            )
                        )

                        weekDayOfDayAfterTomorrowTV.text =
                            getStringResource("dayOfWeek${(model.todayWeekDay + 2) % 7}")
                        dayAfterTomorrowWeatherTV.text =
                            getStringResource(weather.daily.skycon[2].value)
                        dayAfterTomorrowTemperTV.text =
                            "${weather.daily.temperature[2].max.toInt()} / ${weather.daily.temperature[2].min.toInt()} ℃"
                        dayAfterTomorrowWeatherIV.setImageResource(
                            CityWeatherModel.toWeatherIcon(
                                weather.daily.skycon[2].value
                            )
                        )

                        detailBtn.setOnClickListener {
                            showDetailInfoDialog(model)
                        }

                    }
                }.start()
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
        ): UserCitiesViewHolder {
            return UserCitiesViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.weather_card, parent, false)
            )
        }

        override fun onBindViewHolder(holder: UserCitiesViewHolder, position: Int) {
            holder.build(list[position])
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }
}