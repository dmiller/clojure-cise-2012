package dmk;

public class Bench {

    public static void main(String[] args) {
	
	if (args.length != 3) {
	    System.out.println("Usage: java dmk.Bench <#Warmups> <#Iters> <#Points>");
	    return;
	}

	timeIt(Integer.parseInt(args[0]),Integer.parseInt(args[1]),Integer.parseInt(args[2]));	
    }

    public static void timeIt(int numWarmups, int numIters, int numPoints)
    {
	double[] rhos = new double[numPoints];
	double[] arr = new double[numPoints];

	for ( int i=0; i<numWarmups; i++ )
	    Solver.stepSliceInPlace(arr,rhos,0,numPoints,0.0,0.0);

	long t1 = System.nanoTime();

	for ( int i=0; i<numIters; i++ )
	    Solver.stepSliceInPlace(arr,rhos,0,numPoints,0.0,0.0);

	long t2 = System.nanoTime();
	reportTime(t1,t2);

    }

    static void reportTime(long t1, long t2)
    {
	System.out.println("Started " + t1);
	System.out.println("Done    " + t2);
	System.out.println("Diff    " + (t2-t1) + " nanos");
	System.out.println("Diff " + ((t2-t1)*1E-6) + " millis");
    }

}