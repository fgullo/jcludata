package clustering;

import entities.Dataset;
import entities.Entry;

import entities.UnivariateEntry;
import java.util.ArrayList;
import java.util.TreeSet;
import statistics.PDF;

public abstract class UAHC_Commons
{
    protected Dataset d;

    protected ArrayList<Cluster[]> dendrogram;
    protected int dendrogrammIndex = 0;

    protected TreeSet<Triple> Q;

    protected long offlineTime;

    protected double[][] domainSampleSet;
    protected double[][][] objectSampleLists;
    protected double[][] objectSumOfProbabilities;
    protected double[][][] EV_LB_UB;
    
    protected double[][] distMatrix;

    //private boolean sMode = true;
    //private String btcMode = "metric";

    protected int n;
    protected int m;
    protected int S;
    
    protected double[] ITadequacies;
    protected double ITadequacySum;
    protected int ITadequacyCount;


    protected void computeEVLBUB()
    {
        this.EV_LB_UB = new double[n][m][4];
        for (int i=0; i<n; i++)
        {
            UnivariateEntry e = (UnivariateEntry)this.d.getEntry(i);
            for (int j=0; j<m; j++)
            {
                PDF pdf = e.getUValues()[j];
                this.EV_LB_UB[i][j][0] = pdf.getMeanValue();
                this.EV_LB_UB[i][j][1] = pdf.getLowerBound();
                this.EV_LB_UB[i][j][2] = pdf.getUpperBound();
                this.EV_LB_UB[i][j][3] = Math.sqrt(pdf.getVariance());
            }
        }
    }

