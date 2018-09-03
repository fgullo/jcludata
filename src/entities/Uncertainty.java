package entities;

import statistics.*;

public class Uncertainty 
{
    private ContinuousPDF pdf;
    
    private double lowerBound;
    private double upperBound;
    
    public Uncertainty(ContinuousPDF pdf, double lowerBound, double upperBound)
    {
        this.pdf = pdf;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }
    
    public double distance(Uncertainty u)
    {
        return 0.0;
    }
    
    public ContinuousPDF getPDF()
    {
        return this.pdf;
    }
    
    public double getLowerBound()
    {
        return this.lowerBound;
    }
    
    public double getUpperBound()
    {
        return this.upperBound;
    }
}
