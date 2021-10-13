package net.carrolltech.athena.core

import android.content.Intent
import android.content.pm.PackageManager
import net.carrolltech.athena.framework.SapphireFrameworkService
import net.carrolltech.athena.framework.SapphireUtils

class CoreRegistrationService: SapphireFrameworkService(){
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        readAssetInfo()

        return super.onStartCommand(intent, flags, startId)
    }

    fun readAssetInfo(){
        val ATHENA_INITIALIZE = "action.athena.skill.INITIALIZE"

        var templateIntent = Intent().setAction(ATHENA_INITIALIZE)
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
