package com.sakuno.whatsweatherlike.utils

import com.google.gson.annotations.SerializedName

data class WeatherInfo(
    @SerializedName("api_status")
    val apiStatus: String,
    @SerializedName("api_version")
    val apiVersion: String,
    @SerializedName("lang")
    val lang: String,
    @SerializedName("location")
    val location: List<Double>,
    @SerializedName("result")
    val result: Result,
    @SerializedName("server_time")
    val serverTime: Long,
    @SerializedName("status")
    val status: String,
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("tzshift")
    val tzshift: Int,
    @SerializedName("unit")
    val unit: String
)

data class Result(
    @SerializedName("alert")
    val alert: Alert,
    @SerializedName("daily")
    val daily: Daily,
    @SerializedName("forecast_keypoint")
    val forecastKeypoint: String,
    @SerializedName("hourly")
    val hourly: Hourly,
    @SerializedName("minutely")
    val minutely: Minutely,
    @SerializedName("primary")
    val primary: Int,
    @SerializedName("realtime")
    val realtime: Realtime
)

data class Alert(
    @SerializedName("adcodes")
    val adcodes: List<Adcode>,
    @SerializedName("content")
    val content: List<AlertInfo>,
    @SerializedName("status")
    val status: String
)

data class AlertInfo(
    @SerializedName("province")
    val province: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("code")
    val code: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("regionId")
    val regionId: String,
    @SerializedName("county")
    val county: String,
    @SerializedName("pubtimestamp")
    val pubtimestamp: Long,
    @SerializedName("latlon")
    val latlon: List<Double>,
    @SerializedName("city")
    val city: String,
    @SerializedName("alertId")
    val alertId: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("adcode")
    val adcode: String,
    @SerializedName("source")
    val source: String,
    @SerializedName("location")
    val location: String,
    @SerializedName("request_status")
    val request_status: String,

)

data class Daily(
    @SerializedName("air_quality")
    val airQuality: AirQuality,
    @SerializedName("astro")
    val astro: List<Astro>,
    @SerializedName("cloudrate")
    val cloudrate: List<Cloudrate>,
    @SerializedName("dswrf")
    val dswrf: List<Dswrf>,
    @SerializedName("humidity")
    val humidity: List<Humidity>,
    @SerializedName("life_index")
    val lifeIndex: LifeIndex,
    @SerializedName("precipitation")
    val precipitation: List<Precipitation>,
    @SerializedName("precipitation_08h_20h")
    val precipitation08h20h: List<Precipitation>,
    @SerializedName("precipitation_20h_32h")
    val precipitation20h32h: List<Precipitation>,
    @SerializedName("pressure")
    val pressure: List<Pressure>,
    @SerializedName("skycon")
    val skycon: List<Skycon>,
    @SerializedName("skycon_08h_20h")
    val skycon08h20h: List<Skycon>,
    @SerializedName("skycon_20h_32h")
    val skycon20h32h: List<Skycon>,
    @SerializedName("status")
    val status: String,
    @SerializedName("temperature")
    val temperature: List<Temperature>,
    @SerializedName("temperature_08h_20h")
    val temperature08h20h: List<Temperature>,
    @SerializedName("temperature_20h_32h")
    val temperature20h32h: List<Temperature>,
    @SerializedName("visibility")
    val visibility: List<Visibility>,
    @SerializedName("wind")
    val wind: List<Wind>,
    @SerializedName("wind_08h_20h")
    val wind08h20h: List<Wind>,
    @SerializedName("wind_20h_32h")
    val wind20h32h: List<Wind>
)


data class Skycon(
    @SerializedName("date")
    val date: String,
    @SerializedName("value")
    val value: String,
)


data class Hourly(
    @SerializedName("air_quality")
    val airQuality: AirQualityX,
    @SerializedName("apparent_temperature")
    val apparentTemperature: List<ApparentTemperature>,
    @SerializedName("cloudrate")
    val cloudrate: List<CloudrateX>,
    @SerializedName("description")
    val description: String,
    @SerializedName("dswrf")
    val dswrf: List<DswrfX>,
    @SerializedName("humidity")
    val humidity: List<HumidityX>,
    @SerializedName("precipitation")
    val precipitation: List<PrecipitationX>,
    @SerializedName("pressure")
    val pressure: List<PressureX>,
    @SerializedName("skycon")
    val skycon: List<SkyconX>,
    @SerializedName("status")
    val status: String,
    @SerializedName("temperature")
    val temperature: List<TemperatureX>,
    @SerializedName("visibility")
    val visibility: List<VisibilityX>,
    @SerializedName("wind")
    val wind: List<WindX>
)

