/**
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
package spl.techniques.ns;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import jmetal.encodings.variable.Binary;
import jmetal.qualityIndicator.util.MetricsUtil;
import jmetal.util.PseudoRandom;
import spl.NSbS_Driver;
import spl.fm.Product;
import spl.techniques.DistancesUtil;
import spl.techniques.ga.Individual;
import spl.utils.FileUtils;

/**
 * 
 * This class implements a novelty search with N archived solutions and one population member
*/
public class NoveltySearch1plusN {
	
	List<Product> archive ; //  
	double[][] distancesMatrix; //
    double [] noveltyScores; 
    
    List<Double> runCoverage ; // record coverage during run
    
	private int archiveSize_; // 
	private int k_; 				// parameter used in novelty score
	private double theta_; 			// novelty score threshold, default 0
	private long timeAllowedMS_;   // 杩愯鏃堕棿
	private double p; // Used in the repair operator
	private int i_;					// 
    private static Random random; //
    boolean matrixInitialized = false; // 
    public static final int RANDOM_SELECTION = 1;
    public static final int BINARY_SELECTION = 2;
    public static final int MAXNOVELTY_SELECTION = 3;
    
    
    public int minID; 
    public int maxID;     

    /**
     * Constructor
     * @param archiveSize
     * @param k
     * @param theta
     * @param timeAllowedMS
     */
    public NoveltySearch1plusN(int archiveSize, int k, double theta, long timeAllowedMS,double p, int i) {
    	this.archiveSize_ = archiveSize;
    	this.k_ = k;
    	this.theta_ = theta;    	
        this.timeAllowedMS_ = timeAllowedMS;
        this.matrixInitialized  = false;
		this.p = p;
		this.i_ = i;        
                
		archive = new ArrayList<Product>(archiveSize_); 
        distancesMatrix = new double [archiveSize_+1][archiveSize_+1]; // plus one because of one population member
        random = new Random();
        noveltyScores = new double [archiveSize_ + 1]; 
        
        runCoverage = new ArrayList<Double> ();
    }
    
    /**
     * Initial score
     * @param product
     * @return
     */
    public void calculateInitialNScore() {
    	        		
		if (!matrixInitialized) {
			initializeMatric();
			matrixInitialized = true;
		}    		
                 	
    	// Obtain the novelty scores    	
    	// reset novelty scores
    	for (int i = 0; i < noveltyScores.length;i++) {
    		noveltyScores[i] = 0.0;
    	}
    	
    	double [] dist = new double [archiveSize_]; 
    	int []    idx =  new int [archiveSize_]; 
    			
    	for (int i = 0; i < archiveSize_;i++) {
    		
    		for (int j = 0; j < archiveSize_; j++) {
    			dist[j] = distancesMatrix[i][j];
    			idx[j] = j;
    		}
    		
    		/* Find the k-nearest neighbors*/
    		DistancesUtil.minFastSort(dist, idx, archiveSize_, k_);
    		
    		noveltyScores[i] = 0.0;    		
    		for (int k = 0; k < k_; k++) {
    			noveltyScores[i] = noveltyScores[i] + dist[k];
    		}
    		
    		noveltyScores[i] = noveltyScores[i]/k_; // the average value
    		
    	} // for i
    	   	  	
    }
    
