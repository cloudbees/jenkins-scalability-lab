# jenkins-scalability-lab
Testbed for measuring scalability of Jenkins

Once you run build.sh, there will be:

**A Jenkins master**
* Available at [http://localhost:8080/](http://localhost:8080/)

**A Graphite server**
* Available at [http://localhost:81/](http://localhost:81/) with visualization

**A Git Server using the local filesystem**
* Available at 

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

# Create jobs (will be automated)
Pending

# To trigger load
1. Go to "Manage Jenkins" -> "Configure Jenkins", and under "Random Job Builder" set rate > 0