data class Minutely(
    @SerializedName("datasource")
    val datasource: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("precipitation")
    val precipitation: List<Double>,
    @SerializedName("precipitation_2h")
    val precipitation2h: List<Double>,
    @SerializedName("probability")
    val probability: List<Double>,
    @SerializedName("status")
    val status: String
)

data class Realtime(
    @SerializedName("air_quality")
    val airQuality: AirQualityXX,
    @SerializedName("apparent_temperature")
    val apparentTemperature: Double,
    @SerializedName("cloudrate")
    val cloudrate: Double,
    @SerializedName("dswrf")
    val dswrf: Double,
    @SerializedName("humidity")
    val humidity: Double,
    @SerializedName("life_index")
    val lifeIndex: LifeIndexX,
    @SerializedName("precipitation")
    val precipitation: PrecipitationXX,
    @SerializedName("pressure")
    val pressure: Double,
    @SerializedName("skycon")
    val skycon: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("temperature")
    val temperature: Double,
    @SerializedName("visibility")
    val visibility: Double,
    @SerializedName("wind")
    val wind: WindXX
)

data class Adcode(
    @SerializedName("adcode")
    val adcode: Int,
    @SerializedName("name")
    val name: String
)

data class AirQuality(
    @SerializedName("aqi")
    val aqi: List<Aqi>,
    @SerializedName("pm25")
    val pm25: List<Pm25>
)

data class Astro(
    @SerializedName("date")
    val date: String,
    @SerializedName("sunrise")
    val sunrise: Sunrise,
    @SerializedName("sunset")
    val sunset: Sunset
)

data class Cloudrate(
    @SerializedName("avg")
    val avg: Double,
    @SerializedName("date")
    val date: String,
    @SerializedName("max")
    val max: Double,
    @SerializedName("min")
    val min: Double
)

data class Dswrf(
    @SerializedName("avg")
    val avg: Double,
    @SerializedName("date")
    val date: String,
    @SerializedName("max")
    val max: Double,
    @SerializedName("min")
    val min: Double
)

data class Humidity(
    @SerializedName("avg")
    val avg: Double,
    @SerializedName("date")
    val date: String,
    @SerializedName("max")
    val max: Double,
    @SerializedName("min")
    val min: Double
)

data class LifeIndex(
    @SerializedName("carWashing")
    val carWashing: List<CarWashing>,
    @SerializedName("coldRisk")
    val coldRisk: List<ColdRisk>,
    @SerializedName("comfort")
    val comfort: List<Comfort>,
    @SerializedName("dressing")
    val dressing: List<Dressing>,
    @SerializedName("ultraviolet")
    val ultraviolet: List<Ultraviolet>
)

data class Precipitation(
    @SerializedName("avg")
    val avg: Double,
    @SerializedName("date")
    val date: String,
    @SerializedName("max")
    val max: Double,
    @SerializedName("min")
    val min: Double,
    @SerializedName("probability")
    val probability: Int
)

data class Pressure(
    @SerializedName("avg")
    val avg: Double,
    @SerializedName("date")
    val date: String,
    @SerializedName("max")
    val max: Double,
    @SerializedName("min")
    val min: Double
)

data class Temperature(
    @SerializedName("avg")
    val avg: Double,
    @SerializedName("date")
    val date: String,
    @SerializedName("max")
    val max: Double,
    @SerializedName("min")
    val min: Double
)

data class Visibility(
    @SerializedName("avg")
    val avg: Double,
    @SerializedName("date")
    val date: String,
    @SerializedName("max")
    val max: Double,
    @SerializedName("min")
    val min: Double
)

data class Wind(
    @SerializedName("avg")
    val avg: AvgX,
    @SerializedName("date")
    val date: String,
    @SerializedName("max")
    val max: WindInfo,
    @SerializedName("min")
    val min: WindInfo
)

data class WindInfo(
    @SerializedName("speed")
    val speed: Double,
    @SerializedName("direction")
    val direction: Double
)

data class Aqi(
    @SerializedName("avg")
    val avg: Avg,
    @SerializedName("date")
    val date: String,
    @SerializedName("max")
    val max: Max,
    @SerializedName("min")
    val min: Min
)

