FROM jenkins/jenkins

MAINTAINER kshultz

# Install telegraf for monitoring && set up telegraf as sudoer so they can launch telegraf as root for extra disk stats
# Also you need openjdk-8-dbg/stable to get the debugging symbols for heap dumps
USER root
RUN apt-get update && apt-get install -y openjdk-8-dbg \
    && apt-get clean && rm -rf /var/lib/apt/lists/* /var/tmp/* \
    && echo 'telegraf ALL=(root) NOPASSWD:ALL' >> /etc/sudoers 
RUN curl -o /tmp/telegraf.deb https://dl.influxdata.com/telegraf/releases/telegraf_1.3.5-1_amd64.deb \
    && dpkg -i /tmp/telegraf.deb && rm -f telegraf.deb
USER jenkins
COPY --chown=jenkins:jenkins telegraf.conf /etc/telegraf/telegraf.conf

# Make tini launch helper script that invokes telegraf as well as Jenkins
COPY --chown=jenkins:jenkins full-start.sh /usr/local/bin/full-start.sh
ENTRYPOINT ["/sbin/tini", "--", "/usr/local/bin/full-start.sh"]

# Plugin installs: custom plugins, test framework plugins + custom snapshots 
# Lock files write is a workaround for a bug in plugin install file where it tries to remove nonexistent lock
# Basic config - install custom and test framework plugins + custom snapshots
COPY --chown=jenkins:jenkins plugins.txt /usr/share/jenkins/ref/plugins.txt
COPY --chown=jenkins:jenkins minimal-plugins.txt /usr/share/jenkins/ref/minimal-plugins.txt
RUN mkdir -p /usr/share/jenkins/ref/plugins \
    && echo 'placeholder' > /usr/share/jenkins/ref/plugins/placeholder.lock \
    && /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt
RUN echo 'placeholder' > /usr/share/jenkins/ref/plugins/placeholder.lock \
    && /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/minimal-plugins.txt
COPY --chown=jenkins:jenkins CUSTOM-PLUGINS/*.*pi /usr/share/jenkins/ref/plugins/

# Remove stale executor files and rename HPI files to JPI files to input custom versions
RUN rm -f /usr/share/jenkins/ref/init.groovy.d/executors.groovy \
    && cd /usr/share/jenkins/ref/plugins/ \
    && for custom_plugin in $(ls *.hpi); do mv "$custom_plugin" "${custom_plugin%.hpi}.jpi"; done
COPY --chown=jenkins:jenkins loadtestsetup.groovy /usr/share/jenkins/ref/init.groovy.d/loadtestsetup.groovy

# prevent install wizard running, but tied to this version
COPY --chown=jenkins:jenkins jenkins.* /usr/share/jenkins/ref/

# Below makes graphite metrics report more often and adds support for attaching a remote debugger
# Becomes JENKINS_JAVA_OPTIONS in later Docker image versions
ENV JAVA_OPTS "-Dgraphite.metrics.intervalSeconds=10 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9011 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false \
-Xloggc:/tmp/gc-%t.log -XX:NumberOfGCLogFiles=5 -XX:+UseGCLogFileRotation -XX:GCLogFileSize=20m -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintHeapAtGC -XX:+PrintGCCause -XX:+PrintTenuringDistribution -XX:+PrintReferenceGC -XX:+PrintAdaptiveSizePolicy \
-server -XX:+AlwaysPreTouch -XX:+UseG1GC -XX:+ExplicitGCInvokesConcurrent -XX:+ParallelRefProcEnabled -XX:+UseStringDeduplication -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20"

# Note: need to get hostname of the VM for cases where we're on Mac
# Note that we can't disable the setup wizard with: -Dhudson.Main.development=true -Djenkins.install.runSetupWizard=false -Djenkins.install.UpgradeWizard.show=false

# Let's try copying a maven config over.
COPY --chown=jenkins:jenkins jenkins.mvn.GlobalMavenConfig.xml /usr/share/jenkins/ref/
COPY --chown=jenkins:jenkins hudson.tasks.Maven.xml /usr/share/jenkins/ref/

USER jenkins
