package com.sakuno.whatsweatherlike

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2


class MainActivity : Activity() {

    fun getStringResource(imageName: String): String =
        resources.getIdentifier(imageName, "string", packageName).takeIf { it != 0 }
            ?.run(::getString) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ViewPager2>(R.id.vp_cardsView).adapter = this.UserCitiesAdapter(
            arrayListOf(
                CityWeatherModel.getExampleModel(),
                CityWeatherModel.getExampleModel(),
                CityWeatherModel.getExampleModel(),
                CityWeatherModel.getExampleModel(),
                CityWeatherModel.getExampleModel(),
                CityWeatherModel.getExampleModel()
            )
        )
    }


    inner class UserCitiesAdapter(
        private var list: ArrayList<CityWeatherModel>,
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

            @SuppressLint("SetTextI18n")
            fun build(model: CityWeatherModel) {
                cityNameTV.text = model.cityName
                updateTimeTV.text = model.updateTime
                nowTemperTV.text = model.nowTemper.toString()
                nowAqiTV.text = model.nowAQI.toString()
                newAqiGradeIV.setImageResource(CityWeatherModel.toAqiIcon(model.nowAQI))
                nowWeatherTV.text = getStringResource("weatherType${model.nowWeatherID}")
                nowWindDirectionTV.text = getStringResource("WD${model.nowWD}")
                nowWindStrengthTV.text = getStringResource("WS${model.nowWS}")

                nowWeatherBackground.setImageResource(
                    CityWeatherModel.toWeatherBackground(model.nowWeatherID)(CityWeatherModel.TIME_NORMAL)
                )

                todayWeatherTV.text = getStringResource("weatherType${model.todayWeatherID}")

                todayTemperTV.text = "${model.todayTemper.first} / ${model.todayTemper.second} ℃"

                todayWeatherIV.setImageResource(CityWeatherModel.toWeatherIcon(model.todayWeatherID))

                tomorrowWeatherTV.text = getStringResource("weatherType${model.tomorrowWeatherID}")

                tomorrowTemperTV.text =
                    "${model.tomorrowTemper.first} / ${model.tomorrowTemper.second} ℃"

                tomorrowWeatherIV.setImageResource(CityWeatherModel.toWeatherIcon(model.tomorrowWeatherID))

                weekDayOfDayAfterTomorrowTV.text =
                    getStringResource("dayOfWeek${(model.todayWeekDay + 2) % 7}")
                dayAfterTomorrowWeatherTV.text =
                    getStringResource("weatherType${model.tomorrowWeatherID}")
                dayAfterTomorrowTemperTV.text =
                    "${model.dayAfterTomorrowTemper.first} / ${model.dayAfterTomorrowTemper.second} ℃"
                dayAfterTomorrowWeatherIV.setImageResource(CityWeatherModel.toWeatherIcon(model.dayAfterTomorrowWeatherID))
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserCitiesViewHolder {
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