data class Pm25(
    @SerializedName("avg")
    val avg: Int,
    @SerializedName("date")
    val date: String,
    @SerializedName("max")
    val max: Int,
    @SerializedName("min")
    val min: Int
)

data class Avg(
    @SerializedName("chn")
    val chn: Int,
    @SerializedName("usa")
    val usa: Int
)

data class Max(
    @SerializedName("chn")
    val chn: Int,
    @SerializedName("usa")
    val usa: Int
)

data class Min(
    @SerializedName("chn")
    val chn: Int,
    @SerializedName("usa")
    val usa: Int
)

data class Sunrise(
    @SerializedName("time")
    val time: String
)

data class Sunset(
    @SerializedName("time")
    val time: String
)

data class CarWashing(
    @SerializedName("date")
    val date: String,
    @SerializedName("desc")
    val desc: String,
    @SerializedName("index")
    val index: String
)

data class ColdRisk(
    @SerializedName("date")
    val date: String,
    @SerializedName("desc")
    val desc: String,
    @SerializedName("index")
    val index: String
)

data class Comfort(
    @SerializedName("date")
    val date: String,
    @SerializedName("desc")
    val desc: String,
    @SerializedName("index")
    val index: String
)

data class Dressing(
    @SerializedName("date")
    val date: String,
    @SerializedName("desc")
    val desc: String,
    @SerializedName("index")
    val index: String
)

data class Ultraviolet(
    @SerializedName("date")
    val date: String,
    @SerializedName("desc")
    val desc: String,
    @SerializedName("index")
    val index: String
)

data class AvgX(
    @SerializedName("direction")
    val direction: Double,
    @SerializedName("speed")
    val speed: Double
)

data class AirQualityX(
    @SerializedName("aqi")
    val aqi: List<AqiX>,
    @SerializedName("pm25")
    val pm25: List<Pm25X>
)

data class ApparentTemperature(
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("value")
    val value: Double
)

data class CloudrateX(
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("value")
    val value: Double
)

data class DswrfX(
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("value")
    val value: Double
)

data class HumidityX(
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("value")
    val value: Double
)

data class PrecipitationX(
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("probability")
    val probability: Int,
    @SerializedName("value")
    val value: Double
)

data class PressureX(
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("value")
    val value: Double
)

data class SkyconX(
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("value")
    val value: String
)

data class TemperatureX(
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("value")
    val value: Double
)

data class VisibilityX(
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("value")
    val value: Double
)

data class WindX(
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("direction")
    val direction: Double,
    @SerializedName("speed")
    val speed: Double
)

data class AqiX(
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("value")
    val value: Value
)

data class Pm25X(
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("value")
    val value: Int
)

data class Value(
    @SerializedName("chn")
    val chn: Int,
    @SerializedName("usa")
    val usa: Int
)

data class AirQualityXX(
    @SerializedName("aqi")
    val aqi: AqiXX,
    @SerializedName("co")
    val co: Double,
    @SerializedName("description")
    val description: Description,
    @SerializedName("no2")
    val no2: Double,
    @SerializedName("o3")
    val o3: Double,
    @SerializedName("pm10")
    val pm10: Double,
    @SerializedName("pm25")
    val pm25: Double,
    @SerializedName("so2")
    val so2: Double
)

data class LifeIndexX(
    @SerializedName("comfort")
    val comfort: ComfortX,
    @SerializedName("ultraviolet")
    val ultraviolet: UltravioletX
)

data class PrecipitationXX(
    @SerializedName("local")
    val local: Local,
    @SerializedName("nearest")
    val nearest: Nearest
)

data class WindXX(
    @SerializedName("direction")
    val direction: Double,
    @SerializedName("speed")
    val speed: Double
)

data class AqiXX(
    @SerializedName("chn")
    val chn: Int,
    @SerializedName("usa")
    val usa: Int
)

data class Description(
    @SerializedName("chn")
    val chn: String,
    @SerializedName("usa")
    val usa: String
)

data class ComfortX(
    @SerializedName("desc")
    val desc: String,
    @SerializedName("index")
    val index: Int
)

data class UltravioletX(
    @SerializedName("desc")
    val desc: String,
    @SerializedName("index")
    val index: Int
)

data class Local(
    @SerializedName("datasource")
    val datasource: String,
    @SerializedName("intensity")
    val intensity: Double,
    @SerializedName("status")
    val status: String
)

data class Nearest(
    @SerializedName("distance")
    val distance: Double,
    @SerializedName("intensity")
    val intensity: Double,
    @SerializedName("status")
    val status: String
)