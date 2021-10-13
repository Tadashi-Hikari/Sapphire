package net.carrolltech.athena.core

import android.content.pm.PackageManager
import net.carrolltech.athena.framework.SapphireFrameworkService

class CoreRegistrationServiceRewrite: SapphireFrameworkService(){
    // Look for manifest record (easily accessible)
    // Also asset information
    fun readAssetInfo(){
        if(".conf" == ".conf"){
            processConf()
        }
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
