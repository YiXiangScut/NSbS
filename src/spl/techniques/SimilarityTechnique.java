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
package spl.techniques;

import java.util.ArrayList;
import java.util.List;

import spl.fm.Product;

/**
 *
 * @author
 */
public class SimilarityTechnique implements PrioritizationTechnique {

    public static final int JACCARD_DISTANCE = 1;
    public static final int GREEDY_SEARCH = 2;
    public static final int NEAR_OPTIMAL_SEARCH = 3;
    private int distanceMethod;
    private int searchMethod;
    private double lastFitnessComputed;

    public SimilarityTechnique(int distanceMethod, int searchMethod) {
        this.distanceMethod = distanceMethod;
        this.searchMethod = searchMethod;
        lastFitnessComputed = -1;
    }

    // should be called after one prioritization
    public double getLastFitnessComputed() {
        return lastFitnessComputed;
    }

    public void setLastFitnessComputed(double lastFitnessComputed) {
        this.lastFitnessComputed = lastFitnessComputed;
    }
   

    
	/**
	 * Return the sum of novelty scores
	 * @param product
	 * @return
	 */
    public double noveltyScoreSum(List<Product> products, int k_) {
    	
    	int n = products.size();
    	
    	double [][] distancesMatrix = new double [n][n];
    	double [] noveltyScores =  new double [n];
    	
    	 for (int i = 0; i < n; i++) {
         	distancesMatrix[i][i] = 0.0;
         	
             for (int j = i + 1; j < n; j++) {   
                 double dist = DistancesUtil.getHammingDistance(products.get(i), products.get(j));
                 distancesMatrix[i][j] = dist;       
                 distancesMatrix[j][i] = dist;  
             } // for j
         } // for i
    	    	     
    	
    	// Obtain the novelty scores    	
    	// reset novelty scores
    	for (int i = 0; i < noveltyScores.length;i++) {
    		noveltyScores[i] = 0.0;
    	}
    	
    	double noveltyScoresSum = 0.0;
    	
    	double [] dist = new double [n]; 
    	int []    idx =  new int [n]; 
    			
    	for (int i = 0; i < n;i++) {
    		
    		for (int j = 0; j < n; j++) {
    			dist[j] = distancesMatrix[i][j];
    			idx[j] = j;
    		}
    		
    		/* Find the k-nearest neighbors*/
    		DistancesUtil.minFastSort(dist, idx, n, k_);
    		
    		noveltyScores[i] = 0.0;    		
    		for (int k = 0; k < k_; k++) {
    			noveltyScores[i] = noveltyScores[i] + dist[k];
//    			System.out.println("k = " + k + ", dist[k] = " + dist[k]);
    		}
//    		System.out.println("----------------------");
    		
    		noveltyScores[i] = noveltyScores[i]/k_; // the average value
    		
    		noveltyScoresSum = noveltyScoresSum + noveltyScores[i];
    	} // for i
    	   	
    	return noveltyScoresSum;
    }
    
    
    
