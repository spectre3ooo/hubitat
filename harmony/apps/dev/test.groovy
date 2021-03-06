/**
 *  Hubitat Harmony - Virtual Switch for Logitech Harmony
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Version history
 */
def version() {	return "v1.0.0" }

definition(
    name: "Hubitat Harmony${parent ? " - Device" : ""}",
    namespace: "Hubitat",
    author: "Keo",
    description: "This is a SmartApp that supports discovery of Harmony api!",
    category: "Convenience",
    //parent: parent ? "turlvo.Hubitat Harmony" : null,
    parent: parent ? "Hubitat.Hubitat Harmony" : null,
    singleInstance: true,
    iconUrl: "https://cdn.rawgit.com/turlvo/HubitatHarmony/master/images/icon/Hubitat_Harmony_Icon_1x.png",
    iconX2Url: "https://cdn.rawgit.com/turlvo/HubitatHarmony/master/images/icon/Hubitat_Harmony_Icon_2x.png",
    iconX3Url: "https://cdn.rawgit.com/turlvo/HubitatHarmony/master/images/icon/Hubitat_Harmony_Icon_3x.png"
)

preferences {
    log.trace "Line 36"
    page(name: "parentOrChildPage")  
    page(name: "mainPage")
    page(name: "installPage")
    page(name: "mainChildPage")
    
}

// ------------------------------
// Pages related to Parent
def parentOrChildPage() {
    log.trace "Line 47"
	parent ? mainChildPage() : mainPage()
}

// mainPage
// seperated two danymic page by 'isInstalled' value 
def mainPage() {
    log.trace "Line 54"
    if (!atomicState?.isInstalled) {
        return installPage()
    } else {
    	def interval
    	discoverHubs(atomicState.harmonyApiServerIP)
        if (atomicState.discoverdHubs) {
            interval = 15
        } else {
            interval = 3
        }
        return dynamicPage(name: "mainPage", title: "", uninstall: true, refreshInterval: interval) {
            //getHubStatus()            
            section("Harmony-API Server IP Address :") {
            	href "installPage", title: "", description: "${atomicState.harmonyApiServerIP}"
            }
            
            section("Harmony Hub List :") {
            	if (atomicState.discoverdHubs) {
                	atomicState.discoverdHubs.each {
                    	paragraph "$it"
                    }                
                } else {
            		paragraph "None"
                }
            }

            section("") {
                app( name: "harmonyDevices", title: "Add a device...", appName: "Hubitat Harmony Device", namespace: "Hubitat", multiple: true, uninstall: false)
            }

            section("Hubitat Harmony Version :") {
                paragraph "${version()}"
            }
        }
    }
}

def installPage() {
    log.trace "Line 93"
	dynamicPage(name: "installPage", title: "", install: !atomicState.isInstalled) {
            section("Enter the Harmony-API Server IP address :") {
       	       input name: "harmonyHubIP", type: "text", required: true, title: "IP address?", submitOnChange: true
            }
            
            if (harmonyHubIP) {
            	atomicState.harmonyApiServerIP = harmonyHubIP
            }
    } 	    
}

def initializeParent() {
    log.trace "Line 106"
    atomicState.isInstalled = true
    atomicState.harmonyApiServerIP = harmonyHubIP
    atomicState.hubStatus = "online"
}

//def getHarmonyApiServerIP() {
//    log.trace "Line 113"
//	return atomicState.harmonyApiServerIP
//}

