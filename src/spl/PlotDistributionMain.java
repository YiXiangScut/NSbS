/**
 * Plot the distribution of samples
 */

package spl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
//import java.util.Set;


import spl.fm.Product;
import spl.techniques.SimilarityTechnique;
import spl.utils.FileUtils;


public class PlotDistributionMain {

	public int nbProds = 100;
	public long timeAllowed; 
	public String outputDir;
	public int runs;
	public String algName;
	 
	public PlotDistributionMain() {
		
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
       
         System.out.println("lowerBound " + lowerBound);
         System.out.println("upperBound " + upperBound);
         
   	     List<Integer> difference = new ArrayList<Integer>();
   	     List<Integer> selectedNumList = new ArrayList<Integer>();
   	     selectedNumList.add(lowerBound);
   	     selectedNumList.add(upperBound);
   	  
   	  
         int r = 0;
                	
         List<Product> products = null;    
                      
         // Load products                            
         products = NSbS_Driver.getInstance().loadProductsFromFile(path + "Products." + r);
         int [] selectedNum = new int[products.size()];
                  
         Collections.sort(products); 
   	   	 for (int i = 0; i < products.size();i++) {
   	   	    selectedNum [i] = products.get(i).getSelectedNumber();
   	   	    selectedNumList.add(selectedNum [i]) ;
   	   	 }   
         
         for(int i = 1; i < products.size();i++) {
        	 difference.add(selectedNum [i] - selectedNum [i-1]);
         }
                           
                  
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
         
        String dir = "./output/mPlots/" + algName  + "/" + fmFileName +"/";
        FileUtils.CheckDir(dir);
        generateConvergenceMFiles(dir,selectedNumList,fmFileName);
//        generateConvergenceMFilesDistribution(dir,selectedNumList,frequency,fmFileName);
        
	}
	
	
	 public void generateConvergenceMFiles(String path,  List<Integer> y, String fmFileName) throws Exception {
         
	 	 String mPath = path + "scatterPlotSelectedNum.m";
	 
         FileUtils.resetFile(mPath);     
         
         // Write header
          FileOutputStream fos   = new FileOutputStream(mPath,false)     ;
 	      OutputStreamWriter osw = new OutputStreamWriter(fos)    ;
 	      BufferedWriter bw      = new BufferedWriter(osw)        ;
 	    
 	            	       	
    	  bw.write("%% Plots for convergence");bw.newLine();	
    	  bw.write("figure ");
    	  bw.newLine();
	      bw.write("clear ");
	      bw.newLine();
	      bw.write("clc ");
	      bw.newLine();
	      
	      bw.write("set(gcf,'Position',[500 200 500 400])"); // ����ͼ����ʾλ��
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
          	              
          bw.write(" y = [");
	       
          for (int i = 0; i < y.size() - 1;i++) {
        	  bw.write(y.get(i) + ",");
          }
          
          bw.write(y.get(y.size() - 1) + "];" );
          bw.newLine();	
          
          bw.write("y = y + 0.001;"); bw.newLine();	
        		  
        bw.write(" b=bar(y,'linewidth',2,'edgecolor','[0.5 0 0]', 'facecolor', '[0.5 0 0]');");
	    bw.newLine();	
          
        bw.write(" tit = title('" + fmFileName.substring(0,fmFileName.length() - 7) + "');");
        bw.newLine();	
        bw.write("set(tit,'fontsize',20)");
        bw.newLine();
         
//        bw.newLine();
//        bw.write("xl = xlabel('Time (s)');");
//        bw.newLine();
//        bw.write("set(xl,'fontsize',20)");
//        bw.newLine();
        	         

        bw.write("yl = ylabel(' Difference');");
        bw.newLine();
        bw.write("set(yl,'fontsize',20)");
        bw.newLine();
        	        
        bw.write("axis([0 102 -0.2 max(y)+0.2]);");
        bw.newLine();
        
//        bw.write("set(gca,'xminortick','on') ;");
//        bw.newLine();
//        
//        bw.write("set(gca,'yminortick','on') ;");
//        bw.newLine();
        
        bw.close();         

    }
	 
