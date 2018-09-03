package statistics;


public abstract class MultivariatePDF
{    
    protected int dimensions;
    
    protected double[] lowerBound;
    protected double[] upperBound;
    
    protected double[] meanValue;
    protected double[] variance;
    
    protected double[][] randomSamples;
    
    public abstract double calculate(double[] value);
    
    public abstract double calculateCDF(double[] value);
    
    public abstract double[][] getRandomSamples();
    
    public abstract double[] getRandomSample();
    
    protected abstract void computeMeanValue();
    
    protected abstract void computeVariance();
    
    public double[] getMeanValue()
    {
        if (this.meanValue==null)
        {
            this.computeMeanValue();
        }            
        return this.meanValue;
    }
    
    public double[] getVariance()
    {
        if(this.variance==null)
        {
            this.computeVariance();
        }
        
        return this.variance;
    }
    
    public double[] getLowerBound()
    {
        return this.lowerBound;
    }
    
    public double[] getUpperBound()
    {
        return this.upperBound;
    }
    
    public abstract int getNumberOfSamples();
}
