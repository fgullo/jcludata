package statistics;


public abstract class DiscretePDF extends PDF
{
    protected int n; //number of samples within the interval
    
    public int getNumberOfSamples()
    {
        return this.n;
    }
    
    
    public double[] getRandomSamples()
    {
        if(this.randomSamples==null)
        {
            this.randomSamples = new double[n];
            
            for (int i=0 ; i<randomSamples.length ; i++)
            {
                randomSamples[i] = getRandomSample();            
            }            
        }
        return this.randomSamples;
    }
    
}
