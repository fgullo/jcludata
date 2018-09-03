package entities;

import java.util.List;


public abstract class Prototype
{
    protected List<Entry> entries;
    
    protected double[] lBounds;
    protected double[] uBounds;
    
    protected double[] meanValue;
    
    public abstract void addPrototype(Prototype p);

    
    public int getNumberOfObjects()
    {
        return this.entries.size();
    }
    
    public int getNumberOfAttributes()
    {
        Entry e = this.entries.get(0);
        double[] values = e.getValues();
        return values.length;
    }
    
    public double[] getLBounds()
    {
        return this.lBounds;
    }
    
    public double[] getUBounds()
    {
        return this.uBounds;
    }
    
    public List<Entry> getEntries()
    {
        return this.entries;
    }
    
   private long fact(int x)
    {
        long val = 1;
        for (int i=1 ; i<=x ; i++)
        {
            val *= i;
        }
        return val;
    }
   
   public double[] getMeanValue()
   {
       return meanValue;
   }
   
   public abstract Prototype clone();
}














