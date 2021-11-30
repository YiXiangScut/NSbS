/*
 * @author Yi Xiang (xiangyi@scut.edu.cn or gzhuxiang_yi@163.com) 
 * Date: 2020年4月30日
 * Copyright: School of Software Engineering, South China University of Technology
 * All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package spl.problem.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import jmetal.util.Configuration;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.minisat.core.Solver;
import org.sat4j.reader.DimacsReader;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

/**
 *
 * @author Yi Xiang
 * 
 * This class implements 
 */
public class coreAndDeadFeatures {
	 private Solver solver;
	 private int numFeatures;
	 private List<Integer> featuresIntList;
     private List<String> featuresList;
     private Map<String, Integer> namesToFeaturesInt;
     private static final String solverName = "MiniSAT";
     private List<Integer> coreFeaturesID, deadFeaturesID;
	/**
	 * 
	 */
	public coreAndDeadFeatures() {
		// TODO Auto-generated constructor stub
		featuresIntList = new ArrayList<Integer>();
		featuresList = new ArrayList<String>();
		coreFeaturesID = new ArrayList<Integer>();
		deadFeaturesID = new ArrayList<Integer>();
		namesToFeaturesInt = new HashMap<String, Integer>();
	}

	public void execute(String filePath) throws Exception{
		//Load fm
		 ISolver dimacsSolver = SolverFactory.instance().createSolverByName(solverName);
         DimacsReader dr = new DimacsReader(dimacsSolver);
         dr.parseInstance(new FileReader(filePath));
         solver = (Solver) dimacsSolver;
         BufferedReader in = new BufferedReader(new FileReader(filePath));
         String line;
         int n = 0;
         
         while ((line = in.readLine()) != null && (line.startsWith("c")||line.startsWith("p"))) {
            
        	 if (line.startsWith("c")) {
        		 StringTokenizer st = new StringTokenizer(line.trim(), " ");        	
	             st.nextToken();
	             n++;
	             String sFeature = st.nextToken().replace('$', ' ').trim();
	             int feature = Integer.parseInt(sFeature);
	             String featureName = st.nextToken();
	             featuresIntList.add(feature);
	             featuresList.add(featureName);
	             namesToFeaturesInt.put(featureName, feature);
        	 }
             
             if (line.startsWith("p")) {
        		 StringTokenizer st = new StringTokenizer(line.trim(), " ");
        		 st.nextToken(); st.nextToken();
        		 numFeatures = Integer.parseInt(st.nextToken());
        		 System.out.println("numFeatures " + numFeatures);
        	}
             
         }                
         
         in.close();
         
         for (int i = 1;i <= numFeatures;i++) {
         	if (!featuresIntList.contains(i)) { // 
         		featuresIntList.add(i);
         		featuresList.add(Integer.toString(i));
         		namesToFeaturesInt.put(Integer.toString(i), i);
         	}
         }
         
         System.out.println("featuresIntList size = " + featuresIntList.size());
         
         
         IVecInt vector = new VecInt();
         // Core and dead features
         for (String feature : featuresList) {
             int f = namesToFeaturesInt.get(feature);
             vector.clear();
             vector.push(-f);
             if (!solver.isSatisfiable(vector)) {
                 coreFeaturesID.add(f); 
             }

             vector.clear();
             vector.push(f);
             if (!solver.isSatisfiable(vector)) {
                 deadFeaturesID.add(f-1);
             }         
         }
         
	}
	