 public void generateConvergenceMFilesDistribution(String path,  List<Integer> y, List<Integer> frequency, String fmFileName) throws Exception {
         
	 	 String mPath = path + "distributionPlot.m";
	 
         FileUtils.resetFile(mPath);     
         
         // Write header
          FileOutputStream fos   = new FileOutputStream(mPath,false)     ;
 	      OutputStreamWriter osw = new OutputStreamWriter(fos)    ;
 	      BufferedWriter bw      = new BufferedWriter(osw)        ;
 	    
 	            	       	
    	  bw.write("%% Plots for distribution");bw.newLine();	
    	  bw.write("figure ");
    	  bw.newLine();
	      bw.write("clear ");
	      bw.newLine();
	      bw.write("clc ");
	      bw.newLine();
	      
	      bw.write("set(gcf,'Position',[500 200 500 150])"); // ����ͼ����ʾλ��
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
          	              
          bw.write("y = [");
	       
          for (int i = 0; i < y.size() - 1;i++) {
        	  bw.write(y.get(i) + ",");
          }
          
          bw.write(y.get(y.size() - 1) + "];" );
          bw.newLine();	
          
          bw.write("frq = [");
	       
          for (int i = 0; i < frequency.size() - 1;i++) {
        	  bw.write(frequency.get(i) + ",");
          }
          
          bw.write(frequency.get(frequency.size() - 1) + "];" );
          bw.newLine();	
          
          bw.write("bound = y(1:2);"); bw.newLine();	
          bw.write("plot(bound,0,'bp','markersize',12,'markerfacecolor','[0 0.2 0.6]')"); bw.newLine();	
          bw.write("hold on"); bw.newLine();	
          bw.write("line([0,y(2)+1],[0, 0],'linewidth',1,'linestyle','--')"); bw.newLine();	
          
          bw.write("y(1:2) = [];") ; bw.newLine();	
          bw.write("y = unique(y)"); bw.newLine();	
          
          
          bw.write("for i=1:length(y)");bw.newLine();
//          bw.write("	 marker_size = 8 + (frq(i) - min(frq))/ (max(frq) - min(frq)) * (14-8);"); bw.newLine();	
          bw.write("	 marker_size = 8;"); bw.newLine();	
		  bw.write("	 plot(y(i),0,'r','marker','o','markersize',marker_size,'markerfacecolor','[1 0.6 0.6]','linewidth',1.2);"); bw.newLine();	
		  bw.write("	 hold on");bw.newLine();	
		  bw.write("end");bw.newLine();	
        		
        		
//        bw.write("plot(y,0,'r','marker','o','markersize',8,'markerfacecolor','r');");
//	    bw.newLine();	
		  
	    bw.write("set(gca,'ytick',[],'yticklabel',[])"); bw.newLine();			  
		  
        bw.write("tit = title('" + fmFileName.substring(0,fmFileName.length() - 7) + "');");
        bw.newLine();	
        bw.write("set(tit,'fontsize',20)");
        bw.newLine();
        
        bw.write("axis([bound(1),bound(2) -0.2 0.2])");
        bw.newLine();
        
        bw.write("box off");
        bw.newLine();
//        bw.newLine();
//        bw.write("xl = xlabel('Time (s)');");
//        bw.newLine();
//        bw.write("set(xl,'fontsize',20)");
//        bw.newLine();
        	         

//        bw.write("yl = ylabel(' Difference');");
//        bw.newLine();
//        bw.write("set(yl,'fontsize',20)");
//        bw.newLine();
        	        
//        bw.write("axis([0 102 -0.2 max(y)+0.2]);");
//        bw.newLine();
        
//        bw.write("set(gca,'xminortick','on') ;");
//        bw.newLine();
//        
//        bw.write("set(gca,'yminortick','on') ;");
//        bw.newLine();
        
        bw.close();         

    }

  /**
   * Main method
   * @param args
 * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    PlotDistributionMain gfr = new PlotDistributionMain();
    
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
//			 "fiasco_17_10",
//			 "axtls_2_1_4",
//			 "fiasco",
//			 "toybox",
//			 "axTLS",
//			 "uClibc-ng_1_0_29",
//			 "toybox_0_7_5",
//			 "uClinux",
//			 "ref4955",
//			 "adderII",			 
//			 "ecos-icse11",
//			 "m5272c3",
//			 "pati",
//			 "olpce2294",
//			 "integrator_arm9",
//			 "at91sam7sek",
//			 "se77x9",
//			 "phycore229x",
//			 "busybox-1.18.0",
//			 "busybox_1_28_0",
//			 "embtoolkit",
//			 "freebsd-icse11",
//			 "uClinux-config",
//			 "buildroot",
//			 "freetz",
//			 "2.6.28.6-icse11",
//			 "2.6.32-2var",
//			 "2.6.33.3-2var"

	};

	gfr.nbProds     = 100;
	gfr.outputDir   = "./output/";
	gfr.runs        = 2;
	gfr.algName     = "UnPredictable";
//	gfr.algName     = "NSk=15";
//	gfr.algName     = "NSk=15NoWeight";
	gfr.algName     = "DDBS";
//	gfr.algName     = "Smarchxy";
//	gfr.algName     = "unigenxy";
//	gfr.algName     = "NSk=15AutoT";
//	gfr.algName     = "NSk=15AutoTNoWeight";
//	gfr.algName     = "NSk=15FixedTimeNoWeight";
//	gfr.algName     = "NSk=15FixedTimeNoDelta";
	
	
	
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