// ------------------------------
// Pages realted to Child App
//def mainChildPage() {
//    log.trace "Line 120"
//    def interval
//    if (atomicState.discoverdHubs && atomicState.deviceCommands && atomicState.device) {
//        interval = 15
//    } else {
//        interval = 3
//    }
//    return dynamicPage(name: "mainChildPage", title: "Add Device", refreshInterval: interval, uninstall: true, install: true) {    	
//        log.debug "mainChildPage>> parent's atomicState.harmonyApiServerIP: ${parent.getHarmonyApiServerIP()}"
//        atomicState.harmonyApiServerIP = parent.getHarmonyApiServerIP()
//        
//        log.debug "installHubPage>> $atomicState.discoverdHubs"        
//        if (atomicState.discoverdHubs == null) {
//            discoverHubs(atomicState.harmonyApiServerIP)
//            section() {            
//                paragraph "Discovering Harmony Hub.  Please wait..."
//            }
//        } else {
//            section("Hub :") {                
//                //def hubs = getHubs(harmonyHubIP)                    
//                input name: "selectHub", type: "enum", title: "Select Hub", options: atomicState.discoverdHubs, submitOnChange: true, required: true
//                log.debug "mainChildPage>> selectHub: $selectHub"
//                if (selectHub) {
//                    discoverDevices(selectHub)
//                    atomicState.hub = selectHub
//                }                
//            }
//        }    
//
//        def foundDevices = getHubDevices()
//        if (atomicState.hub && foundDevices) {
//            section("Device :") {                
//                def labelOfDevice = getLabelsOfDevices(foundDevices)
//                input name: "selectedDevice", type: "enum",  title: "Select Device", multiple: false, options: labelOfDevice, submitOnChange: true, required: true
//                if (selectedDevice) {
//                	discoverCommandsOfDevice(selectedDevice)
//                    atomicState.device = selectedDevice
//                }
//            }
//
//            if (selectedDevice) {
//                section("Device Type :") {
//                    def deviceType = ["Default", "Aircon", "TV", "Roboking", "Fan"]
//                    input name: "selectedDeviceType", type: "enum", title: "Select Device Type", multiple: false, options: deviceType, submitOnChange: true, required: true                    
//                }
//            }  
//
//
//            atomicState.deviceCommands = getCommandsOfDevice()
//            if (selectedDeviceType && atomicState.deviceCommands) {    
//                atomicState.selectedDeviceType = selectedDeviceType
//                switch (selectedDeviceType) {
//                    case "Aircon":
//                    addAirconDevice()
//                    break
//                    case "TV":
//                    case "STB":
//                    addTvDeviceTV()
//                    break
//                    case "STB":
//                    break
//                    case "Roboking":
//                    addRobokingDevice()
//                    break
//                    case "Fan":
//                    addFanDevice()
//                    break
//                    default:
//                        log.debug "selectedDeviceType>> default"
//                    addDefaultDevice()
//                }
//            } else if (selectedDeviceType && atomicState.deviceCommands == null) {
//                // log.debug "addDevice()>> selectedDevice: $selectedDevice, commands : $commands"
//                section("") {
//                    paragraph "Loading selected device's command.  This can take a few seconds. Please wait..."
//                }
//            }
//        } else if (atomicState.hub) {
//            section() {
//                paragraph "Discovering devices.  Please wait..."
//            }
//        }
//    }
//}
//
//// Add device page for Default On/Off device
//def addDefaultDevice() {
//    log.trace "Line 207"
//    def labelOfCommand = getLabelsOfCommands(atomicState.deviceCommands)
//    state.selectedCommands = [:]    
//
//    section("Commands :") {            
//        input name: "selectedPowerOn", type: "enum", title: "Power On", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
//        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
//    }
//    state.selectedCommands["power-on"] = selectedPowerOn
//    state.selectedCommands["power-off"] = selectedPowerOff
//
//	monitorMenu() 
//}
//
//// Add device page for Fan device
//def addFanDevice() {
//    log.trace "Line 223"
//    def labelOfCommand = getLabelsOfCommands(atomicState.deviceCommands)
//    state.selectedCommands = [:]  
//
//    section("Commands :") {            
//        // input name: "selectedPower", type: "enum", title: "Power Toggle", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
//        input name: "selectedPowerOn", type: "enum", title: "Power On", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
//        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
//        input name: "selectedSpeed", type: "enum", title: "Speed", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
//        input name: "selectedSwing", type: "enum", title: "Swing", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
//        input name: "selectedTimer", type: "enum", title: "Timer", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
//        input name: "custom1", type: "enum", title: "Custom1", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "custom2", type: "enum", title: "Custom2", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "custom3", type: "enum", title: "Custom3", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "custom4", type: "enum", title: "Custom4", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "custom5", type: "enum", title: "Custom5", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//    }
//    //state.selectedCommands["power"] = selectedPower
//    state.selectedCommands["power-on"] = selectedPowerOn
//    state.selectedCommands["power-off"] = selectedPowerOff    
//    state.selectedCommands["speed"] = selectedSpeed
//    state.selectedCommands["swing"] = selectedSwing
//    state.selectedCommands["timer"] = selectedTimer
//    state.selectedCommands["custom1"] = custom1
//    state.selectedCommands["custom2"] = custom2
//    state.selectedCommands["custom3"] = custom3
//    state.selectedCommands["custom4"] = custom4
//    state.selectedCommands["custom5"] = custom5    
//
//	monitorMenu() 
//}
//
//// Add device page for Aircon
//def addAirconDevice() {
//    log.trace "Line 257"
//    def labelOfCommand = getLabelsOfCommands(atomicState.deviceCommands)
//    state.selectedCommands = [:]    
//
//    section("Commands :") {            
//        //input name: "selectedPowerToggle", type: "enum", title: "Power Toggle", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
//        input name: "selectedPowerOn", type: "enum", title: "Power On", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
//        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
//        input name: "selectedTempUp", type: "enum", title: "Temperature Up", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
//        input name: "selectedMode", type: "enum", title: "Mode", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
//        input name: "selectedJetCool", type: "enum", title: "JetCool", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "selectedTempDown", type: "enum", title: "Temperature Down", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
//        input name: "selectedSpeed", type: "enum", title: "Fan Speed", options: labelOfCommand, submitOnChange: true, multiple: false, required: false   
//        input name: "custom1", type: "enum", title: "Custom1", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "custom2", type: "enum", title: "Custom2", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "custom3", type: "enum", title: "Custom3", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "custom4", type: "enum", title: "Custom4", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "custom5", type: "enum", title: "Custom5", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//    }
//
//    //state.selectedCommands["power"] = selectedPowerToggle
//    state.selectedCommands["power-on"] = selectedPowerOn
//    state.selectedCommands["power-off"] = selectedPowerOff    
//    state.selectedCommands["tempup"] = selectedTempUp
//    state.selectedCommands["mode"] = selectedMode
//    state.selectedCommands["jetcool"] = selectedJetCool
//    state.selectedCommands["tempdown"] = selectedTempDown
//    state.selectedCommands["speed"] = selectedSpeed
//    state.selectedCommands["custom1"] = custom1
//    state.selectedCommands["custom2"] = custom2
//    state.selectedCommands["custom3"] = custom3
//    state.selectedCommands["custom4"] = custom4
//    state.selectedCommands["custom5"] = custom5  
//
//	monitorMenu() 
//}
//
//// Add device page for TV
//def addTvDeviceTV() {
//    log.trace "Line 296"
//    def labelOfCommand = getLabelsOfCommands(atomicState.deviceCommands)
//    state.selectedCommands = [:]    
//
//    section("Commands :") {            
//        //input name: "selectedPowerToggle", type: "enum", title: "Power Toggle", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
//        input name: "selectedPowerOn", type: "enum", title: "Power On", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
//        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
//        input name: "selectedVolumeUp", type: "enum", title: "Volume Up", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
//        input name: "selectedChannelUp", type: "enum", title: "Channel Up", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
//        input name: "selectedMute", type: "enum", title: "Mute", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "selectedVolumeDown", type: "enum", title: "Volume Down", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
//        input name: "selectedChannelDown", type: "enum", title: "Channel Down", options: labelOfCommand, submitOnChange: true, multiple: false, required: false      
//        input name: "selectedMenu", type: "enum", title: "Menu", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "selectedHome", type: "enum", title: "Home", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
//        input name: "selectedInput", type: "enum", title: "Input", options: labelOfCommand, submitOnChange: true, multiple: false, required: false              
//        input name: "selectedBack", type: "enum", title: "Back", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "custom1", type: "enum", title: "Custom1", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "custom2", type: "enum", title: "Custom2", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "custom3", type: "enum", title: "Custom3", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "custom4", type: "enum", title: "Custom4", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "custom5", type: "enum", title: "Custom5", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//    }
//    
//    //state.selectedCommands["power"] = selectedPowerToggle
//    state.selectedCommands["power-on"] = selectedPowerOn
//    state.selectedCommands["power-off"] = selectedPowerOff  
//	state.selectedCommands["volup"] = selectedVolumeUp
//    state.selectedCommands["chup"] = selectedChannelUp
//    state.selectedCommands["mute"] = selectedMute
//    state.selectedCommands["voldown"] = selectedVolumeDown
//    state.selectedCommands["chdown"] = selectedChannelDown
//    state.selectedCommands["menu"] = selectedMenu
//    state.selectedCommands["home"] = selectedHome
//    state.selectedCommands["input"] = selectedInput
//    state.selectedCommands["back"] = selectedBack
//    state.selectedCommands["custom1"] = custom1
//    state.selectedCommands["custom2"] = custom2
//    state.selectedCommands["custom3"] = custom3
//    state.selectedCommands["custom4"] = custom4
//    state.selectedCommands["custom5"] = custom5  
// 
// 	monitorMenu() 
//}
//
//// Add device page for Aircon
//def addRobokingDevice() {
//    log.trace "Line 343"
//    def labelOfCommand = getLabelsOfCommands(atomicState.deviceCommands)
//    state.selectedCommands = [:]    
//
//    section("Commands :") {
//        input name: "selectedStart", type: "enum", title: "Start", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
//        input name: "selectedHome", type: "enum", title: "Home", options: labelOfCommand, submitOnChange: true, multiple: false, required: true  
//        input name: "selectedStop", type: "enum", title: "Stop", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
//        input name: "selectedUp", type: "enum", title: "Up", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
//        input name: "selectedDown", type: "enum", title: "Down", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "selectedLeft", type: "enum", title: "Left", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
//        input name: "selectedRight", type: "enum", title: "Right", options: labelOfCommand, submitOnChange: true, multiple: false, required: false        
//        input name: "selectedMode", type: "enum", title: "Mode", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
//        input name: "selectedTurbo", type: "enum", title: "Turbo", options: labelOfCommand, submitOnChange: true, multiple: false, required: false   
//        input name: "custom1", type: "enum", title: "Custom1", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "custom2", type: "enum", title: "Custom2", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "custom3", type: "enum", title: "Custom3", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "custom4", type: "enum", title: "Custom4", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//        input name: "custom5", type: "enum", title: "Custom5", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
//    }
//
//	state.selectedCommands["start"] = selectedStart
//    state.selectedCommands["stop"] = selectedStop
//    state.selectedCommands["up"] = selectedUp
//    state.selectedCommands["down"] = selectedDown
//    state.selectedCommands["left"] = selectedLeft
//    state.selectedCommands["right"] = selectedRight
//    state.selectedCommands["home"] = selectedHome
//    state.selectedCommands["mode"] = selectedMode
//    state.selectedCommands["turbo"] = selectedTurbo
//    state.selectedCommands["custom1"] = custom1
//    state.selectedCommands["custom2"] = custom2
//    state.selectedCommands["custom3"] = custom3
//    state.selectedCommands["custom4"] = custom4
//    state.selectedCommands["custom5"] = custom5  
//    
//    monitorMenu() 
//
//}
//
//// ------------------------------------
//// Monitoring sub menu
//def monitorMenu() {
//    log.trace "Line 386"
//    section("State Monitor :") {
//        paragraph "It is a function to complement IrDA's biggest drawback. Through sensor's state, synchronize deivce status."
//        def monitorType = ["Power Meter", "Contact"]
//        input name: "selectedMonitorType", type: "enum", title: "Select Monitor Type", multiple: false, options: monitorType, submitOnChange: true, required: false                    
//    }  
//
//    atomicState.selectedMonitorType = selectedMonitorType
//    if (selectedMonitorType) {            
//        switch (selectedMonitorType) {
//            case "Power Meter":
//            powerMonitorMenu()                
//            break
//            case "Contact":
//            contactMonitorMenu()
//            break
//        }
//    }
//}
//
//def powerMonitorMenu() {
//    log.trace "Line 407"
//    section("Power Monitor :") {
//        input name: "powerMonitor", type: "capability.powerMeter", title: "Device", submitOnChange: true, multiple: false, required: false
//        state.triggerOnFlag = false;
//        state.triggerOffFlag = false;
//        if (powerMonitor) {                
//            input name: "triggerOnValue", type: "decimal", title: "On Trigger Watt", submitOnChange: true, multiple: false, required: true
//            input name: "triggerOffValue", type: "decimal", title: "Off Trigger Watt", submitOnChange: true, multiple: false, required: true                
//        }   
//    } 
//}
//
//def contactMonitorMenu() {
//    log.trace "Line 420"
//    section("Contact :") {
//        input name: "contactMonitor", type: "capability.contactSensor", title: "Device", submitOnChange: true, multiple: false, required: false
//    	if (contactMonitor) {    
//            paragraph "[Normal] : Open(On) / Close(Off)\n[Reverse] : Open(Off) / Close(On)"
//            input name: "contactMonitorMode", type: "enum", title: "Mode", multiple: false, options: ["Normal", "Reverse"], defaultValue: "Normal", submitOnChange: true, required: true	
//    	}
//        atomicState.contactMonitorMode = contactMonitorMode
//    }
//}
//
//
//// ------------------------------------
//// Monitor Handler
//// Subscribe power value and change status
//def powerMonitorHandler(evt) {
//    log.trace "Line 436"
//    def device = []    
//    device = getDeviceByName("$selectedDevice")
//    def deviceId = device.id
//    def child = getChildDevice(deviceId)
//    def event
//
//    log.debug "value is over triggerValue>> flag: $state.triggerOnFlag, value: $evt.value, triggerValue: ${triggerOnValue.floatValue()}"        
//    if (Float.parseFloat(evt.value) >= triggerOnValue.floatValue() && state.triggerOnFlag == false) {    	
//        event =  [value: "on"]
//        child.generateEvent(event)
//        log.debug "value is over send*****"
//        state.triggerOnFlag = true
//    } else if (Float.parseFloat(evt.value) < triggerOnValue.floatValue()) {
//        state.triggerOnFlag = false
//    }
//
//    log.debug "value is under triggerValue>> flag: $state.triggerOffFlag, value: $evt.value, triggerValue: ${triggerOffValue.floatValue()}"
//    if (Float.parseFloat(evt.value) <= triggerOffValue.floatValue() && state.triggerOffFlag == false){    	
//        event =  [value: "off"]        
//        child.generateEvent(event)
//        log.debug "value is under send*****"
//        state.triggerOffFlag = true
//    } else if (Float.parseFloat(evt.value) > triggerOffValue.floatValue()) {
//        state.triggerOffFlag = false
//    }
//
//}
//
//// Subscribe contact value and change status
//def contactMonitorHandler(evt) {
//    log.trace "Line 467"
//    def device = []    
//    device = getDeviceByName("$selectedDevice")
//    def deviceId = device.id
//    def child = getChildDevice(deviceId)
//    def event
//
//	def contacted = "off", notContacted = "on"
//    if (atomicState.contactMonitorMode == "Reverse") {
//    	contacted = "on"
//        notContacted = "off"
//    }
//    log.debug "contactMonitorHandler>> value is : $evt.value"
//    if (evt.value == "open") {
//        event = [value: notContacted] 
//    } else {
//        event = [value: contacted] 
//    }
//    child.generateEvent(event)
//}
//
//// Install child device
//def initializeChild(devicetype) {
//    log.trace "Line 490"
//    //def devices = getDevices()    
//    log.debug "addDeviceDone: $selectedDevice, type: $atomicState.selectedMonitorType"
//    app.updateLabel("$selectedDevice")
//
//	unsubscribe()
//    if (atomicState.selectedMonitorType == "Power Meter") {  
//    	log.debug "Power: $powerMonitor"
//    	subscribe(powerMonitor, "power", powerMonitorHandler)
//    } else if (atomicState.selectedMonitorType == "Contact") {
//    	log.debug "Contact: $contactMonitor"
//    	subscribe(contactMonitor, "contact", contactMonitorHandler)
//    }
//    def device = []    
//    device = getDeviceByName("$selectedDevice")
//    log.debug "addDeviceDone>> device: $device"    
//
//    def deviceId = device.id
//    def existing = getChildDevice(deviceId)
//    if (!existing) {
//        def childDevice = addChildDevice("turlvo", "Hubitat Harmony_${atomicState.selectedDeviceType}", deviceId, null, ["label": device.label])
//    } else {
//        log.debug "Device already created"
//    }
//}
//
//
//// For child Device
//def command(child, command) {
//    log.trace "Line 519"
//	def device = getDeviceByName("$selectedDevice")
//    
//	log.debug "childApp parent command(child)>>  $selectedDevice, command: $command, changed Command: ${state.selectedCommands[command]}"
//    def commandSlug = getSlugOfCommandByLabel(atomicState.deviceCommands, state.selectedCommands[command])
//    log.debug "childApp parent command(child)>>  commandSlug : $commandSlug"
//    
//    def result
//    result = sendCommandToDevice(device.slug, commandSlug)
//    if (result && result.message != "ok") {
//        sendCommandToDevice(device.slug, commandSlug)
//    }
//}
//
//def commandValue(child, command) {
//    log.trace "Line 534"
//	def device = getDeviceByName("$selectedDevice")
//    
//	log.debug "childApp parent commandValue(child)>>  $selectedDevice, command: $command"
//    
//    def result
//    result = sendCommandToDevice(device.slug, command)
//    if (result && result.message != "ok") {
//        sendCommandToDevice(device.slug, command)
//    }
//}



