package entities;

public class DeterministicEntry extends Entry
{
    
    public DeterministicEntry(String id, String classLabel, double[] values)
    {
        this.id = id;
        this.classLabel = classLabel;
        this.values = values;
    }   
   
    public String pdfType()
    {
        return null;
    }
    
    public double[] getRandomSample()
    {
        return null;
    }
    
    public double[][] getRandomSamples()
    {
        return null;
    }
    
    public double[] getMean()
    {
        return values;
    }
    
    public double[] getVariance()
    {
        double[] var = new double[values.length];
        return var;
    }
    
    public double[] getSecondOrderMoment()
    {
        return values;
    }
    
    public double[][] getRegion()
    {
        return null;
    }
}
