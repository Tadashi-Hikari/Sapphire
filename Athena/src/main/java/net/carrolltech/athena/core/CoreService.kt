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

	// Is there a better place to put these state variables?
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
		// Handle actions here. This might be the eventual entrance for a scripting language
		when (initialized) {
			true -> pathProcessing(intent)
			false -> when (intent.action) {
				SapphireUtils().ACTION_SAPPHIRE_INITIALIZE -> startRegistrationService()
				// Why does registration start inti, and not ACTION_SAPPHIRE_INITIALIZE. That's counterintuitive
				SapphireUtils().ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE -> initialize(intent)
				SapphireUtils().ACTION_SAPPHIRE_MODULE_REGISTER -> forwardRegistration(intent)
				SapphireUtils().ACTION_SAPPHIRE_SPEAK -> speakToUser(intent)
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
		// I think this is where the infinite loop is happening
		var outgoingIntent = Intent(intent)
		// This relies on "FROM". Should it need to?
		when(outgoingIntent.getStringExtra(SapphireUtils().FROM)){
			"${this.packageName};${SapphireUtils().REGISTRATION_SERVICE}" -> {
				outgoingIntent.setAction(SapphireUtils().ACTION_SAPPHIRE_MODULE_REGISTER)
				outgoingIntent.setClassName(intent.getStringExtra(SapphireUtils().MODULE_PACKAGE)!!,intent.getStringExtra(SapphireUtils().MODULE_CLASS)!!)
				startRegistrationService(connection,outgoingIntent)
			}
			else -> {

				outgoingIntent.setClassName(PACKAGE_NAME,SapphireUtils().REGISTRATION_SERVICE)
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
		intent.setClassName(this,SapphireUtils().PROCESSOR_SERVICE)
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
	fun initialize(intent: Intent?){
		Log.v("Initializing")
		textToSpeech = TextToSpeech(this,this)
		initialized = true
	}

	// Athena can expect this to exist. The Sapphire Framework cannot
	fun startKaldiService(){
		// Check if service is running first
		// This is no longer a thing, since it's been renamed for the Android Assistant
		var intent = Intent().setClassName(this,"${packageName}.stt.KaldiService")
		startService(intent)
	}

	// Run through the registration process
	fun startRegistrationService(){
		var registrationIntent = Intent().setClassName(this,SapphireUtils().REGISTRATION_SERVICE)
		registrationIntent.setAction(SapphireUtils().ACTION_SAPPHIRE_INITIALIZE)
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