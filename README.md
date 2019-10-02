# jenkins-scalability-lab
Testbed for measuring scalability of Jenkins.

# Requirements
* Docker 17.09 or higher (due to the fix to [Issue #6119](https://github.com/moby/moby/issues/6119))

# Local Usage
* For custom testcases, at the moment you may wish to create branches for your scenarios
* To build everything and bring up the environment, go to the 'local' folder and run `./build-local.sh`
* To shut all of the environment down and kill containers, run `./shutdown.sh` from this location (one level up from 'local')
    - Because of the use of a persistent volume, manual jenkins configuration will be retained between runs, but you can clear configuration with `docker volume rm jenkins_home`
    - Your gitserver repo data will be lost (that's fine, it's copied from a local folder)
    - All of your InfluxDB data will be lost
* **Troubleshooting a Docker error about binding to port 2015**: often this port is used for VPN software and this can result in a conflict. To solve the error, either shut down your VPN service or you will need to change the port used for InfluxDB Graphite listener (`local-scripts/run-local.sh` for Influx configuration, and also change the port used to report metrics from Jenkins in `jenkins/loadtestsetup.groovy`)

# AWS Usage
* See subfolder "aws" which has docs and scripts (requires SSH access to a couple hosts)
* This relies on using Docker with the 'add-host' to bind container hostnames to the IP addresses
    - You'll need to manually customize the IP addresses in scripts since AWS dynamically assigns private IPs

# Fully automated test scenarios
Fully-automated test scenarios are supported via a small 'runner' library and minimal testcase format, located under the 'runner folder'.  

This is built trivially with Maven, simply go to the folder and run `mvn clean install package`.  It will generate a "fat JAR" containing all dependencies (runner/target/hydra-runner-1.0-SNAPSHOT-jar-with-dependencies.jar), so the only requirement after that is Java 8.

This is used by running the fat JAR directly with 'java -jar' , and provides its own help documentation on command-line options.

See runner/run-basic-test.sh for an example of using the script and runner/fulltest.txt and runner/basic-test.txt for example tests.

# Capturing Metrics Dumps & Viewing them Later

In many cases you will want to save the full batch of metrics provided by running a set of scalability tests.  To facilitate this, the script "dump-data.sh" is provided in the root directory of this folder.  If this script or its commands are run from the system hosting the InfluxDB container then it will generate a file 'hydra-metrics.tar.gz' containing a dump of all the metrics for that Influx instance. 

This single tar.gz file can be copied from that host (for example via SCP) and later used to launch an Influx instance with that data in it, which can be viewed with a local Grafana instance.  This can be accomplished with script 'view-data.sh' which will launch a local InfluxDB container and Grafana viewer instance with this data, looking for a tar.gz file containing 'metrics' in the name.

## The Jenkins master: our test target

* Available at [http://localhost:8080/](http://localhost:8080/)
    - From other containers, this host is known as "jenkins"
* Includes credential 'git-ssh' with the private SSH key can be used to clone or interact with the Git server, with remote ssh://git@gitserver:2222/git-server/repos/testcases.git
    - This credential is read from environment variable 'GIT_PRIVATE_KEY' at setup time
    - You can specify this like so: `-e GIT_PRIVATE_KEY="$(cat some/path/id_rsa)"`
* Includes a multibranch project 'testcases' that will have a pipeline for each of the testcases defined as subfolders under gitserver/testcases (see below for more detail)
* Configured to have 0 executors -- all executors will be provided by the Swarm Agent Docker containers
* jenkins/plugins.txt will define plugins included on the master
* jenkins/CUSTOM-PLUGINs contains plugin HPI or JPI files to land directly on the master
    - This is good for testing one-off changes with SNAPSHOT versions or otherwise unreleased plugins
    - Note that **No dependency resolution is done, so your plugins.txt file must include dependencies!**  This can be easier by using a plugins.txt entry for the previous release of the plugin, and then have the custom plugins replace that.
* (snapshot-versioned or otherwise customized) to directly land on the masters.  This is useful for testing one-off changes or using specific builds to test something (or for unreleased content)
* jenkins/minimal-plugins.txt defines the minimal plugins needed to create a functional master for testing practices (once their dependencies are also installed)
* You can customize the initial setup of the master by modifying jenkins/loadtestsetup.groovy, for example to modify security setup or add users.  Note that **you must make the Groovy commands idempotent, because this will run every time you start the Docker image baked from it.**
* To grab a heap dump (note that you must have the java debug symbols installed, i.e. package openjdk-8-dbg/stable): 
    - `docker exec -it -u jenkins jenkins bash`

        You'll be running a shell inside of the Jenkins docker container under test. Now, run:

        ```
        jenkins@jenkins:/$ ps -ef
        UID        PID  PPID  C STIME TTY          TIME CMD
        jenkins      1     0  0 18:55 pts/0    00:00:00 /sbin/tini -- /usr/local/bin/full-start.sh
        jenkins      9     1  0 18:55 pts/0    00:00:00 /bin/bash /usr/local/bin/full-start.sh
        jenkins     10     9  1 18:55 pts/0    00:02:22 telegraf
        jenkins     11     9 79 18:55 pts/0    01:50:25 java -Duser.home=/var/jenkins_home -Dgraphite.metrics.intervalSeconds=10 -Dcom.sun.management.jmxremote -Dcom.sun.manag
        ```
        In our example, Jenkins is running under PID 11. Meaning we can now run:

    - `jmap -dump:live,format=b,file=/tmp/heapdump.bin 11`
    
        Which will save the file to disk. Now, exit the docker container with `exit`, and from your host machine, run:

    - `docker cp jenkins:/tmp/heapdump.bin ./heapdump.bin`
* From here, you can use your favorite heap analysis tool, like maybe visualvm, to look at what's going on.

## Git Server: testcases, shared libraries and testcase data

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

* Grafana available at [http://localhost:81/](http://localhost:81/)
    - Login is admin, password admin
    - Configure it to speak to InfluxDB for its data source @ http://influx:8086, database "hydra"
* Suggested starting point: [Core metrics dashdoard](http://localhost:81/dashboard/db/scalability-lab-core-metrics)
* Note that you may need to change the "root block device id" variable to get IO info to show correctly (it'll be echoed when launching the environment)
    - For example if the root device for Jenkins is /dev/vda (Mac with "native" Docker, generally), this will be "vda"
    - You can obtain it by running the following: `echo $(docker run --rm -it tutum/influxdb lsblk -d -o NAME | tail -n 1 | tr -d '\r' | tr -d '\n')`
    - If your Jenkins container has resource limits, the device name will be there
    - Grafana will also bind in the environment variable "$ROOT_BLKDEV_NAME", so if the environment is fully running you can do `docker exec grafana bash -c 'echo $ROOT_BLKDEV_NAME'`


**Telegraf metric collection on Jenkins master**
* Start by running 'telegraf' on the Jenkins master
* Available in InfluxDB under database 'hydra' (you need to create a new grafana datasource, with settings below)
    - URL: http://influx:8086
    - Database: 'hydra'
    - User: root
    - Password: somepassword

**Build agents**
* Connect automatically via the Swarm Agents plugin
* Note that this requires some custom networking. Swarm agent Docker containers can't simply connect via the HTTP port and JNLP port to the master, they appear to need to be on the same Docker network as the master or at least have a container link created

# Manual configuration (currently being automated)

## To trigger load
1. Go to "Manage Jenkins" -> "Configure Jenkins", and under Load Generators, add some and save.  
Then click the "Load Generators" link on the left sidebar and set global autostart to true (click the top button) and activate the generator(s) you want.  Jobs will start

# Troubleshooting
* **Problem:** Basic issues i.e. something broke
    - **Solution:** run the shutdown script, run `docker volume rm jenkins_home`, and run the build-local.sh script again
* **Problem:** Error fetching plugin
    - **Solution:** Look up the plugin in the Jenkins wiki and see if it was just released (may not be fully available yet) - if so, you can hardcode a plugins.txt dependency on the previous version to work around this. 
* **Problem** Halp, I get an error along the lines of:
    * > Error response from daemon: could not find an available, non-overlapping IPv4 address pool among the defaults to assign to the network
    * **Solution:** If you're running a VPN, deactivate it, shut down the load test fully, and start over again
* **Problem** I am running on Mac and trying to attach a profiler but it fails.  For example, I use VisualVM and add a JMX connection (with SSL requirement off, per requirements) to localhost:9011
    * **Solution:** Probably have to use Linux if you want to attach a profiler or run the VM in a linux VM via docker-machine rather than "native" docker. This works fine on Linux and appears to relate to the network setup on Mac.  Things I've tried unsuccessfully:
     * Tried https://stackoverflow.com/questions/35108868/how-do-i-attach-visualvm-to-a-simple-java-process-running-in-a-docker-container
         - Including hostname 127.0.0.1 for the `rmi.server.hostname` and 'jenkins'
     * Tried https://stackoverflow.com/questions/41267305/docker-for-mac-vm-ip 
         - Running this on Mac `soc TCP-LISTEN:9011,reuseaddr,fork,bind=localhost UNIX-CONNECT:/var/run/docker.sock`
         - with the container using:
        `-Djava.rmi.server.hostname=jenkins` and `--network host --add-host=jenkins:127.0.0.1` and BOTH port 9011 exposed and not exposed
         - with `--network scalability-bridge` and the port 9011 exposed or not from container
     * Launch Jenkins, replacing the docker bridge network with entries like  `--network host --add-host=jenkins:127.0.0.1` (for Jenkins, others will need it too)

# Limitations

* Doesn't automate the configuration (currently it requires manually tickling some files and folders)
* Requires a Debian-based Jenkins Docker image (not Alpine) due to Telegraf installation - perhaps fixable with direct binary install via [the releases](https://portal.influxdata.com/downloads)
* Requires fairly modern Jenkins core and plugins to run

# Special Uses

## Run a non-containerized Jenkins and the rest in containers, for profiler use
1. Launch swarm agents specially so they can speak to your local Jenkins: 

```shell
docker run --rm -d \
  -l role=agent  \
  -e "COMMAND_OPTIONS=-master http://$HOSTNAME:8080 -executors 1 -description swarm-slave" \
  temp-buildagent:1.0
```
2. Revise git server container URLs to point to localhost:2222 rather than gitserver:22  -- just on Jenkins within the Pipeline shared libraries (configure Jenkins) + Multibranch Project config
3. On Jenkins global configuration, change the Graphite server to point at your computer's hostname rather than 'influx' (the ports will map correctly)

# Custom Plugin Code Locations
Besides the Load Generator logic (currently a branch off of the Random Job Builder plugin), we use a small "Scalability Info Plugin" to provide some additional metrics including Flownodes-per-second for Pipeline and Load Generator Task Counts.  

Source for this currently lives on Github, at [https://github.com/cloudbees/scalability-info-plugin](https://github.com/cloudbees/scalability-info-plugin).