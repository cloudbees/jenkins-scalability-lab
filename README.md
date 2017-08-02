# jenkins-scalability-lab
Testbed for measuring scalability of Jenkins.

# Usage
* To build everything and bring up the environmeent, run `cd local && ./build-local.sh`
* To shut all of the environment down and kill containers, run './shutdown.sh'
    - Because of the use of a persistent volume, manual jenkins configuration will be retained between runs
    - Due to the use of a local volume mount, git server configuration will be retained
    - All of your graphite data will be lost

**A Jenkins master**
* Available at [http://localhost:8080/](http://localhost:8080/)

**A Graphite server**
* Available at [http://localhost:81/](http://localhost:81/) with visualization

**A Git Server**
* You can clone locally via:
```
ssh-agent $(ssh-add ./id_rsa; git clone ssh://git@localhost:2222/git-server/repos/testcases.git)
```
* You can push (with local repo) via:
```
ssh-agent $(ssh-add ./id_rsa; git push origin $myBranchName)
```

**Build agents**
* Launched, but may need manual configuration on the master

# Manual configuration (currently being automated)
* Manually install the Metrics Graphite reporter plugin & configure it
    1. To configure, go to "Mange Jenkins" then "Configure system" and in the section "Graphite metrics reporting" add server hostname "graphite" and port 2003 (default)
    2. Leave prefix blank, and save
    3. Stats will show in grapite, under the tree on the left, under "localhost" (http, jenkins, system, vm, etc) for the Jenkins master
* Manually create build agents on the master (will be automated)
    1. First go to "Manage Jenkins" then "Manage Nodes", then on the left sidebar click "new node"
    2. Node name will be "agent-1", "agent-2", etc etc and type is "Permanent Agent"
    3. Executors: you can create one agent with n (where n > 1) executors, or 4 agents with 1 executor your call (4 agent containers are created by default).  Agent counts should probably not be much more than total cores in the system, initially.
    4. Set the following properties for each:
        - Remote root directory: /home/jenkins
        - Labels: executor
        - Launch Method: launch agents via SSH
        - Under launch, host is agent-1, agent-2, up to agent-4
        - You'll need to add a new credential of type SSH username with private key
            + Global scope, username is "jenkins", private key is "From a file on Jenkins Master"
            + The file is "/tmp/id_rsa"
            + Passphrase is blank, and the id and description are whatever you like
            + When created, select this credential under the agent launch method
        - Host-key verification strategy is set to: Non verifying Verification Strategy
        - All else is default

# Git server setup (needs to be better automated)
1. We're using the [jkarlos git server docker image](https://hub.docker.com/r/jkarlos/git-server-docker/) which has docs
2. You'll need to do the SCP stuff to set up remote repos on the git server, AKA to do 'docker exec -it gitserver sh' and then:
    1. Run 'cd /git-server/repos'
    2. Run 'mkdir myRepoName && cd myRepoName'
    3. Initialize the git repo with 'git init --shared=true'
4. Should be able to add your user keys to push/check out from that git host

# To trigger load
1. Go to "Manage Jenkins" -> "Configure Jenkins", and under "Random Job Builder" set rate > 0