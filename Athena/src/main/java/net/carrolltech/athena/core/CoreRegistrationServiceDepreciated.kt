package net.carrolltech.athena.core

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager.GET_RESOLVED_FILTER
import net.carrolltech.athena.framework.SapphireUtils
import java.lang.Exception

/***
 * This service handles the registration for all compatable modules on the device, at least the ones
 * it's designed for (I intent to make it compatable w/ Google Assistant apps, but that's for another
 * day). The primary thing that needs to be registered is pre-designed pipelines through the assistant.
 * For instance, an generic utterance *should* go from the STT module, to the processor, and then on
 * to it's intended target. However, a user might want to interject another module or route picture
 * data, etc. It's meant to allow a flexible flow of data, and data collection on the fly. This also
 * collects essential PendingIntents for the CoreService, and registers services needed at boot.
 */

class CoreRegistrationServiceDepreciated: SapphireCoreService(){
	var sapphireModuleStack = mutableListOf<Intent>()
	var dataKey = mutableListOf<String>()
	var pendingIntentLedger = Intent()

	val ATHENA_INITIALIZE = "action.athena.skill.INITIALIZE"

	override fun onCreate() {
		super.onCreate()
		Log.i("Starting registration service")
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		when(intent?.action){
			SapphireUtils().ACTION_SAPPHIRE_INITIALIZE -> scanModules()
			else -> Log.e( "There was an issue with the registration intent. Dispatching remaining intents")
		}
		dispatchRemainingIntents()
		return super.onStartCommand(intent, flags, startId)
	}

	fun dispatchRemainingIntents(){
		if(sapphireModuleStack.isNotEmpty()){
			// Pop it from the stack, and dispatch it.
			// Do I need to redirect this to core? ugh, I think I do
			Log.i("Dispatching ${sapphireModuleStack.last().getStringExtra(SapphireUtils().MODULE_CLASS)!!}")
			// Remove the last one in the list
			returnSapphireService(sapphireModuleStack.removeAt(sapphireModuleStack.size-1))
		}else{
			Log.i("All modules registered")
			var finalIntent = Intent()
			finalIntent.action = SapphireUtils().ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE
			finalIntent.setClassName(this,"com.example.sapphireassistantframework.CoreService")
			// Does this cast it ok?
			Log.v("Casting ${dataKey}")
			var dataKeyArrayList = dataKey.toCollection(ArrayList())
			Log.v("Cast result: ${dataKeyArrayList}")
			finalIntent.putExtra(SapphireUtils().DATA_KEYS,dataKeyArrayList)
			// Hopefull this works fine
			finalIntent.fillIn(pendingIntentLedger,0)
			Log.v("Returning PendingIntent names ${finalIntent.getStringArrayListExtra(SapphireUtils().DATA_KEYS)}")
			startService(finalIntent)
		}
	}

	fun scanModules(){
		var templateIntent = Intent().setAction(ATHENA_INITIALIZE)
		var availableSapphireModules = this.packageManager.queryIntentServices(templateIntent,GET_RESOLVED_FILTER)
		Log.i("${availableSapphireModules.size} modules found")

		for(module in availableSapphireModules){
			try{
				var packageName = module.serviceInfo.packageName
				var className = module.serviceInfo.name
				// This will get pushed to a list, and popped off to register all intents
				var registrationIntent = Intent(templateIntent)
				// Add it to the stack (yes, I know it's not a literal stack)
				sapphireModuleStack.add(registrationIntent)
			}catch(exception: Exception){
				Log.d(exception.toString())
				continue
			}
		}
	}

	// Save the PostOfficeService PendingIntent for CoreService
	fun registerPendingIntent(intent: Intent){
		try{
			Log.v("Registering pending intent")
			var pendingIntent = intent.getParcelableExtra<PendingIntent>("PENDING")!!
			// The move to PendingIntent renders the MODULE_PACKAGE and MODULE_CLASS separation pointless
			var moduleInfo = "${intent.getStringExtra(SapphireUtils().MODULE_PACKAGE)};${intent.getStringExtra(SapphireUtils().MODULE_CLASS)}"
			dataKey.add(moduleInfo)
			Log.v("Module info for pending intent: ${moduleInfo}")
			Log.v("Pending intent info: ${pendingIntent}")
			pendingIntentLedger.putExtra(moduleInfo,pendingIntent)
			Log.v("Ledger has ${moduleInfo}?: ${pendingIntentLedger.hasExtra(moduleInfo)}")
		}catch(exception: Exception){
			Log.d("There was an error registering the PendingIntent")
			exception.printStackTrace()
		}
	}
}