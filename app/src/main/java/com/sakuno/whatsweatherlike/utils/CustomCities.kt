package com.sakuno.whatsweatherlike.utils
import com.google.gson.annotations.SerializedName


data class CustomCities(
    @SerializedName("cities")
    var cities: List<City>
) {

    fun check(): Boolean {
        val oldSize = cities.size
        cities = cities.toMutableList().run {
            removeIf { it.isLocal }
            add(0, City.getLocalCity)
            distinct().toList()
        }
        return cities.size == oldSize
    }
}

data class City(
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("show_name")
    val showName: String,
    @SerializedName("is_local")
    val isLocal: Boolean
) {
    companion object {
        val getLocalCity = City(0.0, 0.0, "", true)
    }
}