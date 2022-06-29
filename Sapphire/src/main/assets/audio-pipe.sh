# Take the given process, redirect its stdout to a pipefile, and pass that pipefile back to Android

#!/bin/bash

# Run w/e STT application we were looking to use (ie precise)
bash $@ < stdin.pipe > stdout.log
# You need to give BOTH permissions, as
am startservice -a action.sapphire.microphone.NEW -d file://stdin.pipe --grant-write-uri-permission --grant-write-uri-permission studio.hikari.spellbook/studio.hikari.spellbook.SimpleAudioProcessor -e declare_processor_type processor_type_wake_word

# I can directly launch SimpleAudioProcessor w/ the config info from here...