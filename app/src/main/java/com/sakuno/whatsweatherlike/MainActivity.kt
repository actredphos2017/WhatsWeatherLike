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

    var caiyunWeatherKey = "mkhvpq9w0AsN6gjl"

    var baiduAK = "7G00KgUlyZW6DnNI2lM0Xr4NNcP0sqWk"

    fun getStringResource(imageName: String): String =
        resources.getIdentifier(imageName, "string", packageName).takeIf { it != 0 }
            ?.run(::getString) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ViewPager2>(R.id.vp_cardsView).adapter = this.UserCitiesAdapter(
            arrayListOf(
                Pair(113.1257, 22.4219),
                Pair(112.1257, 32.4219)
            )
        )
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

            @SuppressLint("SetTextI18n")
            fun build(position: Pair<Double, Double>) {

                Thread {

                    val model = CityWeatherModel()

                    if(model.updateWithAreaID(caiyunWeatherKey, position.first, position.second).weatherInfo == null){
                        return@Thread
                    }

                    val weather = model.weatherInfo!!.result

                    val cityNameBuilder = StringBuilder()

                    for(it in weather.alert.adcodes)
                        cityNameBuilder.append(it.name)

                    runOnUiThread {
                        cityNameTV.text = cityNameBuilder.toString()
                        updateTimeTV.text = model.updateTime
                        nowTemperTV.text = weather.realtime.temperature.toString()
                        nowAqiTV.text = weather.realtime.airQuality.aqi.chn.toString()
                        newAqiGradeIV.setImageResource(CityWeatherModel.toAqiIcon(weather.realtime.airQuality.aqi.chn))
                        nowWeatherTV.text = getStringResource(weather.realtime.skycon)
                        nowWindDirectionTV.text = getStringResource("WD${CityWeatherModel.windDirectionIndicator(weather.realtime.wind.direction)}")
                        nowWindStrengthTV.text = getStringResource("WS${CityWeatherModel.windStrengthIndicator(weather.realtime.wind.speed)}")

                        nowWeatherBackground.setImageResource(
                            CityWeatherModel.toWeatherBackground(weather.realtime.skycon)(
                                CityWeatherModel.TIME_NORMAL
                            )
                        )

                        todayWeatherTV.text =
                            getStringResource(weather.daily.skycon[0].value)

                        todayTemperTV.text =
                            "${weather.daily.temperature[0].max.toInt()} / ${weather.daily.temperature[0].min.toInt()} ℃"

                        todayWeatherIV.setImageResource(CityWeatherModel.toWeatherIcon(weather.daily.skycon[0].value))

                        tomorrowWeatherTV.text =
                            getStringResource(weather.daily.skycon[1].value)

                        tomorrowTemperTV.text =
                            "${weather.daily.temperature[1].max.toInt()} / ${weather.daily.temperature[1].min.toInt()} ℃"

                        tomorrowWeatherIV.setImageResource(CityWeatherModel.toWeatherIcon(weather.daily.skycon[1].value))

                        weekDayOfDayAfterTomorrowTV.text =
                            getStringResource("dayOfWeek${(model.todayWeekDay + 2) % 7}")
                        dayAfterTomorrowWeatherTV.text =
                            getStringResource(weather.daily.skycon[2].value)
                        dayAfterTomorrowTemperTV.text =
                            "${weather.daily.temperature[2].max.toInt()} / ${weather.daily.temperature[2].min.toInt()} ℃"
                        dayAfterTomorrowWeatherIV.setImageResource(CityWeatherModel.toWeatherIcon(weather.daily.skycon[2].value))
                    }
                }.start()


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