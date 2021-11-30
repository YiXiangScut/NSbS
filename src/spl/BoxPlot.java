/**
 * The class BoxPlot is used to plot boxplots for a set of algorithms, often controlled by a newly proposed algorithm.
 * The boxplots are stored in folder :experimentName/BoxPlot/cotrolalg/
 * Created by Yi Xiang
 * Last modified 2014/2/22
 */
package spl;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;

import jmetal.qualityIndicator.util.MetricsUtil;
import jmetal.util.Configuration;
import spl.utils.FileUtils;

/**
 * @author Administrator
 *
 */
public class BoxPlot {
	String experimentName_;
	String [] algNames_;
	String controlAlg_;// The control algorithm, often be the newly proposed algorithms
	String [] proNames_;
	String [] indicatorNames_;
	int numberOfRuns_;
	int t_;
	int nproducts_;
	long timeMS_;
	/**
	 * 
	 */
	public BoxPlot(String experimentName,String [] algNames,String controlAlg, String [] proNames, 
			String []indicatorNames,int numberOfRuns) {
		experimentName_ = experimentName;
		algNames_ = algNames;
		proNames_ = proNames;
		indicatorNames_ = indicatorNames;
		numberOfRuns_ = numberOfRuns;
		controlAlg_ = controlAlg;
		// TODO Auto-generated constructor stub
	}

	public BoxPlot(String experimentName,String [] algNames,String controlAlg, String [] proNames, 
			String []indicatorNames,int numberOfRuns, int t, int nproducts, long timeMS) {
		experimentName_ = experimentName;
		algNames_ = algNames;
		proNames_ = proNames;
		indicatorNames_ = indicatorNames;
		numberOfRuns_ = numberOfRuns;
		controlAlg_ = controlAlg;
		t_ = t;
		nproducts_ = nproducts;
		timeMS_ = timeMS;
		// TODO Auto-generated constructor stub
	}	
	
	
	
	public void execut(){
		MetricsUtil meticsUtil = new MetricsUtil();
		NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2); 
     
