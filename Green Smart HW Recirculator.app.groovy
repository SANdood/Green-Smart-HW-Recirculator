/**
 *  Green Smart HW Recirculator
 *
 *  Copyright 2014 Barry A. Burke
 *
 *
 * Theory of operation:
 *
 * My house has a  RedyTemp HW recirculation pump (http://www.redytemp.com/). The neat thing about this one is that
 * it allows you to integrate the water recirulation with motion/presence sensors, and that it only runs until the 
 * return water is actually hot. On their web site, they show how to integrate with an X10 system; instead, I hooked
 * it into ST, using a Foscam Mimo-lite to trigger the momentary contact that tells the unit to "recirculate until
 * hot." I then send the Mimo-lite an "on" command to the device whenever one of my inside-the-house motion sensors
 * detect motion, or when the house changes to "Good Morning!" or "I'm Back!", so that the water is always hot; and
 * stays off whenever I'm not home or I'm sleeping. Since the RedyTemp only pumps until the water return is hot, I
 * get hot water pretty much whenever I need it at home - washing dishes or clothes, for example.
 *
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name:		"Green Smart HW Recirculator",
    namespace: 	"SANdood",
    author: 	"Barry A. Burke",
    description: "Fully automated HW Recirculation pump.",
    category: 	"Green Living",
    iconUrl: 	"https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving.png",
    iconX2Url:	"https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@2x.png"
)

preferences {
	page( name: "setupApp" )
}

def setupApp() {
	dynamicPage(name: "setupApp", title: "Smart HW Recirulator Setup", install: true, uninstall: true) {

		section("HW Recirculator") {
			input name: "recircSwitch", type: "capability.switch", title: "Recirculator switch?", multiple: false, required: true
		
			input name: "recircMomentary", type: "bool", title: "Is this a momentary switch?", required: true, defaultValue: true, refreshAfterSelection: true
			if (!recircMomentary) {
				input name: "timedOff", type: "bool", title: "Timed off?", defaultValue: true, refreshAfterSelection: true
				if (timedOff) {
					input name: "offAfterMinutes", type: "number", title: "On for how many minutes?", defaultValue: 3 
				}
			}
		
			input name: "useTargetTemp", type: "bool", title: "Use temperature control?", defaultValue: false, refreshAfterSelection: true
			if (useTargetTemp) {
				input name: "targetThermometer", type: "capability.temperatureMeasurement", title: "Use this thermometer", multiple: false, required: true
				input name: "targetTemperature", type: "number", title: "Target temperature", defaultValue: 105, required: true
				input name: "targetOff", type: "bool", title: "Off at target temp?", defaultValue: true
                input name: "targetOn", type: "bool", title: "On when below target?", defaultValue: false, refreshAfterSelection: true
                if (!targetOff && !targetOn) { useTargetTemp = false }
                if (targetOn) {
                	input name: "targetSwing", type: "number", title: "Below by this many degrees:", defaultValue: 5, required: true
                }
			}		
		}
    
		section("Recirculator Activation events:") {
        
        	paragraph ""
			input name: "motionActive", type: "capability.motionSensor", title: "On when motion is detected here", multiple: true, required: false, refreshAfterSelection: true
			if (motionActivated) {
				input name: "motionInactive", type: "bool", title: "Off when motion stops?", defaultValue: true
			}

			paragraph ""
			input name: "contactOpens", type: "capability.contactSensor", title: "On when any of these things open", multiple: true, required: false, refreshAfterSelection: true
			if (contactOpens) {
				input name: "openCloses", type: "bool", title: "Off when they re-close?", defaultValue: false
			}
			
			paragraph ""
			input name: "contactCloses", type: "capability.contactSensor", title: "On when any of these things close", multiple: true, required: false, refreshAfterSelection: true
			if (contactCloses) {
				input name: "closedOpens", type: "bool", title: "Off when they re-open?", defaultValue: false
			}

			paragraph ""
			input name: "switchedOn", type: "capability.switch", title: "On when a switch is turned on", multiple: false, required: false, refreshAfterSelection: true
			if (switchedOn) {
				input name: "onSwitchedOff", type: "bool", title: "Off when turned off?", defaultValue: false
			}

			paragraph ""
			input name: "useTimer", type: "bool", title: "On using a schedule?", defaultValue: false, refreshAfterSelection: true
			if (useTimer) {
				input name: "onEvery", type: "number", title: "On every XX minutes", defaultValue: 15, required: true
			}
			
			paragraph ""
			input name: "modeChangeOn",  type: "mode", title: "On when the location mode changes to:", multiple: true, required: false
			input name: "modeChangeOff",  type: "mode", title: "Off when the location mode changes to:", multiple: true, required: false, refreshAfterSelection: true
			if (modeChangeOn && modeChangeOff) {
				def plural = ""
				if (modeChangeOff.size() > 1) {
					plural = "s"
				}
				String showModes = "${modeChangeOff}"
                paragraph "This overrides ALL Activation events!"
				input name: "keepOff", type: "bool", title: "Keep off while in ${showModes.substring(1, showModes.length()-1)} mode${plural}?", defaultValue: true
			}
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
    unschedule()
	initialize()
}

def initialize() {
    log.debug "Initializing"
    
    if ((keepOff == "") || (keepOff == null)) { 
    	keepOff = false 		// if not using modes, ensure no blackout periods
    }
    
    if (modeChangeOff && keepOff) {
    	if (modeChangeOff.contains( location.currentValue( "mode" ))) {
        	// Just in case we are installing while Away (or late at Night :)
			state.keepOffNow == true
        }
        else {
        	state.keepOffNow == false
        }
    }

    if (useTargetTemp) {
    	subscribe( targetThermometer, "temperature", tempHandler)
   	}
    
    if (motionActive) {
    	subscribe( motionActive, "motion.active", onHandler)
        if (motionInactive) {
        	subscribe( motionActive, "motion.inactive", offHandler)
        }
    }
    
    if (contactOpens) {
    	subscribe( contactOpens, "contact.open", onHandler)
        if (openCloses) {
        	subscribe( contactOpens, "contact.close", offHandler )
        }
    }
    
    if (contactCloses) {
    	subscribe( contactCloses, "contact.close", onHandler)
        if (closedOpens) {
        	subscribe( contactCloses, "contact.open", offHandler )
        }
    }
    
    if (switchedOn) {
    	subscribe( switchedOn, "switch.on", onHandler)
        if (onSwitchedOff) {
        	subscribe( switchedOn, "switch.off", offHandler )
        }
    }
    
    if (modeChangeOn || modeChangeOff) {
    	subscribe( location, locationModeHandler)
    }
    else {														// not using modes - check if using schedule 24x7
    	if ( useTimer ) {
    	schedule("0 */${onEvery} * * * ?", "onHandler")         	// schedule onHandler every $onEvery minutes
        }
    }
}

