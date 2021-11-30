package spl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import spl.fm.Product;
import spl.utils.FileUtils;

public class UnifyFormat {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String [] fms = {
                // -------------待测FM-------------
//                 "lrzip",
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
////				 				 
//				 "fiasco_17_10",
//				 "axtls_2_1_4",
//				 "fiasco",
//				 "toybox",
//				 "axTLS",
//				 "uClibc-ng_1_0_29",
				 "toybox_0_7_5",
				 "uClinux",
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
//    			 "buildroot",
//				 "freetz",
//				 "2.6.28.6-icse11",
//				 "2.6.32-2var",
//				 "2.6.33.3-2var"
        };

		int runTimes = 30;
		String alg = "unigen"; 
		String rootPath = "./output/";
		
		for (int i = 0; i < fms.length;i++) {
			String loadPathFixed = rootPath + alg + "/" + fms[i] ;
			String writePathFixed = rootPath + alg + "xy/" + fms[i] + ".dimacs/100prods/";
			FileUtils.CheckDir(writePathFixed); 
			
			for(int r = 0; r < runTimes; r++) {
				String loadPath = loadPathFixed + "/" + (r) + "/" +  "samples";
				String writePath = writePathFixed + "Products." + r;
				
				List<Product> products = loadProductsFromFileUnigen(loadPath);
				writeProductsToFile(writePath, products) ;
				
			}
		}
		
	}

	/**
     * 将products写入文件
     * @param outFile
     * @param products
     * @throws Exception
     */
    public static void writeProductsToFile(String outFile, List<Product> products) throws Exception {

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
              if (done < prodFeaturesList.size()) {
                  out.write(";");
              }
              done++;
          }

          out.newLine();
      }
      out.close();
  }
    
    /**
     * 从文件读取products
     * @param outFile
     * @param products
     * @throws Exception
     */
    public static List<Product> loadProductsFromFile(String inFile) throws Exception {
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
     * 从文件读取products
     * @param outFile
     * @param products
     * @throws Exception
     */
    public static List<Product> loadProductsFromFileUnigen(String inFile) throws Exception {
    	List<Product> products = new  ArrayList <Product>();
    	
        BufferedReader in = new BufferedReader(new FileReader(inFile));
        String line;
       
        while ((line = in.readLine()) != null && !(line.isEmpty())) {
           
        	StringTokenizer tokenizer = new StringTokenizer(line, " ");
            Product product = new Product();     
            
            while (tokenizer.hasMoreTokens()) {
                String tok = tokenizer.nextToken().trim();
                
                if (Integer.parseInt(tok) != 0) {
                	product.add(Integer.parseInt(tok));
                }
            }
             
            products.add(product);
          
        }       
        
        in.close();
        
    	return products;
   
  }
}
