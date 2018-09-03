package entities;

public abstract class Entry 
{
    protected String id;
    protected String classLabel;
    protected int classLabelID=-1;
    protected double[] values;
    
    
    public String getId()
    {
        return this.id;
    }
    
    public String getClassLabel()
    {
        return this.classLabel;
    }
    
    public int getClassLabelID()
    {
        return this.classLabelID;
    }
    
    public void setClassLabelID(int value)
    {
        this.classLabelID = value;
    }
   
    public double[] getValues()
    {
        return this.values;
    }
    
    public abstract String pdfType();
    
    public abstract double[] getRandomSample();
    
    public abstract double[][] getRandomSamples();
    
    public abstract double[] getMean();
    
    public abstract double[] getVariance();
    
    public abstract double[] getSecondOrderMoment();
    
    public double[] distanceBetweenExpectedValues(Entry e)
    {
        double[] mean1 = this.getMean();
        double[] mean2 = e.getMean();
        
        double[] res = new double[mean1.length];
        for (int i=0; i<res.length; i++)
        {
            res[i] = Math.abs(mean1[i]-mean2[i]);
        }
        
        return res;
    }
    
    public boolean equals(Object o)
    {
        if(o instanceof Entry)
        {
            return (this.id).equals(((Entry)o).id);
        }
        else return false;
    }
    
    public abstract double[][] getRegion();
}
