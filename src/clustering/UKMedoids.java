package clustering;

import entities.Dataset;
import entities.Entry;
import java.util.ArrayList;
import utility.DistancePersistenceManager;

public class UKMedoids
{
    public final int MAX_ITERATIONS = 100;
    private Dataset d;
    
    private int iterations = 0;
    
    private double[][] distances;
        
    public UKMedoids(Dataset d)
    {
        this.d = d;
    }
    
    public Cluster[] execute(String distancePath, int k)
    {
        System.out.println("\nUK-medoids starting...\n");
        loadDistances(distancePath);
        Cluster[] clustering = new Cluster[k];
        
        boolean done = false;
        iterations = 0;
        
        Entry[] medoids = getInitialMedoids(k);
        int[] objectToMedoid = new int[d.getEntries().length];
        for (int i=0; i<objectToMedoid.length; i++)
        {
            objectToMedoid[i] = -1;
        }
        
        while(!done && iterations<MAX_ITERATIONS)
        {
            done = true;
            ArrayList[] clusters = new ArrayList[k];
            for (int i=0; i<clusters.length; i++)
            {
                clusters[i] = new ArrayList();
            }
            
            for (int i=0; i<objectToMedoid.length; i++)
            {
                int precAss = objectToMedoid[i];
                double minDist = Double.POSITIVE_INFINITY;
                for (int j=0; j<medoids.length; j++)
                {
                    if(medoids[j]==null)
                    {
                        System.out.println("Medoid "+j+" is null "+medoids.length);
                    }
                    double dist_ij = -1;
                    
                    if(i>=Integer.parseInt(medoids[j].getId()))//triangular distance matrix
                    {
                        dist_ij = this.distances[i][Integer.parseInt(medoids[j].getId())];
                        
                    }
                    else
                    {
                        dist_ij = this.distances[Integer.parseInt(medoids[j].getId())][i];
                    }                    
                    
                    if (dist_ij < minDist)
                    {
                        minDist = dist_ij;
                        objectToMedoid[i] = j;
                    }
                }
                
                if (objectToMedoid[i] != precAss)
                {
                    done = false;
                }
            }
            
            for (int i=0; i<objectToMedoid.length; i++)
            {
                Entry obj = d.getEntry(i);
                clusters[objectToMedoid[i]].add(obj);
            }
            
            for (int i=0; i<clusters.length; i++)
            {
                Cluster c = new Cluster(clusters[i], i);
                clustering[i] = c;
                
                if (c.getObjects().isEmpty())
                {
                    medoids[i] = getInitialMedoids(1)[0];
                }
                else
                {
                    medoids[i] = c.computeMedoid(distances);
                }
            }
            
            iterations++;
        }
        
        return clustering;        
    }
    
    public int getIterations()
    {
        return iterations;
    }
    
    public void computeMultivariateDistances(DistancePersistenceManager dpm, int regionSamples) 
    {
        distances = new double[d.getSize()][d.getSize()];
        int nSamples = regionSamples;
        double[] probSums = new double[d.getSize()];
        
        
       ArrayList<double[][]> entrySamples = new ArrayList<double[][]>(d.getEntries().length);
       for (int i=0; i<d.getEntries().length; i++)
       {
           Entry e = d.getEntries()[i];
           double[][] list = new double[nSamples][d.getEntry(0).getValues().length+1];
           
           for (int j=0; j<list.length; j++)
           {
               double[] tmp = e.getRandomSample();
               double p = tmp[tmp.length-1];

               if (Double.isInfinite(p) || Double.isNaN(p) || p < -0.0000000001 || p > 1.0000000001)
               {
                   throw new RuntimeException("Any probability value must be within (0,1]---p="+p);
               }

               list[j] = tmp;
               probSums[i] += p;
           }

           if (Double.isInfinite(probSums[i]) || Double.isNaN(probSums[i]))
           {
               throw new RuntimeException("The sum of all sample probabilities must be within (0,1]---sum="+probSums[i]);
           }

           entrySamples.add(i, list);
       }   
        
        
        
        
        for(int i=0 ; i<distances.length ; i++)
        {                   
            Entry e1 = d.getEntry(i);
            distances[Integer.parseInt(e1.getId())][Integer.parseInt(e1.getId())] = 0;  
            
            int dimensions = e1.getValues().length;
            
            double[][] samples1 = entrySamples.get(i);
            
           for(int j=0 ; j<i ; j++)
            {
                Entry e2 = d.getEntry(j);
                
                double[][] samples2 = entrySamples.get(j);
                
                double distance = 0.0;
                for(int s=0 ; s<samples1.length ; s++)
                {
                    double prob1 = samples1[s][samples1[s].length-1]/probSums[i];
                    
                    for (int k=0 ; k<samples2.length ; k++)
                    {
                        double prob2 = samples2[k][samples2[k].length-1]/probSums[j];
                        double dTemp = 0.0;
                        for (int z=0 ; z<samples1[s].length-1 ; z++)
                        {
                            dTemp += (samples1[s][z]-samples2[k][z])*(samples1[s][z]-samples2[k][z]);
                        }
                        distance += dTemp*prob1*prob2;
                    }
                }
                distances[Integer.parseInt(e1.getId())][Integer.parseInt(e2.getId())] = distance; 
                dpm.writeData(distance);
            }
            dpm.newLine();
        }
    }

