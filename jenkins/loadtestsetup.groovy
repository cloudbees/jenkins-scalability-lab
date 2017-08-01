import jenkins.model.*
import jenkins.install.InstallState
import hudson.security.SecurityRealm
Jenkins.instance.setNumExecutors(0);
Jenkins.instance.setSecurityRealm(SecurityRealm.NO_AUTHENTICATION);

// Solves issues with host URL for Swarm agent plugin
JenkinsLocationConfiguration.get().setUrl("http://jenkins:8080/")

/**
Need to create new global credential:
user: git
SSH key from file - $JENKINS_HOME/id_rsa 

For git repo, here's the config:
ssh://git@gitserver:22/git-server/repos/repo-source.git
use that credential

*/