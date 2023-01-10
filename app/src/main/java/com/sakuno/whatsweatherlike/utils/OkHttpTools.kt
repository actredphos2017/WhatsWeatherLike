package com.sakuno.whatsweatherlike.utils

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

class OkHttpTools {
    companion object {

        fun getJsonObjectResponse(url: String): String {

            Log.d("OkHttpGET", url)

            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            var response: Response? = null

            try {
                response = client.newCall(request).execute()
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }

            val prepareRes = response.body?.string() ?: ""

            try {
                JSONObject(prepareRes)
            } catch (_: Exception){
                return ""
            }
            return prepareRes
        }
    }
}