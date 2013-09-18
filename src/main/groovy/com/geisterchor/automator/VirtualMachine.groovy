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
    
	def toDict() {
		[
			name: name,
			templateVmx: templateVmx,
			ip: ip,
			vmx: vmx,
			pool: pool,
			cores: cores,
			memory: memory,
			//ethernetAdapters*.toDict()
			sshUser: sshUser,
			sshPort: sshPort,
			created: created,
			poweredon: poweredon,
			persistent: persistent,
			destroyed: destroyed
		]
	}
	
	String toJson() {
		bldr = new groovy.json.JsonBuilder(this.toDict())
		writer = new StringWriter()
		bldr.writeTo(writer)
		return writer.toString()
	}
	
    private def hypervisor
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

    private String _ip = null
    String getIp() {
		if(!created)
			return _ip
        else if(!_ip)
            _ip = hypervisor.getVmIp(this)
        return _ip
    }

    void setIp(String ip) {
        this._ip = ip
    }

    def ssh(def config) {
		assert config.cmd
        if (!config.validExitValues) config.validExitValues = [0]
        def ret = RemoteTools.ssh(this, config, config.cmd)
        if (!(ret.exitValue in config.validExitValues)) {
            println "exited with invalid exit value: ${ret.exitValue}"
            throw new AssertionError("ssh on VM ${this} '${config.cmd}' returned exit code ${ret.exitValue}")
        }
        return ret
    }
	
	def ssh(String cmd) {
		ssh([cmd: cmd])
	}

    def rsync(def config=[]) {
		assert config.source
		assert config.dest
		config.user = this.sshUser
		config.host = this.ip
        def ret = RemoteTools.rsync(config)
        if (!(ret.exitValue in [0])) {
            print ret.stdout
            println "rsync exited with invalid exit value: ${ret.exitValue}"
            throw new AssertionError("rsync ${config.source} to ${this} ${config.dest}' failed with exit code ${ret.exitValue}")
        }
    }

    def puppetRun() {
        ssh("puppet agent -t")
    }
	
	def puppetApply(def conf) {
		conf.cmd = "puppet apply --detailed-exitcodes ${conf.manifest}"
		conf.validExitValues = [0,2]
		ssh conf
	}

    def puppetApply(String manifest) {
		puppetApply manifest: manifest
    }
}