// ------------------------------------
// ------- Default Common Method -------
def installed() {    
    log.trace "Line 551"
    initialize()
}

def updated() {
    log.trace "Line 556"
    //unsubscribe()
    initialize()
}

def initialize() {
    log.trace "Line 562"
	log.debug "initialize()"
   parent ? initializeChild() : initializeParent()
}


def uninstalled() {
    log.trace "Line 569"
	parent ? null : removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    log.trace "Line 574"
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}


// ---------------------------
// ------- Hub Command -------

// getSelectedHub
// return : Installed hub name
def getSelectedHub() {
    log.trace "Line 587"
	return atomicState.hub
}

// getLabelsOfDevices
// parameter :
// - devices : List of devices in Harmony Hub {label, slug}
// return : Array of devices's label value
//def getLabelsOfDevices(devices) {
//    log.trace "Line 596"
//	def labels = []
//   devices.each { 
//        //log.debug "labelOfDevice: $it"
//        labels.add(it.label)
//    }
    
//    return labels

//}

// getLabelsOfCommands
// parameter :
// - cmds : List of some device's commands {label, slug}
// return : Array of commands's label value
//def getLabelsOfCommands(cmds) {
//    log.trace "Line 612"
//	def labels = []
//    log.debug "getLabelsOfCommands>> cmds"
//    cmds.each {
//    	//log.debug "getLabelsOfCommands: it.label : $it.label, slug : $it.slug"
//    	labels.add(it.label)
//    }
    