    @Override
    public List<Product> prioritize(List<Product> products) {
        int size = products.size();
        List<Product> prioritizedProducts = new ArrayList<Product>(size);
        List<Product> productsCopy = new ArrayList<Product>(products);
        double[][] distancesMatrix = new double[size][size];

        lastFitnessComputed = 0;
        // Computation of the distances
        for (int i = 0; i < distancesMatrix.length; i++) {
            for (int j = 0; j < distancesMatrix.length; j++) {
                distancesMatrix[i][j] = -1;
                if (j > i) {
                    switch (distanceMethod) {
                        case JACCARD_DISTANCE:
                            double dist = DistancesUtil.getJaccardDistance(productsCopy.get(i), productsCopy.get(j));
                            lastFitnessComputed += dist;
                            distancesMatrix[i][j] = dist;
                            break;
                        default:
                            ;
                    }

                }

            }
        }

        // Selection
        switch (searchMethod) {
            case GREEDY_SEARCH:
                while (!productsCopy.isEmpty()) {
                    if (productsCopy.size() != 1) {
                        double dmax = -1;
                        int toAddIIndex = -1;
                        int toAddJIndex = -1;
                        for (int i = 0; i < distancesMatrix.length; i++) {
                            for (int j = 0; j < distancesMatrix.length; j++) {
                                if (j > i) {
                                    if (distancesMatrix[i][j] > dmax) {
                                        dmax = distancesMatrix[i][j];
                                        toAddIIndex = i;
                                        toAddJIndex = j;
                                    }
                                }
                            }
                        }

                        Product pi = products.get(toAddIIndex);
                        Product pj = products.get(toAddJIndex);
                        prioritizedProducts.add(pi);
                        prioritizedProducts.add(pj);
                        productsCopy.remove(pi);
                        productsCopy.remove(pj);

                        for (int i = 0; i < distancesMatrix.length; i++) {
                            distancesMatrix[toAddIIndex][i] = distancesMatrix[i][toAddIIndex] = distancesMatrix[i][toAddJIndex] = distancesMatrix[toAddJIndex][i] = -1;
                        }
                    } else {
                        prioritizedProducts.add(productsCopy.get(0));
                        productsCopy.clear();
                    }
                }
                break;

            case NEAR_OPTIMAL_SEARCH:
                List<Integer> possibleIndices = new ArrayList<Integer>();
                List<Integer> doneIndices = new ArrayList<Integer>();
                for (int i = 0; i < size; i++) {
                    possibleIndices.add(i);

                }
                double maxDistance = -1;
                int toAddIIndex = -1;
                int toAddJIndex = -1;
                for (int i = 0; i < distancesMatrix.length; i++) {
                    for (int j = 0; j < distancesMatrix.length; j++) {
                        if (j > i) {
                            if (distancesMatrix[i][j] > maxDistance) {
                                maxDistance = distancesMatrix[i][j];
                                toAddIIndex = i;
                                toAddJIndex = j;
                            }
                        }
                    }
                }
                Product pi = products.get(toAddIIndex);
                Product pj = products.get(toAddJIndex);

                prioritizedProducts.add(pi);
                prioritizedProducts.add(pj);
                productsCopy.remove(pi);
                productsCopy.remove(pj);
                possibleIndices.remove((Integer) toAddIIndex);
                possibleIndices.remove((Integer) toAddJIndex);
                doneIndices.add(toAddIIndex);
                doneIndices.add(toAddJIndex);


                while (!productsCopy.isEmpty()) {
                    
                    
                    
                    if (possibleIndices.size() > 1) {
                        double maxDist = -1;
                        int toAdd = -1;
                        for (Integer i : possibleIndices) {

                            double distance = 0;
                            for (Integer j : doneIndices) {
                                distance += (j > i) ? distancesMatrix[i][j] : distancesMatrix[j][i];
                            }
                            if (distance > maxDist) {
                                maxDist = distance;
                                toAdd = i;
                            }
                        }
                        Product p = products.get(toAdd);

                        prioritizedProducts.add(p);
                        productsCopy.remove(p);
                        possibleIndices.remove((Integer) toAdd);
                        doneIndices.add(toAdd);

                    } else {
                        prioritizedProducts.add(products.get(possibleIndices.get(0)));
                        productsCopy.clear();
                    }
                }
            default:
                break;
        }
        return prioritizedProducts;
    }
    
