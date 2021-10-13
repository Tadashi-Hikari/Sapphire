package net.carrolltech.athena.core

import android.content.Intent
import android.content.pm.PackageManager
import net.carrolltech.athena.framework.SapphireFrameworkService
import net.carrolltech.athena.framework.SapphireUtils

class CoreRegistrationService: SapphireFrameworkService(){
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            SapphireUtils().ACTION_SAPPHIRE_INITIALIZE -> readAssetInfo()
            else -> Log.e( "There was an issue with the registration intents")
        }

        return super.onStartCommand(intent, flags, startId)
    }

    fun readAssetInfo(){
        // I had this set to initialize. Why would I do that
        var templateIntent = Intent().setAction(SapphireUtils().ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE)
        var availableSapphireModules = this.packageManager.queryIntentServices(templateIntent,
            PackageManager.GET_RESOLVED_FILTER
        )

        Log.i("${availableSapphireModules.size} modules found")

        if(".conf" == ".conf"){
            processConf()
        }

        // Let the CoreService know that the registration is finished
        Log.i("All modules registered")
        var finalIntent = Intent().setClassName(this,SapphireUtils().CORE_SERVICE)
        finalIntent.action = SapphireUtils().ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE
        startService(finalIntent)
    }

    fun processConf(){
        // Simple control file
        // Uses record-jar format
    }

    fun checkVersion(string: String){
        var packageInfo = packageManager.getPackageInfo(string,0)
        if(packageInfo.versionName == "0.0.1"){
            Log.d("Cool, the versions match)")
        }
    }

    // if record indicates complex (termux) then ->
        // trigger bound initialize
        // update stuff
        // break binding
    // Collect PendingIntents for CoreServices?
}
