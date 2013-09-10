package com.geisterchor.automator

import org.junit.Test

public class RemoteToolsTest {
    @Test
    public void testRsync() {
        def config = [timeout:150, source: "localDir", dest: "targetDir", host:"127.0.0.1", user: "root"]

        def rsyncCommand
        def rsyncConfig

        RemoteTools.rsync(config,
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
        def config = [timeout:150, source: "localDir", dest: "targetDir", host:"127.0.0.1", user: "root", exclude: [".git",".hg"]]

        def rsyncCommand
        def rsyncConfig

        RemoteTools.rsync(config,
            { def conf, def cmd ->
                rsyncCommand = cmd
                rsyncConfig = conf
            }
        )

        String cmdString = rsyncCommand.join(" ")
        assert cmdString == "rsync -avz --exclude .git --exclude .hg --delete -e ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=.known_hosts localDir root@127.0.0.1:targetDir"
    }

}
