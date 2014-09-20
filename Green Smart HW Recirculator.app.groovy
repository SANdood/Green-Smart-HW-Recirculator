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
    name: 		"Green Smart HW Recirculator",
    namespace: 	"SANdood",
    author: 	"Barry A. Burke",
    description: "Fully automated HW Recirculation pump.",
    category: 	"Green Living",
    iconUrl:	"https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: 	"https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page( name: "setupApp" )
}

def setupApp() {
	dynamicPage(name: "setupApp", title: "Smart HW Recirulator Setup", install: true, uninstall: true)

	section("HW Recirculator") {
		input name: "recircSwitch", type: "capability.switch", title: "Recirculator switch?", multiple: false, required: true
		
		input name: "recircMomentary", type: "bool", title: "Is this a momentary switch?", required: true, defaultValue: true, refreshAfterSelection: true
		if (!recircMomentary) {
			input name: "offTimed", type: "bool", title: "Timed off?", defaultValue: true, refreshAfterSelection: true
			if (offTimed) {
				input name: "offMinutes", type: "number", title: "Off after XX minutes", defaultValue: 3 
			}
		}
		
		input name: "useTargetTemp", type: "bool", title: "Use temperature control?", defaultValue: false, refreshAfterSelection: true
		if (useTargetTemp) {
			input name: "targetThermometer", type: "capability.temperatureMeasurement", title: "Use this thermometer", multiple: false, required: true
			input name: "targetTemperature", type: "number", title: "Target temperature", defaultValue: 105, required: true
		}		
	}
    /*
	section("Recirculator Activation events:") {
	
		input name: "motionDetected", type: "cability.motionSensor", title: "On when motion is detected here", multiple: true, required: false, refreshAfterSelection: true
			if (motionDetected) {
			input name: "motionStops", type: "bool", title: "Off when motion stops?", defaultValue: true
		}
	
		input name: "contactOpens", type: "capability.contactSensor", title: "On when any of these things open", multiple: true, required: false, refreshAfterSelection: true
		if (contactOpens) {
			input name: "openCloses", type: "bool", title: "Off when they re-close?", defaultValue: false
		}
	
		input name: "contactCloses", type: "capability.contactSensor", title: "On when any of these things close", multiple: true, required: false, refreshAfterSelection: true
		if (contactCloses) {
			input name: "closedOpens", type: "bool", title: "Off when they re-open?", defaultValue: false
		}
	
		input name: "switchedOn", type: "capability.switch", title: "On when a switch is turned on", multiple: true, required: false, refreshAfterSelection: true
		if (switchedOn) {
			input name: "onSwitchedOff", type: "bool", title: "Off when turned off?", defaultValue: false
		}
	
		input name: "modeChangeOn",  type: "mode", title: "On when the location mode changes to:", multiple: true, required: false
		input name: "modeChangesOff",  type: "mode", title: "Off when the location mode changes to:", multiple: true, required: false, refreshAfterSelection: true
		if (modeChangesOff) {
			input name: "keepOff", type: "bool", title: "Keep off while in ${modeChangesOff} mode(s)?", defaultValue: true
		}
	
		input name: "useTimer", type: "bool", title: "On using a schedule?", defaultValue: false, refreshAfterSelection: true
		if (useTimer) {
			input name: "onEvery", type: "number", title: "On every XX minutes", defaultValue: 15, required: true
		}
	} */
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
    log.debug "Initializing"
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
