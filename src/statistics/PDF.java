package statistics;


public abstract class PDF
{    
    protected double lowerBound;
    protected double upperBound;
    
    protected double meanValue;
    protected double variance;
    protected double secondOrderMoment;
    
    protected double[] randomSamples;
    
    protected boolean computedMeanValue = false;
    protected boolean computedVariance = false;
    protected boolean computedSecondOrderMoment = false;
    
    public abstract double calculate(double value);
    
    public abstract double calculateCDF(double value);
    
    public abstract double[] getRandomSamples();
    
    public abstract double getRandomSample();
    
    protected abstract void computeMeanValue();
    
    protected abstract void computeVariance();
    
    protected abstract void computeSecondOrderMoment();
    
    public double getMeanValue()
    {
        if (this.computedMeanValue == false)
        {
            this.computeMeanValue();
            this.computedMeanValue = true;
        }            
        return this.meanValue;
    }
    
    public double getVariance()
    {
        if(this.computedVariance == false)
        {
            this.computeVariance();
            this.computedVariance = true;
        }
        return this.variance;
    }
    
        public double getSecondOrderMoment()
    {
        if(this.computedSecondOrderMoment == false)
        {
            this.computeSecondOrderMoment();
            this.computedSecondOrderMoment = true;
        }
        return this.secondOrderMoment;
    }
    
    public double getLowerBound()
    {
        return this.lowerBound;
    }
    
    public double getUpperBound()
    {
        return this.upperBound;
    }
    
    public abstract int getNumberOfSamples();
}
