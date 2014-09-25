/**
 *  Green Smart HW Recirculator
 *
 *  Copyright 2014 Barry A. Burke
 *
 *
 * For usage information & change log: https://github.com/SANdood/Green-Smart-HW-Recirculator
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
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
	description: "Intelligent event-driven optimizer for mhole house Hot Water recirculation system.",
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
			settings.recircMomentary = false
			input name: "recircMomentary", type: "bool", title: "Is this a momentary switch?", required: true, defaultValue: true, refreshAfterSelection: true
			if (!recircMomentary) {
				input name: "timedOff", type: "bool", title: "Timed off?", defaultValue: false, refreshAfterSelection: true
				if (timedOff) {
					input name: "offAfterMinutes", type: "number", title: "On for how many minutes?", defaultValue: 3 
				}
			}
			
			paragraph ""
			input name: "useTargetTemp", type: "bool", title: "Use temperature control?", defaultValue: false, refreshAfterSelection: true
			if (useTargetTemp) {
				input name: "targetThermometer", type: "capability.temperatureMeasurement", title: "Use this thermometer", multiple: false, required: true
				input name: "targetTemperature", type: "number", title: "Target temperature", defaultValue: 105, required: true
				input name: "targetOff", type: "bool", title: "Off at target temp?", defaultValue: true
				input name: "targetOn", type: "bool", title: "On when below target?", defaultValue: false, refreshAfterSelection: true
				if (!targetOff && !targetOn) { settings.useTargetTemp = false }
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
			input name: "switchedOn", type: "capability.switch", title: "On when a switch is turned on", multiple: true, required: false, refreshAfterSelection: true
			if (switchedOn) {
				input name: "onSwitchedOff", type: "bool", title: "Off when turned off?", defaultValue: false
			}

			paragraph ""
			input name: "useTimer", type: "bool", title: "On using a schedule?", defaultValue: false, refreshAfterSelection: true
			if (useTimer) {
				input name: "onEvery", type: "number", title: "On every XX minutes", defaultValue: 15, required: true
			}
			
			paragraph ""
			input name: "modeOn",  type: "mode", title: "Enable for specific mode(s)?", multiple: true, required: false
		}
		
		section([mobileOnly:true]) {
			label title: "Assign a name for this SmartApp", required: false
//			mode title: "Set for specific mode(s)", required: false
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

	state.keepOffNow = false 

	if (modeOn) {
    	subscribe( location, locationModeHandler)
        if (location.currentMode in modeOn) {
        	state.keepOffNow == false 
            
            }
        else { state.keepOffNow = true }
	}
    
	if (useTargetTemp) { subscribe( targetThermometer, "temperature", tempHandler) }

	if (motionActive) {
		subscribe( motionActive, "motion.active", onHandler)
		if (motionInactive) { subscribe( motionActive, "motion.inactive", offHandler) }
	}

	if (contactOpens) {
		subscribe( contactOpens, "contact.open", onHandler)
		if (openCloses) { subscribe( contactOpens, "contact.close", offHandler ) }
	}

	if (contactCloses) {
		subscribe( contactCloses, "contact.close", onHandler)
		if (closedOpens) { subscribe( contactCloses, "contact.open", offHandler ) }
	}

	if (switchedOn) {
		subscribe( switchedOn, "switch.on", onHandler)
		if (onSwitchedOff) { subscribe( switchedOn, "switch.off", offHandler ) }
	}

    if ( !state.keepOffNow && useTimer ) { schedule("0 */${onEvery} * * * ?", "turnItOn") }
}

def tempHandler(evt) {
	log.debug "tempHandler $evt.name: $evt.value"
    
    if (targetOff) {
    	if (evt.integerValue >= targetTemperature) { offHandler() }
    }
    
    if (targetOn) {
    	if ( evt.integerValue < targetTemperature) { onHandler() }
    }
}

def onHandler(evt) {
	log.debug "onHandler $evt.name: $evt.value"

	turnItOn()
}
         
def turnItOn() { 
    if (state.keepOffNow) { return }				// we're not supposed to turn it on right now

	def turnOn = true
    if (useTargetTemp) {							// only turn it on if not hot enough yet
    	if (targetThermometer.currentTemperature >= targetTemperature) { turnOn = false }
    }
    
    if (turnOn) {
		if (!recircMomentary) {
			if (recircSwitch.currentSwitch != "on") { recircSwitch.on() }
		}
    	else { recircSwitch.on() }
    
    	if (timedOff) {
    		runIn(offAfterMinutes * 60, "turnItOff", [overwrite: false])
        }
    }
}

def offHandler(evt) {
	log.debug "offHandler $evt.name: $evt.value"

    turnItOff()
}

def turnItOff() {

	def turnOff = true
    if (useTargetTemp) {						// only turn it off if it's hot enough
    	if (targetThermometer.currentTemperature < targetTemperature) { turnOff = false }
    }

	if (turnOff) {
		if (recircSwitch.currentSwitch != "off" ) { recircSwitch.off() }// avoid superfluous off()s
        
        // If we want to let the recirculator to run longer if there are multiple demand calls, comment out
        // the following
    	if (timedOff) { unschedule( "turnItOff" ) }						// delete any other pending off schedules
    }
}

def locationModeHandler(evt) {
	log.debug "locationModeHandler: $evt.name, $evt.value"
    
	if (modeOn) {

        if (evt.value in modeOn) {
        	log.debug "Enabling GSHWR"
        	sendNotificationEvent ( "Plus, I enabled ${recircSwitch.displayName}" )
    		state.keepOffNow = false
    		if (useTimer) {
    			unschedule( "turnItOn" )											// stop any lingering schedules
        		schedule("0 */${onEvery} * * * ?", "turnItOn")         // schedule onHandler every $onEvery minutes							// schedule onHandler every $onEvery minutes 
    		}
            turnItOn()													// and turn it on to start the day!
		}
        else {
			log.debug "Disabling GSHWR"
            sendNotificationEvent ( "Plus, I disabled ${recircSwitch.displayName}" )
        	if (useTimer) { unschedule( "turnItOn" ) }					// stop timed on schedules
    		if (timedOff) { unschedule( "turnItOff" ) }					// delete any pending off schedules
			turnItOff()													// Send one final turn-off    											// offHandler reschedules on events
    		if (keepoff) {
    			state.keepOffNow = true									// make sure nobody turns it on again
    		}
        }
    }
}
