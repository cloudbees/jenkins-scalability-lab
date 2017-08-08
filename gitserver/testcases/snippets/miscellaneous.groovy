// First we'll generate a text file in a subdirectory on one node and stash it.
stage ("Write file then stash it") {

node('first-node') {
    // Make the output directory.
    sh "mkdir -p output"
    // Let's write a bunch of junk to it. Same shell step as seen in 
    // lots-of-logging-to-disk.
    sh 'cat /dev/urandom | env LC_CTYPE=c tr -dc \'[:alpha:]\' | fold -w 320 | head -n 1 > output/320chars'

    stash name: "stashedFile", includes: "output/320chars"
    }
}

// Next, we'll make a new directory on a second node, and unstash the original
// into that new directory, rather than into the root of the build.
stage "second step on second node"

// Run on a node with the "second-node" label.
node('second-node') {
    // Run the unstash from within that directory!
    dir("first-stash") {
        unstash "first-stash"
    }

    // Look, no output directory under the root!
    // pwd() outputs the current directory Pipeline is running in.
    sh "ls -la ${pwd()}"

    // And look, output directory is there under first-stash!
    sh "ls -la ${pwd()}/first-stash"
}