package net.carrolltech.athena.sapphire_framework

abstract class SapphireUtils{
    val CORE_SERVICE="net.carrolltech.athena.core.CoreService"
    val REGISTRATION_SERVICE="net.carrolltech.athena.core.CoreRegistrationService"
    val PROCESSOR_SERVICE="net.carrolltech.athena.natural_language_processor.ProcessorService"
    val ENTITY_TRAINING_SERVICE="net.carrolltech.athena.natural_language_processor.ProcessorEntityTrainingService"
    val CLASSIFIER_TRAINING_SERVICE="net.carrolltech.athena.natural_language_processor.ProcessorTrainingService"
    val TTS_SERVICE="net.carrolltech.athena.tts_service."
    val MAIN_ACTIVITY="net.carrolltech.athena.tts_service.MainActivity"
    val TTS_ACTUAL_SERVICE="net.carrolltech.athena.tts_service.tts.AthenaActualTextToSpeechService"
    val TTS_ANDROID_SERVICE="net.carrolltech.athena.tts_service.tts.AthenaTextToSpeechService"
    val STT_ANDROID_SERVICE="net.carrolltech.athena.stt_service.AthenaVoiceInteractionSessionService"
    val SAPPHIRE_ALARM_SKILL="net.carrolltech.athenaalarmskill.AlarmService"

    val PENDING_INTENT = "assistant.framework.module.protocol.PENDING_INTENT"

    var REGISTRATION_TABLE = "registration.tbl"
    val DEFAULT_MODULES_TABLE = "defaultmodules.tbl"
    val PENDING_INTENT_TABLE = "pendingintent.tbl"
    val STARTUP_TABLE = "background.tbl"
    val ROUTE_TABLE = "routetable.tbl"
    val ALIAS_TABLE = "alias.tbl"

    val MESSAGE="assistant.framework.protocol.MESSAGE"
    val STDERR="assistant.framework.protocol.STDERR"
    // This is going to be for ENV_VARIABLES
    val POSTAGE="assistant.framework.protocol.POSTAGE"
    // Route is used by a module to update the path on the go. Will need security
    val ROUTE="assistant.framework.protocol.ROUTE"
    // This is only needed from the originating module
    val FROM= "assistant.framework.protocol.FROM"
    val ID = "assistant.framework.module.protocol.ID"
    // This shouldn't be needed, since ID can track it
    val RECEIPIANT = "assistant.framework.module.protocol.RECEIPIANT"

    // Maybe this should be used elsewhere...
    var STARTUP_ROUTE = "assistant.framework.protocol.STARTUP_ROUTE"

    val MODULE_PACKAGE = "assistant.framework.module.protocol.PACKAGE"
    val MODULE_CLASS = "assitant.framework.module.protocol.CLASS"
    var MODULE_TYPE = "assistant.framework.module.protocol.TYPE"
    val MODULE_VERSION = "assistant.framework.module.protocol.VERSION"

    /**
     * I don't know that I need to list all of these explicitly, and I think I'll
     * let the user override them anyway. This is just for initial install purposes
     */
    val CORE="assistant.framework.module.type.CORE"
    val PROCESSOR="assistant.framework.module.type.PROCESSOR"
    val MULTIPROCESS="assistant.framework.module.type.MULTIPROCESS"
    // These are the ones I don't think are essential
    val INPUT="assistant.framework.module.type.INPUT"
    val TERMINAL="assistant.framework.module.type.TERMINAL"
    val GENERIC="assistant.framework.module.type.GENERIC"

    // Module specific extras
    val PROCESSOR_ENGINE="assistant.framework.processor.protocol.ENGINE"
    val PROCESSOR_VERSION="assistant.framework.processor.protocol.VERSION"
    val DATA_KEYS="assistant.framework.module.protocol.DATA_KEYS"

    // Actions
    // I don't think I need this with the new design
    val ACTION_SAPPHIRE_CORE_BIND="assistant.framework.core.action.BIND"
    // This is sent to the CORE from the module, so the core can handle the registration process
    // This is for a module to request *all* data from the core (implicit intent style)
    val ACTION_SAPPHIRE_ROUTE_LOOKUP = "assistant.framework.core.action.ROUTE_LOOKUP"
    val ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE = "assistant.framework.core.action.REGISTRATION_COMPLETE"
    val ACTION_SAPPHIRE_CORE_REQUEST_DATA="assistant.framework.core.action.REQUEST_DATA"
    val ACTION_SAPPHIRE_UPDATE_ENV = "action.framework.module.action.UPDATE_ENV"
    val ACTION_SAPPHIRE_MODULE_REGISTER = "assistant.framework.module.action.REGISTER"
    // this is -V on the command line
    val ACTION_SAPPHIRE_MODULE_VERSION = "assistant.framework.module.action.VERSION"
    val ACTION_SAPPHIRE_EXPORT_CONFIG = "assistant.framework.module.action.EXPORT_CONFIG"
    // This is for core to request data from a specific module
    val ACTION_SAPPHIRE_TRAIN="assistant.framework.processor.action.TRAIN"
    val ACTION_SAPPHIRE_INITIALIZE="assistant.framework.processor.action.INITIALIZE"
    val ACTION_SAPPHIRE_TEST="assistant.framework.processor.action.TEST"
    val ACTION_SAPPHIRE_PROCESSOR_TRAIN="assistant.framework.processor.action.TRAIN"
    val ACTION_SAPPHIRE_SPEAK="assistant.framework.core.action.SPEAK"

    // Work in progress
    var ACTION_SAPPHIRE_DATA_TRANSFER = "ACTION_SAPPHIRE_DATA_TRANSFER"
    var ACTION_MANIPULATE_FILE_DATA = "action.framework.module.MANIPULATE_FILE_DATA"
    var ACTION_REQUEST_FILE_DATA = "action.framework.module.REQUEST_FILE_DATA"
    val GUI_BROADCAST = "assistant.framework.broadcast.GUI_UPDATE"
    var CANONICAL_CLASS_NAME = this.javaClass.canonicalName
}