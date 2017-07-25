/*
steps-and-logging.groovy

Steps, with logging.
*/

for (int i=0; i<3; i++) {
	stage ("Stage in Branch $i") {
		// Some echos.
		for (int j=0; j < 20; j++) {
			// Put in some logging.
            echo "Here goes echo number $j"
		}
	}
}