def tempHandler(evt) {
	log.debug "tempHandler $evt.name: $evt.value"
    
    if (targetOff) {
    	if (evt.value >= targetTemperature) {
    		offHandler()
    	}
    }
    
    if (targetOn) {
    	if ( evt.value < targetTemperature) {
        	onHandler()
        }
    }
}

def onHandler() {

    if (keepOff && state.keepOffNow) {		// we're not supposed to turn it on right now
    	return
    }
    if (useTargetTemp) {					// only turn it on if not hot enough yet

    	if (targetThermometer.latestValue( "temperature" ) < targetTemperature) {
        	turnItOn()
        }
    }
    else {
    	turnItOn()
    }
}
         
def turnItOn() {    
	recircSwitch.on()
    if (timedOff) {
    	unschedule()
    	runIn(offAfterMinutes * 60, "offHandler", [overwrite: false])
        if (useTimer) {
    		schedule("0 */${onEvery} * * * ?", "onHandler")         	// schedule onHandler every $onEvery minutes
        }
    }
}

def offHandler() {
    if (recircSwitch.latestValue( "switch" ) != "off" ) {  	// avoid superfluous off()s
    	recircSwitch.off()
        if (timedOff) {
    		unschedule()												// clean up runIn() bug)
        	schedule("0 */${onEvery} * * * ?", "onHandler")         	// schedule onHandler every $onEvery minutes
    	}
    }

}

def locationModeHandler(evt) {
	log.debug "locationModeHandler: $evt.name, $evt.value"
    
	if (modeChangeOn) {
    	if (modeChangeOn.contains( "$evt.Value" )) {									
    		state.keepOffNow = false
            onHandler()
    		if (useTimer) {
    			unschedule()											// stop any lingering schedules
        		schedule("0 */${onEvery} * * * ?", "onHandler")         // schedule onHandler every $onEvery minutes							// schedule onHandler every $onEvery minutes 
    		}
        }
        return
    }
    
    if (modeChangeOff) {
    	if (modeChangeOff.contains( "$evt.Value" )) {
        	unschedule()												// stop any scheduled -on or -off
    		offHandler()												// Send one final turn-off
            unschedule()     											// offHandler reschedules on events
    		if (keepoff) {
    			state.keepOffNow = true									// make sure nobody turns it on again
    		}
        }
    }
}
