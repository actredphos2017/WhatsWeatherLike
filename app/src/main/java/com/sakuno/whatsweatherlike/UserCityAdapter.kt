package com.sakuno.whatsweatherlike

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserCitiesAdapter(
    private var list: ArrayList<CityWeatherModel>,
    var context: MainActivity
    ) :
    RecyclerView.Adapter<UserCitiesAdapter.UserCitiesViewHolder>() {

    companion object {

        const val TIME_NORMAL = 0

        const val TIME_MORNING = 1

        const val TIME_EVENING = 2

        const val TIME_NIGHT = 3

        private val sunnyMipmap: (Int) -> Int = {
            when (it) {
                TIME_MORNING -> R.mipmap.sunny_morning
                TIME_EVENING -> R.mipmap.sunny_evening
                TIME_NIGHT -> R.mipmap.sunny_night
                else -> R.mipmap.sunny
            }
        }

        private val cloudyMipmap: (Int) -> Int = {
            when (it) {
                TIME_MORNING -> R.mipmap.cloudy_morning
                TIME_NIGHT -> R.mipmap.cloudy_night
                else -> R.mipmap.cloudy
            }
        }

        private val overcastMipmap: (Int) -> Int = {
            when (it) {
                TIME_NIGHT -> R.mipmap.overcast_night
                else -> R.mipmap.overcast
            }
        }

        private val rainyMipmap: (Int) -> Int = {
            when (it) {
                TIME_NIGHT -> R.mipmap.rainy_night
                else -> R.mipmap.rainy
            }
        }

        private val thunderMipmap: (Int) -> Int = {
            when (it) {
                TIME_NIGHT -> R.mipmap.thunder_night
                else -> R.mipmap.thunder
            }
        }

        private val snowMipmap: (Int) -> Int = {
            when (it) {
                TIME_NIGHT -> R.mipmap.snow_night
                else -> R.mipmap.snow
            }
        }

        private val foggyMipmap: (Int) -> Int = {
            when (it) {
                else -> R.mipmap.fog
            }
        }

        private val blackMipmap: (Int) -> Int = {
            when (it) {
                else -> sunnyMipmap(it)
            }
        }

        val toWeatherBackground: (String) -> ((Int) -> Int) = {
            when (it) {
                "00" -> sunnyMipmap
                "01" -> cloudyMipmap
                "02" -> overcastMipmap
                "03" -> rainyMipmap
                "04" -> thunderMipmap
                "05" -> blackMipmap
                "06" -> snowMipmap
                "07" -> rainyMipmap
                "08" -> rainyMipmap
                "09" -> rainyMipmap
                "10" -> rainyMipmap
                "11" -> rainyMipmap
                "12" -> rainyMipmap
                "13" -> snowMipmap
                "14" -> snowMipmap
                "15" -> snowMipmap
                "16" -> snowMipmap
                "17" -> snowMipmap
                "18" -> foggyMipmap
                "19" -> rainyMipmap
                "20" -> blackMipmap
                "21" -> rainyMipmap
                "22" -> rainyMipmap
                "23" -> rainyMipmap
                "24" -> rainyMipmap
                "25" -> rainyMipmap
                "26" -> snowMipmap
                "27" -> snowMipmap
                "28" -> snowMipmap
                "29" -> blackMipmap
                "30" -> blackMipmap
                "31" -> blackMipmap
                "53" -> foggyMipmap
                "99" -> sunnyMipmap
                "32" -> foggyMipmap
                "49" -> foggyMipmap
                "54" -> foggyMipmap
                "55" -> foggyMipmap
                "56" -> foggyMipmap
                "57" -> foggyMipmap
                "58" -> foggyMipmap
                "301" -> rainyMipmap
                "302" -> snowMipmap
                else -> blackMipmap
            }
        }

        val toWeatherIcon: (String) -> Int = {
            when (it) {
                "00" -> R.mipmap.sunny_icon
                "01" -> R.mipmap.cloudy_icon
                "02" -> R.mipmap.overcoat_icon
                "03" -> R.mipmap.small_rainy_icon
                "04" -> R.mipmap.thunder_rainy_icon
                "05" -> R.mipmap.thunder_rainy_icon
                "06" -> R.mipmap.rainy_snowy_icon
                "07" -> R.mipmap.small_rainy_icon
                "08" -> R.mipmap.middle_rainy_icon
                "09" -> R.mipmap.big_rainy_icon
                "10" -> R.mipmap.big_rainy_icon
                "11" -> R.mipmap.big_rainy_icon
                "12" -> R.mipmap.big_rainy_icon
                "13" -> R.mipmap.small_snowy_icon
                "14" -> R.mipmap.small_snowy_icon
                "15" -> R.mipmap.middle_snowy_icon
                "16" -> R.mipmap.big_snowy_icon
                "17" -> R.mipmap.big_snowy_icon
                "18" -> R.mipmap.foggy_icon
                "19" -> R.mipmap.rainy_snowy_icon
                "20" -> R.mipmap.dust_icon
                "21" -> R.mipmap.middle_rainy_icon
                "22" -> R.mipmap.big_rainy_icon
                "23" -> R.mipmap.big_rainy_icon
                "24" -> R.mipmap.big_rainy_icon
                "25" -> R.mipmap.big_rainy_icon
                "26" -> R.mipmap.middle_snowy_icon
                "27" -> R.mipmap.big_snowy_icon
                "28" -> R.mipmap.big_snowy_icon
                "29" -> R.mipmap.dust_icon
                "30" -> R.mipmap.dust_icon
                "31" -> R.mipmap.dust_icon
                "53" -> R.mipmap.haze_icon
                "99" -> R.mipmap.sunny_icon
                "32" -> R.mipmap.foggy_icon
                "49" -> R.mipmap.foggy_icon
                "54" -> R.mipmap.haze_icon
                "55" -> R.mipmap.haze_icon
                "56" -> R.mipmap.haze_icon
                "57" -> R.mipmap.foggy_icon
                "58" -> R.mipmap.foggy_icon
                "301" -> R.mipmap.big_rainy_icon
                "302" -> R.mipmap.big_snowy_icon
                else -> R.mipmap.sunny_icon
            }
        }

        fun toAqiIcon(aqi: Int): Int = when (aqi) {
            in 0..50 -> R.mipmap.aqi_excellent_icon
            in 51..100 -> R.mipmap.aqi_great_icon
            in 101..150 -> R.mipmap.aqi_mild_icon
            in 151..200 -> R.mipmap.aqi_moderate_icon
            in 201..300 -> R.mipmap.aqi_heavy_icon
            else -> R.mipmap.aqi_severe_icon
        }

    }

    inner class UserCitiesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val cityNameTV: TextView = view.findViewById(R.id.tv_cityTitle)

        private val updateTimeTV: TextView = view.findViewById(R.id.tv_updateTime)

        private val nowTemperTV: TextView = view.findViewById(R.id.tv_nowTemper)
        private val nowAqiTV: TextView = view.findViewById(R.id.tv_aqi)
        private val newAqiGradeIV: ImageView = view.findViewById(R.id.iv_aqiGrade)
        private val nowWeatherTV: TextView = view.findViewById(R.id.tv_nowWeather)
        private val nowWindDirectionTV: TextView = view.findViewById(R.id.tv_windDirection)
        private val nowWindStrengthTV: TextView = view.findViewById(R.id.tv_windStrength)
        private val nowWeatherBackground: ImageView = view.findViewById(R.id.iv_cardWeatherBackground)

        private val todayWeatherTV: TextView = view.findViewById(R.id.tv_todayWeather)
        private val todayTemperTV: TextView = view.findViewById(R.id.tv_todayTemper)
        private val todayWeatherIV: ImageView = view.findViewById(R.id.iv_todayWeather)

        private val tomorrowWeatherTV: TextView = view.findViewById(R.id.tv_tomorrowWeather)
        private val tomorrowTemperTV: TextView = view.findViewById(R.id.tv_tomorrowTemper)
        private val tomorrowWeatherIV: ImageView = view.findViewById(R.id.iv_tomorrowWeather)

        private val weekDayOfDayAfterTomorrowTV: TextView =
            view.findViewById(R.id.tv_weekDayOfDayAfterTomorrow)
        private val dayAfterTomorrowWeatherTV: TextView = view.findViewById(R.id.tv_dayAfterTomorrowWeather)
        private val dayAfterTomorrowTemperTV: TextView = view.findViewById(R.id.tv_dayAfterTomorrowTemper)
        private val dayAfterTomorrowWeatherIV: ImageView =
            view.findViewById(R.id.iv_dayAfterTomorrowWeather)

        @SuppressLint("SetTextI18n")
        fun build(model: CityWeatherModel) {
            cityNameTV.text = model.cityName
            updateTimeTV.text = model.updateTime
            nowTemperTV.text = model.nowTemper.toString()
            nowAqiTV.text = model.nowAQI.toString()
            newAqiGradeIV.setImageResource(toAqiIcon(model.nowAQI))
            nowWeatherTV.text = context.getStringResource("weatherType${model.nowWeatherID}")
            nowWindDirectionTV.text = context.getStringResource("WD${model.nowWD}")
            nowWindStrengthTV.text = context.getStringResource("WS${model.nowWS}")

            nowWeatherBackground.setImageResource(
                toWeatherBackground(model.nowWeatherID)(TIME_NORMAL)
            )

            todayWeatherTV.text =
                context.getStringResource("weatherType${model.todayWeatherID}")

            todayTemperTV.text = "${model.todayTemper.first} / ${model.todayTemper.second} ℃"

            todayWeatherIV.setImageResource(toWeatherIcon(model.todayWeatherID))

            tomorrowWeatherTV.text =
                context.getStringResource("weatherType${model.tomorrowWeatherID}")

            tomorrowTemperTV.text =
                "${model.tomorrowTemper.first} / ${model.tomorrowTemper.second} ℃"

            tomorrowWeatherIV.setImageResource(toWeatherIcon(model.tomorrowWeatherID))

            weekDayOfDayAfterTomorrowTV.text =
                context.getStringResource("dayOfWeek${(model.todayWeekDay + 2) % 7}")
            dayAfterTomorrowWeatherTV.text =
                context.getStringResource("weatherType${model.tomorrowWeatherID}")
            dayAfterTomorrowTemperTV.text =
                "${model.dayAfterTomorrowTemper.first} / ${model.dayAfterTomorrowTemper.second} ℃"
            dayAfterTomorrowWeatherIV.setImageResource(toWeatherIcon(model.dayAfterTomorrowWeatherID))
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