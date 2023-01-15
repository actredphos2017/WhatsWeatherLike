package com.sakuno.whatsweatherlike.utils

import android.util.Log
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.google.gson.Gson
import com.sakuno.whatsweatherlike.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class CityWeatherModel {

    fun weatherURL(key: String, longitude: Double, latitude: Double): String {
        return "https://api.caiyunapp.com/v2.6/${key}/${longitude},${latitude}/weather?alert=true&dailysteps=15&hourlysteps=24"
    }

    var available = false

    var weatherInfo: WeatherInfo? = null

    var todayWeekDay: Int = 0

    var updateTime: String = ""

    var cityName: String = "待获取城市名"

    var dataBody: String = ""

    companion object {
        var localCityPosition: City? = null

        val localPositionListener = object : BDAbstractLocationListener() {
            override fun onReceiveLocation(p0: BDLocation) {
                Log.d("BaiduLocation", "ErrorCode: ${p0.locType}")
                localCityPosition = City(p0.longitude, p0.latitude, "", false)
                locateFinish = true
            }
        }

        var locateFinish = false

        var intervalOfCheckInformationAcquisition = 1000L

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

        fun windStrengthIndicator(speed: Double): Int = windStrengthIndicator(speed.toInt())

        fun windStrengthIndicator(speed: Int): Int = when (speed) {
            0 -> 0
            in 1..5 -> 1
            in 6..11 -> 2
            in 12..19 -> 3
            in 20..28 -> 4
            in 29..38 -> 5
            in 39..49 -> 6
            in 50..61 -> 7
            in 62..74 -> 8
            in 75..88 -> 9
            in 89..102 -> 10
            in 103..117 -> 11
            in 118..133 -> 12
            in 134..149 -> 13
            in 150..166 -> 14
            in 167..183 -> 15
            in 184..201 -> 16
            in 202..220 -> 17
            else -> 99
        }

        fun windDirectionIndicator(direction: Double): String =
            windDirectionIndicator(direction.roundToInt())

        fun windDirectionIndicator(direction: Int): String = when (direction) {
            in 0..22 -> "n"
            in 23..67 -> "en"
            in 68..112 -> "e"
            in 113..157 -> "es"
            in 158..202 -> "s"
            in 203..247 -> "ws"
            in 248..292 -> "w"
            in 293..337 -> "wn"
            in 338..360 -> "n"
            else -> "null"
        }

        val toWeatherBackground: (String) -> ((Int) -> Int) = {
            when (it) {
                "CLEAR_DAY", "CLEAR_NIGHT" -> sunnyMipmap
                "PARTLY_CLOUDY_DAY", "PARTLY_CLOUDY_NIGHT", "CLOUDY", "WIND" -> cloudyMipmap
                "FOG" -> overcastMipmap
                "LIGHT_HAZE", "MODERATE_HAZE", "HEAVY_HAZE", "DUST", "SAND" -> foggyMipmap
                "LIGHT_RAIN", "MODERATE_RAIN" -> rainyMipmap
                "STORM_RAIN", "HEAVY_RAIN" -> thunderMipmap
                "LIGHT_SNOW", "MODERATE_SNOW", "HEAVY_SNOW", "STORM_SNOW" -> snowMipmap
                else -> blackMipmap
            }
        }

        val toWeatherIcon: (String) -> Int = {
            when (it) {
                "CLEAR_DAY" -> R.mipmap.sunny_icon
                "CLEAR_NIGHT" -> R.mipmap.sunny_night_icon
                "PARTLY_CLOUDY_DAY", "PARTLY_CLOUDY_NIGHT" -> R.mipmap.cloudy_icon
                "CLOUDY" -> R.mipmap.overcoat_icon
                "LIGHT_HAZE", "MODERATE_HAZE", "HEAVY_HAZE" -> R.mipmap.haze_icon
                "LIGHT_RAIN" -> R.mipmap.small_rainy_icon
                "MODERATE_RAIN" -> R.mipmap.middle_rainy_icon
                "HEAVY_RAIN" -> R.mipmap.big_rainy_icon
                "STORM_RAIN" -> R.mipmap.thunder_rainy_icon
                "FOG" -> R.mipmap.foggy_icon
                "LIGHT_SNOW" -> R.mipmap.small_snowy_icon
                "MODERATE_SNOW" -> R.mipmap.middle_snowy_icon
                "HEAVY_SNOW", "STORM_SNOW" -> R.mipmap.big_snowy_icon
                "DUST", "SAND" -> R.mipmap.dust_icon
                "WIND" -> R.mipmap.wind_icon
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

        fun toAqiGradeStringResourceName(aqi: Int): String = when (aqi) {
            in 0..50 -> "excellent"
            in 51..100 -> "great"
            in 101..150 -> "mild"
            in 151..200 -> "moderate"
            in 201..300 -> "heavy"
            else -> "severe"
        }
    }

    private val localCity: City
        get() {
            while(!locateFinish) {
                Log.d("BaiduLocation", "Trying Locate...")
                Thread.sleep(intervalOfCheckInformationAcquisition)
            }
            Log.d("BaiduLocation", "Locate Finish!")
            return localCityPosition!!
        }

    fun updateWithDataString(dataString: String, showName: String): CityWeatherModel {
        dataBody = dataString

        try {
            weatherInfo = Gson().fromJson(dataBody, WeatherInfo::class.java)

            val calendar = Calendar.getInstance()

            calendar.timeInMillis = weatherInfo!!.serverTime * 1000

            todayWeekDay = calendar.get(Calendar.DAY_OF_WEEK) - 1
            updateTime =
                SimpleDateFormat("HH:mm", Locale("cn")).format(calendar.time)

            Log.d("Data", "InM Time: ${weatherInfo!!.serverTime}")
            Log.d(
                "Data",
                "Update Time: ${calendar.get(Calendar.YEAR)} ${calendar.get(Calendar.MONTH) + 1} ${
                    calendar.get(Calendar.DAY_OF_MONTH)
                }"
            )

            cityName = showName.ifBlank {
                val cityNameBuilder = StringBuilder()
                var isProvinceName = true
                for (it in weatherInfo!!.result.alert.adcodes) {
                    if (isProvinceName) isProvinceName = false
                    else cityNameBuilder.append(' ')
                    cityNameBuilder.append(it.name)
                }
                cityNameBuilder.toString()
            }

            available = true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Weather Api", "Data Package Error")
        }

        return this
    }

    fun updateWithCity(key: String, city: City): CityWeatherModel {

        val prepareCity = if (city.isLocal) localCity else city

        return updateWithDataString(
            OkHttpTools.getJsonObjectResponse(
                weatherURL(
                    key,
                    prepareCity.longitude,
                    prepareCity.latitude
                )
            ), prepareCity.showName
        )
    }

}