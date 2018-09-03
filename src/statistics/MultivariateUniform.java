package statistics;


public class MultivariateUniform extends ContinuousMultivariatePDF
{
    private double probValue;
    
    public MultivariateUniform(double[] lowerBound, double[] upperBound, int nSamples)
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
        
        this.probValue = 1.0;
        for (int i=0; i<this.dimensions; i++)
        {
            this.probValue /= (upperBound[i] - lowerBound[i]);
        }
        
    }

    public double calculate(double[] value)
    {
        for (int i=0; i<value.length; i++)
        {
            if (value[i] >= this.lowerBound[i] && value[i] <= this.upperBound[i])
            {
                return probValue;
            }
        }
        
        return 0.0;
    }
    
    public double calculateCDF(double[] value)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }    
    
    public double[][] getRandomSamples()
    {
        if(this.randomSamples==null)
        {
            this.randomSamples = new double[this.nSamples][this.dimensions];
        
            for (int i=0 ; i<randomSamples.length ; i++)
            {
                double value[] = new double[this.dimensions];
                for (int j=0; j<this.dimensions; j++)
                {
                    double v = 0.0;    
                    do
                    {
                        v = (this.upperBound[j]-this.lowerBound[j])*Math.random()+this.lowerBound[j];
                    }
                    while (v<this.lowerBound[j] || v>this.upperBound[j] || Double.isNaN(v));
                    
                    value[j] = v;
                }
                randomSamples[i] = value;            
            }
        }
        return this.randomSamples;
    }
    
    public double[] getRandomSample()
    {
        double value[] = new double[this.dimensions];
        for (int j=0; j<this.dimensions; j++)
        {
            double v = 0.0;    
            do
            {
                v = (this.upperBound[j]-this.lowerBound[j])*Math.random()+this.lowerBound[j];
            }
            while (v<this.lowerBound[j] || v>this.upperBound[j] || Double.isNaN(v));

            value[j] = v;
        }
        
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
        for (int i=0; i<this.meanValue.length; i++)
        {        
            this.variance[i] = (this.upperBound[i]-this.lowerBound[i])*(this.upperBound[i]-this.lowerBound[i])/12;
        }
    }
}
