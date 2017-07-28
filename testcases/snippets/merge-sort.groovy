/*
merge-sort.groovy

Oy.
*/

int[] firstThing = [1, 2, 3]
int[] secondThing = [4, 5, 6]


System.out.println(firstThing);
System.out.println(secondThing);

merge (firstThing, secondThing);

int[] merge(int[] first, int[] second) {
    System.out.println("--> first is:  " + first);
    System.out.println("--> second is: " + second);
	int[] output = new int[first.length + second.length];
    System.out.println("--> output[] should be uninit'd: " + output + " with length of " + output.length);
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
            System.out.println("--> in merge list output is now: " + output );
		} else {
			output[idx++] = v2;
			j++;
            System.out.println("--> in merge list output is now: " + output );
		}
	}
    // i=250;
    // j=250;

	if ( i < first.length) {
		for (int temp = i; i < first.length; i++) {
			output[idx++] = first[i];
            System.out.println("\t--> in merge list output is now: " + output );
		}
	} else if (j < second.length) {
		for (int temp = j; j < second.length; j++) {
			output[idx++] = second[j];
            System.out.println("\t--> in merge list output is now: " + output );
		}
	}
    else {
        throw new IllegalStateException("Array length of i (" + i + ") and/or j (" + j + ") are all jacked up.");
    }
	output.sort();
	Integer[] finallySorted = new Integer[output.length];
	finallySorted.sort();
	System.out.println("--> Final answer is " + finallySorted);
    return output;
}

/* int[] sortMePlease (int[] vals) {
	if (vals.length <= 1) {
		// If we only have one element in the array, then 
		// there's nothing to do. Return true and bail.
		return vals;
	}

	vals.sort();
	return vals;

}*/