    /**
     * Update archive using a new product 
     * @param product
     * @return
     */
    public boolean updateAchive(Product product) {
    	
    	if (archive.contains(product)) { 	
    		return false;
    	}
    	
    	if (archive.size() < archiveSize_) {
    		
    		archive.add(product);
    		return true;
    		
    	} else { // archive.size() == archiveSize_
    		
    		if (!matrixInitialized) {
    			initializeMatric();
    			matrixInitialized = true;
    		}    		
            
            // Initialize distance matrix	
    		distancesMatrix[archiveSize_][archiveSize_] = 0.0;
            for (int i = 0; i < archive.size(); i++) {
            	distancesMatrix[archiveSize_][i] = DistancesUtil.getWeightedDistance(product,archive.get(i));
            	distancesMatrix[i][archiveSize_]= distancesMatrix[archiveSize_][i];
            }
    		
    	}
    	
    	// Obtain the novelty scores    	
    	// reset novelty scores
    	for (int i = 0; i < noveltyScores.length;i++) {
    		noveltyScores[i] = 0.0;
    	}
    	
    	double [] dist = new double [archiveSize_ + 1]; 
    	int []    idx =  new int [archiveSize_ + 1]; 
    			
    	for (int i = 0; i <= archiveSize_;i++) {
    		
    		for (int j = 0; j <= archiveSize_; j++) {
    			dist[j] = distancesMatrix[i][j];
    			idx[j] = j;
    		}
    		
    		/* Find the k-nearest neighbors*/
    		DistancesUtil.minFastSort(dist, idx, archiveSize_ + 1, k_);
    		
    		noveltyScores[i] = 0.0;    		
    		for (int k = 0; k < k_; k++) {
    			noveltyScores[i] = noveltyScores[i] + dist[k];
    		}
    		
    		noveltyScores[i] = noveltyScores[i]/k_; // the average value
    		
    	} // for i
    	   	
    	
    	//Find the solution with worst (smallest) novelty score
    	double minScore = Double.MAX_VALUE;
    	minID  = -1;
    	
    	double maxScore = - Double.MAX_VALUE;
    	maxID  = -1;
    	
        for (int i = 0; i < archiveSize_; i++) {          
                if (noveltyScores[i] < minScore) {
                	minScore = noveltyScores[i];
                	minID = i;
                }
                
                if (noveltyScores[i] > maxScore) {
                	maxScore = noveltyScores[i];
                	maxID = i;
                } 
        }
          
                
        // Try to replace the worst member        
		if (noveltyScores[archiveSize_]  > theta_ && (noveltyScores[archiveSize_] > minScore)) {
			archive.set(minID, product); // replace
			
//			System.out.println("noveltyScores[archiveSize_]" + noveltyScores[archiveSize_] +",minScore"  + minScore);
			// Update the distance matrix
			for (int j=0;j < archiveSize_; j++) {
				distancesMatrix[minID][j] = distancesMatrix[archiveSize_][j];
				distancesMatrix[j][minID] = distancesMatrix[j][archiveSize_];
			}
			
			distancesMatrix[minID][minID] = 0.0;
			noveltyScores[minID] = noveltyScores[archiveSize_];
			return true;
		}    		
		
    	return false;
    }
        
    
    public List<Product> getArchive() {
		return archive;
	}

	public void setArchive(List<Product> archive) {
		this.archive = archive;
	}

	/**
     * 鍒濆鍖栬窛绂荤煩闃�     * @param product
     */
    public void initializeMatric() {
    	// Computation of the distances
        for (int i = 0; i < archiveSize_; i++) {
        	distancesMatrix[i][i] = 0.0;
        	
            for (int j = i + 1; j < archiveSize_; j++) {   
                double dist =  DistancesUtil.getWeightedDistance(archive.get(i), archive.get(j));
                
                distancesMatrix[i][j] = dist;       
                distancesMatrix[j][i] = dist;  
            } // for j
        } // for i
        
    } // initializeMatric
    
    
    
