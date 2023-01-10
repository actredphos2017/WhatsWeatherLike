package com.sakuno.whatsweatherlike.utils

import com.sakuno.whatsweatherlike.R
import kotlin.reflect.KProperty1

class ResourceUtil {
    companion object {
        fun getResIDByString(aClass: Class<R.string>, name: String): Int {
            var resId = 0
            try {
                val field = aClass.getField(name)
                resId = field.getInt(aClass.newInstance())
            }  catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InstantiationException) {
                e.printStackTrace()
            }
            return resId
        }
    }
}