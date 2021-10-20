package net.carrolltech.athena.core

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
import net.carrolltech.athena.framework.SapphireFrameworkService
import net.carrolltech.athena.framework.SapphireUtils
import org.json.JSONObject
import java.util.*

/***
 * CoreService handles redirecting intents through Athena, to ensure that Android doesn't block the
 * intent as being from a background service. It also offers a hook for the Sapphire Framework (not
 * yet implemented) so that the system can be made more modular
 */

/**
 * This might not start w/o the activity showing, since it's a normal service. I can either add
 * an invisible overlay, or embrace the UI that pops up for Google assistant, and work it in to this
 * I can also add a start/stop foreground service when processing, though I imagine that'd get annoying
 */

/**
 * There is possibliity that this is a priveledged service, as it resides in the same package as the assistant special service
 */

class CoreService: SapphireCoreService(), TextToSpeech.OnInitListener{

	// This should be thread safe... Should I shut it down when not in use? or is that too resource intense
	override fun onInit(status: Int) {
		if(utterance != null) {
			textToSpeech!!.speak(
				utterance,
				TextToSpeech.QUEUE_FLUSH,
				null,
				null
			)
		}
	}

	// Is there a better place to put these state variables?
	var utterance: String? = null
	var textToSpeech: TextToSpeech? = null

	//State variables
	var initialized = false
	// Oh God, this is the start of a registry. I hate it, and need to make it more 'unix-y'
	var state = JSONObject()
	// This should probably be looked at more
	private var connections: LinkedList<Pair<String, Connection>> = LinkedList()
	// and this. Though this is kind of a 'fake' connection
	// Shit. I think I need one of these for EVERY connection
	var connection = Connection()
	// This holds the available modules. It's close to a registry, and I hate everything about it
	var pendingIntentLedger = mutableMapOf<String,PendingIntent>()

	override fun onCreate() {
		super.onCreate()
		// When this service is first run, start it up!
		// This may not be needed, I just have to see WHY processor service isn't starting
		buildForegroundNotification()
		startRegistrationService()
	}

	private lateinit var notificationManager: NotificationManager
	private val CHANNEL_ID = "SAF"
	private val NAME = "Sapphire Assistant Framework"
	private val SERVICE_TEXT = "Sapphire Assistant Framework"

	fun buildForegroundNotification() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val importance = NotificationManager.IMPORTANCE_HIGH
			val channel = NotificationChannel(CHANNEL_ID, NAME, importance).apply {
				description = SERVICE_TEXT
			}

			notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.createNotificationChannel(channel)
		}

		var notification = NotificationCompat.Builder(this, "SAF")
			.setSmallIcon(R.drawable.ic_launcher_foreground)
			.setContentTitle("Sapphire Assistant")
			.setContentText("Thank you for trying out the Sapphire Framework")
			.setOngoing(true)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.build()

		startForeground(1337, notification)
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		Log.v("CoreService received an intent")
		if(intent == null) {
			Log.w("For some reason, core service didn't actually receive an intent. It just said it did")
		}else {
			sortMail(intent)
		}

		// This may need to be moved, if I am to do things in the background
		return super.onStartCommand(intent, flags, startId)
	}

	// What is the nervous systems function called
	fun sortMail(intent: Intent) {
		Log.i("Sorting intent")
7
		// Handle actions here. This might be the eventual entrance for a scripting language
		when (initialized) {
			true -> when (intent.action) {
				SapphireUtils().ACTION_SAPPHIRE_SPEAK -> speakToUser(intent)
				else -> pathProcessing(intent)
			}
			false -> when (intent.action) {
				// This should be an "on boot" or "when set to assistant" thing... Will it even start w/o the UI?
				// It needs to install modules and train the processor
				SapphireUtils().ACTION_SAPPHIRE_INITIALIZE -> startRegistrationService()
				// Why does registration start inti, and not ACTION_SAPPHIRE_INITIALIZE. That's counterintuitive
				SapphireUtils().ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE -> initialize()
				SapphireUtils().ACTION_SAPPHIRE_MODULE_REGISTER -> forwardRegistration(intent)
			}
		}
	}

	// This is just here to remind me that I need to break the binding to services if they run too long
	fun breakBinding(){

	}

	// This needs a better naming scheme
	fun speakToUser(intent: Intent){
		// I think I accidentally deleted this component
		utterance = intent.getStringExtra("SPEAKING_PAYLOAD")
		textToSpeech = TextToSpeech(this,this)
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
	fun generateFlowID():String{
		return "1"
	}

	fun pathProcessing(intent: Intent){
		Log.d("Doing path processing")
		if(intent?.getStringExtra(SapphireUtils().FROM) == SapphireUtils().STT_ANDROID_SERVICE){
			var newIntent = Intent(intent)
			newIntent.setClassName(this,SapphireUtils().PROCESSOR_SERVICE)
			Log.d("Sending to processor service")
			startService(newIntent)
		}else {
			Log.d("Intent died in pathProcessing. Sending to alarm as a default")
			// This is just for me
			var alarmIntent = Intent().setClassName("net.carrolltech.athenaalarmskill",SapphireUtils().SAPPHIRE_ALARM_SKILL)
			startService(alarmIntent)
			//bindService(alarmIntent,connection,Context.BIND_AUTO_CREATE)
		}
		//var path = checkID(intent.getStringExtra("ID")!!)
		//unbindPriorService("service")
		// send to chosen intent
		//if(intent.hasExtra("ENTITIES")){
		//	Log.d(intent.getStringArrayListExtra("ENTITIES").toString())
		//}
	}

	// This will be used to start those bound modules
	fun sendToExternalModule(name: String, intent: Intent){
		//var jsonDictionary = loadTable(SapphireUtils().PENDING_INTENT_TABLE)
		// This is just pseudocode
		if(pendingIntentLedger.containsKey(name)){
			var pendingIntent = pendingIntentLedger.get(name)!!
			// do the thing. Update the information
			pendingIntent.send(this,13,intent)
		}else{
			var outgoingIntent = Intent()
			bindService(outgoingIntent,connection,Context.BIND_AUTO_CREATE)
		}
	}

	// This isn't designed to initialize from being set as the assistant. I need to change that
	fun initialize(){
		Log.v("Initializing")
		textToSpeech = TextToSpeech(this,this)
		var trainIntent = Intent().setClassName(this,SapphireUtils().PROCESSOR_SERVICE)
		trainIntent.setAction(SapphireUtils().ACTION_SAPPHIRE_PROCESSOR_TRAIN)
		// I need to remember to do this
		startService(trainIntent)
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