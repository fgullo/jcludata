package entities;

import statistics.*;

public class UnivariateEntry extends Entry 
{
    private PDF[] uValues;
    
    public UnivariateEntry(String id, String classLabel, double[] values, PDF[] uValues)
    {
        this.id = id;
        this.classLabel = classLabel;
        this.values = values;
        this.uValues = uValues;
    }
    
    public PDF[] getUValues()
    {
        return this.uValues;
    }
    
    public void setPDF(PDF pdf, int index)
    {
        this.uValues[index] = pdf;
    }
    
    public String pdfType()
    {
        if (uValues[0] instanceof Binomial)
        {
            return "d";
        }
        else return "c";
    }
    
    public double[] getRandomSample()
    {
        double[] ret = new double[this.values.length+1];
        
        double prob = 1.0;
        for (int i=0; i<this.uValues.length-1; i++)
        {
            double sample = this.uValues[i].getRandomSample();
            ret[i] = sample;
            prob *= this.uValues[i].calculate(sample);
        }
        
        ret[ret.length-1] = prob;
        
        return ret;
    }
    
    public double[][] getRandomSamples()
    {        
        double[][] ret = new double[this.uValues[0].getNumberOfSamples()][this.uValues.length+1];
        
        for (int i=0; i<this.uValues.length; i++)
        {
            double[] samples = this.uValues[i].getRandomSamples();
            for (int j=0; j<samples.length; j++)
            {
                ret[j][i] = samples[j];
            }
        }
        
        for (int j=0; j<ret.length; j++)
        {
            double prob = 1.0;
            for (int i=0; i<ret[j].length-1; i++)
            {
                prob *= this.uValues[i].calculate(ret[j][i]);
            }
            
            ret[j][ret[j].length-1] = prob;
        }
        
        return ret;
    }   
    
    public double[] getMean()
    {   
        double[] ret = new double[this.uValues.length];
        
        for (int i=0; i<ret.length; i++)
        {
           ret[i] = this.uValues[i].getMeanValue();
        }
        
        return ret;        
    }
    
    @Override
    public double[] distanceBetweenExpectedValues(Entry e)
    {
        if (!(e instanceof UnivariateEntry))
        {
            throw new RuntimeException("ERROR: a multivariate entry should be provided as input.");
        }
        
        return super.distanceBetweenExpectedValues(e);
    }
    
    public double[] getVariance()
    {        
        double[] ret = new double[this.uValues.length];
        
        for (int i=0; i<ret.length; i++)
        {
           ret[i] = this.uValues[i].getVariance();
        }
        
        return ret;        
    }
    
    public double[] getSecondOrderMoment()
    {        
        double[] ret = new double[this.uValues.length];
        
        for (int i=0; i<ret.length; i++)
        {
           ret[i] = this.uValues[i].getSecondOrderMoment();
        }
        
        return ret;        
    }
    
    public double[][] getRegion()
    {
        double[][] region = new double[this.uValues.length][2];
        
        for (int i=0; i<region.length; i++)
        {
            region[i][0] = this.uValues[i].getLowerBound();
            region[i][1] = this.uValues[i].getUpperBound();
        }
        
        return region;
    }
}


















