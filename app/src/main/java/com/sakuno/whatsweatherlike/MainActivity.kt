package com.sakuno.whatsweatherlike

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2

class MainActivity : Activity() {

    fun getStringResource(imageName: String): String =
        resources.getIdentifier(imageName, "string", packageName).takeIf { it != 0 }
            ?.run(::getString) ?: ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ViewPager2>(R.id.vp_cardsView).adapter = UserCitiesAdapter(
            arrayListOf(
                CityWeatherModel.getExampleModel(),
                CityWeatherModel.getExampleModel(),
                CityWeatherModel.getExampleModel(),
                CityWeatherModel.getExampleModel(),
                CityWeatherModel.getExampleModel(),
                CityWeatherModel.getExampleModel()
            ),
            this
        )
    }
}