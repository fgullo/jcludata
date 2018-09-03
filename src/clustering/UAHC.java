package clustering;

import entities.Dataset;

public class UAHC extends UAHC_Commons
{
    private double sumadq = 0.0;

    private double dEVIT = 0.0;
    private double dEV = 0.0;
    private double dIT = 0.0;
    private int dCount = 0;

    public UAHC(Dataset d, int nSamples)
    {
        this.d = d;
        this.S= nSamples;
        this.n = d.getSize();
        this.m = d.getEntrySize();
        computeEVLBUB();
    }

    protected double computeDistanceBetweenObjectsOrPrototypes(int i1, int i2)
    {
        double[] deltaIT = new double[m];
        double[] deltaEV = new double[m];
        double[] rhos = new double[m];
        double[] delta = new double[m];

        //deltaEV
        for (int j=0; j<m; j++)
        {
            double std1 = this.EV_LB_UB[i1][j][3];
            double std2 = this.EV_LB_UB[i2][j][3];
            
            if (std1 == std2 && this.EV_LB_UB[i1][j][0] == this.EV_LB_UB[i2][j][0])
            {
                deltaEV[j] = 1.0;
            }
            else
            {
                double a = this.EV_LB_UB[i1][j][0]-std1;
                double b = this.EV_LB_UB[i1][j][0]+std1;
                
                double a1 = this.EV_LB_UB[i2][j][0]-std1;
                double b1 = this.EV_LB_UB[i2][j][0]+std1;
                
                double ED = (2*(b*b+a*b+a*a)+2*(b1*b1+b1*a1+a1*a1)-3*(a+b)*(a1+b1))/6;
                
                double value = Math.exp(-ED);
                if (Double.isInfinite(value) || Double.isNaN(value) || value < -0.0000001 || value > 1.0000001)
                {
                    throw new RuntimeException("Value must be within [0,1]---value="+value);
                }
                if (value < 0.0) value = 0.0;
                if (value > 1.0) value = 1.0;
                deltaEV[j] = value;
            }
        }

        //deltaIT & rhos
        for (int j=0; j<m; j++)
        {
            //deltaIT
            double sumI1 = this.objectSumOfProbabilities[i1][j];
            double sumI2 = this.objectSumOfProbabilities[i2][j];

            double B = 1.0;
            double rho = 0.0;
            if (sumI1 != 0.0 && sumI2 != 0.0)
            {
                for (int s=0; s<S; s++)
                {
                    rho += Math.sqrt(this.objectSampleLists[s][j][i1]*this.objectSampleLists[s][j][i2]);
                }

                if (rho != 0.0)
                {
                    rho /= Math.sqrt(sumI1*sumI2);
                    if (Double.isInfinite(rho) || Double.isNaN(rho) || B < -0.00001 || B > 1.00001)
                    {
                        throw new RuntimeException("Rho must be within [0,1]---Rho="+rho);
                    }
                    if (rho < 0.0) rho = 0.0;
                    if (rho > 1.0) rho = 1.0;

                    B = Math.sqrt(1-rho);
                }
            }
            
            rhos[j] = rho;
            deltaIT[j] = B;
        }

        //delta
        for(int j=0; j<m; j++)
        {
            double v1 = -(1-Math.sqrt(rhos[j]))*deltaEV[j];
            double value = deltaIT[j]+v1;

            dIT += deltaIT[j];
            dEV += v1;
            dEVIT += value;
            dCount++;

            if (Double.isInfinite(value) || Double.isNaN(value) || value < -0.0000001 || value > 1.0000001)
            {
                throw new RuntimeException("Value must be within [0,1]---value="+value);
            }
            if (value < 0.0) value = 0.0;
            if (value > 1.0) value = 1.0;
            delta[j] = value;
        }

        double overallDist = 0.0;
        for (int j=0; j<m; j++)
        {
            overallDist += delta[j]*delta[j];
        }
        overallDist = Math.sqrt(overallDist)/m;
        if (Double.isInfinite(overallDist) || Double.isNaN(overallDist) || overallDist < -0.0000001 || overallDist > 1.0000001)
        {
            throw new RuntimeException("OverallDist must be within [0,1]---overallDist="+overallDist);
        }
        if (overallDist < 0.0) overallDist = 0.0;
        if (overallDist > 1.0) overallDist = 1.0;

        return overallDist;
    }
    
    protected double updateDistanceBetweenClusters(int merged1, int merged2, int x, int sizeMerged1, int sizeMerged2)
    {
        int a1 = (x>merged1)?x:merged1;
        int a2 = (x<merged1)?x:merged1;
        
        return computeDistanceBetweenObjectsOrPrototypes(a1, a2);
    }

    protected void printDeltaContributions()
    {
        System.out.println();
        System.out.println("Delta="+this.dEVIT/this.dCount+"    "+"DeltaEV="+this.dEV/this.dCount+"    "+"DeltaIT="+this.dIT/this.dCount);
    }

    public double getDEVIT()
    {
        return this.dEVIT;
    }

    public double getDEV()
    {
        return this.dEV;
    }

    public double getDIT()
    {
        return this.dIT;
    }

    public int getCountD()
    {
        return this.dCount;
    }
}




























