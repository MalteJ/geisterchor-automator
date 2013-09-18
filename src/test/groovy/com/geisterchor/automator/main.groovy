package com.geisterchor.automator

import org.junit.Test

public class GroovyAutomatorTest {
    @Test
    public void testMain() {
        def yoda = new vSphereHypervisor(hostname: "yoda")
        
        def vm1 = new VirtualMachine(
             name: "debian-live",
             vmx: "/vmfs/volumes/ssd0/debian-live-test2/debian-live-test.vmx",
             templateVmx: "/vmfs/volumes/hdd1/DebianWheezy32-Template/DebianWheezy32-Template.vmx",
             pool: "pool4",
             cores: 4,
             memory: 512,
             hypervisor: yoda,
			 created: true,
             ip: "aembak-storage",
            )
        
        assert vm1.ip == "aembak-storage"
        vm1.ssh "whoami"
        vm1.ssh cmd: "hostname", timeout:600
		
		println yoda.toJson()
    }
}
