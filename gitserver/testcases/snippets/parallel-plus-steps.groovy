/*
parallel-plus-steps

Runs a bunch of echos in a parallelized series of...things.
*/

parallelStages = [:]

for (int i = 1; i < 21; i++) {
	echo "--> Creating ParallelStages[$i]"
    parallelStages["Branch $i"] = {
        stage ("Parallel stage $i") {
            for (int j=0; j < 10; j++) {
                echo "\t --> Echo i $i"
                echo "\t --> Echo j $j"
            }
        }
	}
}
parallel parallelStages