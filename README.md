# NSbS：Search-based diverse sampling from real-world software product lines
The entry of this project is the main function of src/spl/NSbS_Driver/main.
  	The following is some paramters to be set by users.
	
	String outputDir = "./output/"; // Output file
  	int runs = 1; // How many runs
  	boolean dimacs = true;
  	int nbProds = 100; // How many cases in each sample
    	long timeAllowed = 0; 
    	int k = 15; // Parameter k used in novelty score 
