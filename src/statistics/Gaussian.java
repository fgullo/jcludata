package statistics;

import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.LFSR113;


public class Gaussian extends ContinuousPDF
{
    
    public Gaussian(double lowerBound, double upperBound, int nSamples)
    {
        if(lowerBound >= upperBound)
        {
            throw new RuntimeException("ERROR: lower-bound larger than upper-bound.");
        }
        
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.nSamples = nSamples;
        
        computeMeanValue();
        computeVariance();
    }
    
    public double calculate(double value)
    {
        if(value<this.lowerBound || value>this.upperBound) return 0.0;
        
        return (1/(Math.sqrt(2*Math.PI)))*
                Math.exp(-(value-meanValue)*(value-meanValue)/(2*variance));
    }
    
    public double calculateCDF(double value)
    {
        if (value <= this.lowerBound)
        {
            return 0;
        }
        
        if (value >= this.upperBound)
        {
            return 1;
        }
        
        double v = NormalDist.cdf(this.meanValue, Math.sqrt(this.variance), value);
        
        if (v > 1 || v < 0)
        {
            throw new RuntimeException("ERROR: value grater than 1 or smaller than 0");
        }
        
        return v;
    }
    
    public double getRandomSample()
    {
        double value = 0.0;
        do
        {
            value = NormalGen.nextDouble(new LFSR113() , this.meanValue, Math.sqrt(this.variance));
            
        }
        while (value<this.lowerBound || value>this.upperBound || Double.isNaN(value));
        return value;
    }
       
    protected void computeMeanValue()
    {
        this.meanValue = (lowerBound+upperBound)/2;
    }
    
    protected void computeVariance()
    {
        this.variance = ((meanValue-lowerBound)/3)*((meanValue-lowerBound)/3);             
    }
    
    protected void computeSecondOrderMoment()
    {
        computeMeanValue();
        computeVariance();
        this.secondOrderMoment = this.meanValue*this.meanValue+this.variance;
    }
}
