#!/usr/bin/env groovy
@Grab('com.geisterchor:automator:0.1.3')
import com.geisterchor.automator.*

def yoda = new vSphereHypervisor(hostname: "yoda")

def vm1 = new VirtualMachine(
     name: "debian-live",
     vmx: "/vmfs/volumes/ssd0/debian-live-test2/debian-live-test.vmx",
     templateVmx: "/vmfs/volumes/hdd1/DebianWheezy32-Template/DebianWheezy32-Template.vmx",
     pool: "pool4",
     cores: 4,
     memory: 512,
     hypervisor: yoda,
     _ip: "aembak-storage",
    )


vm1.ssh "whoami"
vm1.ssh("hostname", [timeout:600])

