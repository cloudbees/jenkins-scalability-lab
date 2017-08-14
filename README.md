# jenkins-scalability-lab
Testbed for measuring scalability of Jenkins.

# Usage
* To build everything and bring up the environmeent, go to the 'local' folder and run `./build-local.sh`
* To shut all of the environment down and kill containers, run `./shutdown.sh` from this location (one level up from 'local')
    - Because of the use of a persistent volume, manual jenkins configuration will be retained between runs, but you can clear configuration with `docker volume rm jenkins_home`
    - Your gitserver repo data will be lost (that's fine, it's copied from a local folder)
    - All of your graphite data will be lost
* There are some helpful scripts to run via script console on the master in the 'user-scripts' folder :)

## The Jenkins master: our test target

* Available at [http://localhost:8080/](http://localhost:8080/)
    - From other containers, this host is known as "jenkins"
* Includes credential 'git-ssh' with the SSH key can be used to clone or interact with the Git server, with remote ssh://git@gitserver/git-server/repos/testcases.git
* Includes a multibranch project 'testcases' that will have a pipeline for each of the testcases defined as subfolders under gitserver/testcases (see below for more detail)
* Configured to have 0 executors -- all executors will be provided by the Swarm Agent Docker containers
* jenkins/plugins.txt will define plugins included on the master
* jenkins/CUSTOM-PLUGINs contains plugin HPI or JPI files to land directly on the master
    - This is good for testing one-off changes with SNAPSHOT versions or otherwise unreleased plugins
    - Note that **No dependency resolution is done, so your plugins.txt file must include dependencies!**  This can be easier by using a plugins.txt entry for the previous release of the plugin, and then have the custom plugins replace that.
* (snapshot-versioned or otherwise customized) to directly land on the masters.  This is useful for testing one-off changes or using specific builds to test something (or for unreleased content)
* jenkins/minimal-plugins.txt defines the minimal plugins needed to create a functional master for testing practices (once their dependencies are also installed)

##Git Server: testcases, shared libraries and testcase data

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
* Alternately, **to push/clone the git repository using normal SSH credentials**, if you copy your public SSH key (usually `~/.ssh/id_rsa.pub`) to a new file **THAT IS NOT id_rsa.pub** in gitserver/keys, you can pull/push from [ ssh://git@localhost:2222/git-server/repos/testcases.git]( ssh://git@localhost:2222/git-server/repos/testcases.git) with your normal SSH credentials.  Note that testcases must be new branches and include a Jenkinsfile in the branch, plus you must re-trigger the 'testcases' multibranch project indexing

* For further info on how to interact with the server and create new repos on it, please see: [the jkarlos/git-server-docker](https://hub.docker.com/r/jkarlos/git-server-docker/)
    - Note that repos you upload to the server must be in bare repo format not just a normal repo -- usually this is done by cloning a normal repo using `git clone --bare` to, uh, bare-ify it

## InfluxDB server - Metrics (Timeseries) and Events
* Container name "influx"
    - Graphite input/output - port 2015: Graphite data input/output (reported by graphite-metrics plugin on Jenkins).  [Docs here](https://github.com/influxdata/influxdb/blob/master/services/graphite/README.md).
    - Web UI - port 8083 - [http://localhost:8083/](http://localhost:8083/) for exploring [InfluxDB queries and data](https://docs.influxdata.com/influxdb/v1.2/query_language/)
    - InfluxDB input - port 8086 - used as data source for Grafana
* Provides semi-persistent storage of data

## Grafana - Visualization

* Suggested starting point: [Core metrics dashdoard](http://localhost:81/dashboard/db/scalability-lab-core-metrics)
* Grafana available at [http://localhost:81/](http://localhost:81/)
    - Login is admin, password admin
    - Configure it to speak to InfluxDB for its data source @ http://influx:8086, databases telegraf and my_db


**Telegraf metric collection on Jenkins master**
* Start by running 'telegraf' on the Jenkins master
* Available in InfluxDB under database 'telegraf' (you need to create a new grafana datasource, with settings below)
    - URL: http://influx:8086
    - Database: 'telegraf'
    - User: root
    - Password: somepassword

**Build agents**
* Connect automatically via the Swarm Agents plugin
* Note that this requires some custom networking. Swarm agent Docker containers can't simply connect via the HTTP port and JNLP port to the master, they appear to need to be on the same Docker network as the master or at least have a container link created

# Manual configuration (currently being automated)

## To trigger load
1. Go to "Manage Jenkins" -> "Configure Jenkins", and under Load Generators, add some and save.  Then clock the "Load Generators" link on the left sidebar and set global autostart to true (click the top button) and activate the generator(s) you want.  Jobs will start

# Troubleshooting
* **Problem:** Basic issues i.e. something broke
    - **Solution:** run the shutdown script, run `docker rm volume jenkins_home`, and run the build-local.sh script again
* **Problem:** Error fetching plugin
    - **Solution:** Look up the plugin in the Jenkins wiki and see if it was just released (may not be fully available yet) - if so, you can hardcode a plugins.txt dependency on the previous version to work around this. 
* **Problem** Halp, I get an error along the lines of:
    * > Error response from daemon: could not find an available, non-overlapping IPv4 address pool among the defaults to assign to the network
    * **Solution:** If you're running a VPN, deactivate it, shut down the load test fully, and start over again

# Limitations

* Doesn't automate the configuration (currently it requires manually tickling some files and folders)
* Requires a Debian-based Jenkins Docker image (not Alpine) due to Telegraf installation - perhaps fixable with direct binary install via [the releases](https://portal.influxdata.com/downloads)
* Requires fairly modern Jenkins core and plugins to run
