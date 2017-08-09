/*

Pipeline Testcases:

- Run lots of trivial echos
- Highly branched parallel with echos
- Run complex groovy on its own (merge sort)
- Simple pipeline that is expensive to serialize?
- Mix complex groovy with pipeline steps (merge sort echoing along the way?)
- Complex groovy in NonCPS with pipeline steps?
- Complex groovy in parallel 

Use a sorting algorithm for the complex code

*/

// Simple echo, exercises step logic
for (int i=0; i<100; i++) {
	echo "Ran step $i"
}


// Parallels with trivial step, ideally this runs almost as fast as the raw echo above!
parallelSet = [:]
for (int i=0; i<100; i++) {
	parallelSet["branch $i"] = {
		echo "Running branch $i"
	}
}
parallel parallelSet


// Sorting in a pipeline, exercises raw groovy interpreter performance
int[] sorted(int[] input) {
	if (input == null || input.length <= 1) {
		return input;
	}
	if (isSorted(input)) {
		return input;
	}
	int midPoint = input.length / 2;  // Checkme  ==  0, 1, 2, 3,4   5/2 = 

	int[] sortedFirst = new int[];  // Copy over

	int[] sortedLast = new int[];
	int[] output = new int[input.length];
	return output;
}

// Merge inputs
void merge(int[] first, int[] second) {
	int[] output = new int[first.length + second.length];
	int idx = 0;
	int i=0;
	int j=0;

	// Merge lists
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

	if ( i< first.length) {
		for (int temp = i; i < first.length; i++) {
			output[idx++] = first[i];
		}
	} else if (j < second.length) {
		for (int temp = j; j < second.length; j++) {
			output[idx++] = second[j];
		}
	}
		throw new IllegalStateException("Never happens?");
	}

	return output;
}

boolean isSorted(int[] vals) {
	if (vals.length <= 1) {
		return true;
	}

	int start = vals[0];
	for (int i=1; i<vals.length; i++) {
		int next = vals[i];
		if (next < start) {
			return false;
		} else {
			start = next;
		}
	}
	return true;
}
