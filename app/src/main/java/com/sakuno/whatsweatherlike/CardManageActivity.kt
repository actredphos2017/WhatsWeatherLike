package com.sakuno.whatsweatherlike

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.UiModeManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.sakuno.whatsweatherlike.utils.City
import com.sakuno.whatsweatherlike.utils.CityList
import com.sakuno.whatsweatherlike.utils.CustomCities
import com.sakuno.whatsweatherlike.utils.MyToast
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class CardManageActivity : Activity() {

    private var citiesPreferences: SharedPreferences? = null

    private var nightMode = false

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
            fleshData(favoriteCityIndex)
        }

    private var favoriteCityIndex: Int
        get() = try {
            mAdapter!!.cities.indexOf(favoriteCity).takeIf { it in mAdapter!!.cities.indices } ?: 0
        } catch (_: Exception) {
            0
        }
        set(value) {
            favoriteCity = mAdapter!!.cities[value]
        }

    private var mAdapter: CityEditAdapter? = null

    inner class MyCallBack : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
        ): Int = makeMovementFlags(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.START or ItemTouchHelper.END
        )

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition + 1

            val toPosition = target.adapterPosition + 1

            if (fromPosition < toPosition) for (i in fromPosition until toPosition) Collections.swap(
                mAdapter!!.cities, i, i + 1
            )
            else for (i in fromPosition downTo toPosition + 1) Collections.swap(
                mAdapter!!.cities, i, i - 1
            )

            customCitiesFromPreferences = CustomCities(mAdapter!!.cities).apply { check() }
            mainList!!.adapter!!.notifyItemMoved(fromPosition - 1, toPosition - 1)

            return true
        }

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.5f

        override fun getSwipeEscapeVelocity(defaultValue: Float): Float = 5f

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            mAdapter!!.deleteItem(viewHolder.adapterPosition)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.card_manage)

        window.statusBarColor = getColor(R.color.black_translation)

        nightMode =
            (getSystemService(Context.UI_MODE_SERVICE) as UiModeManager).nightMode == UiModeManager.MODE_NIGHT_YES

        try {
            citiesPreferences = getSharedPreferences("cities", Context.MODE_PRIVATE)
            mainList = findViewById<RecyclerView?>(R.id.manage_rv_cards).apply {
                layoutManager = LinearLayoutManager(this@CardManageActivity)
                CityEditAdapter(customCitiesFromPreferences.cities.toMutableList()).let {
                    adapter = it
                    mAdapter = it
                    ItemTouchHelper(MyCallBack()).attachToRecyclerView(this)
                }
            }
        } catch (_: Exception) {
            MyToast.Builder(this@CardManageActivity).setMessage("城市数据获取失败").create().show()
            setResult(1)
            finish()
        }

        initCityList()

        findViewById<ImageButton>(R.id.manageitem_ib_favorite).setOnClickListener {
            favoriteCityIndex = 0
        }

        findViewById<ImageButton>(R.id.manage_ib_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.manage_ib_add).setOnClickListener {
            showAddCityDialog()
        }

        fleshData(favoriteCityIndex)
    }

    private fun fleshData(favoriteIndex: Int? = null) {
        if (favoriteIndex != null) findViewById<ImageButton>(R.id.manageitem_ib_favorite).setImageResource(
            if (0 == favoriteIndex) R.drawable.icon_star_fill
            else R.drawable.icon_star
        )
        mainList!!.adapter = mAdapter
    }

    inner class CityEditAdapter(val cities: MutableList<City>) :
        RecyclerView.Adapter<CityEditAdapter.CityViewHolder>() {

        inner class CityViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

            fun bind(info: City, index: Int) {
                view.findViewById<ImageButton>(R.id.manageitem_ib_favorite).run {
                    setImageResource(
                        if (index == favoriteCityIndex) R.drawable.icon_star_fill
                        else R.drawable.icon_star
                    )
                    setOnClickListener {
                        setImageResource(R.drawable.icon_star_fill)
                        favoriteCity = info
                    }
                }
                view.findViewById<TextView>(R.id.manageitem_tv_city_name).text = info.showName
            }
        }

        fun deleteItem(position: Int) {
            val isFavorite = (favoriteCityIndex == position + 1)
            if (isFavorite) this@CardManageActivity.findViewById<ImageButton>(R.id.manageitem_ib_favorite)
                .setImageResource(R.drawable.icon_star_fill)
            val delCity = cities[position + 1]
            cities.removeAt(position + 1)
            customCitiesFromPreferences = CustomCities(cities).apply { check() }
            notifyItemRemoved(position)
            MyToast.Builder(this@CardManageActivity).setMessage("你刚刚删除了 ${delCity.showName}")
                .setButton("撤销") {
                    addItem(position, delCity, isFavorite)
                }.setHoldTime(5000).create().show()
        }

        fun addItem(position: Int, city: City, fromRevokeFavorite: Boolean = false) {
            if (fromRevokeFavorite) this@CardManageActivity.findViewById<ImageButton>(R.id.manageitem_ib_favorite)
                .setImageResource(R.drawable.icon_star)
            cities.add((position + 1).takeIf { it in 0..cities.size } ?: cities.size, city)
            customCitiesFromPreferences = CustomCities(cities).apply { check() }
            notifyItemInserted(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder =
            CityViewHolder(
                LayoutInflater.from(this@CardManageActivity)
                    .inflate(R.layout.manage_card_item, parent, false)
            )


        override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
            holder.bind(cities[position + 1], position + 1)
        }

        override fun getItemCount(): Int = cities.size - 1
    }

    private var cityListAvailable = false

    private var mainList: RecyclerView? = null

    private var cityList: CityList? = null

    private var addCityDialog: Dialog? = null

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
                MyToast.Builder(this@CardManageActivity).setMessage("城市列表已损坏").create().show()
            }
        }
    }

    private fun showAddCityDialog() = (addCityDialog ?: initAddCityDialog())?.show()

    private fun initAddCityDialog(): Dialog? {
        if (!cityListAvailable) {
            runOnUiThread {
                MyToast.Builder(this@CardManageActivity).setMessage("城市列表已损坏").create().show()
            }
            return addCityDialog
        }

        addCityDialog = Dialog(this@CardManageActivity, R.style.dialog_bottom_full)
        addCityDialog!!.setCanceledOnTouchOutside(true)
        addCityDialog!!.setCancelable(true)

        val window = addCityDialog!!.window!!
        window.setGravity(Gravity.BOTTOM)
        window.setWindowAnimations(R.style.share_animation)

        View.inflate(this@CardManageActivity, R.layout.add_city_dialog, null).run {
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

        view.findViewById<LinearLayout>(R.id.addcity_ll_background).background =
            AppCompatResources.getDrawable(
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

                    if (searchKey.isEmpty()) return

                    searchResultLayout.removeAllViews()

                    val searchRes = cityList!!.searchCity(searchKey)

                    if (searchRes.isEmpty()) return

                    for (each in searchRes) View.inflate(
                        this@CardManageActivity, R.layout.add_city_result_item, null
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
                            val sameCityIndex = mAdapter!!.cities.indexOf(each)
                            if (sameCityIndex >= 0) {
                                AlertDialog.Builder(this@CardManageActivity).setTitle("提示")
                                    .setMessage("该城市已存在，详见第 ${sameCityIndex + 1} 张卡片")
                                    .setPositiveButton("确定") { _, _ ->
                                        addCityDialog!!.hide()
                                    }.show()
                            } else {
                                mAdapter!!.addItem(mAdapter!!.itemCount, each)
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
                    MyToast.Builder(this@CardManageActivity).setMessage("城市名不能为空").create().show()
                    return@setOnEditorActionListener true
                }

                searchResultLayout.removeAllViews()

                val searchRes = cityList!!.searchCity(searchKey)

                if (searchRes.isEmpty()) {
                    MyToast.Builder(this@CardManageActivity).setMessage("搜索结果为空").create().show()
                    return@setOnEditorActionListener true
                }

                return@setOnEditorActionListener false
            }
    }

    private fun dp2px(v: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics
    ).toInt()
}