    public static List<Integer> productToList(Product product) {
        List<Integer> li = new ArrayList<Integer>(product);

        Collections.sort(li, new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return Double.compare(Math.abs(o1), Math.abs(o2));
            }
        });


        return li;
    }
 

    public Individual crossover(Individual indiv1, Individual indiv2) {

        Individual offspring1, offspring2;

        int crossPoint = (int) (Math.random() * (indiv1.getSize() - 1)) + 1;
        offspring1 = new Individual(indiv1);
        offspring2 = new Individual(indiv2);

        boolean b = random.nextBoolean();

        if (b) {
            for (int i = crossPoint; i < offspring1.getSize(); i++) {
                Product p = offspring1.getProducts().get(i);
                offspring1.getProducts().set(i, offspring2.getProducts().get(i));
                offspring2.getProducts().set(i, p);
            }
        } else {
            for (int i = 0; i <= crossPoint; i++) {
                Product p = offspring1.getProducts().get(i);
                offspring1.getProducts().set(i, offspring2.getProducts().get(i));
                offspring2.getProducts().set(i, p);
            }
        }

        offspring1.fitnessAndOrdering();
        offspring2.fitnessAndOrdering();

        if (offspring1.getFitness() > offspring2.getFitness()) {
            return offspring1;
        } else if (offspring1.getFitness() < offspring2.getFitness()) {
            return offspring2;
        } else {
            b = random.nextBoolean();
            return b ? offspring1 : offspring2;
        }
    }
    
    /**
     * 灏咮inary杞崲鎴恜roduct
     * @param vector
     * @return
     */
    public Product bin2Product(Binary bin) {

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
     * Bit-wise mutation
     * @param prod
     * @param probability
     * @return
     */
   public Product mutate(Product prod, double probability) {
	  
	   Binary bin = Product2Bin(prod);	   
	   	
    	for (Integer j : NSbS_Driver.getInstance().featureIndicesAllowedFlip){ //flip only not "fixed" features
			
       	 if (PseudoRandom.randDouble() < probability) {								
       		 	bin.bits_.flip(j);							
			}
		}
    	
    	return bin2Product(bin);
    	
    }
    
   /**
    *  Uniform crossover
    * @param prod1
    * @param prod2
    * @param probability
    * @return
    */
   public Product [] crossover(Product prod1, Product prod2, double probability) {
		  
	   Binary parent1 = Product2Bin(prod1);	   
	   Binary parent2 = Product2Bin(prod2);	
	   Binary [] offSpringBin = new Binary[2];
	   
	   offSpringBin[0] = new Binary(prod1.size());
	   offSpringBin[1] = new Binary(prod2.size());
			   
	   Product [] offSpring = new  Product [2];
	   
	   if (PseudoRandom.randDouble() < probability) {
	    	for (int i = 0;i < parent1.getNumberOfBits();i++){ 
	    		
	    		if (PseudoRandom.randDouble() < 0.5) {
	        		boolean value = parent1.getIth(i);	        
	        		offSpringBin[0].setIth(i, value);	        		
	        		
	        		value = parent2.getIth(i);
	        		offSpringBin[1].setIth(i, value);
	        	
	  			} else {
	  				boolean value = parent2.getIth(i);
	  				offSpringBin[0].setIth(i, value);
	        		
	        		value = parent1.getIth(i);	        
	        		offSpringBin[1].setIth(i, value);
	
	  			} // if
	       	
			} // for i
	    	
	    	offSpring[0] = bin2Product(offSpringBin[0]);
	    	offSpring[1] = bin2Product(offSpringBin[1]);
	    	 
	   } else{// if  (PseudoRandom.randDouble() < probability) {
		   
		   offSpring[0] = prod1;
		   offSpring[1] = prod2;
	   }
    	    	
	   return offSpring;  
    	 	
    }
   
   /**
    * Select two parents based on novelty scores
    * @return
    */
   public Product [] selection(int strategy) {
	   Product [] parents = new Product [2];
	   int p1 = -1,p2 = 0;
	   
	   if (strategy == RANDOM_SELECTION) {
		   p1 = PseudoRandom.randInt(0, archive.size() - 1);
		   p2 = PseudoRandom.randInt(0, archive.size() - 1);
		   while (p2==p1) {
			   p2 = PseudoRandom.randInt(0, archive.size() - 1); 
		   }
	   } else if (strategy == BINARY_SELECTION) {
		   
		  p1 = binarySelection();
		  p2 = binarySelection();
		  
		  while (p2==p1) {
			   p2 = binarySelection(); 
		   }
		  		   
	   } else if (strategy == MAXNOVELTY_SELECTION) { // 閫夋嫨novelty score 鏈�ぇ鐨勪袱涓釜浣�		   
		   List <Integer>  maxIDs = new ArrayList <Integer> ();
		   
		   double maxScore = -1;
		   
		   for (int i = 0; i < archive.size();i++) {
			   
			   if (noveltyScores[i] > maxScore) {
				   maxScore = noveltyScores[i];
				   maxIDs.clear();
				   maxIDs.add(i);
			   } else if (noveltyScores[i] == maxScore) {
				   maxIDs.add(i);
			   }
			   
		   }
		   		   
		   p1 = maxIDs.get(0);		   
		   p2 = binarySelection();
			  
		   while (p2==p1) {
			   p2 = binarySelection(); 
		   }
	   }
	   	   
	   parents[0] = archive.get(p1);
	   parents[1] = archive.get(p2);
	   
	   return parents;	   
   }
   
   
   public int binarySelection() {
	   int r1 = PseudoRandom.randInt(0, archive.size() - 1);
	   int r2 = PseudoRandom.randInt(0, archive.size() - 1);
	   while (r2==r1) {
		   r2 = PseudoRandom.randInt(0, archive.size() - 1); 
	   }
	   
	   if (noveltyScores[r1] > noveltyScores[r2]) {
		   return r1;
	   } else if (noveltyScores[r2] > noveltyScores[r1]) {
		   return r2;
	   } else {
		   if (PseudoRandom.randDouble() < 0.5) 
			   return r1;
		   else
			   return r2;
	   }
	   
   } 
   
   
    public int runNS_Convergence(String path,  int numberOfPoints, int individualSize, int type) throws Exception {
    	long sumTimeMS = 0;       
                                    
//        archive = SPL.getInstance().getUnpredictableProducts(archiveSize_);
    	archive = NSbS_Driver.getInstance().loadProductsFromFile(path.replaceAll("NSprobSAT", "GA") + "Products.0");
    	
    	
        int nbIter = 0;       		
        long startTimeMS = System.currentTimeMillis();
        int interval = (int) ((timeAllowedMS_ - sumTimeMS)/(numberOfPoints - 1));
        
      // Record time	
        int recordTimes = 0;  
    	timeRecord(path,archive,recordTimes); 
        recordTimes++;   
                        
        while (sumTimeMS < timeAllowedMS_) {
        	
        	startTimeMS = System.currentTimeMillis();
        	
        	Product product = null;     
//        	Product [] parents = selection(RANDOM_SELECTION);        	
        	Product [] parents = selection(BINARY_SELECTION);
        	Product [] offspring = crossover(parents[0], parents[1],1);
        	
        	offspring[0] = mutate(offspring[0],0.5);
          	offspring[1] = mutate(offspring[1],0.5);  
          	
          	offspring[0] = NSbS_Driver.getInstance().repairProducts(offspring[0],p);
          	offspring[1] = NSbS_Driver.getInstance().repairProducts(offspring[1],p);  
          	        
          	
          	if (sumTimeMS >  0.5 * timeAllowedMS_)
          	      theta_ = 0.0;
          	
          	updateAchive(offspring[0]);
        	updateAchive(offspring[1]);

        	nbIter++;
            
        	// -------------------Record time------------------
        	sumTimeMS = sumTimeMS + (System.currentTimeMillis() - startTimeMS); //  绱姞鏃堕棿  
        	if (sumTimeMS >= (recordTimes - 1) * interval && sumTimeMS >= (recordTimes) * interval) {
             	timeRecord(path,archive,recordTimes); 
             	recordTimes++;          
            }            
        }
        
        noveltyBasedSort();
        System.out.println("-----------nbIter in NS------------- " + nbIter);
        System.out.println("-----------sumTimeMS in NS------------- " + sumTimeMS); 
        return recordTimes;                
       
    }
    
    public void noveltyBasedSort() {
    	 List <Product> productCopy = new ArrayList <Product>(archive);
    	     	 
    	 int [] idx = new int[noveltyScores.length - 1];
    	 for (int i = 0; i < idx.length;i++) {
    		 idx[i] = i;
    	 }
    	 
    	 DistancesUtil.maxFastSort(noveltyScores, idx, archiveSize_ , archiveSize_);
    	 
    	 for (int i = 0; i < idx.length;i++) {
    		 archive.set(i, productCopy.get(idx[i]));   
    		 
    	 }
    	 
    }
    
    

    
    /**
     * Record time stones
     * @param indiv
     * @throws Exception 
     */
    public void timeRecord(String path, List<Product> archive,int recordTimes) throws Exception {
    	
    	(NSbS_Driver.getInstance()).writeProductsToFile(path + "Products." + recordTimes, archive);      
       
    }
    

    /**
     * Generate diverse samples using NS
     * @param outputDir
     * @param fmFileName
     * @param currentRun
     * @return
     * @throws Exception
     */
    public List<Product> runSimpleNS(String outputDir, String fmFileName, int currentRun) throws Exception {
        long startTimeMS = System.currentTimeMillis();
        
        // Get a random population
        archive = NSbS_Driver.getInstance().getUnpredictableProducts(archiveSize_);

        // Or load initial population from files
//        String loadPath = outputDir + "UnPredictable/" + fmFileName +"/" + archiveSize_ + "prods/";
//        archive = SPL.getInstance().loadProductsFromFile(loadPath + "Products." + currentRun);
       
        System.out.println("Number of selected features, initial");
        Collections.sort(archive); 
  	   	for (int i = 0; i< archive.size();i++) {
  		   System.out.print(archive.get(i).getSelectedNumber() + " ");
  	   	}
  	   	
  	   	int generation = 0;
  	   	List<Double> timeAxis = new ArrayList<Double>();
  	   	List<Double> scoreAxis = new ArrayList<Double>();
  	   	
  	   	//--------------Initialize score--------------------------
  	   	calculateInitialNScore();
  	   	
      	double sum = 0.0;
    	for (int i = 0; i < noveltyScores.length - 1; i++) {
    		sum += noveltyScores[i];
    	}
    	    
    	double oldSum = sum;
    			
    	double changeRatio = 1e10;    			
    			
    	timeAxis.add(0.0);
    	scoreAxis.add(sum);
  	   	
    	int gap = 0;
    	
    	boolean terminal = false;
    	
        while (!terminal) {         	
    		
    		// -------------------crossover + mutation--------------     	
        	Product [] parents = selection(BINARY_SELECTION);          	
        	Product [] offspring = crossover(parents[0], parents[1],1.0);
        	
        	offspring[0] = mutate(offspring[0],0.1);
          	offspring[1] = mutate(offspring[1],0.1);            	

          	// Repair: Using only probSAT is better
          	offspring[0] = NSbS_Driver.getInstance().repairProducts(offspring[0],1);
          	offspring[1] = NSbS_Driver.getInstance().repairProducts(offspring[1],1);  
          	
          	updateAchive(offspring[0]);  
          	updateAchive(offspring[1]); 
                  	
          	sum = 0.0;
        	for (int i = 0; i < noveltyScores.length - 1; i++) {
        		sum += noveltyScores[i];
        	}
        	
        	changeRatio = Math.abs((sum - oldSum))/oldSum * 100;
        
        	if (changeRatio < 0.1) {
        		gap++;
        	} else {
        		gap = 0;
        	}
        		
        	// Used in automatic termination
        	if (gap > 10 || System.currentTimeMillis() - startTimeMS > 3600*1000) {
        		terminal = true;
        	}        	
        	        	
        	oldSum = sum;
        	
        	timeAxis.add((System.currentTimeMillis() - startTimeMS)/1000.0);
        	scoreAxis.add(sum);
          	generation++;
        } // while 
        
        System.out.println("\n Number of selected features after optimization");
        Collections.sort(archive); 
  	   	for (int i = 0; i< archive.size();i++) {
  		   System.out.print(archive.get(i).getSelectedNumber() + " ");
  	   	}
  	   	
  	   System.out.println("\nGeneration: " + generation);    	   
  	   //Generate convergence m file
  	   generateConvergenceMFiles(NSbS_Driver.getInstance().path, currentRun, timeAxis,scoreAxis);
       return archive;                
       
    }


	public List<Double> getRunCoverage() {
		return runCoverage;
	}

	public void setRunCoverage(List<Double> runCoverage) {
		this.runCoverage = runCoverage;
	}

	 public void generateConvergenceMFiles(String path, int runID, List<Double> x, List<Double> y) throws Exception {
         
		 	 String mPath = path + "Convergence_" + runID + ".m";
		 
	         FileUtils.resetFile(mPath);     
	         
	         // Write header
	          FileOutputStream fos   = new FileOutputStream(mPath,false)     ;
	 	      OutputStreamWriter osw = new OutputStreamWriter(fos)    ;
	 	      BufferedWriter bw      = new BufferedWriter(osw)        ;
	 	    
	 	            	       	
	    	  bw.write("%% Plots for convergence");bw.newLine();	
		      bw.write("clear ");
		      bw.newLine();
		      bw.write("clc ");
		      bw.newLine();
		      
		      bw.write("set(gcf,'Position',[500 200 500 400])"); // 锟斤拷锟斤拷图锟斤拷锟斤拷示位锟斤拷
	          bw.newLine();
	        
	          bw.write("if get(gca,'Position') <= [inf inf 400 400]");
	          bw.newLine();
	          bw.write("    Size = [3 5 .8 18];");
	          bw.newLine();
	          bw.write("else");
	          bw.newLine();
	          bw.write("    Size = [6 3 2 18];");
	          bw.newLine();
	          bw.write("end");
	          bw.newLine();
	        
	          bw.write("set(gca,'NextPlot','add','Box','on','Fontname','Times New Roman','FontSize',Size(4),'LineWidth',1.3);");
	          bw.newLine();
	          	    
	          bw.write(" x = [");
		       
	          for (int i = 0; i < x.size() - 1;i++) {
	        	  bw.write(x.get(i) + ",");
	          }
	          
	          bw.write(x.get(x.size() - 1) + "];" );
	          bw.newLine();	
	          
	          bw.write(" y = [");
		       
	          for (int i = 0; i < y.size() - 1;i++) {
	        	  bw.write(y.get(i) + ",");
	          }
	          
	          bw.write(y.get(y.size() - 1) + "];" );
	          bw.newLine();	
	          
	        bw.write(" plot(x,y,'.r')");
		    bw.newLine();	
	          
	        bw.write(" tit = title('Convergence');");
	        bw.newLine();	
	        bw.write("set(tit,'fontsize',20)");
	        bw.newLine();
	         
	        bw.newLine();
	        bw.write("xl = xlabel('Time (s)');");
	        bw.newLine();
	        bw.write("set(xl,'fontsize',20)");
	        bw.newLine();
	        	         

	        bw.write("yl = ylabel(' Novelty score');");
	        bw.newLine();
	        bw.write("set(yl,'fontsize',20)");
	        bw.newLine();
	        	 	        
	        bw.close();         
	
	    }
} // Class
