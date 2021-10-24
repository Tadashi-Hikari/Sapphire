package net.carrolltech.athena.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import net.carrolltech.athena.R
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
 * There is possiblity that this is a priveledged service, as it resides in the same package as the assistant special service
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
	// This holds the available PendingIntents. It's close to a registry, and I hate everything about it
	var pendingIntentLedger = mutableMapOf<String,PendingIntent>()
	// This matches a PendingIntent component to its ID
	var idLedger = JSONObject()
	// This just holds components we're waiting for the pending intent for
	var actionQueue = mutableListOf<String>()

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
				"action.athena.core.PENDING_INTENT" -> updatePendingIntentLedger(intent)
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

	// This needs a better naming scheme
	fun speakToUser(intent: Intent){
		// I think I accidentally deleted this component
		utterance = intent.getStringExtra("SPEAKING_PAYLOAD")
		textToSpeech = TextToSpeech(this,this)
	}

	fun generateId():Int{
		// I should probably do a check to make sure this is unique
		var id = Random().nextInt(1000)
		Log.v("The assigned ID is ${id}")
		return id
	}

	// This is the returned intent...
	fun updatePendingIntentLedger(intent: Intent){
		if(intent.hasExtra(SapphireUtils().PENDING_INTENT)){
			try {
				// -1 means there is some kind of error
				var id = intent.getIntExtra(SapphireUtils().ID,-1)
				// This needs error checking
				var className = idLedger.getString(id.toString())
				pendingIntentLedger.put(
					className,
					intent.getParcelableExtra<PendingIntent>(SapphireUtils().PENDING_INTENT)!!
				)
				Log.v("I've added PendingIntent ID:${id} to the pendingIntentLedger")
				var pendingIntent = intent.getParcelableExtra<PendingIntent>(SapphireUtils().PENDING_INTENT)!!
				// This will not work forever. It's just a quick hack that will only work for now
				checkQueue(id)
			}catch(exception: Exception){
				Log.e("What is this? The PendingIntent data isn't right")
				Log.e(exception.toString())
			}
		}else{
			Log.w("There is some kind of error. This intent didn't contain a PendingIntent. Why did it come here?")
		}
	}

	fun checkQueue(id: Int){
		while(actionQueue.isNotEmpty()){
			// This *might* be very inefficent, depending on the data structure
			var compontentName = actionQueue.removeAt(0)
			if(pendingIntentLedger.containsKey(compontentName)){
				Log.v("A PendingIntent was found in the ledger for ${compontentName}")
				var pendingIntent = pendingIntentLedger.get(compontentName)
				// This works, because we've changed the context to make it this app
				pendingIntent!!.send(this,id,null)
			}else{
				Log.v("There is not a PendingIntent in the ledger for ${compontentName}")
			}
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

	fun pathProcessing(intent: Intent){
		Log.d("Doing path processing")
		// This is for the fixed path
		if(intent?.getStringExtra(SapphireUtils().FROM) == SapphireUtils().STT_ANDROID_SERVICE){
			var newIntent = Intent(intent)
			newIntent.setClassName(this,SapphireUtils().PROCESSOR_SERVICE)
			Log.d("Sending to processor service")
			startService(newIntent)
		}else{
			Log.d("Intent died in pathProcessing. Sending to alarm as a default")
			// This is just for me
			var alarmIntent = Intent().setClassName("net.carrolltech.athenaalarmskill",SapphireUtils().SAPPHIRE_ALARM_SKILL)
			// This is doing the heavy lifting for hte PendingIntent verification
			sendToExternalModule(alarmIntent)
		}
	}

	// This will be used to start those bound modules
	fun sendToExternalModule(intent: Intent){
		//var jsonDictionary = loadTable(SapphireUtils().PENDING_INTENT_TABLE)
		// this will get the classname, which can be used as a UID
		if(intent.component != null){
			var name = intent.component?.className
			if(pendingIntentLedger.containsKey(name)){
				var pendingIntent = pendingIntentLedger.get(name)!!
				// do the thing. Update the information
				pendingIntent.send(this,13,intent)
			}else{
				// This will need to be fixed. This name isn't ensured to be the package that gives back the pending intent. I may need to use the registration service
				// get package name &
				Log.v("There isn't a key in the ledger. Request the PendingIntent")
				// It's an external package, so I had to add this
				var outgoingIntent = Intent().setClassName("net.carrolltech.athenaalarmskill",name!!)
				// This is the UID that will be sent to/returned from the module so I know what I'm tracking
				var id = generateId()
				outgoingIntent.putExtra(SapphireUtils().ID,id)
				// Use the ID as a reference to find the component it's going to
				idLedger.put(id.toString(),outgoingIntent.component!!.className)
				bindService(outgoingIntent,connection,Context.BIND_AUTO_CREATE)
				// Add it to be dispatched upon return. Can I just check the ID instead? No because the ID is for PendingIntents so the record must stay intact
				actionQueue.add(outgoingIntent.component!!.className)
			}
		}else{
			Log.e("This intent isn't set up to go to an external module")
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