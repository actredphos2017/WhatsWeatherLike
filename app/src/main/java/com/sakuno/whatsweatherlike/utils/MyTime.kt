package com.sakuno.whatsweatherlike.utils

import java.util.Calendar

data class MyTime(
    var hour: Int, var min: Int
) {
    companion object {
        fun fromMinute(min: Int) = MyTime(0, min)

        fun fromHour(hour: Int) = MyTime(hour, 0)

        fun fromString(time: String): MyTime {
            val list = time.split(':')
            var hour = 0
            var min = 0
            try {
                when (list.size) {
                    2, 3 -> {
                        hour = list[0].toInt()
                        min = list[1].toInt()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return MyTime(hour, min)
        }

        // "2023-01-11T18:00+08:00"

        fun fromCaiyunDateTimeString(datetime: String): MyTime =
            datetime.split('T', '+').getOrElse(1) { "" }.split(':').takeIf { it.size == 2 }
                ?.run { MyTime(get(0).toIntOrNull() ?: 0, get(1).toIntOrNull() ?: 0) } ?: MyTime(
                0, 0
            )

        fun now(): MyTime =
            Calendar.getInstance().run { MyTime(get(Calendar.HOUR), get(Calendar.MINUTE)) }
    }

    data class MyDateTimeAuxiliary(
        var year: Int, var month: Int, var day: Int, var time: MyTime
    ) {
        companion object {
            fun fromCaiyunDatetimeString(datetime: String): MyDateTimeAuxiliary =
                datetime.split('T', '+').run {
                    val dateRes = get(0).split('-')
                    MyDateTimeAuxiliary(
                        dateRes.getOrNull(0)?.toIntOrNull() ?: 2000,
                        dateRes.getOrNull(1)?.toIntOrNull() ?: 1,
                        dateRes.getOrNull(2)?.toIntOrNull() ?: 1,
                        fromString(getOrNull(1) ?: "0:0")
                    )
                }
        }

        fun toDateString() = "${month}月${day}日"

    }

    init {
        check()
    }

    private fun check(): MyTime {
        while (min >= 60) {
            min -= 60
            hour += 1
        }
        while (hour >= 24) hour -= 24

        while (min < 0) {
            min += 60
            hour -= 1
        }
        while (hour < 0) hour += 24

        return this
    }

    fun toSumMin(): Int {
        return hour * 60 + min
    }

    fun intervalTo(value: MyTime): Int =
        MyTime(hour - value.hour, min - value.min).check().run {
            while (this@run.toSumMin() > 720)
                this@run.hour -= 12
            this@run.toSumMin()
        }

    operator fun plus(value: MyTime): MyTime = MyTime(hour + value.hour, min + value.min)

    operator fun plusAssign(value: MyTime) {
        hour += value.hour
        min += value.min
        check()
    }

    operator fun minus(value: MyTime): MyTime = MyTime(hour - value.hour, min - value.min)

    operator fun minusAssign(value: MyTime) {
        hour -= value.hour
        min -= value.min
        check()
    }

    operator fun times(value: Int): MyTime = MyTime(hour * value, min * value)

    fun toHourTime(): MyTime = MyTime(hour, 0)

    fun toHourString(): String {
        check()
        val resHour = hour.toString().run {
            when (length) {
                1 -> "0$this"
                else -> this
            }
        }
        return "${resHour}h"
    }

    override fun toString(): String {
        check()
        val resHour = hour.toString().run {
            when (length) {
                1 -> "0$this"
                else -> this
            }
        }
        val resMin = min.toString().run {
            when (length) {
                1 -> "0$this"
                else -> this
            }
        }
        return "${resHour}:$resMin"
    }
}