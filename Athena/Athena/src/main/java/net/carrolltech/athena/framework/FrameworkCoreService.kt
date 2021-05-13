package net.carrolltech.athena.framework

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import net.carrolltech.athena.core.SapphireCoreService
import java.util.*

class FrameworkCoreService: SapphireCoreService(){
    //State variables
    var initialized = false

    // This should probably be looked at more
    private var connections: LinkedList<Pair<String, Connection>> = LinkedList()

    // and this. Though this is kind of a 'fake' connection
    var connection = Connection()
    private lateinit var notificationManager: NotificationManager
    private val CHANNEL_ID = "SAF"
    private val NAME = "Sapphire Assistant Framework"
    private val SERVICE_TEXT = "Sapphire Assistant Framework"

    // This holds the available modules. It's close to a registry, and I hate everything about it
    var pendingIntentLedger = mutableMapOf<String, PendingIntent>()

    override fun onCreate() {
        super.onCreate()
        buildForegroundNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        var passedIntent = cleanRoute(intent!!)
        sortMail(passedIntent)
        // This may need to be moved, if I am to do things in the background
        return super.onStartCommand(intent, flags, startId)
    }

    fun buildForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, NAME, importance).apply {
                description = SERVICE_TEXT
            }

            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        var notification = NotificationCompat.Builder(this, CHANNEL_ID)
            //.setSmallIcon(R.drawable.assistant)
            .setContentTitle("Sapphire Assistant")
            .setContentText("Thank you for trying out the Sapphire Framework")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground(1337, notification)
    }

    // What is the nervous systems function called
    fun sortMail(intent: Intent) {
        Log.i("Sorting intent")
        // Looking for a better mental abstraction. These actions are more akin to heartbeats, digestion, etc. Autonomous actions, but unchangeable
        // Handle actions here
        when (initialized) {
            true -> defaultPath(intent)
            false -> when (intent.action) {
                ACTION_SAPPHIRE_INITIALIZE -> startRegistrationService()
                ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE -> initialize(intent)
                ACTION_SAPPHIRE_MODULE_REGISTER -> forwardRegistration(intent)
            }
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
        // Send to processor
        // send to chosen intent
    }

    fun initialize(intent: Intent){
        Log.v("Initializing")
        // Might want to try/catch this
        for(key in intent.getStringArrayListExtra(DATA_KEYS)!!){
            Log.d("Offloading PendingIntent for ${key}")
            // Whelp, just load it up...
            pendingIntentLedger.put(key,intent.getParcelableExtra(key)!!)
        }
        startKaldiService()
        initialized = true
    }

    fun startKaldiService(){

    }

    // Run through the registration process
    fun startRegistrationService(){
        Log.i("Starting registration service")
        var registrationIntent = Intent().setClassName(this.packageName,"${this.packageName}.CoreRegistrationService")
        registrationIntent.setAction(ACTION_SAPPHIRE_INITIALIZE)
        Log.v("starting service ${"${this.packageName}.CoreRegistrationService"}")
        startService(registrationIntent)
    }

    override fun onDestroy() {
        notificationManager.cancel(1337)
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