/*
lots-of-logging-to-disk.groovy

Does lots of logging to disk.

Hammers durable-task, logging via remoting, 

*/ 
for (int i = 0; i < 5; i++) {

    stage ("Stage $i") {
        for (int j = 0; j < 5; j++) {
            node {
                sh 'for i in `seq 1 100`; do echo "Stage $i Loop $j"; cat /dev/urandom | env LC_CTYPE=c tr -dc \'[:alpha:]\' | fold -w 320 | head -n 1; done'
            }
        }
    }
}