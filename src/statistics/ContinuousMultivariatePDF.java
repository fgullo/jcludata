package statistics;



public abstract class ContinuousMultivariatePDF extends MultivariatePDF
{       
    protected int nSamples;
    
    public int getNumberOfSamples()
    {
        return this.nSamples;
    }
    
    public double[][] getRandomSamples()
    {
        if(this.randomSamples==null)
        {
            this.randomSamples = new double[this.nSamples][this.dimensions];
             
            for (int i=0 ; i<randomSamples.length ; i++)
            {
                randomSamples[i] = getRandomSample();            
            }
        }
        return this.randomSamples;
    }
}
