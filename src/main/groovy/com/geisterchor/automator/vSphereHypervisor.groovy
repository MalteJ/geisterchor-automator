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

    String toString() { hostname }

    def ssh(def cmd, def assertExitValues = true) {
        def ret = RemoteTools.ssh(cmd, [ip: hostname, user: username])
        if (assertExitValues && ret.exitValue != 0) throw new RuntimeException("Non zero exit value!")
        return ret
     }

    def addVm(VirtualMachine vm) {
        vms.add(vm)
        vm.hypervisor = this
    }

    def createVm(VirtualMachine vm) {

        def disks = ssh("grep vmdk ${vm.templateVmx}").stdout.readLines()*.split('"').collect{it[1]}
        println disks
        ssh "mkdir -p `dirname ${vm.vmx}`"
        disks.collect { it -> ssh "vmkfstools -d thin -i `dirname ${vm.templateVmx}`/${it} `dirname ${vm.vmx}`/${it}" }

        ssh "cp ${vm.templateVmx} ${vm.vmx}"
        ssh " vim-cmd solo/registervm ${vm.vmx} ${vm.name} ${vm.pool}"
        return vm
    }

    def powerOnVm(VirtualMachine vm) { 
        ssh "vim-cmd vmsvc/power.on ${vm.vmx} & sleep 1 ; vim-cmd vmsvc/message ${vm.vmx} _vmx1 2", false
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
        ssh("vim-cmd vmsvc/get.summary ${vm.vmx} | grep ipAddress | grep -v \"unset\" | cut -d \\\" -f 2 | egrep '[[:digit:]]{1,3}\\.[[:digit:]]{1,3}\\.[[:digit:]]{1,3}\\.[[:digit:]]{1,3}'").stdout
    }
}
