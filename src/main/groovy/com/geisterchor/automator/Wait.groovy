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

class Wait {
    static def forPing(HashMap config=[timeout:60], VirtualMachine vm) {
        println "wait for ping VM ${vm}, timeout: ${config.timeout}"
        def i=0
        while (i++ < config.timeout) {
            try {
                def cmd = "ping -c 1 -W 1 ${vm.ip}"
                def p = cmd.execute()
                p.waitForOrKill(1200)
                if (p.exitValue() == 0)
                    println "got a ping after ${i} seconds"
                    return true
            } catch (Exception e) {
            }
        }
        println "got no ping!"
        throw new RuntimeException("got no ping from VM ${vm}")
    }

    static def forSeconds(Integer seconds) {
        println "wait for ${seconds} seconds"
        for (Integer i=0; i<seconds; i++)
            Thread.sleep(1000)
    }
}
