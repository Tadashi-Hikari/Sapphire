package net.carrolltech.athena.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import net.carrolltech.athena.R

/***
 * For now, I'm going to have this show the basic settings of Athena, and list all installed modules
 *
 */

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    fun populateSettingsButtons(view: View){
        // I need to look more into context. I still don't quite get it
        var button = Button(this)
        button.text = "Something"
        // This is just an example, I don't this code would work
        //ScrollView.add?(button)
    }

    fun generateInstalledModulesList(view: View){

    }

    fun accessIntentFiles(){

    }

    fun accessEntityFiles(){

    }
}