//    return labels
//}

// getCommandsOfDevice
// return : result of 'discoverCommandsOfDevice(device)' method. It means that recently requested device's commands
//def getCommandsOfDevice() {
//    log.trace "Line 626"
//    //log.debug "getCommandsOfDevice>> $atomicState.foundCommandOfDevice"
//    
//    return atomicState.foundCommandOfDevice
//
//}

// getSlugOfCommandByLabel
// parameter :
// - commands : List of device's command
// - label : name of command
// return : slug value same with label in the list of command
//def getSlugOfCommandByLabel(commands, label) {
//    log.trace "Line 639"
//	//def commands = []
//    def slug
//    
//    commands.each {    	
 //   	if (it.label == label) {
//        	//log.debug "it.label : $it.label, device : $device"
//        	log.debug "getSlugOfCommandByLabel>> $it"
//        	//commands = it.commands
//            slug = it.slug
//        }
//    }
//    return slug
//}

// getDeviceByName
// parameter :
// - name : device name searching
// return : device matched by name in Harmony Hub's devices
//def getDeviceByName(name) {
//    log.trace "Line 659"
//	def device = []    
//	atomicState.devices.each {
//    	//log.debug "getDeviceByName>> $it.label, $name"
//    	if (it.label == name) {
//    		log.debug "getDeviceByName>> $it"
//            device = it
//        }
//	}
//    
//    return device
//}
// 
// getHubDevices
// return : searched list of device in Harmony Hub when installed
//def getHubDevices() {
//    log.trace "Line 675"
//	return atomicState.devices
//}


