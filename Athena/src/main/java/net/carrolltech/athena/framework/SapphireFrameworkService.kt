package net.carrolltech.athena.framework

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

/***
 * This is supposed to be a service that can be used to make the development of new modules pretty
 * painless. It mostly contains strings for messaging w/in the framework, and a few convenience
 * functions
 */

abstract class SapphireFrameworkService: Service() {
	var CLASS_NAME = this.javaClass.name

	var Log = LogOverride()

	inner class LogOverride{
		fun i(message: String){
			android.util.Log.i(CLASS_NAME,message)
			broadcastStatus(CLASS_NAME,message)
		}

		fun d(message: String){
			android.util.Log.d(CLASS_NAME,message)
			broadcastStatus(CLASS_NAME,message)
		}

		fun e(message: String){
			android.util.Log.e(CLASS_NAME,message)
			broadcastStatus(CLASS_NAME,message)
		}

		fun v(message: String){
			android.util.Log.v(CLASS_NAME,message)
			broadcastStatus(CLASS_NAME,message)
		}

		fun w(message: String){
			android.util.Log.w(CLASS_NAME,message)
			broadcastStatus(CLASS_NAME,message)
		}
	}

	lateinit var PACKAGE_NAME: String

	open override fun onCreate() {
		super.onCreate()
		// This needs a created context to work
		PACKAGE_NAME = this.packageName
	}

	var jsonPostage = JSONObject()

	fun loadTable(tablename: String): JSONObject{
		var moduleJsonDataTable = JSONObject()

		var file = File(filesDir,tablename)
		when(file.exists()){
			true -> moduleJsonDataTable = JSONObject(file.readText())
			false -> moduleJsonDataTable = JSONObject()
		}
		return moduleJsonDataTable
	}

	fun convertAssetToFile(filename: String): File {
		var suffix = ".temp"
		// This file needs to be tab separated columns
		var asset = assets.open(filename)
		var fileReader = InputStreamReader(asset)

		var tempFile = File.createTempFile(filename, suffix)
		var tempFileWriter = FileOutputStream(tempFile)
		// This is ugly AF
		var data = fileReader.read()
		while (data != -1) {
			tempFileWriter.write(data)
			data = fileReader.read()
		}
		// Do a little clean up
		asset.close()
		tempFileWriter.close()

		return tempFile
	}

	fun broadcastStatus(name: String, message:String) {
		var notifyIntent = Intent()
		notifyIntent.putExtra(SapphireUtils().MESSAGE, "${name}: ${message}")
		notifyIntent.setAction(SapphireUtils().GUI_BROADCAST)
		sendBroadcast(notifyIntent)
	}

	// I need this to do my bound stuff, cause I'm lazy and don't want to do it a ton
	fun startSapphireService(intent: Intent){
		var updatedIntent = Intent(intent)
		updatedIntent = validatePostage(intent)
		startService(updatedIntent)
	}

	// The name is a little overkill, but w/e
	fun dispatchSapphireServiceToCore(intent:Intent){
		intent.setClassName(this,"${this.packageName}.CoreService")
		startService(intent)
	}

	fun validatePostage(intent: Intent): Intent{
		var jsonDefaultModules = JSONObject()
		for(key in jsonDefaultModules.keys()){
			jsonPostage!!.put(key,jsonDefaultModules.getString(key))
		}
		Log.v("Postage is ${jsonPostage.toString()}")
		intent.putExtra(SapphireUtils().POSTAGE,jsonPostage!!.toString())

		return intent
	}

	// This is for having a SAF compontent pass along the route w/o a callback to core
	fun parseRoute(string: String): List<String>{
		var route = string.split(",")
		return route
	}

	// This needs to be made generic. It returns an intent to Core for processing
	fun returnSapphireService(intent: Intent){
		var returnIntent = Intent(intent)
		// This should read from postage
		returnIntent.setClassName(this,"com.example.sapphireassistantframework.CoreService")
		returnIntent.putExtra(SapphireUtils().FROM,"${this.packageName};${this.javaClass.name}")
		startService(returnIntent)
	}

	override fun onBind(intent: Intent?): IBinder? {
		if(intent!!.hasExtra("ACTUALLY_BIND")){
			TODO("Let someone else implement this logic. It should be overwritten")
		}
		return null
	}
}