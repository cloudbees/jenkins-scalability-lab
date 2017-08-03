/*
merge-sort.groovy

Merges the two integer arrays and then sorts them.

At the moment this triggers a couple scriptApproval 
things.

TODO: Make this take an argument for the number of 
elements in the two arrays.
*/

properties [[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', daysToKeepStr: '-1', numToKeepStr: '100']], [$class: 'ScannerJobProperty', doNotScan: false]]

int[] firstThing = generateRandomArray(1000);
int[] secondThing = generateRandomArray(1000);

// Call merge on the two lists.
merge (firstThing, secondThing);

// merge method.
int[] merge(int[] first, int[] second) {
	int[] output = new int[first.length + second.length];
	int idx = 0;
	int i=0;
	int j=0;

	while (i < first.length && j < second.length) {
		int v1 = first[i];
		int v2 = second[j];
		if (v1 > v2) {
			output[idx++] = v1; 
			i++;
		} else {
			output[idx++] = v2;
			j++;
		}
	}

	if ( i < first.length) {
		for (int temp = i; i < first.length; i++) {
			output[idx++] = first[i];
		}
	} else if (j < second.length) {
		for (int temp = j; j < second.length; j++) {
			output[idx++] = second[j];
		}
	}
    else {
        throw new IllegalStateException("Array length of i (" + i + ") and/or j (" + j + ") are all jacked up.");
    }
	Integer[] finallySorted = new Integer[output.length];
	finallySorted = output;
	finallySorted.sort();

	// Comment out this stage if you don't want a whole bunch of echos.
	stage ("Show me the list") {
	    node ("agent-4") {
	        echo "Here's your list:"
			int k = 0;
			while (k < finallySorted.length) {
				// Apparently with an integer array, if you want to 
				// iterate through each element, that must be outside 
				// the double quotes for the echo. Otherwise we just 
				// spit out the entire array `k` times.
				echo " --> " + finallySorted[k];
				k++;
			}
	    }
	}
    return finallySorted;
}

// Generate a random array of integers
int[] generateRandomArray(int arrayLength){
	int[] jumble = new int[arrayLength];
	Random random = new Random();

	for (int i = 0; i < arrayLength; i++) {
		jumble[i] = random.nextInt();
	}
	return jumble;
}
