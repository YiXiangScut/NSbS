# NSbS：Search-based diverse sampling from real-world software product lines
# 1. The entry of this project is the main function of src/spl/NSbS_Driver/main.


public static void main(String[] args) throws Exception {

	String [] fms = {
       // -------------Specify feature models-------------
       "lrzip",
	};

	String outputDir = "./output/"; // Output file
	int runs = 1; // How many runs
	boolean dimacs = true;
	int nbProds = 100; // How many cases in each sample	
	long timeAllowed = 0; 	
	int k = 15; // Parameter k used in novelty score   	
	String fmFile = null;

	for (int i = 0;i < fms.length;i++) {

		fmFile = "./FM_NS/" + fms[i] + ".dimacs"; 
		timeAllowed = 3600 * 1000; // timeout = 1 hour

		System.out.println(fmFile);

		long startTimeMS = System.currentTimeMillis() ;   
		NSbS_Driver.getInstance().initializeModelSolvers(fmFile); // Initialize solvers
		long endTimeMS = System.currentTimeMillis();

		// Used when automatic termination. The time of initialization is counted
		long remainTimeAllowed = (endTimeMS - startTimeMS); 

		System.out.println("Maximum time allowed " + timeAllowed);   	

		// Call NS to perform diverse sampling
		NSbS_Driver.getInstance().findProductsNSDiverse(fmFile, outputDir, runs, dimacs, nbProds, timeAllowed, remainTimeAllowed,k);	

	}    	
  } // main


# 2. The following is some paramters to be set by users.
	
	String outputDir = "./output/"; // Output file
	int runs = 1; // How many runs
	boolean dimacs = true;
	int nbProds = 100; // How many cases in each sample
	long timeAllowed = 0; 
	int k = 15; // Parameter k used in novelty score 
