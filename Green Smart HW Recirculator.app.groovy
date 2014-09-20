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
    name: "Green Smart HW Recirculator",
    namespace: "SANdood",
    author: "Barry A. Burke",
    description: "Turns on HW Recirculation pump when Home, off when Away or Sleep. Optionally turns it on on a schedule whilst home (good for momentary contact switch).",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page( name: "setupApp" )
}

def setupApp() {
	dynamicPage(name: "setupApp", title: "Smart HW Recirulator Setup", install: true, uninstall: true)
	


	section("HW Recirculator") {
		input "recircSwitch", "capability.switch", title: "Recirculator switch?", multiple: false, required: true
		
		input "recircMomentary", type: "bool", title: "Is this a momentary switch?", required: true, defaultValue: true, refreshAfterSelection: true
		if (!recircMomentary) {
			input "offTimed", type: "bool", title: "Timed off?", defaultValue: true, refreshAfterSelection: true
			if (offTimed) {
				input "offMinutes", type: "number", title: "Off after XX minutes", defaultValue: 3, 
			}
		}
		
		input "useTargetTemp", type: "bool", title: "Use temperature control?", defaultValue: false, refreshAfterSelection: true
		if (useTargetTemp) {
			input "targetThermometer", "capability.temperatureMeasurement", title: "Use this thermometer", multiple: false, required: true
			input "targetTemperature", type: "number", title: "Target temperature", defaultValue: 105, required: true
		}
		
	}
	section("Recirculator Activation events:") {
	
		input "motionDetected", "cability.motion", title: "On when motion is detected here", multiple: true, required: false, refreshAfterSelection: true
			if (motionDetected) {
			input "motionStops", type: "bool", title: "Off when motion stops?", defaultValue: true
		}
	
		input "contactOpens", "capability.contactSensor", title: "On when any of these things open", multiple: true, required: false, refreshAfterSelection: true
		if (contactOpens) {
			input "openCloses", type: "bool", title: "Off when they re-close?", defaultValue: false
		}
	
		input "contactCloses", "capability.contactSensor", title: "On when any of these things close", multiple: true, required: false, refreshAfterSelection: true
		if (contactCloses) {
			input "closedOpens", type: "bool", title: "Off when they re-open?", defaultValue: false
		}
	
		input "switchedOn", "capability.switch", title: "On when a switch is turned on", multiple: true, required: false, refreshAfterSelection: true
		if (switchedOn) {
			input "onSwitchedOff", type: "bool", title: "Off when turned off?", defaultValue: false
		}
	
		input "modeChangeOn", location.modes, title: "On when the location mode changes to:", multiple: true, required: false
		input "modeChangesOff", location.modes, title: "Off when the location mode changes to:", multiple: true, required: false, refreshAfterSelection: true
		if (modeChangesOff) {
			input "keepOff", type: "bool", title: "Keep off while in ${modeChangesOff} mode(s)?", defaultValue: true
		}
	
		input "useTimer", type: "bool", title: "On using a schedule?", defaultValue: false, refreshAfterSelection: true
		if (useTimer) {
			input "onEvery", type: "number", title: "On every XX minutes", defaultValue: 15, required: true
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
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    // subscribe to location.mode changes: turn "on" on changes to Home; off for changes to Away or Night
}

def locationModeHandler() {

// Home
	// turn switch on
    // unschedule()
    // if (interval) { schedule "on" every XX secs/mins }
    
// Away & Night
	// turn switch off
    // unschedule()
}

def onScheduleHandler() {
	// swtich "on"
}
