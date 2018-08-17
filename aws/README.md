# Instance setup

* Baseline: Amazon Linux 2 LTS Candidate AMI 2017.12.0
* Util host (Git + Influx) - t2.large, ~10GB volume
    - Only really need 10 GB, 2 cores part of the time and some IO
    - Uses only a few GB of disk space
* Jenkins basic - c5.xlarge (4 core, 8 GB RAM)
    - Volume determines storage perf
* Swarm workers - probably need public IPs, terminate on stop

#Setup:

* 'sudo yum install -y docker && sudo service docker start && sudo su' 
* Then run image(s)

# Nasty manual AWS setup

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
7. SCP gitserver SSH keys to hydra-util, and copy to /tmp/keys
