package com.sakuno.whatsweatherlike.utils

class AqiCalculator(
    pm25: Double, pm10: Double, o3: Double, so2: Double, no2: Double, co: Double
) {

    private var map = mapOf<GasType, Double>()

    init {
        map = mapOf(
            GasType.PM25 to pm25,
            GasType.PM10 to pm10,
            GasType.O3 to o3,
            GasType.SO2 to so2,
            GasType.NO2 to no2,
            GasType.CO to co,
        )
    }

    fun toMap(): Map<GasType, Triple<Boolean, Double, Double>> {

        val iaqiMap = mutableMapOf<GasType, Double>()

        for ((i, it) in map) iaqiMap[i] = iaqi(it, i)

        val maxType = iaqiMap.maxBy { it.value }.key
        val res = mutableMapOf<GasType, Triple<Boolean, Double, Double>>()

        for ((i, it) in map) res[i] = Triple(i == maxType, it, iaqiMap[i]!!)

        return res.toMap()
    }

    enum class GasType(val table: DoubleArray) {
        IAQIStep(doubleArrayOf(0.0, 50.0, 100.0, 150.0, 200.0, 300.0, 400.0, 500.0)),
        PM25(doubleArrayOf(0.0, 35.0, 75.0, 115.0, 150.0, 250.0, 350.0, 500.0)),
        PM10(doubleArrayOf(0.0, 50.0, 150.0, 250.0, 350.0, 420.0, 500.0, 600.0)),
        O3(doubleArrayOf(0.0, 160.0, 200.0, 300.0, 400.0, 800.0, 1000.0, 1200.0)),
        SO2(doubleArrayOf(0.0, 150.0, 500.0, 650.0, 800.0, 1600.0, 2100.0, 2620.0)),
        NO2(doubleArrayOf(0.0, 100.0, 200.0, 700.0, 1200.0, 2340.0, 3090.0, 3840.0)),
        CO(doubleArrayOf(0.0, 5.0, 10.0, 35.0, 60.0, 90.0, 120.0, 150.0))
    }

    companion object {

        fun iaqi(value: Double, gas: GasType): Double =
            gas.table.takeIf { it.size == 8 }?.sort()?.run {
                var endIndex = 0
                for ((i, it) in gas.table.withIndex()) if (value < it) {
                    endIndex = i
                    break
                }
                (GasType.IAQIStep.table[endIndex] - gas.table[endIndex - 1]) / (gas.table[endIndex] - gas.table[endIndex - 1]) * (value - gas.table[endIndex - 1]) + GasType.IAQIStep.table[endIndex - 1]
            } ?: -1.0
    }
}