    public Cluster[] execute(int numberOfClusters, boolean computeDendrogram)
    {
        long start = System.currentTimeMillis();
        
        if (computeDendrogram)
        {
            this.ITadequacies = new double[n-1];
        }
        else
        {
            this.ITadequacies = new double[n-numberOfClusters+1];
        }
        this.ITadequacySum = 0.0;
        this.ITadequacyCount = 0;
        int it = 0;
        
        sampling();

        //initial clustering: all singletons
        ArrayList<int[]> clustering = new ArrayList<int[]>(n);
        for (int i=0; i<n; i++)
        {
            clustering.add(new int[]{i});
        }

        //priority queue
        this.Q = new TreeSet<Triple>();

        //initial pairwise distance matrix
        //and initial distances into the priority queue
        this.distMatrix = new double[n][n];
        for (int i1=0; i1<n; i1++)
        {
            for (int i2=0; i2<n; i2++)
            {
                if (i2<i1)
                {
                    distMatrix[i1][i2] = computeDistanceBetweenObjectsOrPrototypes(i1,i2);
                    Triple t = new Triple(i1,i2,distMatrix[i1][i2],false);
                    boolean ctrl = this.Q.add(t);
                    if (!ctrl)
                    {
                        throw new RuntimeException("The triple has not been inserted into the priority queue");
                    }
                }
                else if (i2==i1)
                {
                    distMatrix[i1][i2] = 0.0;
                }
                else
                {
                    distMatrix[i1][i2] = -1.0;
                }
            }
        }
        if (this.Q.size() != n*(n-1)/2)
        {
            throw new RuntimeException("The size of the priority queue must be equal to "+(n*(n-1)/2)+"---size="+Q.size());
        }
        
        this.ITadequacies[it] = this.ITadequacySum/this.ITadequacyCount;

        //add level to dendrogram (if required)
        if(computeDendrogram)
        {
            this.dendrogrammIndex = 0;
            this.dendrogram = new ArrayList<Cluster[]>(n-1);
            addLevelToDendrogram(clustering);
        }

        long stop = System.currentTimeMillis();
        this.offlineTime = stop-start;

        //System.out.println("\nUAHC\n");

        int nClusters = clustering.size();
        while ((computeDendrogram && nClusters>1) || (!computeDendrogram && nClusters>numberOfClusters) )
        {
            //System.out.println("Computing clustering with "+(clusters-1)+" clusters");

            this.ITadequacySum = 0.0;
            this.ITadequacyCount = 0;
            it++;
            
            int oldQsize = this.Q.size();
            Triple minimum  = this.Q.pollFirst(); //retrieve and remove the pair of clusters having minimum distance
            int index1 = minimum.getId1();
            int index2 = minimum.getId2();
            if (index1 < index2)
            {
                int tmp = index1;
                index1 = index2;
                index2 = tmp;
            }

            int[] clust1 = clustering.get(index1);
            int[] clust2 = clustering.get(index2);
            if (clust1==null || clust2==null)
            {
                throw new RuntimeException("Clusters already merged: how is it possible???---id1="+index1+", id2="+index2);
            }

            //merging clusters
            int size1 = clust1.length;
            int size2 = clust2.length;
            int sizeNew = size1+size2;
            int[] newClust = new int[sizeNew];
            int x=0;
            for (int i=0; i<clust1.length; i++)
            {
                newClust[x] = clust1[i];
                x++;
            }
            for (int i=0; i<clust2.length; i++)
            {
                newClust[x] = clust2[i];
                x++;
            }
            clustering.set(index1, newClust);
            clustering.set(index2, null);

            //update prototypes (sample lists, expected values, lower bounds, upper bounds)
            if (this.EV_LB_UB != null)
            {
                for (int j=0; j<m; j++)
                {
                    double mu1 = this.EV_LB_UB[index1][j][0];
                    double mu2 = this.EV_LB_UB[index2][j][0];
                    double muNew = ((double)size1*mu1+size2*mu2)/sizeNew;
                    this.EV_LB_UB[index1][j][0] = muNew;

                    double lb1 = this.EV_LB_UB[index1][j][1];
                    double lb2 = this.EV_LB_UB[index2][j][1];
                    double lbNew = (lb1<lb2)?lb1:lb2;
                    this.EV_LB_UB[index1][j][1] = lbNew;

                    double ub1 = this.EV_LB_UB[index1][j][2];
                    double ub2 = this.EV_LB_UB[index2][j][2];
                    double ubNew = (ub1>ub2)?ub1:ub2;
                    this.EV_LB_UB[index1][j][2] = ubNew;
                }

                for (int j=0; j<m; j++)
                {
                    double sum = 0.0;
                    for (int s=0; s<S; s++)
                    {
                        double prob1 = this.objectSampleLists[s][j][index1];
                        double prob2 = this.objectSampleLists[s][j][index2];
                        double probNew = ((double)size1*prob1+size2*prob2)/sizeNew;

                        sum += probNew;

                        this.objectSampleLists[s][j][index1] = probNew;
                    }

                    this.objectSumOfProbabilities[index1][j] = sum;
                }
            }

            //internal loop: update distances from the new prototype
            for (int y=0; y<clustering.size(); y++)
            {
                if (y!=index1 && y!=index2 && clustering.get(y)!=null)
                {
                    int a1 = (y>index2)?y:index2;
                    int a2 = (y<index2)?y:index2;
                    Triple t2 = new Triple(a1,a2,distMatrix[a1][a2],false);
                    boolean ctrl = this.Q.remove(t2);
                    if (!ctrl)
                    {
                        throw new RuntimeException("The triple to be removed is not in the priority queue, how is it possible???");
                    }

                    a1 = (y>index1)?y:index1;
                    a2 = (y<index1)?y:index1;
                    Triple t1 = new Triple(a1,a2,distMatrix[a1][a2],false);
                    ctrl = this.Q.remove(t1);
                    if (!ctrl)
                    {
                        throw new RuntimeException("The triple to be removed is not in the priority queue, how is it possible???");
                    }

                    //double distNew = computePrototypeDeltaDistance(a1,a2);
                    double distNew = updateDistanceBetweenClusters(index1, index2, y, size1, size2);

                    distMatrix[a1][a2] = distNew;
                    Triple tNew = new Triple(a1,a2,distMatrix[a1][a2],false);
                    ctrl = this.Q.add(tNew);
                    if (!ctrl)
                    {
                        throw new RuntimeException("The triple to be added is already in the priority queue, how is it possible???");
                    }

                }
            }
            int newQsize = this.Q.size();
            if (newQsize != oldQsize-nClusters+1)
            {
                throw new RuntimeException("The new size of the priority queue must be equal to "+(oldQsize-nClusters+1)+"---newSize="+newQsize);
            }

            nClusters--;
            if (computeDendrogram)
            {
                addLevelToDendrogram(clustering);
            }
            
            this.ITadequacies[it] = this.ITadequacySum/this.ITadequacyCount;
        }

        Cluster[] clusteringRet = null;
        if(computeDendrogram)
        {
            return this.dendrogram.get(this.dendrogram.size()-1);
        }
        else
        {
            clusteringRet = new Cluster[numberOfClusters];
            int index = 0;
            for(int i=0; i<clustering.size(); i++)
            {
                int[] cluster = clustering.get(i);
                if(cluster != null)
                {
                    ArrayList<Entry> al = new ArrayList<Entry>(cluster.length);
                    for (int j=0; j<cluster.length; j++)
                    {
                        al.add(j, this.d.getEntry(cluster[j]));
                    }
                    clusteringRet[index] = new Cluster(al,index);
                    index++;
                }
            }
        }

        //printDeltaContributions();

        return clusteringRet;
    }

