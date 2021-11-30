/** AdMOEAStudy.java
 * This is the latest experimental study of AdMOEA 
 * last modified 2017.3.13
 */

package spl;

import java.io.IOException;

import jmetal.core.Algorithm;
import jmetal.experiments.Experiment;
import jmetal.experiments.Settings;
import jmetal.experiments.util.Friedman;
import jmetal.util.JMException;


public class GenerateTablesMain extends Experiment {

  /**
   * Configures the algorithms in each independent run
   * @param problemName The problem to solve
   * @param problemIndex
   * @throws ClassNotFoundException 
   */
  public void algorithmSettings(String problemName, 
  		                          int problemIndex, 
  		                          Algorithm[] algorithm) throws ClassNotFoundException {
    
  } // algorithmSettings

  /**
   * Main method
   * @param args
   * @throws JMException
   * @throws IOException
   */
  public static void main(String[] args) throws JMException, IOException {
    GenerateTablesMain exp = new GenerateTablesMain();

    exp.experimentName_ = "output";   
    exp.nproducts = 100;
      
    exp.algorithmNameList_ = new String[]{	
//   		     "NSk=15AutoT",
//			 "UnPredictable",
//    		 "DDBS", 
//    		 "NSk=15", 	
//    		 "NSk=15FixedTimeNoWeight",
//    		 "NSk=15FixedTimeNoDelta"
//    		 "NSk=15NoWeight", 	
//			 "NSk=15AutoTNoWeight"

//    		 "Smarchxy"
//    		 "unigenxy",
   		     
   		     
   		     // Parameter study 
   		  "NSk=2FixedTime",
   		  "NSk=5FixedTime",
   		  "NSk=10FixedTime",   		
   		  "NSk=15AutoT",
   		  "NSk=20FixedTime",
   		  "NSk=25FixedTime",
   		  "NSk=50FixedTime",
   		  "NSk=75FixedTime",
   		  "NSk=100FixedTime",
    		};
    exp.problemList_ = new String[]{
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
//			 "buildroot",
//			 "freetz",
			 "2.6.28.6-icse11",
//			 "2.6.32-2var",
//			 "2.6.33.3-2var"
    		};

    for (int i = 0;i<exp.problemList_.length;i++) {	
    	 exp.problemList_[i] =  exp.problemList_[i] + ".dimacs"; 	
    }
    	
    exp.indicatorList_ = new String[]{
    		"invertedDist",
    		"Extension",
    		"STD",
//    		"RUNTIME",
//    		"Fitness",
    		};
    
    int numberOfAlgorithms = exp.algorithmNameList_.length;    
      
    exp.experimentBaseDirectory_ = "./" +  exp.experimentName_ ;

    exp.algorithmSettings_ = new Settings[numberOfAlgorithms];

    exp.independentRuns_ = 10;   

    exp.initExperiment();

    // Run the experiments
    int numberOfThreads ;

//    exp.generateQualityIndicators() ;

    // Generate latex tables
    exp.generateLatexTables(false) ;    // generate tables without test symbols
//    exp.generateLatexTables(true) ;    // generate tables with test symbols
  
    // Applying Friedman test
    Friedman test = new Friedman(exp);
//    test.executeTest("EPSILON");
//    test.executeTest("HV");
//    test.executeTest("GSPREAD");
//    test.executeTest("IGD");
//    test.executeTest("RUNTIME");
  } // main
} // AdMOEAStudy


