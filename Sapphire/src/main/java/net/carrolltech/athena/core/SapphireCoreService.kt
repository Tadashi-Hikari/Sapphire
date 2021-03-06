package net.carrolltech.athena.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import net.carrolltech.athena.framework.SapphireFrameworkService
import net.carrolltech.athena.framework.SapphireUtils
import org.json.JSONObject

/***
 * This just holds a bunch of utility things that I will use across services in the Core module
 */

abstract class SapphireCoreService: SapphireFrameworkService(){

	// This should all be moved into SapphireFrameworkService()
	private val CONFIG = "sample-core-config.conf"

	private var REGISTRATION_TABLE = "registration.tbl"
	private val DEFAULT_MODULES_TABLE = "defaultmodules.tbl"
	private val PENDING_INTENT_TABLE = "pendingintent.tbl"
	private val STARTUP_TABLE = "background.tbl"
	private val ROUTE_TABLE = "routetable.tbl"
	private val ALIAS_TABLE = "alias.tbl"

	// Hmm.... This is close to just passthrough
	fun passthroughService(intent: Intent){
		var postage = intent.getStringExtra(SapphireUtils().POSTAGE)!!
		postage = validatePostage(postage)
		intent.putExtra(SapphireUtils().POSTAGE,postage)
	}

	fun validatePostage(postage: String):String{
		var jsonDefaultModules = JSONObject()
		var jsonPostageTable = JSONObject()

		for(key in jsonDefaultModules.keys()){
			jsonPostageTable.put(key,jsonDefaultModules.getString(key))
		}
		Log.v("Postage is ${jsonPostageTable.toString()}")
		return jsonPostageTable.toString()
	}

	fun expandRoute(route: String): String{
		var routeList = route.split(",").toMutableList()
		// I don't like that this'll load each time. Can I avoid this somehow?
		var variables = loadTable(DEFAULT_MODULES_TABLE)
		Log.v("Defaults tables: ${variables}")
		Log.v("Expanding route ${routeList}")
		// This also doesn't seem to be working
		for(module in routeList.withIndex()){
			Log.v("Checking module ${module.value}")
			if(variables.has(module.value)){
				Log.v("This is a match!")
				routeList.set(module.index,variables.getString(module.value))
			}
		}
		return routeList.joinToString { String -> String }
	}

	// This is only meant to be used by core
	fun startRegistrationService(connection: ServiceConnection, intent: Intent){
		bindService(intent,connection, BIND_AUTO_CREATE)
		// I just need enough time for the service to init, and be non-background
		SystemClock.sleep(700)
		startService(intent)
	}

	fun startPendingService(){
	}
}