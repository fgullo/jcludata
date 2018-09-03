package entities;

import java.util.ArrayList;
import java.util.List;

public class MultivariatePrototype extends Prototype
{        
    ArrayList<double[]> samples; //array list of samples; 
    ArrayList<Double> sampleProbabilities; //probability value assigned to each sample
    int nSamples;    
    
    
    public MultivariatePrototype(Entry e, ArrayList<double[]> samples, ArrayList<Double> samplesProb)
    {
        if (!(e instanceof MultivariateEntry))
        {
            throw new RuntimeException("ERROR: a multivariate entry should be provided as input.");
        }
        
        this.samples = samples;
        for (int i=0; i<this.nSamples; i++)
        {
            this.samples.add(i, new double[this.entries.get(0).getValues().length]);
        }
        this.sampleProbabilities = samplesProb;
        this.nSamples = samples.size();        
        
        
        entries = new ArrayList<Entry>();
        entries.add(e);
        
        lBounds = new double[e.getValues().length];
        uBounds = new double[e.getValues().length];
        
        
        for (int i=0 ; i<lBounds.length ; i++)
        {
            lBounds[i] = ((MultivariateEntry)e).getMultivariatePDF().getLowerBound()[i];
            uBounds[i] = ((MultivariateEntry)e).getMultivariatePDF().getUpperBound()[i];
        }
        
        this.meanValue = new double[e.getValues().length];
        double[] eMean = e.getMean();
        for (int i=0; i<this.meanValue.length; i++)
        {
            this.meanValue[i] = eMean[i];
        }        
    }
    
    public MultivariatePrototype()
    {
        entries = new ArrayList<Entry>();
    }
    
    public void addPrototype(Prototype p1)
    {
        if (!(p1 instanceof MultivariatePrototype))
        {
            throw new RuntimeException("ERROR: a multivariate prototype should be provided as input.");
        }
        
        MultivariatePrototype p = (MultivariatePrototype)p1;
        
        for (int i=0 ; i<lBounds.length ; i++)
        {
            double lb = p.lBounds[i];
            double ub = p.uBounds[i];
            
            if (lb<lBounds[i])
            {
                lBounds[i] = lb;
            }
            if (ub>lBounds[i])
            {
                uBounds[i] = ub;
            }
        } 
        
        
        ArrayList<double[]> newSampleArray = new ArrayList<double[]>(this.nSamples);
        ArrayList<Double> newProbArray = new ArrayList<Double>(this.nSamples);
        
        int sampleRatio = (int)Math.rint(this.entries.size()/(this.entries.size()+p.entries.size()));
          
        for (int i=0; i<sampleRatio; i++)
        {
            double[] sample = this.samples.get(i);
            double probTmp = p.calculate(sample);
            
            newSampleArray.add(i,sample);
            newProbArray.add(i,(this.sampleProbabilities.get(i)*this.entries.size()+probTmp*p.entries.size())/(this.entries.size()+p.entries.size()));
        }
        
        for (int i=sampleRatio; i<this.nSamples; i++)
        {
            double[] sample = p.samples.get(i-sampleRatio);
            double probTmp = this.calculate(sample);
            
            newSampleArray.add(i,sample);
            newProbArray.add(i-sampleRatio,(probTmp*this.entries.size()+p.sampleProbabilities.get(i-sampleRatio)*p.entries.size())/(this.entries.size()+p.entries.size()));
        }

        double[] eMean = p.getMeanValue();
        for (int i=0; i<this.meanValue.length; i++)
        {
            this.meanValue[i] = (this.meanValue[i]*this.entries.size()+eMean[i]*p.entries.size())/(this.entries.size()+p.entries.size());
        }      
        
        List<Entry> entries2 = p.entries;
        entries.addAll(entries2);
        
        for (int i=0 ; i<lBounds.length ; i++)
        {
            double lb = p.lBounds[i];
            double ub = p.uBounds[i];
            
            if (lb<lBounds[i])
            {
                lBounds[i] = lb;
            }
            if (ub>lBounds[i])
            {
                uBounds[i] = ub;
            }
        }       
    }
    
    public double calculate(double[] val)
    {
        for (int i=0; i<val.length; i++)
        {
            if (val[i]<lBounds[i] || val[i]>uBounds[i])
            {
                return 0.0;
            }
        }
        
        double res = 0.0;
        for (int i=0 ; i<entries.size() ; i++)
        {
                res += ((MultivariateEntry)entries.get(i)).getMultivariatePDF().calculate(val);                        
        }
        return res/entries.size();
    }
    
    public double[] computeMeanValue()
    {
        return this.meanValue;
    }
    
    public Prototype clone()
    {
        MultivariatePrototype pNew = new MultivariatePrototype();
        pNew.lBounds = new double[this.lBounds.length];
        pNew.uBounds = new double[this.uBounds.length];
        
        for (int i=0; i<pNew.lBounds.length; i++)
        {
            pNew.lBounds[i] = this.lBounds[i];
        }
        for (int i=0; i<pNew.uBounds.length; i++)
        {
            pNew.uBounds[i] = this.uBounds[i];
        }        
        
        pNew.entries.addAll(this.entries);
        
        
        
        pNew.nSamples = this.nSamples;
        pNew.samples = new ArrayList<double[]>(this.samples.size());
        pNew.sampleProbabilities = new ArrayList<Double>(this.sampleProbabilities.size());
        
        for (int i=0; i<this.nSamples; i++)
        {
            double[] tmp = this.samples.get(i);
            double prob = this.sampleProbabilities.get(i);
                    
            double[] sample = new double[tmp.length];
            for (int j=0; j<tmp.length; j++)
            {
                sample[j] = tmp[j];
            }
            
            pNew.samples.add(i,sample);
            pNew.sampleProbabilities.add(i,prob);
        }
        
        pNew.meanValue = new double[this.meanValue.length];
        for (int i=0; i<pNew.meanValue.length; i++)
        {
            pNew.meanValue[i] = this.meanValue[i];
        }        
        
        return pNew;
    }
    
    public ArrayList<double[]> getSamples()
    {
        return this.samples;
    }
    
    public ArrayList<Double> getSampleProbabilities()
    {
        return this.sampleProbabilities;
    }
    
    public double getITsignificance(MultivariatePrototype p, double[] commonLB, double[] commonUB)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }    
}

