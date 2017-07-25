/*
lots-of-logging-to-disk.groovy

Does lots of logging to disk.

Hammers durable-task, logging via remoting, 

*/ 
for (int i = 0; i < 5; i++) {

    stage ("Stage $i") {
        // 100 looked like it was gonna be WAY too many times
        // to run this lol
        for (int j = 0; j < 5; j++) {
	        // ----
            // So here, we'll put a bash for loop which 
            // runs netstat 100 times, or netstat/vmstat, or
            // some command that's verbose.
            // Some shell logging.
            // Work on a shell step that generates its own 
            // amount of test data for logging, so that it's 
            // always fixed.
            node {
                // echo "--> Stage $i, Step $j"
                sh 'for i in `seq 1 100`; do echo "running loop $i"; netstat -a; done'
                // here's where I'll put the 100 calls to something 
                // verbose thing in the form of a single bash command
                // sh 'netstat -a'
                // echo "--> Run lsof"
                
            }
        }
    }
}