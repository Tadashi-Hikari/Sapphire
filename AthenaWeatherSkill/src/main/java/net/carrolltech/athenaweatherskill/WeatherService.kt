package net.carrolltech.athenaweatherskill

import android.app.Service
import android.content.Intent
import android.os.IBinder

class WeatherService : Service() {

    override fun onBind(intent: Intent): IBinder?{
        when(intent?.action){

            null -> stopSelf()
        }

        return null
    }
}