    protected void sampling()
    {
        //compute domain sample set
        this.domainSampleSet = new double[S][m];

        for (int j=0; j<m; j++)
        {
            for (int s=0; s<S; s++)
            {
                double[] samplesTmp = new double[n];
                for (int i=0; i<n; i++)
                {
                    UnivariateEntry e = (UnivariateEntry)this.d.getEntry(i);
                    samplesTmp[i] = e.getUValues()[j].getRandomSample();
                }

                int iMax = -1;
                double probMax = Double.NEGATIVE_INFINITY;

                for (int i=0; i<n; i++)
                {
                    double probTmp = 0.0;
                    for (int i2=0; i2<d.getSize(); i2++)
                    {
                        UnivariateEntry e = (UnivariateEntry)this.d.getEntry(i2);
                        double value = e.getUValues()[j].calculate(samplesTmp[i]);
                        if (Double.isInfinite(value) || Double.isNaN(value) || value < -0.0000001 || value > 1.0000001)
                        {
                            throw new RuntimeException("Value must be within [0,1]---value="+value);
                        }
                        if (value < 0.0) value = 0.0;
                        if (value > 1.0) value = 1.0;

                        probTmp += value;
                    }
                    probTmp /= d.getSize();

                    if (probTmp > probMax)
                    {
                        probMax = probTmp;
                        iMax = i;
                    }
                }

                domainSampleSet[s][j] = samplesTmp[iMax];
            }
        }

        //compute object sample lists
        this.objectSampleLists = new double[S][m][n];
        this.objectSumOfProbabilities = new double[n][m];
        for (int i=0; i<n; i++)
        {
            for (int j=0; j<m; j++)
            {
                double sum = 0.0;
                for (int s=0; s<S; s++)
                {
                    double value = ((UnivariateEntry)this.d.getEntry(i)).getUValues()[j].calculate(this.domainSampleSet[s][j]);
                    if (Double.isInfinite(value) || Double.isNaN(value) || value < -0.0000001 || value > 1.0000001)
                    {
                        throw new RuntimeException("Value must be within [0,1]---value="+value);
                    }
                    if (value < 0.0) value = 0.0;
                    if (value > 1.0) value = 1.0;

                    sum += value;

                    this.objectSampleLists[s][j][i] = value;
                }

                this.objectSumOfProbabilities[i][j] = sum;
            }
        }
    }

    protected abstract double computeDistanceBetweenObjectsOrPrototypes(int i1, int i2);
    
    protected abstract double updateDistanceBetweenClusters(int merged1, int merged2, int x, int sizeMerged1, int sizeMerged2);

    protected void addLevelToDendrogram(ArrayList<int[]> al)
    {
        int nCluster = 0;
        for (int i=0; i<al.size(); i++)
        {
            if (al.get(i) != null)
            {
                nCluster++;
            }
        }

        if (nCluster != n-this.dendrogrammIndex)
        {
            throw new RuntimeException("The number of clusters must be equal to "+(n-this.dendrogrammIndex)+"---nCluster="+nCluster);
        }

        Cluster[] level = new Cluster[nCluster];
        int x = 0;
        for (int i=0; i<al.size(); i++)
        {
            int[] tmp = al.get(i);
            if (tmp != null)
            {
                ArrayList<Entry> cluster = new ArrayList<Entry>(tmp.length);
                for (int j=0; j<tmp.length; j++)
                {
                    cluster.add(j, this.d.getEntry(tmp[j]));
                }
                level[x] = new Cluster(cluster,x);
                x++;
            }
        }

        this.dendrogram.add(this.dendrogrammIndex, level);
        this.dendrogrammIndex++;
    }


    //auxiliary class---the objects of this class are inserted into the priority queue
    protected class Triple implements Comparable
    {
        int id1;
        int id2;
        double distance;
        boolean LB;

        public Triple(int id1, int id2, double dist, boolean LB)
        {
            this.id1 = id1;
            this.id2 = id2;
            this.distance = dist;
            this.LB = LB;
        }

        public int getId1()
        {
            return this.id1;
        }

        public int getId2()
        {
            return this.id2;
        }

        public double getDistance()
        {
            return this.distance;
        }

        public void setLB(boolean b)
        {
            this.LB = b;
        }

        public void setDistance(double d)
        {
            this.distance = d;
        }

        public boolean equals(Object o)
        {
            if (! (o instanceof Triple))
            {
                return false;
            }

            return this.id1 == ((Triple)o).id1 && this.id2 == ((Triple)o).id2 && this.distance == ((Triple)o).distance;
        }

        public int compareTo(Object o)
        {
            if (this.distance < ((Triple)o).distance)
            {
                return -1;
            }

            if (this.distance > ((Triple)o).distance)
            {
                return 1;
            }

            if (this.distance == ((Triple)o).distance)
            {
                if (this.id1 < ((Triple)o).id1)
                {
                    return -1;
                }

                if (this.id1 > ((Triple)o).id1)
                {
                    return 1;
                }

                if (this.id1 == ((Triple)o).id1)
                {
                    if (this.id2 < ((Triple)o).id2)
                    {
                        return -1;
                    }

                    if (this.id2 > ((Triple)o).id2)
                    {
                        return 1;
                    }
                }
            }

            return 0;
        }
    }


    public ArrayList<Cluster[]> getDendrogram()
    {
        return this.dendrogram;
    }


    public long getOfflineTime()
    {
        return this.offlineTime;
    }
    
    public double[] getITadequacies()
    {
        return this.ITadequacies;
    }

    protected void printDeltaContributions()
    {

    }

    public double getDEVIT()
    {
        return 0.0;
    }

    public double getDEV()
    {
        return 0.0;
    }

    public double getDIT()
    {
        return 0.0;
    }

    public int getCountD()
    {
        return 0;
    }
}