package jmetal.myutils.datacollection;


public class CollectionDataForTestMain {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws MatlabConnectionException 
	 * @throws MatlabInvocationException 
	 */
	public static void main(String[] args) throws Exception {

		int runs = 30;
		int t = 2;
		int nbProduts = 100;
		long timeMS = 600000;
		
		String expPath = "./output/";
		
		String [] problems= new String[]{	
				
	   		     "lrzip",
				 "LLVM",
				 "X264",
				 "Dune",
				 "BerkeleyDBC",
				 "HiPAcc",
				 "JHipster",
				 "Polly",
				 "7z",
				 "JavaGC",
				 "VP9",	    		
				 "fiasco_17_10",
				 "axtls_2_1_4",
				 "fiasco",
				 "toybox",
				 "axTLS",
				 "uClibc-ng_1_0_29",
				 "toybox_0_7_5",
				 "uClinux",
				 "ref4955",
				 "adderII",
//				 
				 "ecos-icse11",
				 "m5272c3",
				 "pati",
				 "olpce2294",
				 "integrator_arm9",
				 "at91sam7sek",
				 "se77x9",
				 "phycore229x",
				 "busybox-1.18.0",
				 "busybox_1_28_0",
				 "embtoolkit",
				 "freebsd-icse11",
				 "uClinux-config",
				 "buildroot",
				 "freetz",
				 "2.6.28.6-icse11",
				 "2.6.32-2var",
				 "2.6.33.3-2var"
				};
		
		String [] algorithms = new String[]{
	   		     "NSk=15AutoT",
//				 "UnPredictable",
//	   		     "DDBS",
//	    		 "unigenxy",
//	   		     "Smarchxy",
//	   		 	 "NSk=15FixedTimeNoWeight",
	   		     "NSk=15FixedTimeNoDelta",
		};
		
		String [] indicators = new String [] {
	    		"invertedDist",
	    		"Extension",
	    		"STD",
//	    		"RUNTIME",
	    		"Fitness",
		};
		
		
		(new CollectionDataForTest(runs, expPath, problems,algorithms,indicators,t,nbProduts,timeMS)).execute();
	}
	
}
