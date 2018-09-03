FROM jenkinsci/jenkins:2.121.3

MAINTAINER svanoort

# Install telegraf for monitoring && set up telegraf as sudoer so they can launch telegraf as root for extra disk stats
USER root
RUN apt-get update && apt-get install -y sudo bc \
    && apt-get clean && rm -rf /var/lib/apt/lists/* /var/tmp/* \
    && echo 'telegraf ALL=(root) NOPASSWD:ALL' >> /etc/sudoers 
RUN curl -o /tmp/telegraf.deb https://dl.influxdata.com/telegraf/releases/telegraf_1.3.5-1_amd64.deb \
    && dpkg -i /tmp/telegraf.deb && rm -f telegraf.deb
USER jenkins
COPY telegraf.conf /etc/telegraf/telegraf.conf

########
# Karl WIP:
# I'd like to not have to manually install tools `jdk8` and `mvn` from the Jenkins UI once 
# the container is up. I'd like that to be automatic.
# Download and install Maven
# RUN wget --no-verbose -O /tmp/apache-maven-3.5.0-bin.tar.gz http://archive.apache.org/dist/maven/maven-3/3.5.0/binaries/apache-maven-3.5.0-bin.tar.gz
# Verify the Maven download
# RUN echo "35c39251d2af99b6624d40d801f6ff02 /tmp/apache-maven-3.5.0-bin.tar.gz" | md5sum -c

# Make tini launch helper script that invokes telegraf as well as Jenkins
COPY full-start.sh /usr/local/bin/full-start.sh
ENTRYPOINT ["/bin/tini", "--", "/usr/local/bin/full-start.sh"]

# Plugin installs: custom plugins, test framework plugins + custom snapshots 
# Lock files write is a workaround for a bug in plugin install file where it tries to remove nonexistent lock
# Basic config - install custom and test framework plugins + custom snapshots
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
COPY minimal-plugins.txt /usr/share/jenkins/ref/minimal-plugins.txt
RUN mkdir -p /usr/share/jenkins/ref/plugins \
    && echo 'placeholder' > /usr/share/jenkins/ref/plugins/placeholder.lock \
    && /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt
COPY CUSTOM-PLUGINS/*.*pi /usr/share/jenkins/ref/plugins/

# Remove stale executor files and rename HPI files to JPI files to input custom versions
RUN rm -f /usr/share/jenkins/ref/init.groovy.d/executors.groovy \
    && cd /usr/share/jenkins/ref/plugins/ \
    && for custom_plugin in $(ls *.hpi); do mv "$custom_plugin" "${custom_plugin%.hpi}.jpi"; done
COPY loadtestsetup.groovy /usr/share/jenkins/ref/init.groovy.d/loadtestsetup.groovy

# prevent install wizard running, but tied to this version
COPY jenkins.* /usr/share/jenkins/ref/

# Below makes graphite metrics report more often and adds support for attaching a remote debugger
# Becomes JENKINS_JAVA_OPTIONS in later Docker image versions
ENV JAVA_OPTS "-Dgraphite.metrics.intervalSeconds=10 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9011 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false \
-Xloggc:/tmp/gc-%t.log -XX:NumberOfGCLogFiles=5 -XX:+UseGCLogFileRotation -XX:GCLogFileSize=20m -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintHeapAtGC -XX:+PrintGCCause -XX:+PrintTenuringDistribution -XX:+PrintReferenceGC -XX:+PrintAdaptiveSizePolicy \
-server -XX:+AlwaysPreTouch -XX:+UseG1GC -XX:+ExplicitGCInvokesConcurrent -XX:+ParallelRefProcEnabled -XX:+UseStringDeduplication -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20"

# Note: need to get hostname of the VM for cases where we're on Mac
# Note that we can't disable the setup wizard with: -Dhudson.Main.development=true -Djenkins.install.runSetupWizard=false -Djenkins.install.UpgradeWizard.show=false

# RSA key hack, try to set it up so it doesn't use tmp in the near future
USER root
COPY id_rsa* /usr/share/jenkins/ref/

# Let's try copying a maven config over.
COPY jenkins.mvn.GlobalMavenConfig.xml /usr/share/jenkins/ref/
COPY hudson.tasks.Maven.xml /usr/share/jenkins/ref/

# Chown and chmod files copied in from externally
RUN chown -R jenkins:jenkins /usr/share/jenkins/ref/id_rsa* \
    && chmod 600 usr/share/jenkins/ref/id_rsa*
USER jenkins