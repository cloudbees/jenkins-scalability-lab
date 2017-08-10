# jenkins-scalability-lab
Testbed for measuring scalability of Jenkins.

# Usage
* To build everything and bring up the environmeent, go to the 'local' folder and run `./build-local.sh`
* To shut all of the environment down and kill containers, run `./shutdown.sh` from this location (one level up from 'local')
    - Because of the use of a persistent volume, manual jenkins configuration will be retained between runs, but you can clear configuration with `docker volume rm jenkins_home`
    - Your gitserver repo data will be lost (that's fine, it's copied from a local folder)
    - All of your graphite data will be lost
* There are some helpful scripts to run via script console on the master in the 'user-scripts' folder :)

**A Jenkins master**
* Available at [http://localhost:8080/](http://localhost:8080/)
    - From other containers, this host is known as "jenkins"
* Includes credential 'git-ssh' with the SSH key can be used to clone or interact with the Git server, with remote ssh://git@gitserver/git-server/repos/testcases.git
* Includes a multibranch project 'testserver' that will have a pipeline for each of the testcases defined as subfolders under gitserver/testcases (see below for more detail)
* Configured to have 0 executors -- all executors will be provided by the Swarm Agent Docker containers
* jenkins/plugins.txt will define plugins included on the master
* jenkins/minimal-plugins.txt defines the minimal plugins needed to create a functional master for testing practices (once their dependencies are also installed)

**A Git Server**
* The git server is automatically populated with testcases from gitserver/testcases
    - Each subfolder of 'testcases' becomes a testcase branch in the repo when you build the container
    - Each subfolder MUST have a Jenkinsfile (plus any ancilliary data)
    - On the master, the Jenkins multibranch project 'testcases' will have a matching pipeline Job for each of these branches/subfolders
* You can clone the repo locally via:
```
ssh-agent $(ssh-add ./id_rsa; git clone ssh://git@localhost:2222/git-server/repos/testcases.git)
```
* Then you can push (with local repo) via:
```
ssh-agent $(ssh-add ./id_rsa; git push origin $myBranchName)
```

* For further info on how to interact with the server and create new repos on it, please see: [the jkarlos/git-server-docker](https://hub.docker.com/r/jkarlos/git-server-docker/)
    - Note that repos you upload to the server must be in bare repo format not just a normal repo -- usually this is done by cloning a normal repo using `git clone --bare` to, uh, bare-ify it

**A Graphite server**
* Grafana available at [http://localhost:81/](http://localhost:81/)
    - Login is admin, password admin
    - Configure it to speak to Graphite with its data source (might be on port 7002, might be 80, might be 82, could be localhost or 127.0.0.1)
* Graphite UI available at [http://localhost:82/](http://localhost:82/) with visualization
* Docs for the container base we're using are [on Github](https://github.com/m30m/docker-graphite-grafana)

**Build agents**
* Connect automatically via the Swarm Agents plugin
* Note that this requires some custom networking. Swarm agent Docker containers can't simply connect via the HTTP port and JNLP port to the master, they appear to need to be on the same Docker network as the master or at least have a container link created

# Manual configuration (currently being automated)

## To trigger load
1. Go to "Manage Jenkins" -> "Configure Jenkins", and under "Random Job Builder" set rate > 0

# Troubleshooting
* **Problem:** Basic issues i.e. something broke
    - **Solution:** run the shutdown script, run `docker rm volume jenkins_home`, and run the build-local.sh script again
* **Problem** Halp, I get an error along the lines of:
    * > Error response from daemon: could not find an available, non-overlapping IPv4 address pool among the defaults to assign to the network
    * **Solution:** If you're running a VPN, deactivate it, shut down the load test fully, and start over again