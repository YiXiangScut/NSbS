/**
 * Calcualte spread QI, similar to IGD 
 */

package spl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
//import java.util.Set;

import spl.fm.Product;
import spl.fm.TSet;
import spl.techniques.SimilarityTechnique;
import spl.utils.FileUtils;


public class GenerateIndicators {

	public int nbProds = 100;
	public long timeAllowed; 
	public String outputDir;
	public int runs;
	public String algName;
	 
	public GenerateIndicators() {
		
	}
		
	
	public void execute(String fmFile) throws Exception{
		 String sNbProds = "" + nbProds;
         String fmFileName = new File(fmFile).getName();
         SimilarityTechnique st = new SimilarityTechnique(SimilarityTechnique.JACCARD_DISTANCE, SimilarityTechnique.NEAR_OPTIMAL_SEARCH);
                         
         String path = outputDir + algName  + "/" + fmFileName +"/"  + sNbProds + "prods/";
         FileUtils.CheckDir(path); 
                 
         int numFeature = NSbS_Driver.getInstance().numFeatures;        		 
         int lowerBound = (NSbS_Driver.getInstance().mandatoryFeaturesIndices).size();
         int upperBound = numFeature - (NSbS_Driver.getInstance().deadFeaturesIndices).size();
        
         List<Integer> referenceSet = new ArrayList<Integer>();
         
         for (int i = lowerBound; i <= upperBound;i++) {
        	 referenceSet.add(i);        	 
         }
         
         System.out.println("referenceSet " + referenceSet.size());
         System.out.println("lowerBound " + lowerBound);
         System.out.println("upperBound " + upperBound);
         
         for (int r = 0; r < runs; r++) {
//           System.out.println("run " + (i + 1));         	
             List<Product> products = null;    
                          
             // Load products                            
             products = NSbS_Driver.getInstance().loadProductsFromFile(path + "Products." + r);
             int [] selectedNum = new int[products.size()];
                  
             Collections.sort(products); 
       	   	 for (int i = 0; i < products.size();i++) {
       	   	    selectedNum [i] = products.get(i).getSelectedNumber();
//       	   	    System.out.print(selectedNum[i] + " ");
       	   	 }    
                            
       	   	 //For each reference point    
       	   	 double invertedDist = 0.0;
       	   	 
             for (int i = 0; i < referenceSet.size(); i++) {
            	 int pointInRef = referenceSet.get(i);
            	 int minDist = Math.abs(pointInRef - selectedNum[0]);
            	 
            	 for (int j = 1; j < selectedNum.length; j++) {
            		 if (Math.abs(pointInRef - selectedNum[j]) < minDist) {
            			 minDist = Math.abs(pointInRef - selectedNum[j]);
            		 }
            	 }
            	 
            	 invertedDist = invertedDist + minDist;
             }

             invertedDist = invertedDist/referenceSet.size();
                                      
             System.out.print("\ninvertedDist " + invertedDist + "\n");                        
             
             // Calculate extension             
             double dl = selectedNum [0] - lowerBound;
             double du = upperBound - selectedNum [selectedNum.length - 1];
            		             		 
             System.out.println("extension = " + (dl + du)/numFeature);
                          
             // Calculate frequency             
             List <Integer> frequency = new ArrayList<Integer> ();
                         
             int currentNum = selectedNum[0];
             int counter = 1;
             
             for (int i = 1; i < selectedNum.length;i++) {
            	 if (selectedNum[i] == currentNum) {
            		 counter++;
            	 }else {
            		 currentNum = selectedNum[i];
            		 frequency.add(counter);          
            		 counter = 1;
            	 }
             }
             // The last one should be added
             frequency.add(counter);
             
             int sum = 0;
             for (int i = 0;i < frequency.size();i++) {
            	 sum = sum + frequency.get(i);
             }
             
             double freAvg = sum/(double)frequency.size();
               
             //Calculate std
             double std = 0.0;
             
             for (int i = 0;i < frequency.size();i++) {
            	 std = std + (frequency.get(i) - freAvg) * (frequency.get(i) - freAvg);
             }
             
             std = Math.sqrt(std/frequency.size());
             
             System.out.println("std = " + std);
             
//             double fitness = st.noveltyScoreSum(products, 15);
             
             NSbS_Driver.getInstance().writeDataToFile(path + "invertedDist." + r, invertedDist);// write coverage
             NSbS_Driver.getInstance().writeDataToFile(path + "Extension." + r, (dl + du)/numFeature);// write fitness        
             NSbS_Driver.getInstance().writeDataToFile(path + "STD." + r, std);// write fitness        
//             SPL.getInstance().writeDataToFile(path + "Fitness." + r, fitness);// write fitness  
         } // for runs         
        
	}

  /**
   * Main method
   * @param args
 * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    GenerateIndicators gfr = new GenerateIndicators();
    
    String [] fms = {

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

	gfr.nbProds     = 100;
	gfr.outputDir   = "./output/";
	gfr.runs        = 10;
//	gfr.algName     = "UnPredictable";
//	gfr.algName     = "NSk=15";
//	gfr.algName     = "NSk=15NoWeight";
//	gfr.algName     = "DDBS";
//	gfr.algName     = "Smarchxy";
//	gfr.algName     = "unigenxy";
//	gfr.algName     = "NSk=15AutoT";
//	gfr.algName     = "NSk=15AutoTNoWeight";
//	gfr.algName     = "NSk=15FixedTimeNoWeight";
//	gfr.algName     = "NSk=15FixedTimeNoDelta";
	gfr.algName     = "NSk=20FixedTime";
	 
	
	long timeAllowed = 0; 		

	String fmFile = null;	
	for (int i = 0;i < fms.length;i++) {				
		fmFile = "./FM_NS/" + fms[i] + ".dimacs"; 	
			
		System.out.println(fmFile);
		NSbS_Driver.getInstance().initializeModelSolvers(fmFile);
				
		gfr.timeAllowed  = timeAllowed;
		
		gfr.execute(fmFile);		
 
	} // main
  }
} // 


