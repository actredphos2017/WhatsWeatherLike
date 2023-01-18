package com.sakuno.whatsweatherlike

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.sakuno.whatsweatherlike.utils.City
import com.sakuno.whatsweatherlike.utils.CustomCities
import java.util.*

class CardManageActivity : Activity() {

    private var citiesPreferences: SharedPreferences? = null

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
            editingList.indexOf(favoriteCity).takeIf { it in editingList.indices } ?: 0
        } catch (_: Exception) {
            0
        }
        set(value) {
            favoriteCity = editingList[value]
            fleshData(value)
        }

    private var editingList: MutableList<City> = mutableListOf()

    private var mainView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.card_manage)

        window.statusBarColor = getColor(R.color.black_translation)

        try {
            citiesPreferences = getSharedPreferences("cities", Context.MODE_PRIVATE)
            editingList = customCitiesFromPreferences.cities.toMutableList()
        } catch (_: Exception) {
            Toast.makeText(this, "城市数据获取失败", Toast.LENGTH_SHORT).show()
            setResult(1)
            finish()
        }

        mainView = findViewById(R.id.manage_rv_cards)

        mainView!!.adapter = CityEditAdapter(editingList)
        mainView!!.layoutManager = LinearLayoutManager(this@CardManageActivity)

        ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
            ): Int = makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition + 1

                val toPosition = target.adapterPosition + 1

                if (fromPosition < toPosition) for (i in fromPosition until toPosition) Collections.swap(
                    editingList, i, i + 1
                )
                else for (i in fromPosition downTo toPosition + 1) Collections.swap(
                    editingList, i, i - 1
                )

                customCitiesFromPreferences = CustomCities(editingList).apply { check() }

                mainView!!.adapter!!.notifyItemMoved(fromPosition - 1, toPosition - 1)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun onSelectedChanged(
                viewHolder: RecyclerView.ViewHolder?, actionState: Int
            ) {
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) viewHolder?.itemView?.setBackgroundColor(
                    Color.LTGRAY
                )

                super.onSelectedChanged(viewHolder, actionState)
            }

            override fun clearView(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
            ) {
                viewHolder.itemView.setBackgroundColor(0)
                fleshData()
                super.clearView(recyclerView, viewHolder)
            }

        }).attachToRecyclerView(mainView)

        findViewById<ImageButton>(R.id.manageitem_ib_favorite).setOnClickListener {
            favoriteCityIndex = 0
            fleshData(0)
        }

        findViewById<ImageButton>(R.id.manage_ib_back).setOnClickListener {
            finish()
        }

        fleshData(favoriteCityIndex)
    }

    private fun fleshData(favoriteIndex: Int? = null) {
        if (favoriteIndex != null)
            findViewById<ImageButton>(R.id.manageitem_ib_favorite).setImageResource(
                if (0 == favoriteIndex) R.drawable.icon_star_fill
                else R.drawable.icon_star
            )
        mainView!!.adapter = CityEditAdapter(editingList)
    }

    inner class CityEditAdapter(private val cities: List<City>) :
        RecyclerView.Adapter<CityEditAdapter.CityViewHolder>() {

        inner class CityViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

            fun bind(info: City, index: Int) {
                view.findViewById<ImageButton>(R.id.manageitem_ib_favorite).run {
                    setImageResource(
                        if (index == favoriteCityIndex) R.drawable.icon_star_fill
                        else R.drawable.icon_star
                    )
                    setOnClickListener {
                        favoriteCityIndex = index
                    }
                }

                view.findViewById<TextView>(R.id.manageitem_tv_city_name).text = info.showName

                view.findViewById<TextView>(R.id.manageitem_tv_longitude).text = info.longitude.toString()

                view.findViewById<TextView>(R.id.manageitem_tv_latitude).text = info.latitude.toString()

            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder =
            CityViewHolder(
                LayoutInflater.from(this@CardManageActivity)
                    .inflate(R.layout.manage_card_item, parent, false)
            )


        override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
            holder.bind(cities[position + 1], position + 1)
        }

        override fun getItemCount(): Int = editingList.size - 1
    }
}