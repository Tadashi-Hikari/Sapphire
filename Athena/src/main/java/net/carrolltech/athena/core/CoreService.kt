package net.carrolltech.athena.core

import android.app.PendingIntent
import android.content.*
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.widget.Toast
import net.carrolltech.athena.framework.SapphireFrameworkService
import net.carrolltech.athena.framework.SapphireUtils
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
			true -> pathProcessing(intent)
			false -> when (intent.action) {
				ACTION_SAPPHIRE_INITIALIZE -> startRegistrationService()
				ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE -> initialize(intent)
				ACTION_SAPPHIRE_MODULE_REGISTER -> forwardRegistration(intent)
				ACTION_SAPPHIRE_SPEAK -> speakToUser(intent)
			}
		}
	}

	// This is just here to remind me that I need to break the binding to services if they run too long
	fun breakBinding(){

	}

	// This needs a better naming scheme
	fun speakToUser(intent: Intent){
		if(ttsInit == true) {
			textToSpeech!!.speak(
				intent.getStringExtra("SPEAKING_PAYLOAD")!!,
				TextToSpeech.QUEUE_FLUSH,
				null,
				null
			)
		}else{
			Log.d("There was an error getting the system to speak")
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

	// This is used for tracking a flow through the assistant
	fun generateFlowID(){

	}

	fun checkID(id: String): String{
		return "the remaining path for this intent"
	}

	fun unbindPriorService(service: String){
		// This will be used to send the unbind signal
	}

	fun pathProcessing(intent: Intent){
		intent.setClassName(this,"net.carrolltech.athena.natural_language_processor.ProcessorService")
		Log.d("Intent died in pathProcessing")
		//var path = checkID(intent.getStringExtra("ID")!!)
		//unbindPriorService("service")
		// send to chosen intent
		//if(intent.hasExtra("ENTITIES")){
		//	Log.d(intent.getStringArrayListExtra("ENTITIES").toString())
		//}
	}

	// This will be used to start those bound modules
	fun sendToExternalModule(){
		var outgoingIntent = Intent()
		bindService(outgoingIntent,connection,0)
	}

	// This isn't designed to initialize from being set as the assistant. I need to change that
	fun initialize(intent: Intent){
		Log.v("Initializing")
		// Might want to try/catch this
		for(key in intent.getStringArrayListExtra(DATA_KEYS)!!){
			Log.d("Offloading PendingIntent for ${key}")
			// Whelp, just load it up...
			//pendingIntentLedger.put(key,intent.getParcelableExtra(key)!!)
		}
		//This needs to be update for Android Assistant compatiblity
		//startKaldiService()
		// Create a textToSpeech reference. Is this bad for the battery?
		textToSpeech = TextToSpeech(this,this)
		initialized = true
	}

	// Athena can expect this to exist. The Sapphire Framework cannot
	fun startKaldiService(){
		// Check if service is running first
		// This is no longer a thing, since it's been renamed for the Android Assistant
		var intent = Intent().setClassName(this,"${packageName}.stt.KaliService")
		startService(intent)
	}

	// Run through the registration process
	fun startRegistrationService(){
		Log.i("Starting registration service")
		var registrationIntent = Intent().setClassName(this,SapphireUtils().REGISTRATION_SERVICE)
		registrationIntent.setAction(ACTION_SAPPHIRE_INITIALIZE)
		Log.v("starting registration service")
		startService(registrationIntent)
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
			// Update the log
		}

		override fun onBindingDied(name: ComponentName?) {
			//Update the log
		}

		override fun onServiceDisconnected(name: ComponentName?) {
			Log.i("Service disconnected")
			// Update the log
		}
	}
}