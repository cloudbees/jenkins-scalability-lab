import jenkins.model.*
import jenkins.install.InstallState
import hudson.security.SecurityRealm

// To enable setting up SSH access to gitserver 
import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;


Jenkins.instance.setNumExecutors(0);
Jenkins.instance.setSecurityRealm(SecurityRealm.NO_AUTHENTICATION);

// Solves issues with host URL for Swarm agent plugin
JenkinsLocationConfiguration.get().setUrl("http://jenkins:8080/")

// Create an SSH credential to communicate with the gitserver ssh://git@gitserver/git-server/repos/testcases.git
SystemCredentialsProvider.ProviderImpl system = ExtensionList.lookup(CredentialsProvider.class).get(SystemCredentialsProvider.ProviderImpl.class);
CredentialsStore systemStore = system.getStore(Jenkins.instance);
BasicSSHUserPrivateKey sshCred = new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL, "git-ssh", "git", new BasicSSHUserPrivateKey.FileOnMasterPrivateKeySource('$JENKINS_HOME/id_rsa'), "", "SSH key to communicate with temporary gitserver");
systemStore.addCredentials(Domain.global(), sshCred);