    public List<Product> prioritize2(List<Product> products) {
        int size = products.size();
        List<Product> prioritizedProducts = new ArrayList<Product>(size);
        List<Product> productsCopy = new ArrayList<Product>(products);
        double[][] distancesMatrix = new double[size][size];

        lastFitnessComputed = 0;
        // Computation of the distances
        for (int i = 0; i < distancesMatrix.length; i++) {
            for (int j = 0; j < distancesMatrix.length; j++) {
                distancesMatrix[i][j] = -1;
                if (j > i) {
                    switch (distanceMethod) {
                        case JACCARD_DISTANCE:
                            double dist = DistancesUtil.getJaccardDistance(productsCopy.get(i), productsCopy.get(j));
                            lastFitnessComputed += dist;
                            distancesMatrix[i][j] = dist;
                            break;
                        default:
                            ;
                    }

                }

            }
        }

        // Selection
        switch (searchMethod) {
            case GREEDY_SEARCH:
                while (!productsCopy.isEmpty()) {
                    if (productsCopy.size() != 1) {
                        double dmax = -1;
                        int toAddIIndex = -1;
                        int toAddJIndex = -1;
                        for (int i = 0; i < distancesMatrix.length; i++) {
                            for (int j = 0; j < distancesMatrix.length; j++) {
                                if (j > i) {
                                    if (distancesMatrix[i][j] > dmax) {
                                        dmax = distancesMatrix[i][j];
                                        toAddIIndex = i;
                                        toAddJIndex = j;
                                    }
                                }
                            }
                        }

                        Product pi = products.get(toAddIIndex);
                        Product pj = products.get(toAddJIndex);
                        prioritizedProducts.add(pi);
                        prioritizedProducts.add(pj);
                        productsCopy.remove(pi);
                        productsCopy.remove(pj);

                        for (int i = 0; i < distancesMatrix.length; i++) {
                            distancesMatrix[toAddIIndex][i] = distancesMatrix[i][toAddIIndex] = distancesMatrix[i][toAddJIndex] = distancesMatrix[toAddJIndex][i] = -1;
                        }
                    } else {
                        prioritizedProducts.add(productsCopy.get(0));
                        productsCopy.clear();
                    }
                }
                break;

            case NEAR_OPTIMAL_SEARCH:
                List<Integer> possibleIndices = new ArrayList<Integer>();
                List<Integer> doneIndices = new ArrayList<Integer>();
                for (int i = 0; i < size; i++) {
                    possibleIndices.add(i);

                }
                double maxDistance = -1;
                int toAddIIndex = -1;
                int toAddJIndex = -1;
                for (int i = 0; i < distancesMatrix.length; i++) {
                    for (int j = 0; j < distancesMatrix.length; j++) {
                        if (j > i) {
                            if (distancesMatrix[i][j] > maxDistance) {
                                maxDistance = distancesMatrix[i][j];
                                toAddIIndex = i;
                                toAddJIndex = j;
                            }
                        }
                    }
                }
                Product pi = products.get(toAddIIndex);
                Product pj = products.get(toAddJIndex);

                prioritizedProducts.add(pi);
                prioritizedProducts.add(pj);
                productsCopy.remove(pi);
                productsCopy.remove(pj);
                possibleIndices.remove((Integer) toAddIIndex);
                possibleIndices.remove((Integer) toAddJIndex);
                doneIndices.add(toAddIIndex);
                doneIndices.add(toAddJIndex);


                while (!productsCopy.isEmpty()) {
                    if (prioritizedProducts.size() >= 2000){
                        return prioritizedProducts;
                    }
                    
                    
                    if (possibleIndices.size() > 1) {
                        double maxDist = -1;
                        int toAdd = -1;
                        for (Integer i : possibleIndices) {

                            double distance = 0;
                            for (Integer j : doneIndices) {
                                distance += (j > i) ? distancesMatrix[i][j] : distancesMatrix[j][i];
                            }
                            if (distance > maxDist) {
                                maxDist = distance;
                                toAdd = i;
                            }
                        }
                        Product p = products.get(toAdd);

                        prioritizedProducts.add(p);
                        productsCopy.remove(p);
                        possibleIndices.remove((Integer) toAdd);
                        doneIndices.add(toAdd);

                    } else {
                        prioritizedProducts.add(products.get(possibleIndices.get(0)));
                        productsCopy.clear();
                    }
                }
            default:
                break;
        }
        return prioritizedProducts;
    }

    public static double getJaccardFitnessSum(double[][] distancesMatrix, int max) {
        double sum = 0;
        for (int i = 0; i < max; i++) {
            for (int j = 0; j < max; j++) {
                if (j > i) {
                    sum += distancesMatrix[i][j];
                }
            }
        }
        return sum;
    }

    public static double getJaccardFitnessSum(List<Product> products) {
        double sum = 0;
        int size = products.size();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (j > i) {
                    sum += DistancesUtil.getJaccardDistance(products.get(i), products.get(j));
                }
            }
        }
        return sum;
    }

    public static double getBinaryDistance(List<Product> products) {
        double sum = 0;
        int size = products.size();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (j > i) {
                    sum += DistancesUtil.getDiffPairs(products.get(i), products.get(j));
                }
            }
        }
        return sum;
    }
}