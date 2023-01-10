package com.sakuno.whatsweatherlike

data class CityWeatherModel(
    var cityName: String,
    var nowWeatherID: String,
    var nowTemper: Double,
    var nowAQI: Int,
    var nowWD: Char,
    var nowWS: Char,
    var updateTime: String,
    var todayWeatherID: String,
    var todayTemper: Pair<Double, Double>,
    var todayWeekDay: Int,
    var tomorrowWeatherID: String,
    var tomorrowTemper: Pair<Double, Double>,
    var dayAfterTomorrowWeatherID: String,
    var dayAfterTomorrowTemper: Pair<Double, Double>,
) {
    companion object {
        fun getExampleModel() = CityWeatherModel(
            cityName = "广东中山",
            nowWeatherID = "00",
            nowTemper = 16.0,
            nowAQI = 99,
            nowWD = '2',
            nowWS = '1',
            updateTime = "00:32",
            todayWeatherID = "03",
            todayTemper = Pair(14.0, 20.0),
            todayWeekDay = 1,
            tomorrowWeatherID = "01",
            tomorrowTemper = Pair(12.0, 15.0),
            dayAfterTomorrowWeatherID = "02",
            dayAfterTomorrowTemper = Pair(14.0, 17.0)
        )
    }
}