// --------------------------------
// ------- HubAction Methos -------
// sendCommandToDevice
// parameter : 
// - device : target device
// - command : sending command
// return : 'sendCommandToDevice_response()' method callback
//def sendCommandToDevice(device, command) {
//    log.trace "Line 688"
//	log.debug("sendCommandToDevice >> harmonyApiServerIP : ${parent.getHarmonyApiServerIP()}")
//    sendHubCommand(setHubAction(parent.getHarmonyApiServerIP(), "/hubs/$atomicState.hub/devices/$device/commands/$command", "sendCommandToDevice_response"))
//}

//def sendCommandToDevice_response(resp) {
//    log.trace "Line 694"
//    def result = []
//    def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
//    log.debug("sendCommandToDevice_response >> $body")
//}
//
// getHubStatus
// parameter : 
// return : 'getHubStatus_response()' method callback
//def getHubStatus() {
//    log.trace "Line 704"
//    log.debug "getHubStatus"
//    sendHubCommand(getHubAction(atomicState.harmonyApiServerIP, "/hubs/$atomicState.hub/status", "getHubStatus_response"))
//    if (atomicState.getHubStatusWatchdog == true) {
//    	atomicState.hubStatus = "offline"
//    }
//    atomicState.getHubStatusWatchdog = true        
//}

