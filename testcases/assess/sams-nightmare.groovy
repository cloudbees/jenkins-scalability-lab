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

// Here's a stash step.
stage ("Write file then stash it") {
    node {
        // Make the output directory.
        sh "mkdir -p stashedStuff"
        // Let's write a bunch of junk to it. Same shell step as seen in 
        // lots-of-logging-to-disk.
        sh 'cat /dev/urandom | env LC_CTYPE=c tr -dc \'[:alpha:]\' | fold -w 5000 | head -n 1 > stashedStuff/5000characters'
        stash name: "stashedFile1", includes: "stashedStuff/5000characters"
    }
}
