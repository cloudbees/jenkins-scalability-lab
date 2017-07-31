/*
sams-nightmare.groovy

This is the "enterprise-y" pipeline. Which is to say, when 
people do exotic , complex, or unexpected things from within 
a pipeline, it's often large enterprises that do so. They 
necessarily have complex processes for build, archive, test,
deploy, and report steps.

Here's what we want:

- Both CPS and non-CPS Groovy, to invoke steps and do processing, dynamically,
- Reading and writing lg. files as inputs to steps
- Running mix of shell (fast), shell (long) steps
- Lots of echos, maybe half the total number of steps in the pipeline
- >=1 parallel, doing "some stuff"
- Stashing/unstashing large files
- Archive artifacts
- XML parsing?

*/

stage ("20 echos") {
    for (int i = 0; i < 20; i++) {
        echo "Echo number $i"
    }
}

// Here's a stash step.
stage ("Write file then stash it") {
    node {
        // Make the output directory.
        sh "mkdir -p stashedStuff"
        // Let's write a bunch of junk to it.
        //   - 100K: ~100KB
        //   - 100M: ~100MB
        //   - 100B: ~1GB
        sh 'cat /dev/urandom | env LC_CTYPE=c tr -dc \'[:alpha:]\' | fold -w 100000 | head -n 1 > stashedStuff/100Kcharacters'
        // sh 'cat /dev/urandom | env LC_CTYPE=c tr -dc \'[:alpha:]\' | fold -w 100000000 | head -n 1 > stashedStuff/100Mcharacters'
        // sh 'cat /dev/urandom | env LC_CTYPE=c tr -dc \'[:alpha:]\' | fold -w 1000000000 | head -n 1 > stashedStuff/1Bcharacters'
        stash name: "stashedFile1", includes: "stashedStuff/*"
    }
}

// Parallel section
parallelStages = [:]

for (int i = 0; i < 20; i++) {
	echo "--> Creating ParallelStages[$i]"
    parallelStages["Branch $i"] = {
        stage ("Parallel stage $i") {
            for (int j=0; j < 10; j++) {
                echo "\t --> Echo i $i"
                echo "\t --> Echo j $j"
            }
        }
	}
}
parallel parallelStages

// Let's put in a hodgepodge of shell steps.
for (int i = 0; i < 3; i++) {
    node {
        stage ("Shell Hodgepodge $i") {
            echo "env?"
            sh "env"
            echo "set?"
            sh "set"
            echo "What about my log?"
            /* sh 'if [ -f /var/log/jenkins/jenkins.log ] then \
                echo "Found our log"; \
                cat /var/log/jenkins/jenkins.log | grep xception; \
                else echo "No log to see here."; \
                fi' */
            sh 'if [ -f /var/log/jenkins/jenkins.log ]; then echo "LOG FOUND"; echo "SECOND MESSAGE"; else echo "NO LOG FOUND"; echo "SECOND MESSAGE"; fi'
        }
    }
}