//def getHubStatus_response(resp) {
//    log.trace "Line 714"
//   	def result = []
//    atomicState.getHubStatusWatchdog = false
    
//    if (resp.description != null && parseLanMessage(resp.description).body) {
//    	log.debug "getHubStatus_response>> response: $resp.description"
//    	def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
	
//       if(body && body.off != null) {            	
//          log.debug "getHubStatus_response>> $body.off"
//            if (body.off == false) {
//            	atomicState.hubStatus = "online"
//            }
//        } else {
//            log.debug "getHubStatus_response>> $body.off"
//            atomicState.hubStatus = "offline"
//        }
//    } else {
//    	log.debug "getHubStatus_response>> Status error"
//        atomicState.hubStatus = "offline"
//    }
//}
//
// discoverCommandsOfDevice
// parameter : 
// - name : name of device searching command
// return : 'discoverCommandsOfDevice_response()' method callback
//def discoverCommandsOfDevice(name) {
//    log.trace "Line 742"
//	device = getDeviceByName(name)
//   log.debug "discoverCommandsOfDevice>> name:$name, device:$device"
    
//   sendHubCommand(getHubAction(atomicState.harmonyApiServerIP, "/hubs/$atomicState.hub/devices/${device.slug}/commands", "discoverCommandsOfDevice_response"))

