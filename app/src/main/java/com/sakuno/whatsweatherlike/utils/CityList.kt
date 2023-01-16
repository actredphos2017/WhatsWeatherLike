package com.sakuno.whatsweatherlike.utils

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName


data class CityList(
    @SerializedName("municipalities") val municipalities: List<CityLocation>,
    @SerializedName("other") val other: List<CityLocation>,
    @SerializedName("provinces") val provinces: List<ProvinceLocation>
) {
    companion object {
        fun fromJsonString(jsonString: String): CityList = try {
            Gson().fromJson(jsonString, CityList::class.java)
        } catch (_: Exception) {
            CityList(listOf(), listOf(), listOf())
        }
    }

    private fun toCity(ori: Pair<CityLocation, String>): City {
        return ori.first.coordinate.split(',', '|').run {
            City(
                longitude = (getOrNull(0))?.toDouble() ?: 110.0,
                latitude = (getOrNull(1))?.toDouble() ?: 20.0,
                showName = (if(ori.second.isBlank()) "" else "${ori.second} ") + ori.first.name,
                isLocal = false
            )
        }
    }

    fun searchCity(key: String): Array<City> {
        val resList = mutableListOf<City>()

        for (it in municipalities) if (it.name.indexOf(key) >= 0) resList += toCity(Pair(it, ""))

        for (it in other) if (it.name.indexOf(key) >= 0) resList += toCity(Pair(it, ""))

        for (it in provinces) {
            if (it.name.indexOf(key) >= 0) resList += toCity(
                Pair(
                    CityLocation(
                        it.coordinate,
                        it.name
                    ), ""
                )
            )
            for (each in it.cities) if (each.name.indexOf(key) >= 0) resList += toCity(
                Pair(
                    each,
                    it.name
                )
            )
        }

        return resList.toTypedArray()
    }
}

data class CityLocation(
    @SerializedName("g") val coordinate: String, @SerializedName("n") val name: String
)

data class ProvinceLocation(
    @SerializedName("cities") val cities: List<CityLocation>,
    @SerializedName("g") val coordinate: String,
    @SerializedName("n") val name: String
)