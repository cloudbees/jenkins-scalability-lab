

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
        System.out.println("--> Are we even getting into this while loop?");
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

	if ( i < first.length) {
		for (int temp = i; i < first.length; i++) {
			output[idx++] = first[i];
            System.out.println("--> in merge list output is now: " + output );
		}
	} else if (j < second.length) {
		for (int temp = j; j < second.length; j++) {
			output[idx++] = second[j];
            System.out.println("--> in merge list output is now: " + output );
		}
	}
		// throw new IllegalStateException("Never happens?");
	// }

	return output;
}
