/*
lots-of-logging-to-disk.groovy

Does lots of logging to disk.
*/ 
for (int i = 0; i < 10; i++) {

    stage ("Stage $i") {
        // 100 looked like it was gonna be WAY too many times
        // to run this lol
        for (int j = 0; j < 10; j++) {
	        // Some shell logging.
            node {
                echo "--> Stage $i, Step $j"
                echo "--> Run vmstat -a"
                sh 'vmstat -a'
                echo "--> Run lsof"
                sh 'lsof'
            }
        }
    }
}