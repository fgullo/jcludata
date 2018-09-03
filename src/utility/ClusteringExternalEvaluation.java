package utility;

import clustering.*;
import entities.Classification;
import entities.Entry;
import java.util.ArrayList;

public class ClusteringExternalEvaluation
{
    private Cluster[] clustering;
    private Classification classification;

    private double precision=-1;
    private double recall=-1;
    private double fmeasure=-1;
    private double fmeasureEnhanced=-1;

    private double[][] localPrecision;
    private double[][] localRecall;

    private int nObjects;

    public ClusteringExternalEvaluation(Cluster[] clustering, Classification classification)
    {
        this.clustering = clustering;
        this.classification = classification;

        nObjects = 0;
        for (int i=0; i<classification.getNumberOfClasses(); i++)
        {
            nObjects += classification.getNumberOfObjectsInClass(i);
        }
    }

    public double computePrecision()
    {
        localPrecision = new double[clustering.length][classification.getNumberOfClasses()];

        for(int i=0 ; i<localPrecision.length ; i++)
        {
            ArrayList<Entry> objects = clustering[i].getObjects();

            for (int j=0 ; j<objects.size() ; j++)
            {
                Entry e = objects.get(j);
                localPrecision[i][e.getClassLabelID()] += ((double)1.0)/objects.size();
            }
        }

        precision = 0.0;
        for(int i=0 ; i<classification.getNumberOfClasses() ; i++)
        {
            double max = 0.0;
            for(int j=0 ; j<localPrecision.length ; j++)
            {
                if (localPrecision[j][i]>max)
                {
                    max = localPrecision[j][i];
                }
            }
            precision += max;
        }
        precision /= classification.getNumberOfClasses();

        if (Double.isNaN(precision) || Double.isInfinite(precision) || precision < 0.0)
        {
            throw new RuntimeException("ERROR: precision must be greater than or equal to zero---precision="+precision);
        }

        return precision;
    }

    public double computeRecall()
    {
        localRecall = new double[clustering.length][classification.getNumberOfClasses()];
        for(int i=0 ; i<localRecall.length ; i++)
        {
            ArrayList<Entry> objects = clustering[i].getObjects();

            for (int j=0 ; j<objects.size() ; j++)
            {
                Entry e = objects.get(j);
                localRecall[i][e.getClassLabelID()] += ((double)1.0)/classification.getNumberOfObjectsInClass(e.getClassLabelID());
            }
        }

        recall = 0.0;
        for(int i=0 ; i<classification.getNumberOfClasses() ; i++)
        {
            double max = 0.0;
            for(int j=0 ; j<localRecall.length ; j++)
            {
                if (localRecall[j][i]>max)
                {
                    max = localRecall[j][i];
                }
            }
            recall += max;
        }
        recall /= classification.getNumberOfClasses();

        if (Double.isNaN(recall) || Double.isInfinite(recall) || recall < 0.0)
        {
            throw new RuntimeException("ERROR: recall must be greater than or equal to zero---recall="+recall);
        }

        return recall;

    }

    public double computeFMeasure()
    {
        if(precision==-1 || recall==-1)
        {
            computePrecision();
            computeRecall();
        }
        if (precision == 0 || recall == 0)
        {
            fmeasure = 0;
        }
        else
        {
            fmeasure = 2*precision*recall/(precision + recall);
        }

        if (Double.isNaN(fmeasure) || Double.isInfinite(fmeasure) || fmeasure < 0.0)
        {
            throw new RuntimeException("ERROR: fmeasure must be greater than or equal to zero---fmeasure="+fmeasure);
        }

        return fmeasure;
    }

    public double computeFMeasureEnhanced()
    {
        if(precision==-1 || recall==-1)
        {
            computePrecision();
            computeRecall();
        }

        double[][] localF = new double[clustering.length][classification.getNumberOfClasses()];
        for (int i=0; i<localF.length; i++)
        {
            for (int j=0; j<localF[i].length; j++)
            {
                double p = this.localPrecision[i][j];
                double r = this.localRecall[i][j];
                localF[i][j] = (p==0.0 || r==0.0)?0.0:2*p*r/(p+r);
            }
        }

        fmeasureEnhanced = 0.0;
        for(int i=0 ; i<classification.getNumberOfClasses() ; i++)
        {
            double max = 0.0;
            for(int j=0 ; j<localF.length ; j++)
            {
                if (localF[j][i]>max)
                {
                    max = localF[j][i];
                }
            }
            fmeasureEnhanced += classification.getNumberOfObjectsInClass(i)*max;
        }
        fmeasureEnhanced /= nObjects;

        if (Double.isNaN(fmeasureEnhanced) || Double.isInfinite(fmeasureEnhanced) || fmeasureEnhanced < 0.0)
        {
            throw new RuntimeException("ERROR: fmeasureEnhanced must be greater than or equal to zero---fmeasureEnhanced="+fmeasureEnhanced);
        }

        return fmeasureEnhanced;
    }
}