	/**
     * write mandatory and dead indices from files
     * @param mandatory
     * @param dead
     * @throws Exception
     */
    public void writeMandatoryDeadFeaturesIndices(String mandatory, String dead) throws Exception {

    	try {
    	      /* Open the file */
    	      FileOutputStream fos   = new FileOutputStream(mandatory,false)     ;
    	      OutputStreamWriter osw = new OutputStreamWriter(fos)    ;
    	      BufferedWriter bw      = new BufferedWriter(osw)        ;
    	    
    	      String strToWrite; 
    	      
    	      for(int i = 0;i < coreFeaturesID.size();i ++){    	    	  	    	 
    	    	  strToWrite =  String.valueOf(coreFeaturesID.get(i));    	
    	    	  bw.write(strToWrite);
    	    	  bw.newLine();
    	      }      	     
    	      bw.newLine();    
    	      /* Close the file */
    	      bw.close();
    	    }catch (IOException e) {
    	      Configuration.logger_.severe("Error acceding to the file");
    	      e.printStackTrace();
    	    }//catch

	    	try {
	  	      /* Open the file */
	  	      FileOutputStream fos   = new FileOutputStream(dead,false)     ;
	  	      OutputStreamWriter osw = new OutputStreamWriter(fos)    ;
	  	      BufferedWriter bw      = new BufferedWriter(osw)        ;
	  	    
	  	      String strToWrite; 
	  	      
	  	      for(int i = 0;i < deadFeaturesID.size();i ++){    	    	  	    	 
	  	    	  strToWrite =  String.valueOf(deadFeaturesID.get(i));    	
	  	    	  bw.write(strToWrite);
	  	    	  bw.newLine();
	  	      }      	     
	  	      bw.newLine();    
	  	      /* Close the file */
	  	      bw.close();
	  	    }catch (IOException e) {
	  	      Configuration.logger_.severe("Error acceding to the file");
	  	      e.printStackTrace();
	  	    }//catch
	    	
         System.out.println("mandatoryFeaturesIndices.size() = " + coreFeaturesID.size());
         System.out.println("deadFeaturesIndices.size() = " + deadFeaturesID.size());
         
    }
    
    public static void main(String args[]) throws Exception {
    	String [] fm = {
//				"Cellphone",
//				"CounterStrikeSimpleFeatureModel",
//				"SPLSSimuelESPnP",
//				"DSSample",
//				"ElectronicDrum",
//				"SmartHomev2.2",
//				"VideoPlayer",
//				"ModelTransformation",
//				"CocheEcologico",
//				"Printers",
//				"ecos-icse11",
//				"freebsd-icse11",
//				"2.6.28.6-icse11",	
//				"Amazon",
//				"WebPortal",
//    			"axTLS",
//    			"Drupal",
//				"E-shop",
//				"BerkeleyDB",
//    			"buildroot",
//    			"busybox-1.18.0",
//    			"coreboot",
//    			"fiasco",
//    			"toybox",
//    			"uClinux",
//    			"uClinux-config",
//    			"SPLOT-3CNF-FM-5000-1000-0,30-SAT-1",
//				"SPLOT-3CNF-FM-1000-200-0,50-SAT-1",
//				"SPLOT-3CNF-FM-1000-200-0,50-SAT-2",
//				"SPLOT-3CNF-FM-1000-200-0,50-SAT-3",
//				"SPLOT-3CNF-FM-1000-200-0,50-SAT-4",
//				"SPLOT-3CNF-FM-1000-200-0,50-SAT-5",
//				"SPLOT-3CNF-FM-1000-200-0,50-SAT-6",
//				"SPLOT-3CNF-FM-1000-200-0,50-SAT-7",
//				"SPLOT-3CNF-FM-1000-200-0,50-SAT-8",
//				"SPLOT-3CNF-FM-1000-200-0,50-SAT-9",
//				"SPLOT-3CNF-FM-1000-200-0,50-SAT-10",
//    			"Random-10000",    
//    			"freetz",
//    			"2018-01-14T09_51_25-08_00",
//    			"2017-01-11T13_56_49+00_00",
//    			"2016-01-07T14_11_32+01_00",
//    			"2015-01-06T11_04_29-08_00",
//    			"2014-01-02T15_48_22-08_00",
//    			"2013-11-06T06_39_45+01_00",
//				"Automotive01",
//				"Automotive02_V1",
//				"Automotive02_V2",
//				"Automotive02_V3",
//				"Automotive02_V4",
    			
//    			"test",
//    			"axtls_2_1_4",
				// "toybox_0_7_5",
				// "lrzip",
				// "BerkeleyDB",
				// "7z",
				// "Hipacc",
				// "LLVM",
				// "x264",
				// "Dune",
				// "Polly",
				// "JavaGC",
				// "VP9"，
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
				"buildroot",
				"freetz",
				"2.6.28.6-icse11",
				"2.6.32-2var",
				"2.6.33.3-2var"
    	};
    	
		// String path = "./all_FM/";
		String path = "/home/arjee/Coding/DDBS/DDBS/FM/";
		// String path = "/home/arjee/Desktop/FM/";
    	
		for (int i = 0; i  < fm.length;i++) {
			coreAndDeadFeatures cdf = new coreAndDeadFeatures();
			cdf.execute(path+fm[i]+".dimacs");
			cdf.writeMandatoryDeadFeaturesIndices(path+fm[i]+".dimacs.mandatory", path+fm[i]+".dimacs.dead");
			System.out.println("Done " + fm[i]);
		}
    	
    }
}
