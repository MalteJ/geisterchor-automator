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

class VirtualMachine {
    String name
    String templateVmx              // Template VM to clone machine from
                                    // may be null if this.created == true
    String vmx                      // VM's VMX file on hypervisor
    String pool
    Integer cores = 2
    Integer memory = 512
    def ethernetAdapters = []

    String  sshUser = "root"
    Integer sshPort = 22

    Boolean created                                 // if true vm wont be cloned at first
    Boolean poweredon                               // if true vm wont be powered on
    Boolean persistent                              // if true vm will not be destroyed in the end
    Boolean destroyed                               // whether machine is destroyed yet
    
    def hypervisor
    def setHypervisor(def hyp) {
        this.hypervisor = hyp
        hyp.vms.add(this)
    }

    String toString() { "${name}@${hypervisor}" }

    def create() {
        println "Creating VM ${this}"
        return hypervisor.createVm(this)
    }

    def poweron() {
        println "Power On VM ${this}"
        return hypervisor.powerOnVm(this)
    }

    def poweroff() {
        println "Power Off VM ${this}"
        return hypervisor.powerOffVm(this)
    }

    def destroy() {
        println "Destroy VM ${this}"
        hypervisor.destroyVm(this)
    }

    String _ip = null
    String getIp() {
        if(!_ip)
            _ip = hypervisor.getVmIp(this)
        return _ip
    }

    void setIp(String ip) {
        this._ip = ip
    }

    def ssh(def cmd, def config=[validExitValues:[0]]) {
        if (!config.validExitValues) config.validExitValues = [0]
        def ret = RemoteTools.ssh(this, config, cmd)
        if (!(ret.exitValue in config.validExitValues)) {
            println "exited with invalid exit value: ${ret.exitValue}"
            throw new AssertionError("ssh on VM ${this} '${cmd}' returned exit code ${ret.exitValue}")
        }
        return ret
    } 

    def rsync(def source, def target, HashMap config=[]) {
        def ret = RemoteTools.rsync(config, this, source, target)
        if (!(ret.exitValue in [0])) {
            print ret.stdout
            println "rsync exited with invalid exit value: ${ret.exitValue}"
            throw new AssertionError("rsync ${source} to ${this} ${target}' failed with exit code ${ret.exitValue}")
        }
    }

    def puppetRun() {
        ssh("puppet agent -t")
    }

    def puppetApply(String manifest) {
        ssh("puppet apply ${manifest}")
    }
}
