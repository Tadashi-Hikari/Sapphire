package net.carrolltech.athena.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.core.app.NotificationCompat
import net.carrolltech.athena.R
import net.carrolltech.athena.ui.MainActivity
import org.json.JSONObject
import java.util.*

/***
 * Honestly, I think I wrote some trash code here, but I'm just hacking it together to get it
 * figured out for now. I want to move away from treating things like a registry, but I need
 * to read a little bit more on design before I figure out what is the best way to go
 */

class CoreService: SapphireCoreService(), TextToSpeech.OnInitListener{

	var ttsInit = false
	var textToSpeech: TextToSpeech? = null

	// I don't like this either
	override fun onInit(status: Int) {
		ttsInit = true
	}

	//State variables
	var initialized = false
	// Oh God, this is the start of a registry. I hate it, and need to make it more 'unix-y'
	var state = JSONObject()
	// This should probably be looked at more
	private var connections: LinkedList<Pair<String, Connection>> = LinkedList()
	// and this. Though this is kind of a 'fake' connection
	var connection = Connection()
	// This holds the available modules. It's close to a registry, and I hate everything about it
	var pendingIntentLedger = mutableMapOf<String,PendingIntent>()

	override fun onCreate() {
		super.onCreate()
		textToSpeech = TextToSpeech(this,this)
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		Log.v("CoreService received an intent")
		var passedIntent = cleanRoute(intent!!)
		sortMail(passedIntent)

		// This may need to be moved, if I am to do things in the background
		return super.onStartCommand(intent, flags, startId)
	}

	// What is the nervous systems function called
	fun sortMail(intent: Intent) {
		Log.i("Sorting intent")
7
		// Handle actions here
		when (initialized) {
			true -> defaultPath(intent)
			false -> when (intent.action) {
				ACTION_SAPPHIRE_INITIALIZE -> startRegistrationService()
				ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE -> initialize(intent)
				ACTION_SAPPHIRE_MODULE_REGISTER -> forwardRegistration(intent)
				ACTION_SAPPHIRE_SPEAK -> speakToUser(intent)
			}
		}
	}

	fun speakToUser(intent: Intent){
		if(ttsInit == true) {
			textToSpeech!!.speak(
				intent.getStringExtra("SPEAKING_PAYLOAD")!!,
				TextToSpeech.QUEUE_FLUSH,
				null,
				null
			)
		}else{
			startService(intent)
		}
	}

	// Can this be wrapped in to nextModule or handleNewInput
	fun forwardRegistration(intent: Intent){
		// I don't think the incoming intent can propagate
		var outgoingIntent = Intent(intent)
		when(outgoingIntent.getStringExtra(FROM)){
			"${this.packageName};${this.packageName}.CoreRegistrationService" -> {
				outgoingIntent.setAction(ACTION_SAPPHIRE_MODULE_REGISTER)
				outgoingIntent.setClassName(intent.getStringExtra(MODULE_PACKAGE)!!,intent.getStringExtra(MODULE_CLASS)!!)
				startRegistrationService(connection,outgoingIntent)
			}
			else -> {
				outgoingIntent.setClassName(PACKAGE_NAME,"${this.packageName}.CoreRegistrationService")
				startService(outgoingIntent)
			}
		}
	}

	fun defaultPath(intent: Intent){
		// Send to processor. How should I track where it is in the path?
		// send to chosen intent
		if(intent.hasExtra("ENTITIES")){
			Log.d(intent.getStringArrayListExtra("ENTITIES").toString())
		}
	}

	fun initialize(intent: Intent){
		Log.v("Initializing")
		// Might want to try/catch this
		for(key in intent.getStringArrayListExtra(DATA_KEYS)!!){
			Log.d("Offloading PendingIntent for ${key}")
			// Whelp, just load it up...
			//pendingIntentLedger.put(key,intent.getParcelableExtra(key)!!)
		}
		startKaldiService()
		initialized = true
	}

	fun startKaldiService(){
		var intent = Intent().setClassName(this,"${packageName}.stt.KaliService")
		startService(intent)
	}

	// Run through the registration process
	/**
	fun startRegistrationService(){
		Log.i("Starting registration service")
		var registrationIntent = Intent().setClassName(this.packageName,"${this.packageName}.CoreRegistrationService")
		registrationIntent.setAction(ACTION_SAPPHIRE_INITIALIZE)
		Log.v("starting service ${"${this.packageName}.CoreRegistrationService"}")
		startService(registrationIntent)
	}
	*/

	// This is just a temporary test
	fun startRegistrationService(){
		var intent = Intent().setClassName(this,"net.carrolltech.athenaalarmskill.simpleAlarmService")

	}

	override fun onDestroy() {
		super.onDestroy()
	}

	// The bound connection. The core attaches to each service as a client, tying them to cores lifecycle
	inner class Connection() : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			Log.i("Service connected")
			if (service != null) {
				// This will be moved to a permission style check, to increase user control and prevent rouge background services
				Toast.makeText(applicationContext, "This service: ${name?.shortClassName} didn't return a null binder, is that ok?", Toast.LENGTH_LONG)
			}
		}

		override fun onServiceDisconnected(name: ComponentName?) {
			Log.i("Service disconnected")
		}
	}
}