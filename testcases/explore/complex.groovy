/*
complex.groovy

Uses complex algorithms in order to produce CPU stress.

*/

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


