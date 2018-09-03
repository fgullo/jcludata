package entities;

import statistics.*;

public class MultivariateEntry extends Entry 
{    
    private MultivariatePDF uValues;
    
    public MultivariateEntry(String id, String classLabel, double[] values, MultivariatePDF uValues)
    {
        this.id = id;
        this.classLabel = classLabel;
        this.values = values;
        this.uValues = uValues;
    }
    
    public MultivariatePDF getMultivariatePDF()
    {
        return this.uValues;
    }
    
    public void setMultivariatePDF(MultivariatePDF pdf)
    {
        this.uValues = pdf;
    }
    
    public String pdfType()
    {   
        return "c";
    }
    
    public double[] getRandomSample()
    {
        double[] ret = new double[this.values.length+1];
        double[] sample = this.uValues.getRandomSample();
        
        for (int i=0; i<ret.length-1; i++)
        {
            ret[i] = sample[i];
        }
        ret[ret.length-1] = this.uValues.calculate(sample);
        
        return ret;        
    }
    
    public double[][] getRandomSamples()
    {
        double[][] ret = new double[this.uValues.getNumberOfSamples()][this.values.length+1];
        double[][] samples = this.uValues.getRandomSamples();
        
        for (int i=0; i<ret.length; i++)
        {
            for (int j=0; j<ret[i].length-1; j++)
            {
                ret[i][j] = samples[i][j];
            }
            ret[i][ret[i].length-1] = this.uValues.calculate(samples[i]);
        }
        
        return ret;
    }
    
    public double[] getMean()
    {
        return this.uValues.getMeanValue();        
    }
    
    @Override
    public double[] distanceBetweenExpectedValues(Entry e)
    {
        if (!(e instanceof MultivariateEntry))
        {
            throw new RuntimeException("ERROR: a multivariate entry should be provided as input.");
        }
        
        return super.distanceBetweenExpectedValues(e);
    }
    
    public double[] getSecondOrderMoment()
    {
        throw new RuntimeException("TO BE IMPLEMENTED");
    }
    
    public double[] getVariance()
    {
        throw new RuntimeException("TO BE IMPLEMENTED");
    }
    
    public double[][] getRegion()
    {
        double[][] region = new double[this.values.length][2];
        
        double[] lbs = this.uValues.getLowerBound();
        double[] ubs = this.uValues.getUpperBound();
        
        for (int i=0; i<region.length; i++)
        {
            region[i][0] = lbs[i];
            region[i][1] = ubs[i];
        }
        
        return region;
    }
}
























