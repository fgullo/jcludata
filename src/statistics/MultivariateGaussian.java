package statistics;

import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.LFSR113;
import umontreal.iro.lecuyer.probdistmulti.MultiNormalDist;
import umontreal.iro.lecuyer.randvarmulti.MultinormalCholeskyGen;
import umontreal.iro.lecuyer.probdist.NormalDist;


public class MultivariateGaussian extends ContinuousMultivariatePDF
{
    protected MultiNormalDist gaussian;
    protected MultinormalCholeskyGen generator;
    protected double rho;
    
    
    public MultivariateGaussian(double[] lowerBound, double[] upperBound, int nSamples, double rho)
    {
        for (int i=0; i<lowerBound.length; i++)
        {
            if(lowerBound[i] >= upperBound[i])
            {
                throw new RuntimeException("ERROR: lower-bound larger than upper-bound.");
            }
        }
        
        this.dimensions = lowerBound.length;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.nSamples = nSamples;
        this.rho = rho;
        
        computeMeanValue();
        computeVariance();
        computeMultiNormalDistAndGenerator();
    }
    
    public double calculate(double[] value)
    {
        for (int i=0; i<value.length; i++)
        {
            if (value[i]<this.lowerBound[i] || value[i]>this.upperBound[i])
            {
                return 0.0;
            }
        }
       
        return this.gaussian.density(value);
    }
    
    public double calculateCDF(double[] value)
    {
        throw new RuntimeException("NOT IMPLEMENTED");        
    }
    
    public double[] getRandomSample()
    { 
        boolean stop = true;
        double value[] = new double[this.dimensions];
        do
        {
            this.generator.nextPoint(value);
            stop = true;
            for (int i=0; i<value.length; i++)
            {
                if (value[i] <this.lowerBound[i] || value[i] > this.upperBound[i] || Double.isNaN(value[i]))
                {
                    stop = false;
                }
            }
            
        }
        while (!stop);
        return value;      
    }
       
    protected void computeMeanValue()
    {
        this.meanValue = new double[this.dimensions];
        
        for (int i=0; i<this.meanValue.length; i++)
        {
            this.meanValue[i] = (lowerBound[i]+upperBound[i])/2;
        }
    }
    
    protected void computeVariance()
    {
        this.variance = new double[this.dimensions];
        
        for (int i=0; i<this.variance.length; i++)
        {
            this.variance[i] = ((meanValue[i]-lowerBound[i])/3)*((meanValue[i]-lowerBound[i])/3);             
        }
    }
    
    protected void computeMultiNormalDistAndGenerator()
    {
       double[][] sigma = computeCovarianceMatrix();
       
       this.gaussian = new MultiNormalDist(this.meanValue, sigma);
       this.generator = new MultinormalCholeskyGen(new NormalGen(new LFSR113(), new NormalDist()), this.meanValue, sigma); 
    }
    
    protected double[][] computeCovarianceMatrix()
    {
        double[][] sigma = new double[this.dimensions][this.dimensions];
        
        for (int i=0; i<sigma.length; i++)
        {
            for (int j=0; j<sigma[i].length; j++)
            {
                if (i==j)
                {
                    sigma[i][j] = this.variance[i];
                }
                else
                {
                    sigma[i][j] = rho*Math.sqrt(this.variance[i])*Math.sqrt(this.variance[j]);
                }
            }
        }
        
        return sigma;
    }
}