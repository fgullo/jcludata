package statistics;

import umontreal.iro.lecuyer.randvar.LognormalGen;
import umontreal.iro.lecuyer.probdist.LognormalDist;
import umontreal.iro.lecuyer.rng.LFSR113;

public class Lognormal extends ContinuousPDF
{
    public final double percentileValue = 2.326; //99%

    private double mode;
    
    private double mu;
    private double sigma;
    
    public Lognormal(double lowerBound, double upperBound, double mode, int nSamples)
    {
        if(lowerBound >=upperBound)
        {
            throw new RuntimeException("ERROR: lower-bound larger than upper-bound.");
        }
        
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.mode = mode;
        this.nSamples = nSamples;
        
        computeSigma();
        computeMu();
        
        computeMeanValue();
        computeVariance();
    }

    public double calculate(double value)
    {        
        if(value<this.lowerBound || value>this.upperBound) return 0.0;
        
        LognormalDist lg = new LognormalDist(this.mu, this.sigma);
        return lg.density(value-this.lowerBound);
    }
    
    public double calculateCDF(double value)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    
    public double getRandomSample()
    {
        double value = 0.0;
        do
        {
            value = LognormalGen.nextDouble(new LFSR113() , this.meanValue, Math.sqrt(this.variance));
            
        }
        while (value<this.lowerBound || value>this.upperBound || Double.isNaN(value));
        return value;
    }
    
    public double getMu()
    {
        return mu;
    }
    
    public double getSigma()
    {
        return sigma;
    }    
    
    private void computeSigma()
    {       
        double xm = mode-lowerBound;
        double xf = upperBound-lowerBound;
        
        this.sigma = (-percentileValue+Math.sqrt(percentileValue*percentileValue-4*(Math.log(xm)-Math.log(xf))))/2;
    }
    
    private void computeMu()
    {
        double xf = upperBound-lowerBound;
        
        this.mu =Math.log(xf)-percentileValue*sigma;
    }
    
    protected void computeMeanValue()
    {
        this.meanValue = Math.exp(mu+sigma*sigma/2)+lowerBound;
    }
    
    protected void computeVariance()
    {
        this.variance = Math.exp(2*(mu+sigma*sigma))-Math.exp(2*mu+sigma*sigma);             
    }
    
    protected void computeSecondOrderMoment()
    {
        computeMeanValue();
        computeVariance();
        
        this.secondOrderMoment = this.variance+this.meanValue*this.meanValue;
    }
}