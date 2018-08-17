Jenkins Scalability Lab in AWS
====

For all kinds of performance tests,
it is recommended to use a Hydra installation in AWS.

# Configuration

The instance consists of 3 VMs:

* Jenkins master
  * Jenkins master port: 8080 (security is disabled)
  * Grafana port: 8081 (admin/admin)
* Utility VM
  * Offers Git server which serves tests and Pipeline libs
  * Also contains InfluxDB needed for Grafana
* Swarm agent host(s)
  * A number of hosts, which provide Jenkins agents for the instance
  * The implementation is based on the Swarm Plugin

# Manual AWS setup

## Instance configuration

* Baseline: Latest Amazon Linux 2
* Util host (Git + Influx) - t2.large, ~10GB volume
** Only really need 10 GB, 2 cores part of the time and some IO
** Uses only a few GB of disk space
* Jenkins basic - c5.xlarge (4 core, 8 GB RAM)
** Volume and script settings determine the storage performance
** It is recommended to have more than 128Gb of storage for the instance
* Swarm workers - probably need public IPs, terminate on stop

## Step 1. Prepare VMs and Configure Hydra

1. Create VPC with IPv4 / IPv6 subnet
2. Create security ACL fo VPC that allows:
  * Incoming: ALL TRAFFIC for the VPC subnet
  * Incoming: ALL TRAFFIC for your personal IP(s)
  * Outgoing to ALL (0.0.0.0/0 and ::/0)
3. Create security group for instances
  * Same as the security ACL
4. Create gateway for VPC to use
5. Add route for gateway
  * Add ::/0 and 0.0.0.0/0 routed to gateway (routes will automatically be created for within the subnet)
6. Launch a hydra-util ec2 instance (t2.medium, security group, in vpc, minimum gp2 volume), and hydra-jenkins (instance of your choice)
  * Will need to get EC2 public IPs for binding to docker containers via --add-host arguments
  * Will need EC2 public IPs for user to access + private IPs for within-vpc communication
7. Launch as many Swarm agent host VMs as needed
8. Save IP addresses of created machines to `aws/settings.sh`
9. Configure other settings as needed
10. Commit changes in the repository and push it to your branch which
    will be used in all further operations.

## Step 2. Deploy Hydra

### Jenkins Master

1. SSH to `hydra-util`, configure Git and Checkout this repository
2. Run the `instance-preconfigure.sh` script
3. Logout and login back
4. Run the `local/build-local.sh` script to build Docker containers
5. Run the `util-containers/launch-util-containers.sh` script
6. Download `id_rsa` and `id_rsa.pub` from `/usr/share/jenkins/ref/` and save them somewhere
   (e.g. in [secrets-store-ops](https://github.com/cloudbees/secrets-store-ops)).
   They will be used to setup Git Server.

### Util Containers

1. SSH to `hydra-util`, configure Git and Checkout this repository
2. Run the `instance-preconfigure.sh` script
3. Logout and login back
4. Run the `util-containers/launch-util-containers.sh` script
5. SCP gitserver SSH keys to hydra-util, and copy to /tmp/keys
6. Ensure that influxdb and gitserver containers are running

### Swarm workers

For each Swarm agent host...

1. SSH to `hydra-util`, configure Git and Checkout this repository
2. Run the `instance-preconfigure.sh` script
3. Logout and login back
4. Run the `util-containers/launch-agent.sh` script

## Step 3. Init test jobs

1. Go to the Jenkins Web UI
2. Go to the `testcases` directory
3. Run branch indexing. A number of folders should be created and start executing jobs
  * If no, gitserver connection is misconfigured
4. Wait till all jobs are exeuted

# Running tests

1. SSH to the Jenkins master VM
2. Install JDK 8 and Maven 3.5.4
3. Go to `runner` and run `mvn clean install`
4. Trigger workload (see README)
5. Run `source aws/settings.sh`
6. Run any test execution script from the `runner` directory
7. Wait...

# Notes

## Updating instances

* Pull the latest changes from the repository
* Run `shutdown.sh` from the root
* Rebuild containers using `local/build-local.sh`
* Restart containers using  `aws/${machineName}/launch-*.sh` scripts
