package utility;

import clustering.Cluster;
import entities.Entry;
import java.util.ArrayList;

public class ClusteringSilhouetteInternalEvaluation
{
    private Cluster[] clustering;
    private int datasetSize;

    private String distanceMatrixPath;
    private double[][] distanceMatrix; //UK-medoids matrix

    private ArrayList<double[]> aji; //arrayList for a_ji 
    private ArrayList<double[]> bji; //arrayList for b_ji
    private ArrayList<double[]> sji; //arrayList for s_ji
    
    private double[] sj; //array for s_j
    
    private double s; //overall silhouette

    public ClusteringSilhouetteInternalEvaluation(Cluster[] clustering, String distanceMatrixPath, int datasetSize)
    {
        this.datasetSize = datasetSize;
        this.clustering = clustering;
        
        this.aji = new ArrayList<double[]>();
        this.bji = new ArrayList<double[]>();
        this.sji = new ArrayList<double[]>();
        
        this.sj = new double[this.clustering.length];
        
        this.distanceMatrixPath = distanceMatrixPath;
        DistancePersistenceManager dpm = new DistancePersistenceManager(distanceMatrixPath, 'r');
        this.distanceMatrix = dpm.readData(distanceMatrixPath, datasetSize);
    }

    public ClusteringSilhouetteInternalEvaluation(Cluster[] clustering, double[][] distancesMatrix, int datasetSize)
    {
        this.datasetSize = datasetSize;
        this.clustering = clustering;
        
        this.aji = new ArrayList<double[]>();
        this.bji = new ArrayList<double[]>();
        this.sji = new ArrayList<double[]>();
        
        this.sj = new double[this.clustering.length];

        this.distanceMatrix = distancesMatrix;
    }

    public double evaluate()
    {
        computeAji();
        computeBji();
        computeSji();
        computeSj();
        computeS();
        return this.getQuality();
    }
    
    private void computeAji()
    {
        for(int k=0 ; k<this.clustering.length ; k++)
        {            
            ArrayList<Entry> objects = this.clustering[k].getObjects();
            double[] a_ji = new double[objects.size()];
            for(int i=0 ; i<objects.size() ; i++)
            {
                Entry oi = objects.get(i);
                int idi = Integer.parseInt(oi.getId());
                
                double sumDist = 0.0;
                
                for(int j=0 ; j<objects.size() ; j++)
                {
                    if(j!=i)
                    {
                        Entry oj = objects.get(j);
                        int idj = Integer.parseInt(oj.getId());
                        double dist = (idi>idj)?this.distanceMatrix[idi][idj]:this.distanceMatrix[idj][idi];
                        if (Double.isNaN(dist))
                        {
                            System.out.println("Distance NaN. i="+idi+" j="+idj);
                        }
                        sumDist += dist;
                    }
                }
                a_ji[i] = sumDist/(objects.size()-1);
            }
            this.aji.add(k, a_ji);
        }        
    }

    private void computeBji()
    {
        for(int k=0 ; k<this.clustering.length ; k++)
        {
            ArrayList<Entry> objects = this.clustering[k].getObjects();
            double[] b_ji = new double[objects.size()];
            
            for(int i=0 ; i<objects.size() ; i++)
            {
                Entry oi = objects.get(i);
                int idi = Integer.parseInt(oi.getId());
                
                double distMin = Double.POSITIVE_INFINITY;
                
                for(int n=0 ; n<this.clustering.length ; n++)
                {                    
                    if(n!=k)
                    {
                        ArrayList<Entry> obj2 = this.clustering[n].getObjects();
                        double sumDist = 0.0;
                        
                        for(int j=0 ; j<obj2.size() ; j++)
                        {
                            Entry oj = obj2.get(j);
                            int idj = Integer.parseInt(oj.getId());
                            double dist = (idi>idj)?this.distanceMatrix[idi][idj]:this.distanceMatrix[idj][idi];
                            sumDist += dist;
                        }
                        
                        sumDist = sumDist/obj2.size();
                        if(sumDist < distMin)
                        {
                            distMin = sumDist;
                        }
                    }                    
                }                
                b_ji[i] = distMin;                
            }
            this.bji.add(k, b_ji);
        }
        
    }
    private void computeSji()
    {
        for(int k=0 ; k<this.clustering.length ; k++)
        {
            double[] s_ji = new double[this.clustering[k].getObjects().size()];
            for(int i=0 ; i<s_ji.length ; i++)
            {
                double a = this.aji.get(k)[i];
                double b = this.bji.get(k)[i];
                double max = (a>b)? a : b;
                
                s_ji[i] = (b-a)/max;
                if(Double.isNaN(s_ji[i]))
                {
                    System.out.println("S_ij is NaN. a="+a+" b="+b);                    
                }
            }
            this.sji.add(k,s_ji);
        }
    }    
    
    private void computeSj()
    {
        for(int k=0 ; k<this.sj.length ; k++)
        {
            double[] s_i = this.sji.get(k);
            for(int i=0 ; i<s_i.length ; i++)
            {
                this.sj[k] += s_i[i];
            }
            
            this.sj[k] /= s_i.length;
        }
    }
    
    private void computeS() 
    {
        this.s = 0.0;
        
        for(int k=0 ; k<this.sj.length ; k++)
        {
            s += this.sj[k];
        }
        this.s /= this.sj.length; 
    }

    


    public double getQuality()
    {
        return this.s;
    }
}
