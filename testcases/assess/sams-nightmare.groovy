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

// Variable instead of hard coded filename
// string stashFileName = "ourFilename";
// string stashName = "ourStashName";

stage ("20 echos") {
    for (int i = 0; i < 20; i++) {
        echo "Echo number $i"
    }
}

// Here's a stash step to run on agent-1.
stage ("Write file then stash it") {
    node ("agent-1") {
        // Make the output directory.
        sh "mkdir -p stashedStuff"
        // Let's write a bunch of junk to it.
        //   - 100K: ~100KB
        //   - 100M: ~100MB
        //   - 100B: ~1GB
        sh 'cat /dev/urandom | env LC_CTYPE=c tr -dc \'[:alpha:]\' | fold -w 100 | head -n 1 > stashedStuff/100Kcharacters'
        // sh 'cat /dev/urandom | env LC_CTYPE=c tr -dc \'[:alpha:]\' | fold -w 100000 | head -n 1 > stashedStuff/\$ourFilename'
        // sh 'cat /dev/urandom | env LC_CTYPE=c tr -dc \'[:alpha:]\' | fold -w 100000000 | head -n 1 > stashedStuff/100Mcharacters'
        // sh 'cat /dev/urandom | env LC_CTYPE=c tr -dc \'[:alpha:]\' | fold -w 1000000000 | head -n 1 > stashedStuff/1Bcharacters'
        stash name: "stashedFile1", includes: "stashedStuff/*"
        // stash name: $ourStashName, inclues: $ourFilename
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
            // echo "What about my log?"
            // sh 'if [ -f /var/log/jenkins/jenkins.log ]; then echo "LOG FOUND"; cat /var/log/jenkins/jenkins.log | grep -i exception; else echo "NO LOG FOUND"; echo "SECOND MESSAGE"; fi'
        }
    }
}

// Unstash the stuff from previously onto agent-2.
stage ("Unstash to agent-2") {
    // I want to run this on agent-2 if possible. But if agent-2 isn't there,
    // can I run it someplace else? Like tell pipeline "try for agent-2 but if 
    // he's not there, just pick anything"
    node('agent-2') {
        // Example says to cd to the filename, which is stashedFile1.
        // I think this needs to be the directory name, which is 'stashedStuff'
        // Or maybe a different directory altogether?
        echo "--> Step is dir(stashedFile1)"
        dir("stashedFile1") {
            echo "--> unstash stashedFile"
            unstash "stashedFile1"
        }

        // Look, no stashedStuff directory under the root!
        // pwd() displays the current directory Pipeline is running in.
        echo "--> ls on PWD"
        sh "ls -alh ${pwd()}"

        // And look, stashedStuff directory is there under stashedFile1!
        echo "--> ls on PWD/stashedFile1"
        sh "ls -alh ${pwd()}/stashedFile1"
    }
    echo "I wonder if that worked."
}
