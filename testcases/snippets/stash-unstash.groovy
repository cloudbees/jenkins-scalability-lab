// First we'll generate a text file in a subdirectory on one node and stash it.
stage "first step on first node"

// Run on a node with the "first-node" label.
node('first-node') {
    // Make the stashedStuff directory.
    sh "mkdir -p stashedStuff"

    // Write a text file there.
    writeFile file: "stashedStuff/somefile", text: "Hey look, some text."

    // Stash that directory and file.
    // Note that the includes could be "stashedStuff/", "stashedStuff/*" as below, or even
    // "stashedStuff/**/*" - it all works out basically the same.
    stash name: "stashedFile1", includes: "stashedStuff/*"
}

// Next, we'll make a new directory on a second node, and unstash the original
// into that new directory, rather than into the root of the build.
stage ("Unstash to agent-2") {

    // Run on a node with the "second-node" label.
    node('agent-2') {
        // Example says to cd to the filename, which is stashedFile1.
        // I think this needs to be the directory name, which is 'stashedStuff'
        // Or maybe a different directory altogether?
        dir("stashedFile1") {
            unstash "stashedFile1"
        }

        // Look, no stashedStuff directory under the root!
        // pwd() displays the current directory Pipeline is running in.
        sh "ls -alh ${pwd()}"

        // And look, stashedStuff directory is there under stashedFile1!
        sh "ls -alh ${pwd()}/stashedFile1"
    }
}