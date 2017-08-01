import jenkins.model.*
import jenkins.install.InstallState
import hudson.security.SecurityRealm
Jenkins.instance.setNumExecutors(0);
Jenkins.instance.setSecurityRealm(SecurityRealm.NO_AUTHENTICATION);

// Solves issues with host URL for Swarm agent plugin
JenkinsLocationConfiguration.get().setUrl("http://jenkins:8080/")