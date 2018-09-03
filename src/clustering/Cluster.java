package clustering;

import entities.DeterministicEntry;
import entities.Entry;
import entities.MultivariateEntry;
import entities.UnivariateEntry;
import java.util.*;

public class Cluster 
{
    private int clusterID;
    private ArrayList<Entry> objects;
    private Entry centroid;
    private Entry medoid;
    
    public Cluster(ArrayList<Entry> objects, int clusterID)
    {
        this.objects = objects;
        this.clusterID = clusterID;
    }
    
    public ArrayList<Entry> getObjects()
    {
        return this.objects;
    }
    
    public Entry computeCentroid()
    {
        double[] finalMeans = new double[this.objects.get(0).getValues().length];
        
        for (int i=0 ; i<this.objects.size() ; i++)
        {
            Entry e = objects.get(i);
            double[] means = e.getMean();

            for (int j=0 ; j<means.length ; j++)
            {
                finalMeans[j] += means[j];
            }
        }
        
        for(int i=0 ; i<finalMeans.length ; i++)
        {
            finalMeans[i] /= this.objects.size();
        }
        
        Entry e = null;
        if (this.objects.get(0) instanceof UnivariateEntry)
        {
            e = new UnivariateEntry(null,null,finalMeans,null);
        }
        else if (this.objects.get(0) instanceof MultivariateEntry)
        {
            e = new MultivariateEntry(null,null,finalMeans,null);
        }
        else if (this.objects.get(0) instanceof DeterministicEntry)
        {
            e = new DeterministicEntry(null,null,finalMeans);
        }
        e.setClassLabelID(clusterID);
        
        
        return e;
    }
    
    
    public Entry computeMedoid(double[][] distances)
    {
        double[] candidateMedoids = new double[this.objects.size()];
        
        //true if object i belongs to this cluster
        boolean[] belongsTo = new boolean[distances.length];
        for (int i=0 ; i<this.objects.size() ; i++)
        {
            belongsTo[Integer.parseInt(this.objects.get(i).getId())] = true;
        }
        
        for (int i=0 ; i<this.objects.size() ; i++)
        {
            int currentID = Integer.parseInt(this.objects.get(i).getId());
            
            double distance = 0.0;
            
            for(int j=0 ; j<currentID ; j++)
            {
                if(belongsTo[j])
                {
                    distance += distances[currentID][j];
                }
            }
            for(int j=currentID+1 ; j<distances.length ; j++)
            {
                if(belongsTo[j])
                {
                    distance += distances[j][currentID];
                }
            }
            
            candidateMedoids[i] = distance/(this.objects.size()-1);             
        }
        
        int minIndex = 0;
        for (int i=1 ; i<candidateMedoids.length ; i++)
        {
            if (candidateMedoids[minIndex]>candidateMedoids[i])
            {
                minIndex = i;
            }
        }
        
        return medoid=this.objects.get(minIndex);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