//}

//def discoverCommandsOfDevice_response(resp) {
//    log.trace "Line 751"
//   	def result = []
//    def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
	
//    if(body) {            	
//        body.commands.each {            
//            def command = ['label' : it.label, 'slug' : it.slug]
//            //log.debug "getCommandsOfDevice_response>> command: $command"
//            result.add(command)            
//        }
//    }
    
//    atomicState.foundCommandOfDevice = result
//}

// discoverDevices
// parameter : 
// - hubname : name of hub searching devices
// return : 'discoverDevices_response()' method callback
def discoverDevices(hubname) {
    log.trace "Line 771"
	log.debug "discoverDevices>> $hubname"
	sendHubCommand(getHubAction(atomicState.harmonyApiServerIP, "/hubs/$hubname/devices", "discoverDevices_response"))
}

def discoverDevices_response(resp) {
    log.trace "Line 777"
	def result = []
    def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
    log.debug("discoverHubs_response >> $body.devices")
	
    if(body) {            	
        body.devices.each {
            //log.debug "getHubDevices_response: $it.id, $it.label, $it.slug"
            def device = ['id' : it.id, 'label' : it.label, 'slug' : it.slug]
            result.add(device)
        }
    }            
    atomicState.devices = result

}


// discoverHubs
// parameter : 
// - host : ip address searching hubs
// return : 'discoverHubs_response()' method callback
def discoverHubs(host) {
    log.trace "Line 799"
	log.debug("discoverHubs")
    return sendHubCommand(getHubAction(host, "/hubs", "discoverHubs_response"))
}

def discoverHubs_response(resp) {
    log.trace "Line 805"
	def result = []
    def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
    log.debug("discoverHubs_response >> $body.hubs")
	
    if(body && body.hubs != null) {            	
        body.hubs.each {
            log.debug "discoverHubs_response: $it"
            result.add(it)
        }
        atomicState.discoverdHubs = result
    } else {
    	atomicState.discoverdHubs = null
    }    
}

// -----------------------------
// -------Hub Action API -------
// getHubAction
// parameter :
// - host : target address to send 'GET' action
// - url : target url
// - callback : response callback method name
def getHubAction(host, url, callback) {
    log.trace "Line 829"
	log.debug "getHubAction>> $host, $url, $callback"
    return new hubitat.device.HubAction("GET ${url} HTTP/1.1\r\nHOST: ${host}\r\n\r\n",
            hubitat.device.Protocol.LAN, "${host}", [callback: callback])
}

// setHubAction
// parameter :
// - host : target address to send 'POST' action
// - url : target url
// - callback : response callback method name
def setHubAction(host, url, callback) {
    log.trace "Line 841"
	log.debug "getHubAction>> $host, $url, $callback"
    return new hubitat.device.HubAction("POST ${url} HTTP/1.1\r\nHOST: ${host}\r\n\r\n",
            hubitat.device.Protocol.LAN, "${host}", [callback: callback])
}
