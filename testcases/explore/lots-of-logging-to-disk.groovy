/*
lots-of-logging-to-disk.groovy

Does lots of logging to disk.

Hammers durable-task, logging via remoting, 

*/ 
for (int i = 0; i < 5; i++) {

    stage ("Stage $i") {
        for (int j = 0; j < 5; j++) {
            node {
                // Basic ls because there's no netstat on the lightweight system we're on
                //   sh 'for i in `seq 1 100`; do echo "running loop $i"; netstat -a; done'
                // This one is trying to restrict to alphanumerics. It also doesn't work.
                //   sh 'for i in `seq 1 100`; do cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 3200 | head -n 1; done'
                // Without the restriction to alphanumeric. This one works but generates gibberish.
                // Might be fine.
                //   sh 'for i in `seq 1 100` do cat /dev/urandom fold -w 500 | head -n 1; done'
                // Sams thing
                sh 'for i in `seq 1 100`; do echo "Stage $i Loop $j"; cat /dev/urandom | env LC_CTYPE=c tr -dc \'[:alpha:]\' | fold -w 320 | head -n 1; done'
            }
        }
    }
}