/*
I want to use a string parameter for the job, to set the size of 
the array of junk I'm creating. Why doesn't this work?
*/

int fileSize = Integer.valueOf(params.setRandomFileSize);

stage ("Write file then stash it") {
    node {
        // echo "--> setRandomFileSize is: $fileSize"
        // Integer.valueOf(paramName)
        echo "--> int fileSize is: $fileSize"
        // int fileSize = ${params.setRandomFileSize};
        sh 'mkdir -p stashedStuffDirectory' 
        sh "cat /dev/urandom | env LC_CTYPE=c tr -dc \'[:alpha:]\' | head -c $fileSize  > stashedStuff/1Bcharacters"
        stash name: "stashedFile1", includes: "stashedStuff/*"
        sh "pwd"
        sh "ls -alh ${pwd()}/"
    }
}