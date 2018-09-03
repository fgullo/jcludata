package utility;

import clustering.Cluster;

public class ClusteringInternalEvaluation
{
    private Cluster[] clustering;

    private String distanceMatrixPath;
    private double[][] distanceMatrix; //UK-medoids matrix

    private double[] intraDistances; //for every cluster
    private double[][] interDistances; //for every pair of clusters
    private double intra;
    private double inter;
    private double quality; // Q=INTER-INTRA

    public ClusteringInternalEvaluation(Cluster[] clustering, String distanceMatrixPath, int datasetSize)
    {
        this.clustering = clustering;
        this.intraDistances = new double[clustering.length];
        this.interDistances = new double[clustering.length][clustering.length];
        
        this.distanceMatrixPath = distanceMatrixPath;
        DistancePersistenceManager dpm = new DistancePersistenceManager(distanceMatrixPath, 'r');
        this.distanceMatrix = dpm.readData(distanceMatrixPath, datasetSize);
        normalizeDistMatr();
    }

    public ClusteringInternalEvaluation(Cluster[] clustering, double[][] distanceMatrix, int datasetSize)
    {
        this.clustering = clustering;
        this.intraDistances = new double[clustering.length];
        this.interDistances = new double[clustering.length][clustering.length];

        this.distanceMatrix = distanceMatrix;
        normalizeDistMatr();
    }

    public double evaluate()
    {
        computeIntra();
        computeInter();
        
        if (inter-intra == 0)
        {
            System.out.println();
        }
        
        return quality=inter-intra;
    }

    private void computeIntra()
    {
        if (clustering.length == 0)
        {
            this.intra = -1.0;
        }
        else
        {
            for(int k=0 ; k<clustering.length ; k++)
            {
                double intraLocal = 0.0;
                for(int i=0 ; i<clustering[k].getObjects().size()-1 ; i++)
                {
                    for(int j=i+1 ; j<clustering[k].getObjects().size() ; j++)
                    {
                        int index1 = Integer.parseInt(clustering[k].getObjects().get(j).getId());
                        int index2 = Integer.parseInt(clustering[k].getObjects().get(i).getId());
                        double dist = (index1>index2)?this.distanceMatrix[index1][index2]:this.distanceMatrix[index2][index1];

                        if (Double.isNaN(dist) || Double.isInfinite(dist) || dist < 0.0)
                        {
                            throw new RuntimeException("ERROR: distance must be greater than or equal to zero---dist="+dist);
                        }

                        intraLocal += dist;

                    }
                }
                int clustSize = clustering[k].getObjects().size();
                if(clustSize>1)
                {
                    this.intraDistances[k] = intraLocal/(clustSize*(clustSize-1)/2);
                }
                else
                {
                    this.intraDistances[k] = intraLocal;
                }

                intra += this.intraDistances[k];
            }
            intra /= clustering.length;

            if (Double.isNaN(intra) || Double.isInfinite(intra) || intra < 0.0)
            {
                throw new RuntimeException("ERROR: intra-distance must be greater than or equal to zero---intra="+intra);
            }
        }
    }

    private void computeInter()
    {
        if (clustering.length == 0)
        {
            this.inter = -1.0;
        }
        else
        {
            int nonEmpty = 0;
            for(int k=0 ; k<clustering.length ; k++)
            {
                if(clustering[k].getObjects().size()>0)
                {
                    nonEmpty++;
                }
            }

            inter = 0.0;
            for(int k=0 ; k<clustering.length-1; k++)
            {
                int sizeC1 = clustering[k].getObjects().size();
                if (sizeC1>0)
                {
                    for(int t=k+1 ; t<clustering.length ; t++)
                    {
                        int sizeC2 = clustering[t].getObjects().size();
                        if (sizeC2>0)
                        {
                            double interTmp = 0.0;
                            for(int c1=0 ; c1<sizeC1 ; c1++)
                            {
                                for(int c2=0 ; c2<sizeC2 ; c2++)
                                {
                                    int index1 = Integer.parseInt(clustering[k].getObjects().get(c1).getId());
                                    int index2 = Integer.parseInt(clustering[t].getObjects().get(c2).getId());

                                    double dist = (index1>index2)?this.distanceMatrix[index1][index2]:this.distanceMatrix[index2][index1];

                                    if (Double.isNaN(dist) || Double.isInfinite(dist) || dist < 0.0)
                                    {
                                        throw new RuntimeException("ERROR: distance must be greater than or equal to zero---dist="+dist);
                                    }

                                    interTmp += dist;
                                }
                            }
                            interTmp /= sizeC1*sizeC2;
                            inter += interTmp;

                            this.interDistances[t][k] = interTmp;
                        }
                    }
                }
            }
            if (nonEmpty > 1)
            {
                inter /= nonEmpty*(nonEmpty-1)/2;
            }

            if (Double.isNaN(inter) || Double.isInfinite(inter) || inter < 0.0)
            {
                throw new RuntimeException("ERROR: inter-distance must be greater than or equal to zero---inter="+inter);
            }
        }
    }

    public double getIntra()
    {
        return this.intra;
    }

    public double getInter()
    {
        return this.inter;
    }

    public double getQuality()
    {
        return this.quality;
    }
    
    private void normalizeDistMatr()
    {
        double max = Double.NEGATIVE_INFINITY;
        
        for (int j=0; j<this.distanceMatrix[0].length; j++)
        {
            for (int i=j+1; i<this.distanceMatrix.length; i++)
            {
                if (this.distanceMatrix[i][j] > max)
                {
                    max = this.distanceMatrix[i][j];
                }
            }
        }
        
        if (max >= 0.0)
        {
            for (int j=0; j<this.distanceMatrix[0].length; j++)
            {
                for (int i=j+1; i<this.distanceMatrix.length; i++)
                {
                    this.distanceMatrix[i][j] /= max;
                }
            }
        }
    }
}
