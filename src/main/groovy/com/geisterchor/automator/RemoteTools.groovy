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
import org.apache.commons.io.output.TeeOutputStream

class RemoteTools {
    static def localExec(def config=[timeout:120], def command) {
        //println command
        if (!config.timeout) config.timeout = 120
        def proc = command.execute()
        def pos = new ByteArrayOutputStream()
        def per = new ByteArrayOutputStream()
        def tee = new TeeOutputStream(System.out, pos)
        def terr= new TeeOutputStream(System.err, per)
        proc.consumeProcessOutput(tee, terr)
        proc.waitForOrKill(config.timeout*1000)
        def outstr = pos.toString()
        def errstr = per.toString()
        return [exitValue: proc.exitValue(), stdout: outstr, stderr: errstr]
    }

    static def rsync(def config=[timeout:120], VirtualMachine vm, String source, String target) {
        def command = ["rsync", "-avz", "--delete", "-e", "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=.known_hosts", source, "${vm.sshUser}@${vm.ip}:${target}"]
        return localExec(config,command)
    }

    static def ssh(String command, def config=[timeout:120]) {
        def sshHost = config.ip
        def sshUser = config.user ?: "root"
        def keyfile = config?.keyfile ?: "${System.getenv().HOME}/.ssh/id_rsa"
        def sshPort = config?.sshPort ?: 22

        def cmd = "ssh -i ${keyfile} -o StrictHostKeyChecking=no -o UserKnownHostsFile=.known_hosts ${sshUser}@${sshHost} -p ${sshPort} ${command}"
        return localExec(config, cmd)
    }

    static def ssh(VirtualMachine vm, def config=[timeout:120], String command) {
        config.ip = vm.ip
        config.user = vm.sshUser
        ssh(command, config)
    }
}
