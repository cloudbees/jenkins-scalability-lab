/*
lots-of-logging-to-disk.groovy

Does lots of logging to disk.

Hammers durable-task, logging via remoting, 

*/ 
for (int i = 0; i < 5; i++) {

    stage ("Stage $i") {
        for (int j = 0; j < 5; j++) {
	        // TODO: 
            // Work on a shell step that generates its own 
            // amount of test data for logging, so that it's 
            // always a fixed amount of logging. More consistent
            // that way.
            node {
                sh 'for i in `seq 1 100`; do echo "running loop $i"; netstat -a; done'
            }
        }
    }
}