    private Entry[] getInitialMedoids(int k) 
    {
        Entry[] centers = new Entry[k];
        
        int dimDataset = d.getEntries().length;
        boolean[] chosenIndex = new boolean[dimDataset];
        
        for (int i=0 ; i<centers.length ; i++)
        {
            int index = 0;
            do
            {
                index = (int)Math.rint(Math.random()*(dimDataset-1));
            }
            while (chosenIndex[index]);
            chosenIndex[index] = true;                
            centers[i] = d.getEntry(index);
        }        
        return centers;
    }
    
    private Entry[] getInitialMedoidsPAM(int k) 
    {
        Entry[] centri = new Entry[k];
        
        int dimDataset = d.getEntries().length;
        boolean[] chosenIndex = new boolean[dimDataset];

        int firstMedoid = -1;
        double distMin = Double.POSITIVE_INFINITY;
        for(int i=0 ; i<distances.length ; i++ )
        {
            double distTmp = 0.0;
            for(int j=0 ; j<i ; j++)
            {
                distTmp += distances[i][j];
            }
            for(int j=i+1 ; j<distances.length ; j++)
            {
                distTmp += distances[j][i];
            }
            if(distTmp<distMin)
            {
                distMin = distTmp;
                firstMedoid = i;
            }
        }
        chosenIndex[firstMedoid] = true;
        centri[0] = d.getEntry(firstMedoid);
        
        int chosenMedoids = 1;
        
        while(chosenMedoids<k)
        {
            int nextMedoid = -1;
            double gainMax = Double.NEGATIVE_INFINITY;
            
            for(int i=0 ; i<distances.length ; i++)
            {
                if (!chosenIndex[i])
                {
                    double gainTmp = 0.0;
                    for(int j=0 ; j<distances.length; j++)
                    {
                        if(!chosenIndex[j] && j!=i)
                        {
                            double dMinJ = Double.POSITIVE_INFINITY;

                            for(int z=0 ; z<chosenIndex.length ; z++)
                            {
                                if (chosenIndex[z])
                                {
                                    double dTmp = (z<j)? distances[z][j] : distances[j][z];
                                    if (dMinJ>dTmp)
                                    {
                                        dMinJ = dTmp;
                                    }
                                }

                            }
                            double distIJ = (i<j)? distances[i][j] : distances[j][i];
                            if((dMinJ - distIJ)>0.0)
                            {
                                gainTmp += (dMinJ - distIJ);
                            }
                        }                                        
                    }
                    if(gainTmp>gainMax)
                    {
                        gainMax = gainTmp;
                        nextMedoid = i;
                    }
                }                
            }
            
            chosenIndex[nextMedoid] = true;
            centri[chosenMedoids] = d.getEntry(nextMedoid);
            chosenMedoids++;
        }
        
        
        
        
        return centri;
    }
    
    private void loadDistances(String path)
    {
        try
        {
            DistancePersistenceManager dpm = new DistancePersistenceManager(path,'r');
            distances = dpm.readData(path, d.getSize());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public long offlineTime(int regionSamples) 
    {
        long start = System.currentTimeMillis();
        distances = new double[d.getSize()][d.getSize()];
        int nSamples = regionSamples;
        
        
       ArrayList<double[][]> entrySamples = new ArrayList<double[][]>(d.getEntries().length);
       for (int i=0; i<d.getEntries().length; i++)
       {
           Entry e = d.getEntries()[i];
           double[][] list = new double[nSamples][d.getEntry(0).getValues().length+1];
           
           for (int j=0; j<list.length; j++)
           {
               list[j] = e.getRandomSample();
           }
           
           entrySamples.add(i, list);
       }   
        
        
        
        
        for(int i=0 ; i<distances.length ; i++)
        {                   
            Entry e1 = d.getEntry(i);
            distances[Integer.parseInt(e1.getId())][Integer.parseInt(e1.getId())] = 0;  
            
            int dimensions = e1.getValues().length;
            
            double[][] samples1 = entrySamples.get(i);
            
           for(int j=0 ; j<i ; j++)
            {
                Entry e2 = d.getEntry(j);
                
                double[][] samples2 = entrySamples.get(j);
                
                double distance = 0.0;
                for(int s=0 ; s<samples1.length ; s++)
                {
                    double prob1 = samples1[s][samples1[s].length-1];
                    
                    for (int k=0 ; k<samples2.length ; k++)
                    {
                        double prob2 = samples2[k][samples2[k].length-1];
                        double dTemp = 0.0;
                        for (int z=0 ; z<samples1[s].length-1 ; z++)
                        {
                            dTemp += (samples1[s][z]-samples2[k][z])*(samples1[s][z]-samples2[k][z]);
                        }
                        distance += dTemp*prob1*prob2;
                    }
                }
                distances[Integer.parseInt(e1.getId())][Integer.parseInt(e2.getId())] = distance; 
            }
        }
       long stop = System.currentTimeMillis();
       
       return stop-start;
    }
}