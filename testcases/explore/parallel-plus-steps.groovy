/*
parallel-plus-steps

Runs a bunch of echos in a parallelized series of...things.
*/

parallelSet = [:]

for (int i=0; i<100; i++) {
	echo "--> Creating ParallelSet[$i]"
	parallelSet["Branch $i"] = {
		for (int j=0; j < 10; j++) {
			echo "--> Branch $i contains:"
			echo "    --> Echo j $j"
		}
	}
}
parallel parallelSet