package com.geisterchor.automator

import org.junit.Test

public class RemoteToolsTest {
    @Test
    public void testRsync() {
        def config = [timeout:150]
        def vm = new VirtualMachine(_ip:"127.0.0.1", sshUser:"root")
        def source = "localDir"
        def target = "targetDir"

        def rsyncCommand
        def rsyncConfig

        RemoteTools.rsync(config, vm, source, target,
            { def conf, def cmd ->
                rsyncCommand = cmd
                rsyncConfig = conf
            }
        )

        String cmdString = rsyncCommand.join(" ")
        assert cmdString == "rsync -avz --delete -e ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=.known_hosts localDir root@127.0.0.1:targetDir"
    }

    @Test
    public void testRsyncExclude() {
        def config = [timeout:150, exclude: [".git",".hg"]]
        def vm = new VirtualMachine(_ip:"127.0.0.1", sshUser:"root")
        def source = "localDir"
        def target = "targetDir"

        def rsyncCommand
        def rsyncConfig

        RemoteTools.rsync(config, vm, source, target,
            { def conf, def cmd ->
                rsyncCommand = cmd
                rsyncConfig = conf
            }
        )

        String cmdString = rsyncCommand.join(" ")
        assert cmdString == "rsync -avz --exclude .git --exclude .hg --delete -e ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=.known_hosts localDir root@127.0.0.1:targetDir"
    }

}
