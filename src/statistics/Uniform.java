package statistics;


public class Uniform extends ContinuousPDF
{
    private double probValue;
    
    public Uniform(double lowerBound, double upperBound, int nSamples)
    {
        if(lowerBound >=upperBound)
        {
            throw new RuntimeException("ERROR: lower-bound larger than upper-bound.");
        }
        
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        
        this.nSamples = nSamples;
        
        this.probValue = 1/(upperBound - lowerBound);
        
    }

    public double calculate(double value)
    {
        if (value >= this.lowerBound && value <= this.upperBound)
        {
            return probValue;
        }
        return 0.0;
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
        
        double k1 = ((double)1)/(this.upperBound-this.lowerBound);
        double k2 = -this.lowerBound/(this.upperBound-this.lowerBound);
        
        double v = k1*value+k2;
        
        if (v > 1 || v < 0)
        {
            throw new RuntimeException("ERROR: value grater than 1 or smaller than 0");
        }
        
        return v;
    }
    
    public double[] getRandomSamples()
    {
        if(this.randomSamples==null)
        {
            this.randomSamples = new double[this.nSamples];
        
            for (int i=0 ; i<randomSamples.length ; i++)
            {
                double value = 0.0;
                do
                {
                    value = (this.upperBound-this.lowerBound)*Math.random()+this.lowerBound;
                }
                while (value<this.lowerBound || value>this.upperBound);
                
                randomSamples[i] = value;            
            }
        }
        return this.randomSamples;
    }
    
    public double getRandomSample()
    {
        double value = 0.0;                  
        do
        {
            value = (this.upperBound-this.lowerBound)*Math.random()+this.lowerBound;
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
        this.variance = (this.upperBound-this.lowerBound)*(this.upperBound-this.lowerBound)/12;           
    }
    
    protected void computeSecondOrderMoment()
    {
        this.secondOrderMoment = (this.lowerBound*this.lowerBound+this.lowerBound*this.upperBound+this.upperBound*this.upperBound)/3;
    }
}