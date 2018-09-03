package entities;

public class Pair 
{
    private double lowerBound;
    private double upperBound;
    
    public Pair(double lower, double upper)
    {
        this.lowerBound = lower;
        this.upperBound = upper;
    }
    
    public double getUpperBound()
    {
        return this.upperBound;
    }
    
    public double getLowerBound()
    {
        return this.lowerBound;
    }
    
    public void setLowerBound(double value)
    {
        this.lowerBound = value;
    }
    
    public void setUpperBound(double value)
    {
        this.upperBound = value;
    }
}
