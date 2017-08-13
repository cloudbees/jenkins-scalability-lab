import hudson.ExtensionList;
import hudson.security.ACL;
import jenkins.model.*
import jenkins.install.InstallState
import hudson.security.SecurityRealm

// To enable setting up SSH access to gitserver 
import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;

// Used to set up a multibranch project for testcases
import hudson.scm.SCM;
import jenkins.branch.BranchSource;
import hudson.plugins.git.GitSCM;
import jenkins.plugins.git.GitSCMSource;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

// Reporting to InfluxDB via Graphite interface
import jenkins.metrics.impl.graphite.GraphiteServer;

// Shared libs setup
import org.jenkinsci.plugins.workflow.libs.*;

Jenkins.instance.setNumExecutors(0);
Jenkins.instance.setSecurityRealm(SecurityRealm.NO_AUTHENTICATION);

// Solves issues with host URL for Swarm agent plugin, but generates harmless "broken reverse proxy" warnings
JenkinsLocationConfiguration.get().setUrl("http://jenkins:8080/")

// Create an SSH credential to communicate with the gitserver ssh://git@gitserver/git-server/repos/testcases.git
SystemCredentialsProvider.ProviderImpl system = ExtensionList.lookup(CredentialsProvider.class).get(SystemCredentialsProvider.ProviderImpl.class);
CredentialsStore systemStore = system.getStore(Jenkins.instance);
if (CredentialsProvider.lookupCredentials(BasicSSHUserPrivateKey.class, ACL.SYSTEM).size() == 0) {
    // We check before creating just to ensure this is idempotent
    BasicSSHUserPrivateKey sshCred = new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL, "git-ssh", "git", new BasicSSHUserPrivateKey.FileOnMasterPrivateKeySource('/var/jenkins_home/id_rsa'), "", "SSH key to communicate with temporary gitserver");
    systemStore.addCredentials(Domain.global(), sshCred);
}

// Autoconfigure the graphite metrics reporter to speak to the InfluxDB container
ArrayList<GraphiteServer> influx = (List<GraphiteServer>)(Arrays.asList(new GraphiteServer("influx", 2015, null)))
Jenkins.instance.getExtensionList(GraphiteServer.DescriptorImpl.class).get(0).setServers(influx);


// Create a multibranch project for testcases
// Note that this will automatically build the branch jobs, since we don't add a PropertyStrategy with NoTriggerBranchProperty
// But that's okay, it ensures somme basic things are set up for the jobs
WorkflowMultiBranchProject proj = Jenkins.instance.getItemByFullName("testcases", WorkflowMultiBranchProject.class);
if (proj == null) {
    // We check before creating & configuring just to ensure this is idempotent and doesn't add excess SCMSources
    proj = Jenkins.instance.createProject(WorkflowMultiBranchProject.class, "testcases");  
    GitSCMSource scm = new GitSCMSource("gitserver", "ssh://git@gitserver/git-server/repos/testcases.git", "git-ssh", "*", "", false);
    proj.getSourcesList().add(new BranchSource(scm));
}

/** Adds a shared-libs bound to the gitserver shared-libs folder, with library name 'sort-lib' (for mergesort use)
    To use, run: `library 'sort-lib' ` in your pipeline
*/
LibraryConfiguration sharedLib = new LibraryConfiguration(
    'sort-lib',
     new SCMSourceRetriever(new GitSCMSource("gitserver", "ssh://git@gitserver/git-server/repos/shared-libs.git", "git-ssh", "*", "", false))
);
sharedLib.setDefaultVersion('master');
GlobalLibraries.get().setLibraries(Arrays.asList(sharedLib));