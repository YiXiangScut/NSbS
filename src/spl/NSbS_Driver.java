/**Search-based diverse sampling from software product lines
 * 
 * Author:  Yi Xiang <xiangyi@scut.edu.cn> or <gzhuxiang_yi@163.com>
 *  
 * Reference: 
 *  
 * Yi Xiang, Han Huang, Yuren Zhou, Sizhe Li, Chuan Luo, Qingwei Lin, Miqing Li 
 * and Xiaowei Yang, Search-based Diverse Sampling from Real-world Software Product Lines. 
 * In Proceedings of The 44th International Conference on Software Engineering (ICSE 2022).
 * ACM, New York, NY, USA, 2022
 * 
 * Data: 12/01/2021
 * Copyright (c) 2021 Yi Xiang
 * 
 * Note: This is a free software developed based on the open 
 * source projects PLEDGE <https://github.com/christopherhenard/pledge> 
 * and jMetal<http://jmetal.sourceforge.net>. The copy right of PLEDGE 
 * belongs to  its original author, Christopher Henard, and the copy 
 * right of jMetal belongs to Antonio J. Nebro and Juan J. Durillo. 
 * Nevertheless, this current version can be redistributed and/or 
 * modified under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of 
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package spl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetal.encodings.variable.Binary;
import jmetal.qualityIndicator.util.MetricsUtil;
import jmetal.util.PseudoRandom;

import org.apache.commons.math3.util.ArithmeticUtils;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.orders.NegativeLiteralSelectionStrategy;
import org.sat4j.minisat.orders.PositiveLiteralSelectionStrategy;
import org.sat4j.minisat.orders.RandomLiteralSelectionStrategy;
import org.sat4j.minisat.orders.RandomWalkDecorator;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.reader.DimacsReader;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;

import spl.fm.Product;
import spl.fm.TSet;
import spl.techniques.DistancesUtil;
import spl.techniques.RandomTechnique;
import spl.techniques.SimilarityTechnique;
import spl.techniques.ga.GA;
import spl.techniques.ns.Individual;
import spl.techniques.ns.NoveltySearch1plusN;
import spl.techniques.pdda.PDDrivenAlgorithm;
import spl.utils.FMToZ3;
import spl.utils.FileUtils;
import splar.core.constraints.CNFClause;
import splar.core.constraints.CNFFormula;
import splar.core.fm.FeatureModel;
import splar.core.fm.XMLFeatureModel;
import splar.core.heuristics.FTPreOrderSortedECTraversalHeuristic;
import splar.core.heuristics.VariableOrderingHeuristic;
import splar.core.heuristics.VariableOrderingHeuristicsManager;
import splar.plugins.reasoners.bdd.javabdd.FMReasoningWithBDD;
import splar.plugins.reasoners.bdd.javabdd.ReasoningWithBDD;
import splar.plugins.reasoners.sat.sat4j.FMReasoningWithSAT;
import splar.plugins.reasoners.sat.sat4j.ReasoningWithSAT;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

public class NSbS_Driver {

    private static Random randomGenerator = new Random();
    private FeatureModel fm;
    private ReasoningWithSAT reasonerSAT;
    private ISolver solverIterator, dimacsSolver;
    private ProbSATLocalSearch repairSolver;
    
    private  IOrder order = new RandomWalkDecorator(new VarOrderHeap(new RandomLiteralSelectionStrategy()), 1);
  
    private static NSbS_Driver instance = null;
    private final int SATtimeout = 1000;
    private final long iteratorTimeout = 150000;
    private boolean dimacs;
    private String dimacsFile;
    private boolean predictable;
    public String path;

    private List<List<Integer>> featureModelConstraints;
    private int nConstraints; // how many constraints
    public int numFeatures; // how many features
    
    public static List<Integer> mandatoryFeaturesIndices;
	public static List<Integer> deadFeaturesIndices;
    public static List<Integer> featureIndicesAllowedFlip;
    
	public FMToZ3 ftz;
	
	public static List<Integer> featuresList = new ArrayList<Integer>();
	public static List<Integer> freeFeaturesList = new ArrayList<Integer>(); //free features
    public static Map<Integer, String> featuresMap = new HashMap<Integer, String>(); // ID
     Map<String, Integer> featuresMapRev = new HashMap<String, Integer>(); // name 
 	 Set<TSet> validTSets;
 	
    protected NSbS_Driver() {

    }

    public static NSbS_Driver getInstance() {
        if (instance == null) {
            instance = new NSbS_Driver();
        }
        return instance;
    }
      
           
    public static void main(String[] args) throws Exception {
//      SPL.getInstance().parseArgs(args);
  	String [] fms = {
              // -------------Specify feature models-------------
                 "lrzip",
//				 "LLVM",
//				 "X264",
//				 "Dune",
//				 "BerkeleyDBC",
//				 "HiPAcc",
//				 "JHipster",
//				 "Polly",
//				 "7z",
//				 "JavaGC",
//				 "VP9",				 				 
//				 "fiasco_17_10",
//				 "axtls_2_1_4",
//				 "fiasco",
//				 "toybox",
//				 "axTLS",
//				 "uClibc-ng_1_0_29",
//				 "toybox_0_7_5",
//				 "uClinux",
//				 "ref4955",
//				 "adderII",
//				 "ecos-icse11",
//				 "m5272c3",
//				 "pati",
//				 "olpce2294",
//				 "integrator_arm9",
//				 "at91sam7sek",
//				 "se77x9",
//				 "phycore229x",
//				 "busybox-1.18.0",
//				 "busybox_1_28_0",
//				 "embtoolkit",
//				 "freebsd-icse11",
//				 "uClinux-config",
//  			 "buildroot",
//				 "freetz",
//				 "2.6.28.6-icse11",
//				 "2.6.32-2var",
//				 "2.6.33.3-2var"
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
  	
  	
  }
    /**
     * 
     * @param fmFile
     * @param outputDir
     * @param runs
     * @param dimacs
     * @param nbPairs
     * @param t
     * @param nbProds
     * @param timeAllowed
     * @throws Exception
     */
    public void findProductsNSDiverse(String fmFile, String outputDir, int runs, boolean dimacs,  int nbProds, 
    		long timeAllowed, long remainTimeAllowed, int k) throws Exception {

    	 String sNbProds = "" + nbProds;
         String fmFileName = new File(fmFile).getName();

         System.out.println("Start findProductsNSDiverse...");        

         SimilarityTechnique st = new SimilarityTechnique(SimilarityTechnique.JACCARD_DISTANCE, SimilarityTechnique.NEAR_OPTIMAL_SEARCH);
       
         double p = 1.0; // repair prob (default value is 1)
         
         this.path = outputDir + "NS" + "k=" + Integer.toString(k) + "AutoT/" + fmFileName +"/"  + sNbProds + "prods/";
         String path = outputDir + "NS" + "k=" + Integer.toString(k) + "AutoT/" + fmFileName +"/" + sNbProds + "prods/";
                  
	 /**  Give a different name to the path       
		//         this.path = outputDir + "NS" + "k=" + Integer.toString(k) + "FixedTimeNoWeight/" + fmFileName +"/"  + sNbProds + "prods/";
		//         String path = outputDir + "NS" + "k=" + Integer.toString(k) + "FixedTimeNoWeight/" + fmFileName +"/" + sNbProds + "prods/";
		    
		//       this.path = outputDir + "NS" + "k=" + Integer.toString(k) + "FixedTime/" + fmFileName +"/"  + sNbProds + "prods/";
		//       String path = outputDir + "NS" + "k=" + Integer.toString(k) + "FixedTime/" + fmFileName +"/" + sNbProds + "prods/";
		   
		//         this.path = outputDir + "NS" + "k=" + Integer.toString(k) + "FixedTimeNoDelta/" + fmFileName +"/"  + sNbProds + "prods/";
		//         String path = outputDir + "NS" + "k=" + Integer.toString(k) + "FixedTimeNoDelta/" + fmFileName +"/" + sNbProds + "prods/";
	*/         
                  
         
         FileUtils.CheckDir(path); 
         
         for (int i = 0; i < runs; i++) {
           System.out.println("run " + (i + 1));
         	
             List<Product> products = null; 
                     
             
             long startTimeMS = System.currentTimeMillis() ;
            
             NoveltySearch1plusN ns = new NoveltySearch1plusN(nbProds,k,0.0,remainTimeAllowed,p,i);             
                     
             products = ns.runSimpleNS(outputDir,fmFileName,i);

             long endTimeMS = System.currentTimeMillis() - startTimeMS;                    
            
             double fitness = st.noveltyScoreSum(products, k);       
             System.out.println("fitness = " + fitness);
             //Save products           
             writeProductsToFile(path + "Products." + i, products);
             //Save fitness
             writeDataToFile(path + "Fitness." + i, fitness);// write Fitness, i.e., novelty score             
             writeDataToFile(path + "RUNTIME." + i, endTimeMS);// write RUNTIME
         } // for runs      

    }
    
    
    /**
     * 
     * @param fmFile
     * @param outputDir
     * @param runs
     * @param dimacs
     * @param nbPairs
     * @param t
     * @param nbProds
     * @param timeAllowed
     * @throws Exception
     */
    public void findProductsUnPredictable(String fmFile, String outputDir, int runs, boolean dimacs,  
    		int nbProds, int k) throws Exception {

    	 String sNbProds = "" + nbProds;
         String fmFileName = new File(fmFile).getName();

         System.out.println("Start findProductsUnPredictable...");        

         SimilarityTechnique st = new SimilarityTechnique(SimilarityTechnique.JACCARD_DISTANCE, SimilarityTechnique.NEAR_OPTIMAL_SEARCH);
               
         String path = outputDir + "UnPredictable/" + fmFileName +"/" + sNbProds + "prods/";

         FileUtils.CheckDir(path); 
         
         for (int i = 0; i < runs; i++) {
           System.out.println("run " + (i + 1));
         	
             List<Product> products = null; 
                                     
             long startTimeMS = System.currentTimeMillis() ;
                
             products = NSbS_Driver.getInstance().getUnpredictableProducts(nbProds);

             long endTimeMS = System.currentTimeMillis() - startTimeMS;
                    
             /*
              * 璁＄畻閫傚簲搴﹀�
              */
             double fitness = st.noveltyScoreSum(products, k);       
             System.out.println("fitness = " + fitness);
             //Save products           
             writeProductsToFile(path + "Products." + i, products);
             writeDataToFile(path + "Fitness." + i, fitness);// write Fitness, i.e., novelty score             
             writeDataToFile(path + "RUNTIME." + i, endTimeMS);// write RUNTIME
         } // for runs      

    }
         
       
   
    /**
     * Count the number of files in a dir
     * @param path
     * @return
     */
    public int getFileNum(String path) {
    	int num = 0;
		File file = new File(path);
		if (file.exists()) {
			File[] f = file.listFiles();
			for (File fs : f) {
				if (fs.isFile()) {
//					System.out.println(fs.getName());
					num++;
				} 
//				else {
//					num = num + getFileNum(fs.getAbsolutePath());
//				} 
			}
		}
 
		return num;
	}
               
    
    public IVecInt productToInt(Product p) {
        IVecInt prod = new VecInt(p.size() - 1);
        List<Integer> productList = GA.productToList(p);
        int j = 0;
        for (Integer n : productList) {
            if (j++ <= p.size() / 2) {
                prod.push(n);
            }

        }
        return prod;
    }

       

    public List<Product> getUnpredictableProducts(int count) throws Exception {
        List<Product> products = new ArrayList<Product>(count);

        while (products.size() < count) {
        	
        	Product product = getOneRandomProduct();
        	
            if (!products.contains(product)) {
                products.add(product);
            }    
                   
        }
        
        return products;
    }

   
    
    public  Product  getOneRandomProduct() throws Exception {
		// Generate a random binary to ensure that all features are considered
		Binary randomBinary = new Binary(numFeatures); 
		
	    for (Integer f : mandatoryFeaturesIndices) { 
	    	randomBinary.setIth(f, true);               	
	     }
	
	     for (Integer f : deadFeaturesIndices) {
	    	 randomBinary.setIth(f, false);    
	     }
	                  
        if (solverIterator.isSatisfiable()) {
        	int rand = (new Random()).nextInt(3);
        	IOrder order;
             if (rand == 0) {
                 order = new RandomWalkDecorator(new VarOrderHeap(new NegativeLiteralSelectionStrategy()), 1);
             } else if (rand == 1) {
                 order = new RandomWalkDecorator(new VarOrderHeap(new PositiveLiteralSelectionStrategy()), 1);
             } else {
                 order = new RandomWalkDecorator(new VarOrderHeap(new RandomLiteralSelectionStrategy()), 1);
             }
             
        	((Solver) solverIterator).setOrder(order); 
	
            int[] partialProd = solverIterator.findModel(); // partialProd contains only variables in CNF constraints
             
            for (int j = 0; j < partialProd.length; j++) {
                int feat = partialProd[j];
                
                int posFeat = feat > 0 ? feat : -feat;

                if (posFeat > 0) {
                	randomBinary.setIth(posFeat - 1,feat > 0);
                }
            }// for
            
        } else {//if
        	System.out.println("solverIterator is not satisfiable()");
        }   
        
        Product product  = toProduct(randomBinary);
  
        return product;            
   }
	       
            
   
    
   public int selectedFeature (Product product) {
	   int selected = 0;
    	
    	for (Integer i : product) {
    		if (i>0) selected++;
    	}
    	return selected;
    	
   }
	      
    public int numViolatedConstraints(Binary b, HashSet<Integer> blacklist) {

        //IVecInt v = bitSetToVecInt(b);
    	List<List<Integer>> constraints =  featureModelConstraints;

        int s = 0;
        for (List<Integer> constraint : constraints) {
            boolean sat = false;

            for (Integer i : constraint) {
                int abs = (i < 0) ? -i : i;
                boolean sign = i > 0;
                if (b.getIth(abs - 1) == sign) {
                    sat = true;
                } else {
                    blacklist.add(abs);
                }
            }
            if (!sat) {
                s++;
            }

        }

        return s;
    }
                  
    

    /**
     * 灏哖roduct杞崲鎴怋inary
     * @param vector
     * @return
     */
    public Binary Product2Bin(Product prod) {

    	Binary bin = new Binary(prod.size());    	    
        
        for (Integer i:prod) {
        	
        	if (i > 0){
        		bin.setIth(i-1, true);
        	} else {
        		bin.setIth(Math.abs(i)-1, false);
        	}
        		
        } // for i
        return bin;
    }
    
    /**
     * Get random products using Random + Repair
     * @param count
     * @return
     * @throws Exception
     */
    public Product repairProducts(Product prod, double p) throws Exception {
              
        	Product product = null;
        	
        	if (randomGenerator.nextDouble() < p) {
	        	 	             
	            Binary randomBinary =  Product2Bin(prod) ;  	            
	            
	            for (Integer f : this.mandatoryFeaturesIndices) {
	            	randomBinary.setIth(f, true);               	
	             }

	             for (Integer f : this.deadFeaturesIndices) {
	            	 randomBinary.setIth(f, false);    
	             }
	             
	        	Binary repaired = (Binary) repairSolver.execute(randomBinary);     // ProbSAT	             
	            
	            product = toProduct(repaired);   

	            if (!isValidProduct(product)) {
	            	product = getOneRandomProduct();   // SAT4J
	            }
	            
        	} else {
	        	if (solverIterator.isSatisfiable()) {
	        		product = getOneRandomProduct(); 
	        	}
        	}
        	
                             
     
        return product;
    }

    
    public Product getUnpredictableProduct(Product startProduct) throws Exception {
        Product product = null;
        while (product == null) {
            try {
                if (solverIterator.isSatisfiable()) {
//                	int rand = (new Random()).nextInt(3);
//                	IOrder order;
//                     if (rand == 0) {
//                         order = new RandomWalkDecorator(new VarOrderHeap(new NegativeLiteralSelectionStrategy()), 1);
//                     } else if (rand == 1) {
//                         order = new RandomWalkDecorator(new VarOrderHeap(new PositiveLiteralSelectionStrategy()), 1);
//                     } else {
//                         order = new RandomWalkDecorator(new VarOrderHeap(new RandomLiteralSelectionStrategy()), 1);
//                     }
//                     
//                	((Solver) solverIterator).setOrder(order);                	         
//                	System.out.println("# Selected featues before " + this.selectedFeature(startProduct));
//                    product = toProduct(solverIterator.findModel(productToIntVec(startProduct)));    
                    product = toProduct(solverIterator.findModel());     
//                    System.out.println("# Selected featues after " + this.selectedFeature(product));
                    
                } else {
                	System.out.println("reinitialize solvers in getUnpredictableProduct()");
                    if (!dimacs) {
                        reasonerSAT.init();
                        if (!predictable) {
                            ((Solver) reasonerSAT.getSolver()).setOrder(order);
                        }
                        solverIterator = new ModelIterator(reasonerSAT.getSolver());
                        solverIterator.setTimeoutMs(iteratorTimeout);
                    } else {
                        dimacsSolver = SolverFactory.instance().createSolverByName("MiniSAT");
                        dimacsSolver.setTimeout(SATtimeout);
                        DimacsReader dr = new DimacsReader(dimacsSolver);
                        dr.parseInstance(new FileReader(dimacsFile));
                        if (!predictable) {
                            ((Solver) dimacsSolver).setOrder(order);
                        }
                        solverIterator = new ModelIterator(dimacsSolver);
                        solverIterator.setTimeoutMs(iteratorTimeout);
                    }
                }
            } catch (TimeoutException e) {
            }
        }
        return product;
    }

   
    /**
     * 鑾峰緱鈥滃彲棰勬祴鈥濈殑涓�粍娴嬭瘯闆�     * @param count
     * @param numberOfFeatures
     * @return
     * @throws Exception
     */
    public List<Product> getPredictableProducts(int count, int numberOfFeatures) throws Exception {
        List<Product> products = new ArrayList<Product>(count);
        while (products.size() < count) {
            try {
                if (solverIterator.isSatisfiable()) {
                    Product product = toProduct(solverIterator.model());
                    if (randomGenerator.nextInt(numberOfFeatures) == numberOfFeatures - 1) {

                        if (!products.contains(product)) {
                            products.add(product);
                        }
                    }
                } else {
                    if (!dimacs) {
                        reasonerSAT.init();
                        if (!predictable) {
                            ((Solver) reasonerSAT.getSolver()).setOrder(order);
                        }
                        solverIterator = new ModelIterator(reasonerSAT.getSolver());
                        solverIterator.setTimeoutMs(iteratorTimeout);
                    } else {
                        dimacsSolver = SolverFactory.instance().createSolverByName("MiniSAT");
                        dimacsSolver.setTimeout(SATtimeout);
                        DimacsReader dr = new DimacsReader(dimacsSolver);
                        dr.parseInstance(new FileReader(dimacsFile));
                        if (!predictable) {
                            ((Solver) dimacsSolver).setOrder(order);
                        }
                        solverIterator = new ModelIterator(dimacsSolver);
                        solverIterator.setTimeoutMs(iteratorTimeout);
                    }
                }

            } catch (TimeoutException e) {
            }
        }
        return products;
    }
    

  
    /**
     * 灏唒roducts鍐欏叆鏂囦欢
     * @param outFile
     * @param products
     * @throws Exception
     */
    public void writeProductsToFile(String outFile, List<Product> products) throws Exception {

      FileUtils.resetFile(outFile);
      
      BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
          
//      out.write(products.size() + " products");
//      out.newLine();
      
      for (Product product : products) {
          List<Integer> prodFeaturesList = new ArrayList<Integer>(product);
          Collections.sort(prodFeaturesList, new Comparator<Integer>() {

              @Override
              public int compare(Integer o1, Integer o2) {
                  return ((Integer) Math.abs(o1)).compareTo(((Integer) Math.abs(o2)));
              }
          });

          int done = 1;
          for (Integer feature : prodFeaturesList) {
              out.write(Integer.toString(feature));   
              if (done < numFeatures) {
                  out.write(";");
              }
              done++;
          }

          out.newLine();
      }
      out.close();
  }
    
   
    
    /**
     * 浠庢枃浠惰鍙杙roducts
     * @param outFile
     * @param products
     * @throws Exception
     */
    public List<Product> loadProductsFromFile(String inFile) throws Exception {
    	List<Product> products = new  ArrayList <Product>();
    	
        BufferedReader in = new BufferedReader(new FileReader(inFile));
        String line;
       
        while ((line = in.readLine()) != null && !(line.isEmpty())) {
           
        	StringTokenizer tokenizer = new StringTokenizer(line, ";");
            Product product = new Product();     
            
            while (tokenizer.hasMoreTokens()) {
                String tok = tokenizer.nextToken().trim();
                product.add(Integer.parseInt(tok));
            }
             
            products.add(product);
          
        }       
        
        in.close();
        
    	return products;
   
  }
    
    /**
     * 灏唒roducts鍐欏叆鏂囦欢
     * @param outFile
     * @param products
     * @throws Exception
     */
    public void writeDataToFile(String outFile, double data) throws Exception {

      FileUtils.resetFile(outFile);
      
      BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
          
      out.write(Double.toString(data));
      
      out.close();

  }
    
    /**
     * 灏唒roducts鍐欏叆鏂囦欢
     * @param outFile
     * @param products
     * @throws Exception
     */
    public void writeDataToFile(String outFile, List <Double> data) throws Exception {

      FileUtils.resetFile(outFile);
      
      BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
          
      int done = 0;
      
      for (int i = 0; i < data.size();i++) {
    	  out.write(Double.toString(data.get(i)));
    	  done++;
    	  
    	  if(done < data.size()) {
    		  out.newLine();
    	  }
      }
            
      out.close();
  }
    
    /**
     * 灏唒roducts鍐欏叆鏂囦欢
     * @param outFile
     * @param products
     * @throws Exception
     */
    public void writeDataToFile(String outFile, long data) throws Exception {

      FileUtils.resetFile(outFile);
      
      BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
          
      out.write(Long.toString(data));
      
      out.close();

  }
    
    public boolean isValidProduct(Product product, Map<Integer, String> featuresMap, List<Integer> featuresList) throws Exception {
        IVecInt prod = new VecInt(product.size());

        for (Integer s : product) {
        	if (!dimacs) {
	            if (s < 0) {
	                prod.push(-reasonerSAT.getVariableIndex(featuresMap.get(featuresList.get((-s) - 1))));
	            } else {
	                prod.push(reasonerSAT.getVariableIndex(featuresMap.get(featuresList.get(s - 1))));
	            }
        	} else {
        		 prod.push(s);
        	}
        }
        if (!dimacs) {
        	return reasonerSAT.getSolver().isSatisfiable(prod);
        } else {
        	return dimacsSolver.isSatisfiable(prod);
        }
    }

    public boolean isValidProduct(Product product) throws Exception {
        IVecInt prod = new VecInt(product.size());

        for (Integer s : product) {        	
        		 prod.push(s);        	
        }
        
        if (!dimacs) {
        	return reasonerSAT.getSolver().isSatisfiable(prod);
        } else {
        	return dimacsSolver.isSatisfiable(prod);
        }
    }
    
   
   
    
    public void computeFeatures(ReasoningWithSAT reasonerSAT, Map<Integer, String> featuresMap, Map<String, Integer> featuresMapRev, List<Integer> featuresList, boolean dimacs, String dimacsFile) throws Exception {

        if (!dimacs) {
            String[] features = reasonerSAT.getVarIndex2NameMap(); // 鐗瑰緛鍚嶏紝瀛楃涓�
            for (int i = 0; i < features.length; i++) {
                String feature = features[i];
                int n = i + 1;
                featuresList.add(n); // ID
                featuresMap.put(n, feature);
                featuresMapRev.put(feature, n);
            }


        } else {
            BufferedReader in = new BufferedReader(new FileReader(dimacsFile));
            String line;
            int n = 0;
            while ((line = in.readLine()) != null && (line.startsWith("c")||line.startsWith("p"))) {
               
            	if (line.startsWith("c")) {
            		 StringTokenizer st = new StringTokenizer(line.trim(), " ");
            		 st.nextToken();
                     n++;
                     String sFeature = st.nextToken().replace('$', ' ').trim(); // 鏈変簺鐗瑰緛ID鍚庨潰鍔犱簡$锛岃〃鏄庤鐗瑰緛鍚嶆槸绯荤粺浜х敓鐨�                    
                     int feature = Integer.parseInt(sFeature);
//                     if (n != feature) { // ID 瑕佹寜鐓т粠灏忓埌澶х殑椤哄簭鎺掑垪
//                         throw new Exception("Incorrect dimacs file, missing feature number " + n + " ?");
//                     }
                     String featureName = st.nextToken();
                     featuresList.add(feature);
                     featuresMap.put(feature, featureName);
                     featuresMapRev.put(featureName, feature);
            	}
            	
            	if (line.startsWith("p")) {
            		 StringTokenizer st = new StringTokenizer(line.trim(), " ");
            		 st.nextToken(); st.nextToken();
            		 numFeatures = Integer.parseInt(st.nextToken());
//            		 System.out.println("numFeatures in computeFeatures " + numFeatures);
            	}
               
            } // while             
            
            in.close();
            
            for (int i = 1;i <= numFeatures;i++) {
            	if (!featuresList.contains(i)) { // 
            		  featuresList.add(i);
                      featuresMap.put(i, Integer.toString(i));
                      featuresMapRev.put(Integer.toString(i), i);
            	}
            }
            
        }

        int n = 1;
        int featuresCount = featuresList.size();
        while (n <= featuresCount) {
            featuresList.add(-n); // 鎶婅礋ID涔熷姞鍏eatureList
            n++;
        }

    }

    /**
     * load constraints 
     * @param reasonerSAT
     * @param featuresMap
     * @param featuresMapRev
     * @param featuresList
     * @param dimacs
     * @param dimacsFile
     * @throws Exception
     */
    public void computeConstraints(ReasoningWithSAT reasonerSAT, boolean dimacs, String dimacsFile) 
    		throws Exception {
    	
//    	  if (!dimacs) {
//              fm = loadFeatureModel(fmFile);
//              fm.loadModel();
//              reasonerSAT = new FMReasoningWithSAT("MiniSAT", fm, SATtimeout);
//              reasonerSAT.init();
//          } else {
//              dimacsSolver = SolverFactory.instance().createSolverByName("MiniSAT");
//              dimacsSolver.setTimeout(SATtimeout);
//
//              DimacsReader dr = new DimacsReader(dimacsSolver);
//              dr.parseInstance(new FileReader(fmFile));
//          }
    	  
    	  
    	if (!dimacs) {   	      

         CNFFormula formula = fm.FM2CNF();
         nConstraints = formula.getClauses().size();
         
         featureModelConstraints = new ArrayList<List<Integer>>(nConstraints);
                  
         
         for (CNFClause clause : formula.getClauses()) {
        	
        	 // 姣忎釜瀛楀彞瀵瑰簲涓�釜 List<Integer> 
             List<Integer> constraint = new ArrayList<Integer>(clause.getLiterals().size());
             
             for (int i = 0; i < clause.getLiterals().size(); i++) { // 瀛愬彞鐨勬瘡涓枃瀛�            	            	 
                 int signal = clause.getLiterals().get(i).isPositive() ? 1 : -1;
                 int varID = reasonerSAT.getVariableIndex(clause.getLiterals().get(i).getVariable().getID());
       
                 if (signal < 0) {
                	 constraint.add(- varID);
                 } else {
                	 constraint.add(varID);
                 }
             } // for i
             
             featureModelConstraints.add(constraint);
         }
         
    	} else { // dimacs褰㈠紡锛屽垯浠庢枃浠惰鍙栫害鏉�        	 
        	 BufferedReader in = new BufferedReader(new FileReader(dimacsFile));
             String line;

             while ((line = in.readLine()) != null) {
                 if (line.startsWith("p")) {
                     StringTokenizer st = new StringTokenizer(line.trim(), " ");
                     st.nextToken();
                     st.nextToken();
                     st.nextToken();
                     nConstraints = Integer.parseInt(st.nextToken());
                     break;

                 }
             }
             
             in.close();

             featureModelConstraints = new ArrayList<List<Integer>>(nConstraints);
             
             //Initialize free features
             for (int i = 1;i <= numFeatures;i++) {
             	if (!freeFeaturesList.contains(i)) { 
             		freeFeaturesList.add(i);
             	}
             }             
             
             // -------------------------------------------------------------
             in = new BufferedReader(new FileReader(dimacsFile));
         
             while ((line = in.readLine()) != null) {
                 if (!line.startsWith("c") && !line.startsWith("p") && !line.isEmpty()) {
                	 List<Integer> constraint = new ArrayList<Integer>();
                     StringTokenizer st = new StringTokenizer(line.trim(), " ");

                     while (st.hasMoreTokens()) {
                         int f = Integer.parseInt(st.nextToken());

                         if (f != 0)  constraint.add(f);  
                         
                         if (f != 0) {
                        	 if (freeFeaturesList.contains(Math.abs(f))) {                        		 
                        		 freeFeaturesList.remove(freeFeaturesList.indexOf(Math.abs(f)));//Remove from free features
                        	 }
                         }// IF
                     }  
                     
                     featureModelConstraints.add(constraint);  
                 } // if  
                 
             } // while            
             in.close();
             
             //-----------------print 
//             for (int i = 0; i < featureModelConstraints.size();i++) {
//            	 for (int j = 0;j < featureModelConstraints.get(i).size();j++)  {
//            		 System.out.print(featureModelConstraints.get(i).get(j) + " ");
//            	 }
//            	 System.out.println();
//             }
             
//             System.out.println("Num of free Features = " + freeFeaturesList.size());

//        	 for (int j = 0;j < freeFeaturesList.size();j++)  {
//        		 System.out.print(freeFeaturesList.get(j) + " ");
//        	 }             
    	}     
             
    } //
           
   

    /**
     * 灏唙ector杞崲鎴恜roduct锛岀洿鎺ector鐨勫厓绱犵洿鎺ュ姞鍏roduct闆嗗悎鍗冲彲
     * @param vector
     * @return
     */
    public Product toProduct(int[] vector) {

        Product product = new Product();
        for (int i : vector) {
            product.add(i);
        }
        return product;
    }

    /**
     * 灏唒roduct杞崲鎴怚VecInt
     * @param vector
     * @return
     */
    public IVecInt productToIntVec(Product product) {

    	 IVecInt iv = new VecInt();

         for (Integer j: product) {            
             iv.push(j);   
         }
         
        return iv;
    }
    
    /**
     * Binary to product
     * @param vector
     * @return
     */
    public Product toProduct(Binary bin) {

        Product product = new Product();
        
        for (int i = 0; i < bin.getNumberOfBits();i++) {
        	
        	if (bin.getIth(i) == true){
        		product.add(i + 1);
        	} else {
        		product.add(-(i + 1));
        	}
        		
        } // for i
        return product;
    }
    
       
    
    /**
     * Load mandatory and dead indices from files
     * @param mandatory
     * @param dead
     * @throws Exception
     */
    public void loadMandatoryDeadFeaturesIndices(String mandatory, String dead) throws Exception {

        mandatoryFeaturesIndices = new ArrayList<Integer>(numFeatures);
        deadFeaturesIndices = new ArrayList<Integer>(numFeatures);
        featureIndicesAllowedFlip = new ArrayList<Integer>(numFeatures);               

        File file = new File(mandatory);   
        
        if (file.exists()) {      
        
	        BufferedReader in = new BufferedReader(new FileReader(mandatory));
	        String line;
	        while ((line = in.readLine()) != null) {
	            if (!line.isEmpty()) {
	                int i = Integer.parseInt(line) - 1;
	                mandatoryFeaturesIndices.add(i);	               
	            }
	
	        }
	        
	        in.close();
        } 
        
        file = new File(dead);   
        
        if (file.exists()) {           	
        
	        BufferedReader  in = new BufferedReader(new FileReader(dead));
	        String line;
	        while ((line = in.readLine()) != null) {
	            if (!line.isEmpty()) {
	                int i = Integer.parseInt(line) - 1;
	                deadFeaturesIndices.add(i);	        
	            }
	
	        }
	        
	        in.close();
        } // if 
        
         for (int i = 0; i < numFeatures; i++) {
            if (! mandatoryFeaturesIndices.contains(i) && !deadFeaturesIndices.contains(i))
                featureIndicesAllowedFlip.add(i);
            
        }

         System.out.println("mandatoryFeaturesIndices.size() = " + mandatoryFeaturesIndices.size());
         System.out.println("deadFeaturesIndices.size() = " + deadFeaturesIndices.size());
//         System.out.println("featureIndicesAllowedFlip.size() = " + featureIndicesAllowedFlip.size());
         
    }
         
            
     
       

    /**
     * 鍒濆鍖栨ā鍨嬪強姹傝В鍣�     
     * @param fmFile
     * @param nbPairs
     * @param t
     * @throws Exception
     */
    public void initializeModelSolvers(String fmFile) throws Exception {
 
        if (!new File(fmFile).exists()) {
            throw new ParameterException("The specified FM file does not exist");
        }

        this.predictable = false;// use unpredictable way to generate new configurations
        this.dimacs = true; // FM is in dimacs format
        this.dimacsFile = fmFile;// file to FM
        
        //System.out.println("--------------Initialize solvers------------");
        /**
         * ---------------------Initialize SAT4J------------------------------
         */
        dimacsSolver = SolverFactory.instance().createSolverByName("MiniSAT");
        dimacsSolver.setTimeout(SATtimeout);
        DimacsReader dr = new DimacsReader(dimacsSolver);
        dr.parseInstance(new FileReader(fmFile));
        
    	if (!predictable) {
    		((Solver) dimacsSolver).setOrder(order);
    	}
    	
        solverIterator = new ModelIterator(dimacsSolver);// 
        solverIterator =  dimacsSolver; 
        solverIterator.setTimeoutMs(iteratorTimeout);//
        // ---------------------Initialize SAT4J end------------------------------
         
      
        /**
         * ---------------------Compute core and dead features--------------------
         */
        featuresList   = new ArrayList<Integer>();
        featuresMap    = new HashMap<Integer, String>(); // ID 
        featuresMapRev = new HashMap<String, Integer>(); // name 
      
        computeFeatures(null, featuresMap, featuresMapRev, featuresList, true, fmFile);
        computeConstraints(null, true, fmFile);               
        
        System.out.println("numFeatures"  + numFeatures);
        System.out.println("numConstraints"  + nConstraints);
        
        //Load mandatory and dead feature indices
        loadMandatoryDeadFeaturesIndices(fmFile+".mandatory", fmFile+".dead");
     
        
        /**
         * Initialize probSAT solver
         */
        HashMap  localSearchParameters ;     
        localSearchParameters = new HashMap() ;
        localSearchParameters.put("constraints",featureModelConstraints); //featureModelConstraints 鍦╟omputeConstraints涓鍙栦簡
        localSearchParameters.put("num_vars",numFeatures); 
        localSearchParameters.put("max_flips",4000);
        localSearchParameters.put("wp", 0.567);

        repairSolver = new ProbSATLocalSearch(localSearchParameters);// ProbSAT
        
        /**
         *Z3 solver*/    
//		ftz = new FMToZ3();
//		ftz.parseDimacs(new FileReader(fmFile));
//		ftz.parseMandatory(new FileReader(fmFile+".mandatory"));
//		ftz.parseDead(new FileReader(fmFile+".dead"));
//		ftz.parseAugment();      
 
        System.out.println("-------------initializeModelSolvers end-------------");
    } // end of class                  
 
}
