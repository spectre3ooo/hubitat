/**
 *  Hubitat Harmony Device - Virtual Switch for Logitech Harmony
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
def version() { return "v1.0.0" }
/*
 * 03/09/2018
 * This is a port from Hubitat Harmony to Hubitat.  Almost all of the code is the same ecept the changes from graphapi to hubitat
 * Hubitat didn't like how the code was written for dynamic child/parent apps, so a child app called Hubitat Harmony Device was created.
 * Special thanks to mattw for his assistance in debuging this port
 * https://community.hubitat.com/t/verbos-logging-help-with-kuku-harmony-port/821/16
 *
 * Hibitat Harmony Device needs to be installed on Hubitat as well.
*/

definition(
    name: "Hubitat Harmony Device",
    namespace: "keo",
    author: "Hubitat",
    description: "This is a SmartApp that support to control Harmony's device!",
    category: "Convenience",
    parent: "keo:Hubitat Harmony",
    singleInstance: true,
    //iconUrl: "https://cdn.rawgit.com/keo/HubitatHarmony/master/images/icon/Hubitat_Harmony_Icon_1x.png",
    //iconX2Url: "https://cdn.rawgit.com/keo/HubitatHarmony/master/images/icon/Hubitat_Harmony_Icon_2x.png",
    //iconX3Url: "https://cdn.rawgit.com/keo/HubitatHarmony/master/images/icon/Hubitat_Harmony_Icon_3x.png")

preferences {
    page(name: "parentOrChildPage")   
    page(name: "mainPage")
    page(name: "installPage")
    page(name: "mainChildPage")   
}

// ------------------------------
// Pages related to Parent
 def parentOrChildPage() {
     log.debug "Line 64: parent Or Child Page"
     mainChildPage()
 }

// mainPage
// seperated two danymic page by 'isInstalled' value 
def mainPage() {
    log.debug "Line 71: Main Page"
    if (!atomicState?.isInstalled) {
        return installPage()
    } else {
    	def interval
        log.debug "Line 77: Running discoverHubs."
    	discoverHubs(atomicState.harmonyApiServerIP)
        if (atomicState.discoverdHubs) {
            log.debug "Line 78: atomicState = ${atomicState.discoverdHubs}"
            interval = 15
        } else {
            log.debug "Line 81: Interval = 3"
            interval = 3
        }
        log.debug "Line 83+: dynamic main page"
        return dynamicPage(name: "mainPage", title: "", uninstall: true, refreshInterval: interval) {
            //getHubStatus()            
            section("Harmony-API Server IP Address :") {
            	href "installPage", title: "", description: "${atomicState.harmonyApiServerIP}"
            }
            
            section("Harmony Hub List :") {
                log.debug "Line 89+: Harmony Hub List"
            	if (atomicState.discoverdHubs) {
                	atomicState.discoverdHubs.each {
                        log.debug "Line 95: discoverHubs"
                    	paragraph "$it"
                    }                
                } else {
            		paragraph "None"
                }
            }
	    //log.debug "Line 102: Harmony Devices"
            section("Harmony Devices") {
                log.debug "Line 104: harmony Devices"
                app( name: "harmonyDevices", title: "Add a device...", appName: "Hubitat Harmony", namespace: "keo", multiple: true, uninstall: false)
                //app( name: "harmonyDevices", title: "Add a device...", appName: "Hubitat Harmony Device", namespace: "keo", multiple: true, uninstall: false)
            }

            section("Hubitat Harmony Version :") {
                log.debug "Line 107: Hubitat Harmony Version"
                paragraph "${version()}"
            }
        }
    }
}

def installPage() {
    log.debug "Line 115: installPage"
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
    log.debug "Line 127: Init Parent"
    atomicState.isInstalled = true
    atomicState.harmonyApiServerIP = harmonyHubIP
    atomicState.hubStatus = "online"
}

def getHarmonyApiServerIP() {
    log.debug "Line 134: get Harmony API Server IP"
	return atomicState.harmonyApiServerIP
}

// ------------------------------
// Pages realted to Child App
def mainChildPage() {
    log.debug "Line 141: mainChildPage"
    def interval
    if (atomicState.discoverdHubs && atomicState.deviceCommands && atomicState.device) {
        interval = 15
    } else {
        interval = 3
    }
    return dynamicPage(name: "mainChildPage", title: "Add Device", refreshInterval: interval, uninstall: true, install: true) {    	
        log.debug "Line 141: mainChildPage>> parent's atomicState.harmonyApiServerIP: ${parent.getHarmonyApiServerIP()}"
        atomicState.harmonyApiServerIP = parent.getHarmonyApiServerIP()
        
        log.debug "Line 144: installHubPage>> $atomicState.discoverdHubs"        
        if (atomicState.discoverdHubs == null) {
            discoverHubs(atomicState.harmonyApiServerIP)
            section() {            
                paragraph "Discovering Harmony Hub.  Please wait..."
            }
        } else {
            section("Hub :") {                
                //def hubs = getHubs(harmonyHubIP)                    
                input name: "selectHub", type: "enum", title: "Select Hub", options: atomicState.discoverdHubs, submitOnChange: true, required: true
                log.debug "Line 154: mainChildPage>> selectHub: $selectHub"
                if (selectHub) {
                    discoverDevices(selectHub)
                    atomicState.hub = selectHub
                }                
            }
        }    

        def foundDevices = getHubDevices()
        log.debug "Line 172: getHubDevices"
        if (atomicState.hub && foundDevices) {
            section("Device :") {                
                def labelOfDevice = getLabelsOfDevices(foundDevices)
                input name: "selectedDevice", type: "enum",  title: "Select Device", multiple: false, options: labelOfDevice, submitOnChange: true, required: true
                if (selectedDevice) {
                	discoverCommandsOfDevice(selectedDevice)
                    atomicState.device = selectedDevice
                }
            }

            if (selectedDevice) {
                log.debug "Line 184: selectedDevices"
                section("Device Type :") {
                    def deviceType = ["Default", "Aircon", "TV", "Roboking", "Fan"]
                    input name: "selectedDeviceType", type: "enum", title: "Select Device Type", multiple: false, options: deviceType, submitOnChange: true, required: true                    
                }
            }

            atomicState.deviceCommands = getCommandsOfDevice()
            if (selectedDeviceType && atomicState.deviceCommands) {
                log.debug "Line 193: selectedDeviceType"
                atomicState.selectedDeviceType = selectedDeviceType
                switch (selectedDeviceType) {
                    case "Aircon":
                    addAirconDevice()
                    break
                    case "TV":
                    case "STB":
                    addTvDeviceTV()
                    break
                    case "STB":
                    break
                    case "Roboking":
                    addRobokingDevice()
                    break
                    case "Fan":
                    addFanDevice()
                    break
                    default:
                        log.debug "Line 201: selectedDeviceType>> default"
                    addDefaultDevice()
                }
            } else if (selectedDeviceType && atomicState.deviceCommands == null) {
                log.debug "Line 205: addDevice()>> selectedDevice: $selectedDevice, commands : $commands"
                section("") {
                    paragraph "Loading selected device's command.  This can take a few seconds. Please wait..."
                }
            }
        } else if (atomicState.hub) {
            section() {
                paragraph "Discovering devices.  Please wait..."
            }
        }
    }
}

// Add device page for Default On/Off device
def addDefaultDevice() {
    log.debug "Line 231: addDefaultDevices"
    def labelOfCommand = getLabelsOfCommands(atomicState.deviceCommands)
    state.selectedCommands = [:]    

    section("Commands :") {
        log.debug "Line 236: Commands"
        input name: "selectedPowerOn", type: "enum", title: "Power On", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
    }
    state.selectedCommands["power-on"] = selectedPowerOn
    state.selectedCommands["power-off"] = selectedPowerOff

	monitorMenu() 
}

// Add device page for Fan device
def addFanDevice() {
    log.debug "Line 248: addFanDevice"
    def labelOfCommand = getLabelsOfCommands(atomicState.deviceCommands)
    state.selectedCommands = [:]  

    section("Commands :") {            
        // input name: "selectedPower", type: "enum", title: "Power Toggle", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOn", type: "enum", title: "Power On", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedSpeed", type: "enum", title: "Speed", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedSwing", type: "enum", title: "Swing", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedTimer", type: "enum", title: "Timer", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "custom1", type: "enum", title: "Custom1", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "custom2", type: "enum", title: "Custom2", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "custom3", type: "enum", title: "Custom3", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "custom4", type: "enum", title: "Custom4", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "custom5", type: "enum", title: "Custom5", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
    }
    //state.selectedCommands["power"] = selectedPower
    state.selectedCommands["power-on"] = selectedPowerOn
    state.selectedCommands["power-off"] = selectedPowerOff    
    state.selectedCommands["speed"] = selectedSpeed
    state.selectedCommands["swing"] = selectedSwing
    state.selectedCommands["timer"] = selectedTimer
    state.selectedCommands["custom1"] = custom1
    state.selectedCommands["custom2"] = custom2
    state.selectedCommands["custom3"] = custom3
    state.selectedCommands["custom4"] = custom4
    state.selectedCommands["custom5"] = custom5    

	monitorMenu() 
}

// Add device page for Aircon
def addAirconDevice() {
    log.debug "Line 282: addAirconDevice"
    def labelOfCommand = getLabelsOfCommands(atomicState.deviceCommands)
    state.selectedCommands = [:]    

    section("Commands :") {            
        //input name: "selectedPowerToggle", type: "enum", title: "Power Toggle", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOn", type: "enum", title: "Power On", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedTempUp", type: "enum", title: "Temperature Up", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedMode", type: "enum", title: "Mode", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedJetCool", type: "enum", title: "JetCool", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "selectedTempDown", type: "enum", title: "Temperature Down", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
        input name: "selectedSpeed", type: "enum", title: "Fan Speed", options: labelOfCommand, submitOnChange: true, multiple: false, required: false   
        input name: "custom1", type: "enum", title: "Custom1", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "custom2", type: "enum", title: "Custom2", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "custom3", type: "enum", title: "Custom3", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "custom4", type: "enum", title: "Custom4", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "custom5", type: "enum", title: "Custom5", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
    }

    //state.selectedCommands["power"] = selectedPowerToggle
    state.selectedCommands["power-on"] = selectedPowerOn
    state.selectedCommands["power-off"] = selectedPowerOff    
    state.selectedCommands["tempup"] = selectedTempUp
    state.selectedCommands["mode"] = selectedMode
    state.selectedCommands["jetcool"] = selectedJetCool
    state.selectedCommands["tempdown"] = selectedTempDown
    state.selectedCommands["speed"] = selectedSpeed
    state.selectedCommands["custom1"] = custom1
    state.selectedCommands["custom2"] = custom2
    state.selectedCommands["custom3"] = custom3
    state.selectedCommands["custom4"] = custom4
    state.selectedCommands["custom5"] = custom5  

	monitorMenu() 
}

// Add device page for TV
def addTvDeviceTV() {
    log.debug "Line 321: addTvDevice"
    def labelOfCommand = getLabelsOfCommands(atomicState.deviceCommands)
    state.selectedCommands = [:]    

    section("Commands :") {            
        //input name: "selectedPowerToggle", type: "enum", title: "Power Toggle", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOn", type: "enum", title: "Power On", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedVolumeUp", type: "enum", title: "Volume Up", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedChannelUp", type: "enum", title: "Channel Up", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedMute", type: "enum", title: "Mute", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "selectedVolumeDown", type: "enum", title: "Volume Down", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
        input name: "selectedChannelDown", type: "enum", title: "Channel Down", options: labelOfCommand, submitOnChange: true, multiple: false, required: false      
        input name: "selectedMenu", type: "enum", title: "Menu", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "selectedHome", type: "enum", title: "Home", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
        input name: "selectedInput", type: "enum", title: "Input", options: labelOfCommand, submitOnChange: true, multiple: false, required: false              
        input name: "selectedBack", type: "enum", title: "Back", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "custom1", type: "enum", title: "Custom1", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "custom2", type: "enum", title: "Custom2", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "custom3", type: "enum", title: "Custom3", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "custom4", type: "enum", title: "Custom4", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "custom5", type: "enum", title: "Custom5", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
    }
    
    //state.selectedCommands["power"] = selectedPowerToggle
    state.selectedCommands["power-on"] = selectedPowerOn
    state.selectedCommands["power-off"] = selectedPowerOff  
	state.selectedCommands["volup"] = selectedVolumeUp
    state.selectedCommands["chup"] = selectedChannelUp
    state.selectedCommands["mute"] = selectedMute
    state.selectedCommands["voldown"] = selectedVolumeDown
    state.selectedCommands["chdown"] = selectedChannelDown
    state.selectedCommands["menu"] = selectedMenu
    state.selectedCommands["home"] = selectedHome
    state.selectedCommands["input"] = selectedInput
    state.selectedCommands["back"] = selectedBack
    state.selectedCommands["custom1"] = custom1
    state.selectedCommands["custom2"] = custom2
    state.selectedCommands["custom3"] = custom3
    state.selectedCommands["custom4"] = custom4
    state.selectedCommands["custom5"] = custom5  
 
 	monitorMenu() 
}

// Add device page for Aircon
def addRobokingDevice() {
    log.debug "Line 368: addRobokingDevice"
    def labelOfCommand = getLabelsOfCommands(atomicState.deviceCommands)
    state.selectedCommands = [:]    

    section("Commands :") {
        input name: "selectedStart", type: "enum", title: "Start", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedHome", type: "enum", title: "Home", options: labelOfCommand, submitOnChange: true, multiple: false, required: true  
        input name: "selectedStop", type: "enum", title: "Stop", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedUp", type: "enum", title: "Up", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedDown", type: "enum", title: "Down", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "selectedLeft", type: "enum", title: "Left", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
        input name: "selectedRight", type: "enum", title: "Right", options: labelOfCommand, submitOnChange: true, multiple: false, required: false        
        input name: "selectedMode", type: "enum", title: "Mode", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
        input name: "selectedTurbo", type: "enum", title: "Turbo", options: labelOfCommand, submitOnChange: true, multiple: false, required: false   
        input name: "custom1", type: "enum", title: "Custom1", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "custom2", type: "enum", title: "Custom2", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "custom3", type: "enum", title: "Custom3", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "custom4", type: "enum", title: "Custom4", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "custom5", type: "enum", title: "Custom5", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
    }

	state.selectedCommands["start"] = selectedStart
    state.selectedCommands["stop"] = selectedStop
    state.selectedCommands["up"] = selectedUp
    state.selectedCommands["down"] = selectedDown
    state.selectedCommands["left"] = selectedLeft
    state.selectedCommands["right"] = selectedRight
    state.selectedCommands["home"] = selectedHome
    state.selectedCommands["mode"] = selectedMode
    state.selectedCommands["turbo"] = selectedTurbo
    state.selectedCommands["custom1"] = custom1
    state.selectedCommands["custom2"] = custom2
    state.selectedCommands["custom3"] = custom3
    state.selectedCommands["custom4"] = custom4
    state.selectedCommands["custom5"] = custom5  
    
    monitorMenu() 

}

// ------------------------------------
// Monitoring sub menu
def monitorMenu() {
    log.debug "Line 411: monitorMenu"
    section("State Monitor :") {
        paragraph "It is a function to complement IrDA's biggest drawback. Through sensor's state, synchronize deivce status."
        def monitorType = ["Power Meter", "Contact"]
        input name: "selectedMonitorType", type: "enum", title: "Select Monitor Type", multiple: false, options: monitorType, submitOnChange: true, required: false                    
    }  

    atomicState.selectedMonitorType = selectedMonitorType
    if (selectedMonitorType) {            
        switch (selectedMonitorType) {
            case "Power Meter":
            powerMonitorMenu()                
            break
            case "Contact":
            contactMonitorMenu()
            break
        }
    }
}

def powerMonitorMenu() {
    log.debug "Line 432: powerMonitorMenu"
    section("Power Monitor :") {
        input name: "powerMonitor", type: "capability.powerMeter", title: "Device", submitOnChange: true, multiple: false, required: false
        state.triggerOnFlag = false;
        state.triggerOffFlag = false;
        if (powerMonitor) {                
            input name: "triggerOnValue", type: "decimal", title: "On Trigger Watt", submitOnChange: true, multiple: false, required: true
            input name: "triggerOffValue", type: "decimal", title: "Off Trigger Watt", submitOnChange: true, multiple: false, required: true                
        }   
    } 
}

def contactMonitorMenu() {
    log.debug "Line 445: contactMonitorMenu"
    section("Contact :") {
        input name: "contactMonitor", type: "capability.contactSensor", title: "Device", submitOnChange: true, multiple: false, required: false
    	if (contactMonitor) {    
            paragraph "[Normal] : Open(On) / Close(Off)\n[Reverse] : Open(Off) / Close(On)"
            input name: "contactMonitorMode", type: "enum", title: "Mode", multiple: false, options: ["Normal", "Reverse"], defaultValue: "Normal", submitOnChange: true, required: true	
    	}
        atomicState.contactMonitorMode = contactMonitorMode
    }
}


// ------------------------------------
// Monitor Handler
// Subscribe power value and change status
def powerMonitorHandler(evt) {
    def device = []    
    device = getDeviceByName("$selectedDevice")
    def deviceId = device.id
    def child = getChildDevice(deviceId)
    def event

    log.debug "value is over triggerValue>> flag: $state.triggerOnFlag, value: $evt.value, triggerValue: ${triggerOnValue.floatValue()}"        
    if (Float.parseFloat(evt.value) >= triggerOnValue.floatValue() && state.triggerOnFlag == false) {    	
        event =  [value: "on"]
        child.generateEvent(event)
        log.debug "value is over send*****"
        state.triggerOnFlag = true
    } else if (Float.parseFloat(evt.value) < triggerOnValue.floatValue()) {
        state.triggerOnFlag = false
    }

    log.debug "value is under triggerValue>> flag: $state.triggerOffFlag, value: $evt.value, triggerValue: ${triggerOffValue.floatValue()}"
    if (Float.parseFloat(evt.value) <= triggerOffValue.floatValue() && state.triggerOffFlag == false){    	
        event =  [value: "off"]        
        child.generateEvent(event)
        log.debug "value is under send*****"
        state.triggerOffFlag = true
    } else if (Float.parseFloat(evt.value) > triggerOffValue.floatValue()) {
        state.triggerOffFlag = false
    }

}

// Subscribe contact value and change status
def contactMonitorHandler(evt) {
    def device = []    
    device = getDeviceByName("$selectedDevice")
    def deviceId = device.id
    def child = getChildDevice(deviceId)
    def event

	def contacted = "off", notContacted = "on"
    if (atomicState.contactMonitorMode == "Reverse") {
    	contacted = "on"
        notContacted = "off"
    }
    log.debug "Line 502: contactMonitorHandler>> value is : $evt.value"
    if (evt.value == "open") {
        event = [value: notContacted] 
    } else {
        event = [value: contacted] 
    }
    child.generateEvent(event)
}

// Install child device
def initializeChild(devicetype) {
    log.debug "Line 516: Init Child"
    //def devices = getDevices()    
    log.debug "Line 514: addDeviceDone: $selectedDevice, type: $atomicState.selectedMonitorType"
    app.updateLabel("$selectedDevice")

	unsubscribe()
    if (atomicState.selectedMonitorType == "Power Meter") {  
    	log.debug "Power: $powerMonitor"
    	subscribe(powerMonitor, "power", powerMonitorHandler)
    } else if (atomicState.selectedMonitorType == "Contact") {
    	log.debug "Contact: $contactMonitor"
    	subscribe(contactMonitor, "contact", contactMonitorHandler)
    }
    def device = []    
    device = getDeviceByName("$selectedDevice")
    log.debug "addDeviceDone>> device: $device"    

    def deviceId = device.id
    def existing = getChildDevice(deviceId)
    if (!existing) {
        def childDevice = addChildDevice("keo", "Hubitat Harmony_${atomicState.selectedDeviceType}", deviceId, null, ["label": device.label])
    } else {
        log.debug "Device already created"
    }
}


// For child Device
def command(child, command) {
    log.debug "Line 544: command function"
	def device = getDeviceByName("$selectedDevice")
    
	log.debug "Line 543: childApp parent command(child)>>  $selectedDevice, command: $command, changed Command: ${state.selectedCommands[command]}"
    def commandSlug = getSlugOfCommandByLabel(atomicState.deviceCommands, state.selectedCommands[command])
    log.debug "Line 545: childApp parent command(child)>>  commandSlug : $commandSlug"
    
    def result
    result = sendCommandToDevice(device.slug, commandSlug)
    if (result && result.message != "ok") {
        sendCommandToDevice(device.slug, commandSlug)
    }
}

def commandValue(child, command) {
	def device = getDeviceByName("$selectedDevice")
	log.debug "Line 557: childApp parent commandValue(child)>>  $selectedDevice, command: $command"
    def result
    result = sendCommandToDevice(device.slug, command)
    if (result && result.message != "ok") {
        sendCommandToDevice(device.slug, command)
    }
}



// ------------------------------------
// ------- Default Common Method -------
def installed() {    
    initialize()
}

def updated() {
    //unsubscribe()
    initialize()
}

def initialize() {
   log.debug "Line 580: initialize()"
   parent ? initializeChild() : initializeParent()
}


def uninstalled() {
	parent ? null : removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}


// ---------------------------
// ------- Hub Command -------

// getSelectedHub
// return : Installed hub name
def getSelectedHub() {
    log.debug "Line 602: getSelectedHub"
	return atomicState.hub
}

// getLabelsOfDevices
// parameter :
// - devices : List of devices in Harmony Hub {label, slug}
// return : Array of devices's label value
def getLabelsOfDevices(devices) {
    log.debug "Line 611: getLabelsOfDevices"
	def labels = []
    devices.each { 
        //log.debug "labelOfDevice: $it"
        labels.add(it.label)
    }
    return labels
}

// getLabelsOfCommands
// parameter :
// - cmds : List of some device's commands {label, slug}
// return : Array of commands's label value
def getLabelsOfCommands(cmds) {
    log.debug "Line 627: getLabelsOfCommands"
	def labels = []
    log.debug "getLabelsOfCommands>> cmds"
    cmds.each {
    	//log.debug "getLabelsOfCommands: it.label : $it.label, slug : $it.slug"
    	labels.add(it.label)
    }
    return labels
}

// getCommandsOfDevice
// return : result of 'discoverCommandsOfDevice(device)' method. It means that recently requested device's commands
def getCommandsOfDevice() {
    log.debug "Line 641: getCommandsOfDevice>> $atomicState.foundCommandOfDevice"
    return atomicState.foundCommandOfDevice
}

// getSlugOfCommandByLabel
// parameter :
// - commands : List of device's command
// - label : name of command
// return : slug value same with label in the list of command
def getSlugOfCommandByLabel(commands, label) {
	//def commands = []
    log.debug "Line 654: getSlugOfCommandByLabel"
    def slug
    
    commands.each {    	
    	if (it.label == label) {
        	//log.debug "it.label : $it.label, device : $device"
        	log.debug "Line 660: getSlugOfCommandByLabel>> $it"
        	//commands = it.commands
            slug = it.slug
        }
    }
    return slug
}

// getDeviceByName
// parameter :
// - name : device name searching
// return : device matched by name in Harmony Hub's devices
def getDeviceByName(name) {
    log.debug "Line 673: getDeviceByName"
	def device = []    
	atomicState.devices.each {
    	//log.debug "getDeviceByName>> $it.label, $name"
    	if (it.label == name) {
    		log.debug "getDeviceByName>> $it"
            device = it
        }
	}
    
    return device
}
 
// getHubDevices
// return : searched list of device in Harmony Hub when installed
def getHubDevices() {
    log.debug "Line 689: getHubDevices"
	return atomicState.devices
}


// --------------------------------
// ------- HubAction Methos -------
// sendCommandToDevice
// parameter : 
// - device : target device
// - command : sending command
// return : 'sendCommandToDevice_response()' method callback
def sendCommandToDevice(device, command) {
	log.debug("Line 702: sendCommandToDevice >> harmonyApiServerIP : ${parent.getHarmonyApiServerIP()}")
    sendHubCommand(setHubAction(parent.getHarmonyApiServerIP(), "/hubs/$atomicState.hub/devices/$device/commands/$command", "sendCommandToDevice_response"))
}

def sendCommandToDevice_response(resp) {
    def result = []
    def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
    log.debug("sendCommandToDevice_response >> $body")
}

// getHubStatus
// parameter : 
// return : 'getHubStatus_response()' method callback
def getHubStatus() {	
    log.debug "Line 716: getHubStatus"
    sendHubCommand(getHubAction(atomicState.harmonyApiServerIP, "/hubs/$atomicState.hub/status", "getHubStatus_response"))
    if (atomicState.getHubStatusWatchdog == true) {
    	atomicState.hubStatus = "offline"
    }
    atomicState.getHubStatusWatchdog = true        
}

def getHubStatus_response(resp) {
    log.debug "Line 721: getHubStatus Response"
   	def result = []
    atomicState.getHubStatusWatchdog = false
    
    if (resp.description != null && parseLanMessage(resp.description).body) {
    	log.debug "Line 729: getHubStatus_response>> response: $resp.description"
    	def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
	
        if(body && body.off != null) {            	
            log.debug "Line 733: getHubStatus_response>> $body.off"
            if (body.off == false) {
            	atomicState.hubStatus = "online"
            }
        } else {
            log.debug "Line 738: getHubStatus_response>> $body.off"
            atomicState.hubStatus = "offline"
        }
    } else {
    	log.debug "Line 742: getHubStatus_response>> Status error"
        atomicState.hubStatus = "offline"
    }
}

// discoverCommandsOfDevice
// parameter : 
// - name : name of device searching command
// return : 'discoverCommandsOfDevice_response()' method callback
def discoverCommandsOfDevice(name) {
	device = getDeviceByName(name)
    log.debug "Line 753: discoverCommandsOfDevice>> name:$name, device:$device"
    sendHubCommand(getHubAction(atomicState.harmonyApiServerIP, "/hubs/$atomicState.hub/devices/${device.slug}/commands", "discoverCommandsOfDevice_response"))
}

def discoverCommandsOfDevice_response(resp) {
    log.debug "Line 758: discoverCommandsOfDevice_response"
   	def result = []
    def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
	
    if(body) {            	
        body.commands.each {            
            def command = ['label' : it.label, 'slug' : it.slug]
            log.debug "Line 765: getCommandsOfDevice_response>> command: $command"
            result.add(command)            
        }
    }
    atomicState.foundCommandOfDevice = result
}

// discoverDevices
// parameter : 
// - hubname : name of hub searching devices
// return : 'discoverDevices_response()' method callback
def discoverDevices(hubname) {
	log.debug "Line 773: discoverDevices>> $atomicState.harmonyApiServerIP $hubname"
	sendHubCommand(getHubAction(atomicState.harmonyApiServerIP, "/hubs/$hubname/devices", "discoverDevices_response"))
}

def discoverDevices_response(resp) {
	def result = []
    def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
    log.debug("Line 784: discoverDevices_response >> $body.devices")
	
    if(body) {            	
        body.devices.each {
            log.debug "Line 788: getHubDevices_response: $it.id, $it.label, $it.slug"
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
	log.debug("Line 796: discoverHubs")
    return sendHubCommand(getHubAction(host, "/hubs", "discoverHubs_response"))
}

def discoverHubs_response(resp) {
	def result = []
    def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
    log.debug("Line 803: discoverHubs_response >> $body")
	
    if(body && body.hubs != null) { 
        log.debug "Line 808: ${body} and ${body.hubs}"
        body.hubs.each {
            log.debug "Line 810: discoverHubs_response: $it"
            result.add(it)
        }
        atomicState.discoverdHubs = result
    } else {
    	atomicState.discoverdHubs = null
    }  
    log.debug "Line 821: atomicState.discoverdHubs = ${atomicState.discoverdHubs}"
}

// -----------------------------
// -------Hub Action API -------
// getHubAction
// parameter :
// - host : target address to send 'GET' action
// - url : target url
// - callback : response callback method name
def getHubAction(host, url, callback) {
	log.debug "Line 828: getHubAction>> $host, $url, $callback"
    return new hubitat.device.HubAction("GET ${url} HTTP/1.1\r\nHOST: ${host}\r\n\r\n", hubitat.device.Protocol.LAN, "${host}", [callback: callback])
    log.debug "GET ${url} HTTP/1.1\r\nHOST: ${host}\r\n\r\n, hubitat.device.Protocol.LAN, "${host}", [callback: callback])"
}

// setHubAction
// parameter :
// - host : target address to send 'POST' action
// - url : target url
// - callback : response callback method name
def setHubAction(host, url, callback) {
	log.debug "Line 838: setHubAction>> $host, $url, $callback"
    return new hubitat.device.HubAction("POST ${url} HTTP/1.1\r\nHOST: ${host}\r\n\r\n", hubitat.device.Protocol.LAN, "${host}", [callback: callback])
}
