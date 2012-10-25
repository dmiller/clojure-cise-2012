package dmk;
public class Solver {
    
    

    static double nextValue(double rho, double left, double right) {
	return 0.5 * (left + right + rho);
    }

    public static void stepSliceInPlace(double[] arr, double[] rhos, int startIdx, int endIdx, double lval, double rval)
    {
	int lastIdx = endIdx-1;
	double oldVal = lval;
	for ( int i = startIdx; i<=lastIdx; i++) {
	    if ( i == lastIdx )
		arr[lastIdx] = nextValue(rhos[lastIdx],oldVal,rval);
	    else {
		double curVal = arr[i];
		arr[i] = nextValue(rhos[i],oldVal,arr[i+1]);
		oldVal = curVal;
	    }
	}
    }

}