/*
 * Copyright 2013 by Malte Janduda
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.geisterchor.automator

class vSphereHypervisor {
    String hostname           // hostname or ip to connect to
    String username = "root"  // SSH username 
    String keyFile            // SSH private key file

    def vms = []
	
	def toDict() {
		[
			hostname: hostname,
			username: username,
			keyFile: keyFile,
			vms: vms*.toDict(),	
		]
	}
	
	String toJson() {
		def bldr = new groovy.json.JsonBuilder(this.toDict())
		def writer = new StringWriter()
		bldr.writeTo(writer)
		return writer.toString()
	}

    String toString() { hostname }

    def ssh(def cnf) {
		def config
		if (cnf.getClass() == String)
			config = [cmd: cnf]
		else
			config = cnf			
			
		config.assertExitValues = config.assertExitValues ?: true
		config.ip = this.hostname
        def ret = RemoteTools.ssh(config)
        if (config.assertExitValues && ret.exitValue != 0) throw new RuntimeException("Non zero exit value!")
        return ret
     }
	
	def ssh(String cmd) {
		ssh(cmd: cmd)
	}

    def addVm(VirtualMachine vm) {
        vms.add(vm)
        vm.hypervisor = this
    }

    def createVm(VirtualMachine vm) {

        def disks = ssh("grep vmdk ${vm.templateVmx}").stdout.readLines()*.split('"').collect{it[1]}
        println disks
        ssh "mkdir -p `dirname ${vm.vmx}`"
        disks.collect { it -> ssh cmd: "vmkfstools -d thin -i `dirname ${vm.templateVmx}`/${it} `dirname ${vm.vmx}`/${it}", timeout: 600 }

        ssh "cp ${vm.templateVmx} ${vm.vmx}"
        ssh " vim-cmd solo/registervm ${vm.vmx} ${vm.name} ${vm.pool}"
		vm.created = true
        return vm
    }

    def powerOnVm(VirtualMachine vm) { 
        ssh cmd: "vim-cmd vmsvc/power.on ${vm.vmx} & sleep 1 ; vim-cmd vmsvc/message ${vm.vmx} _vmx1 2", assertExitValues: false
    }
   
    def powerOffVm(VirtualMachine vm) {
        ssh "vim-cmd vmsvc/power.off ${vm.vmx}"
        vm.destroyed = true
        return vm
    }
    
    def destroyVm(VirtualMachine vm) {
        ssh "vim-cmd vmsvc/power.off ${vm.vmx}; vim-cmd vmsvc/destroy ${vm.vmx}"
        vm.destroyed = true
        return vm
    }
    def getVmIp(VirtualMachine vm) {
        ssh("vim-cmd vmsvc/get.summary ${vm.vmx} | grep ipAddress | grep -v \"unset\" | cut -d \\\" -f 2 | egrep '[[:digit:]]{1,3}\\.[[:digit:]]{1,3}\\.[[:digit:]]{1,3}\\.[[:digit:]]{1,3}'").stdout.replaceAll('\n', '')
    }
}
