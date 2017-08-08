/*
parallel-plus-steps-in-stages.groovy

A pipeline which uses parallels to generate a whole bunch o
of disk I/O at once. Within each branch, we'll do ten echo steps,
each of which will spit out something.

This has a bunch of comments in it which are Karl's way of trying to 
figure out how this works on the fly.

I want to make this go to 100, once it, you know, works. 

What I expect to have happen is, we populate parallelSet[] 
with :
	Branch[0]
	Branch[1]
	...and so on

Until the end of the loop. Each branch has inside of it a 
stage, which should be called:
	Stage in Branch 0
	Stage in Branch 1
	... and so on.

So I expect to see Pipeline stages in Stage View, with titles like:
	Stage in Branch 0.

But I'm only getting 
	Stage in Branch <whatever the last element is, in this case 2>
parallelSet = [:]

WTF?
*/

parallelSet = [:]

for (int i=0; i<3; i++) {
	echo "--> CREATE stage"
	// stage ("Stage in Branch $i") {
	echo "    --> Creating ParallelSet[$i]"
	parallelSet["Branch $i"] = {
		stage ("Stage in Branch $i") {
			// Some echos.
			for (int j=0; j < 2; j++) {
				echo "Here goes echo number $j"
			}
		}
	}
}
parallel parallelSet