		for (int k = 0;k < proNames_.length;k++){ // for each problem
			 FileUtils.CheckDir(experimentName_ + "/mBoxPlot/");    
			 String fmFileName = proNames_[k];
			 
			 String mFileName = "FM_" + fmFileName;
	         mFileName =  mFileName.replace('.', '_');
	         mFileName =  mFileName.replace('-', '_');
	         mFileName = mFileName.replace(',', '_');
	         
	         for(int j = 0;j < indicatorNames_.length; j++) {//for each indicator
	        	 
	        	 String indicator = indicatorNames_[j];
	        	 String storePath = experimentName_ + "/mBoxPlot/" + controlAlg_ + "/" ;
	        	 FileUtils.CheckDir(storePath);
	        	 
        		 storePath = storePath + mFileName + "_" + indicator +"_BoxPlot.m";
    	    	 FileUtils.resetFile(storePath);System.out.println(storePath); 
    	    	 
    	    	 double [][] indicatorValues = new double[numberOfRuns_][algNames_.length];	
    	    	 
    	    	 
    	    	 // Read data for each algorithm
    	    	 for(int i = 0;i < algNames_.length;i++) {//for each algorithm
    	    		 
    	    		String indicatorPath = experimentName_  + algNames_[i] + "/" +  proNames_[k] + "/"  
    	    				 + nproducts_ + "prods/" + indicatorNames_[j];
    	    		
 					System.out.println(indicatorPath);
 					double [][] values = meticsUtil.readFront(indicatorPath);
// 					System.out.println(values.length);
 					for (int r = 0;r < numberOfRuns_;r++){
// 						System.out.println(values[r][0]);
 						indicatorValues[r][i] = values[r][0];
 					}//for r					
 					
    	    	 } // for i
    	    	 
    	    	 try {
    				 /* Open the file */
    			      FileOutputStream fos   = new FileOutputStream(storePath)     ;
    			      OutputStreamWriter osw = new OutputStreamWriter(fos)    ;
    			      BufferedWriter bw      = new BufferedWriter(osw)        ;
    			         	 
    			      
    			      bw.write("figure ");
    		    	  bw.newLine();
    			      bw.write("clear ");
    			      bw.newLine();
    			      bw.write("clc ");
    			      bw.newLine();
    			      
    			      bw.write("set(gcf,'Position',[500 200 500 450])"); // 
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
    		          
    			      bw.write("indicatorValues = [ ");
    			      String strToWrite;
    			      
    			      for(int ii=0;ii < numberOfRuns_;ii++){
    			    	  strToWrite ="";
    			    	  for(int jj=0;jj < algNames_.length;jj++){
    			    		  strToWrite = strToWrite +  nf.format(indicatorValues[ii][jj]) +" ";
    			    		  
    			    		 
    			    	  }
    			    	  bw.write(strToWrite);
    			    	  bw.newLine();
    			      }
    			      bw.write("]; ");
    			      bw.newLine();    			      
    			          			      
    			      bw.write("meanValue = mean(indicatorValues);");bw.newLine();
    			      
//    			      bw.write("plot(meanValue,'LineStyle','g--','MarkerType', 'o', 'MarkerFaceColor',m,"
//    			      		+ "'linewidth',2)");bw.newLine();
    			      		
//    			      bw.write("plot(meanValue,'LineStyle','--','color','[0.07,0.21,0.14]','Marker', 'o', "
//    			      		+ "'MarkerFaceColor','g','MarkerEdgeColor','[0.07,0.21,0.14]','linewidth',1.5)");
//    			      bw.newLine();
    			      
    			      bw.write("plot(meanValue,'LineStyle','--','color','k','Marker', 'o', 'Markersize',8," +
    			      		 "'MarkerFaceColor','[0.8 0.8 0.8]','MarkerEdgeColor','k','linewidth',1.5);");
    			      bw.newLine();
    			      
    			      bw.write("hold on");bw.newLine();
    			    		  
//    			      bw.write("h = boxplot(indicatorValues," + "'sym'"+ "," + "'r*'," + "" + "'outliersize',3" +  ",'notch','on');");
//    			      bw.newLine();
    			      
    			      bw.write(" h = boxplot(indicatorValues,'sym','r+','outliersize',5,'notch','off','color','k');");
    			      bw.newLine();
    			        			      
    			      
    			      bw.write("set(h,'LineWidth',2.0);");  bw.newLine();
    			      
    			      bw.write(" tit = title('" + fmFileName + "');");
    			      bw.newLine();
    			      bw.write("set(tit,'fontsize',25)");
    			      bw.newLine();
    			      
    			      bw.write("set(gca,'fontsize',23)");
    			      bw.newLine();
    			      String xtick = "[";
    			      String xtickLabel = "{'";
    			      for (int kk = 0; kk< algNames_.length-1;kk++ ) {
    			    	  xtick = xtick + (kk+1) + " ";
    			    	  xtickLabel = xtickLabel + algNames_[kk]+ "',' ";
    			      }
    			      xtick = xtick + algNames_.length + "]";
    			      xtickLabel = xtickLabel + algNames_[algNames_.length-1]+ "'}";
    			      bw.write("set(gca, 'XTick'," + xtick+")") ;
    			      bw.newLine();
    			      bw.write("set(gca,'XTickLabel',"+xtickLabel+")") ;
    			      bw.newLine();			      
    			      
//    			      xticklabel_rotate([1:12],60,{'dMOABC',' MOEAD',' SMPSO',' GDE3',' AbYSS',' CellDE',' IBEA',' MOCell',' OMOPSO',' NSGAII',' PAES',' SPEA2'},'interpreter','none')			      
    			      bw.write(" xl = xlabel('\\itk');");
    			      bw.newLine();			      
//    			      bw.write("set(xl,'fontsize',16)");
//    			      bw.newLine();
    			      bw.write("set(gca,'XTickLabel',{'2','15','25','50','75','100'})");
    			      bw.newLine();
    			      bw.write(" yl = ylabel('"+ indicatorNames_[j]+ "');");
    			      bw.newLine();
    			      
    			      bw.write(" yl = ylabel('Spread');");
    			      bw.newLine();
    			      
    			      bw.write("set(yl,'fontsize',25)");
    			      bw.newLine();
//    			      bw.write("xticklabel_rotate(["+ "1:"+algNames_.length+"],30,"+ xtickLabel+ ",'interpreter','none')");
    			      bw.newLine();
    			      
    			      bw.write("grid on");
    			      bw.newLine();
    			      
    			      /* Close the file */
    			      bw.close();
    			    }catch (IOException e) {
    				      Configuration.logger_.severe("Error acceding to the file");
    				      e.printStackTrace();
    			    }//catch
    	    	 
	         } // for j  
			 
		} // for k	
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		    
	    String experimentName = "./output/";   
	    
	    int t   = 6; // t-wise
	    int nproducts = 100; // the number of products
	    long timeMS = (long) 6000; // the time allowed
		  
		int numberofRuns = 10;
		
		String [] algNames = new String[]{
				"NSk=2FixedTime",
				"NSk=15AutoT",
				"NSk=25FixedTime",
				"NSk=50FixedTime",
				"NSk=75FixedTime",
				"NSk=100FixedTime",
		};
		
		String [] problemNames = new String[]{				
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
//				 "buildroot",
//				 "freetz",
				 "2.6.28.6-icse11",
//				 "2.6.32-2var",
//				 "2.6.33.3-2var"
		};
		
		for (int i = 0; i < problemNames.length;i++) {
			problemNames [i] = problemNames [i] + ".dimacs";
		}
				
		
		String [] indicatorNames =new String[]{
				"invertedDist",
				};		
		
		String controlAlg = "NS";//
		
		BoxPlot boxPlot = new BoxPlot(experimentName,algNames,controlAlg,problemNames, 
				indicatorNames,numberofRuns, t, nproducts,timeMS);
		boxPlot.execut();
		// TODO Auto-generated method stub

	}

}
