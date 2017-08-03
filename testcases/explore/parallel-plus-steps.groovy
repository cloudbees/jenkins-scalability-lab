/*
parallel-plus-steps

Runs a bunch of echos in a parallelized series of...things.
*/

int branchCount = 15;
int stepsPerBranch = 30;

parallelSet = [:]

for (int i = 0; i < branchCount; i++) {
	echo "--> Creating ParallelSet[$i]"
	parallelSet["Branch $i"] = {
		for (int j = 0; j < stepsPerBranch; j++) {
			echo "--> Branch $i contains:"
			echo "    --> Echo j $j"
		}
	}
}
parallel parallelSet