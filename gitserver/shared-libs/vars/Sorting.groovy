/** Library of mergesort functions to use as a testcase for more complex groovy-based logic
*   Intended to be used to compare & measure performance with CPS and NonCPS logic, plus CPS + steps
*     - CPS will stress the CPS interpretation performance
*     - NonCPS will be used as a baseline for pure Groovy performance
*     - CPS + steps will test CPS performance + serializing the CPS program + any side effects from mixing complex groovy/steps
*
*   Implementation notes:
*      - This is INTENTIONALLY a fairly naive, inefficient and unoptimized implementation
*      - Naive, inefficient implementations are good for testing because they stress raw language constructs
*      - We are using an out-of-place sort (creates lots of arrays here) to ensure that there's lots of program state
*           that has to be written to disk when we serialize program.dat
*      - We do not use the internal Java/groovy sort operations because we want to stress the CPS engine directly
*      - We do not use System.arrayCopy because we want to stress array access and assignment
*      - This is split up into multiple functions so we can mix and match CPS and NonCPS versions + steps
*/

// Generate an array of ints of a specified length.
static int[] generateRandomArrayCPS(int arrayLength){
    int[] jumble = new int[arrayLength];
    Random random = new Random();

    for (int i = 0; i < arrayLength; i++) {
        jumble[i] = random.nextInt();
    }
    return jumble;
}

// Generate an array of ints of a specified length.
@NonCPS
static int[] generateRandomArray(int arrayLength){
    int[] jumble = new int[arrayLength];
    Random random = new Random();

    for (int i = 0; i < arrayLength; i++) {
        jumble[i] = random.nextInt();
    }
    return jumble;
}

/** Merge two already sorted arrays, to create one output array that is overall sorted */
@NonCPS
static int[] merge(int[] first, int[] second) {
    int[] output = new int[first.length + second.length];
    int idx = 0;
    int i=0;
    int j=0;

    // Walk through until we hit end of one array, appending the larger of the two values from current
    // position in each array
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

    // Copy out any residuals from the longer array after we've reached end of its mate
    if ( i < first.length) {
        for (int temp = i; i < first.length; i++) {
            output[idx++] = first[i];
        }
    } else if (j < second.length) {
        for (int temp = j; j < second.length; j++) {
            output[idx++] = second[j];
        }
    }
    return output;
}

/** Merge two already sorted arrays, to create one output array that is overall sorted
*   CPS form of the above
*/
static int[] mergeCPS(int[] first, int[] second) {
    int[] output = new int[first.length + second.length];
    int idx = 0;
    int i=0;
    int j=0;

    // Walk through until we hit end of one array, appending the larger of the two values from current
    // position in each array
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

    // Copy out any residuals from the longer array after we've reached end of its mate
    if ( i < first.length) {
        for (int temp = i; i < first.length; i++) {
            output[idx++] = first[i];
        }
    } else if (j < second.length) {
        for (int temp = j; j < second.length; j++) {
            output[idx++] = second[j];
        }
    }
    return output;
}

static boolean isSortedCPS(int[] array) {
    if (array == null || array.length <= 1) {
        return true;
    }
    int last = array[0];
    for (int i=1; i<array.length; i++) {
        int val = array[i];
        if (val < last) {
            return false;
        }
        last = val;
    } 
    return true;
}

@NonCPS
static boolean isSorted(int[] array) {
    if (array == null || array.length <= 1) {
        return true;
    }
    int last = array[0];
    for (int i=1; i<array.length; i++) {
        int val = array[i];
        if (val < last) {
            return false;
        }
        last = val;
    } 
    return true;
}

/** Sort the array using a (mostly) external mergesort and return sorted result */
@NonCPS
static int[] mergeSort(int[] array) {

    if (isSorted(array)) {
        return array;
    }

    if (array.length == 2) {
        // Guaranteed not to be sorted because we checked already above, so just swap
        int first = array[0];
        array[0] = array[1];
        array[1] = first;
        return array;
    }

    // Split the array in half
    int midpoint = array.length/2;
    int[] firstHalf = new int[midpoint];
    int[] secondHalf = new int[array.length - midpoint];
    int idx =0;
    for (int i = 0; i < midpoint; i++) {
        firstHalf[idx++] = array[i];
    }
    idx=0;
    for (int i = midpoint; i < array.length; i++) {
        secondHalf[idx++] = array[i];
    }

    // Merge 'em for a sorted result'
    return merge(mergeSort(firstHalf), mergeSort(secondHalf));
}

/** Merge sort in pure CPS */
static int[] mergeSortCPS(int[] array) {

    if (isSortedCPS(array)) {
        return array;
    }

    if (array.length == 2) {
        // Guaranteed not to be sorted because we checked already above, so just swap
        int first = array[0];
        array[0] = array[1];
        array[1] = first;
        return array;
    }

    // Split the array in half
    int midpoint = array.length/2;
    int[] firstHalf = new int[midpoint];
    int[] secondHalf = new int[array.length - midpoint];
    int idx =0;
    for (int i = 0; i < midpoint; i++) {
        firstHalf[idx++] = array[i];
    }
    idx=0;
    for (int i = midpoint; i < array.length; i++) {
        secondHalf[idx++] = array[i];
    }

    // Merge 'em for a sorted result'
    return mergeCPS(mergeSortCPS(firstHalf), mergeSortCPS(secondHalf));
}

/** Needed because Script security might otherwise block this */
@NonCPS
static String concatArrayToString(int[] array) {
    return array.join(",")
}
