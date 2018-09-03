package utility;

import clustering.Cluster;
import java.util.ArrayList;

public class DendrogramEvaluation 
{
    private ArrayList<Cluster[]> dendrogram;
    int datasetSize;
    double[][] objDist;
    double objDistMean;
    int[][] dendrDist;
    double dendrDistMean;
    
    
    public DendrogramEvaluation(ArrayList<Cluster[]> dendrogram, String distancesPath)
    {
        this.dendrogram = dendrogram;
        datasetSize = dendrogram.get(0).length;
        DistancePersistenceManager dpm = new DistancePersistenceManager(distancesPath, 'r');
        objDist = dpm.readData(distancesPath, dendrogram.get(0).length);
    }
    
    /*
     * Evaluation of the goodness of a dendrogram by means of the Cophenetic coefficient
     * 
     */    
    public double evaluate()
    {
       Cluster[] dataset = dendrogram.get(0);
            
       objDistMean = 0.0;
       
       dendrDist = new int[datasetSize][datasetSize];
       dendrDistMean = 0.0;
       
       for(int i=0 ; i<datasetSize ; i++)
       {
           for(int j=0 ; j<i ; j++)
           {              
               objDistMean += objDist[i][j];
               
               boolean found=false;
               int level = datasetSize;
               for(int l=1 ; !found && l<this.dendrogram.size() ; l++)
               {
                   Cluster[] c = dendrogram.get(l);
                   for(int k=0 ; !found && k<c.length ; k++)
                   {
                       if(c[k].getObjects().contains(dataset[i].getObjects().get(0)))
                       {
                           if(c[k].getObjects().contains(dataset[j].getObjects().get(0)))
                           {
                               found=true;
                               level = l;
                           }
                       }
                   }
               }
               dendrDist[i][j] = level;
               dendrDistMean += level;            
           }
       }
       objDistMean /=datasetSize*((datasetSize-1)/2);
       dendrDistMean /= datasetSize*((datasetSize-1)/2);       
       
       return copheneticCorrelation();
       
    }

    private double copheneticCorrelation()
    {
        double num = 0.0;
        double den1 = 0.0;
        double den2 = 0.0;
        for(int i=0 ; i<datasetSize ; i++)
        {
            for(int j=0 ; j<i ; j++)
            {
                num += (this.objDist[i][j]-this.objDistMean)*(this.dendrDist[i][j]-this.dendrDistMean);
                den1 +=  (this.objDist[i][j]-this.objDistMean)*(this.objDist[i][j]-this.objDistMean);
                den2 += (this.dendrDist[i][j]-this.dendrDistMean)*(this.dendrDist[i][j]-this.dendrDistMean);
            }
        }
        return num/(Math.sqrt(den1*den2));        
    }
}
