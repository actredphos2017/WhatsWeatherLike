package com.sakuno.whatsweatherlike.utils

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName


class CityList : ArrayList<NewCityListItem>() {

    companion object {
        fun fromJsonString(jsonString: String): CityList = try {
            Gson().fromJson(jsonString, CityList::class.java)
        } catch (_: Exception) {
            arrayListOf<NewCityListItem>() as CityList
        }
    }

    fun searchCity(key: String): Array<City> {
        val resList = mutableListOf<City>()

        for (it in this) if (it.province.indexOf(key) >= 0 || it.city.indexOf(key) >= 0 || it.district.indexOf(key) >= 0
        ) resList += it.toCity()

        return resList.toTypedArray()
    }

}

data class NewCityListItem(
    @SerializedName("city") val city: String,
    @SerializedName("cityId") val cityId: String,
    @SerializedName("district") val district: String,
    @SerializedName("latitude") val latitude: String,
    @SerializedName("longitude") val longitude: String,
    @SerializedName("province") val province: String
) {
    fun toCity(): City = City(
        longitude = longitude.toDouble(),
        latitude = latitude.toDouble(),
        showName = "${if (province != city) "$province " else ""}${city}${if (district == city) "" else " $district"}",
        isLocal = false
    )

}