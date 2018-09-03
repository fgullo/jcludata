package entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import statistics.PDF;


public class UnivariatePrototype extends Prototype
{
    ArrayList<Double>[] samples; //array of array list of samples; 1 array list per dimension
    ArrayList<Double>[] sampleProbabilities; //probability of each sample
    int nSamples;
    
    
    public UnivariatePrototype(Entry e, ArrayList<Double>[] samples, ArrayList<Double>[] samplesProb)
    {
        if (!(e instanceof UnivariateEntry))
        {
            throw new RuntimeException("ERROR: a multivariate entry should be provided as input.");
        }
        
        this.samples = samples;
        this.sampleProbabilities = samplesProb;
        this.nSamples = samples[0].size();
        
        entries = new ArrayList<Entry>();
        entries.add(e);
        
        lBounds = new double[e.getValues().length];
        uBounds = new double[e.getValues().length];
        

        
        for (int i=0 ; i<lBounds.length ; i++)
        {
            lBounds[i] = ((UnivariateEntry)e).getUValues()[i].getLowerBound();
            uBounds[i] = ((UnivariateEntry)e).getUValues()[i].getUpperBound();
        }
        
        this.meanValue = new double[e.getValues().length];
        double[] eMean = e.getMean();
        for (int i=0; i<this.meanValue.length; i++)
        {
            this.meanValue[i] = eMean[i];
        }
    }
    
    public UnivariatePrototype()
    {
        entries = new ArrayList<Entry>();
    }
    
    public void addPrototype(Prototype p1)
    {
        if (!(p1 instanceof UnivariatePrototype))
        {
            throw new RuntimeException("ERROR: a multivariate prototype should be provided as input.");
        }
        
        UnivariatePrototype p = (UnivariatePrototype)p1;
        
        
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
        
        ArrayList[] newSampleArray = new ArrayList[this.samples.length];
        ArrayList[] newProbArray = new ArrayList[this.sampleProbabilities.length];
        
        int sampleRatio = (int)Math.rint(this.entries.size()/(this.entries.size()+p.entries.size()));
        
        for (int i=0; i<this.samples.length; i++) //for all dimensions
        {
            ArrayList<Double> samples1 = this.samples[i];
            ArrayList<Double> prob1 = this.sampleProbabilities[i];
            ArrayList<Double> samples2 = p.samples[i];
            ArrayList<Double> prob2 = p.sampleProbabilities[i];
            
            ArrayList<Double> newSamples = new ArrayList<Double>(samples1.size());
            ArrayList<Double> newProb = new ArrayList<Double>(prob1.size());
            
            for (int j=0; j<sampleRatio; j++)
            {
                double s = samples1.get(j);
                double pr = prob1.get(j);
                
                double prTmp = p.calculate(s, i);
                
                double newPr = (pr*this.entries.size()+prTmp*p.entries.size())/(this.entries.size()+p.entries.size());
                
                
                newSamples.add(j,s);
                newProb.add(j,newPr);            
            }
            
            for (int j=0; j<this.nSamples-sampleRatio; j++)
            {
                double s = samples2.get(j);
                double pr = prob2.get(j);
                
                double prTmp = this.calculate(s, i);
                
                double newPr = (pr*p.entries.size()+prTmp*this.entries.size())/(this.entries.size()+p.entries.size());
                
                
                newSamples.add(j+sampleRatio,s);
                newProb.add(j+sampleRatio,newPr);            
            }
            
            newSampleArray[i] = newSamples;
            newProbArray[i] = newProb;
       }
        
        
        double[] eMean = p.getMeanValue();
        for (int i=0; i<this.meanValue.length; i++)
        {
            this.meanValue[i] = (this.meanValue[i]*this.entries.size()+eMean[i]*p.entries.size())/(this.entries.size()+p.entries.size());
        }         
        
       
        List<Entry> entries2 = p.entries;
        Iterator<Entry> it = entries2.iterator();
        while (it.hasNext())
        {
            this.entries.add(it.next());
        }
    }
    
    public double calculate(double val, int attr)
    {
        if (val<lBounds[attr] || val>uBounds[attr])
        {
            return 0.0;
        }
        
        double result = 0.0;
        for (int i=0 ; i<entries.size() ; i++)
        {
            result += ((UnivariateEntry)entries.get(i)).getUValues()[attr].calculate(val);            
        }
        return result/entries.size();
    }
    
    public double computeMeanValue(int attr)
    {
        return this.meanValue[attr];
    }
    
    public Prototype clone()
    {
        UnivariatePrototype pNew = new UnivariatePrototype();
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
        pNew.samples = new ArrayList[this.samples.length];
        pNew.sampleProbabilities = new ArrayList[this.sampleProbabilities.length];
        
        for (int i=0; i<pNew.samples.length; i++)
        {
            ArrayList<Double> as = this.samples[i];
            ArrayList<Double> ps = this.sampleProbabilities[i];
            
            ArrayList<Double> asnew = new ArrayList<Double>(as.size());
            ArrayList<Double> psnew = new ArrayList<Double>(ps.size());
            
            for (int j=0; j<as.size(); j++)
            {
                asnew.add(j, new Double(as.get(j)));
                psnew.add(j, new Double(ps.get(j)));
            }
            
            pNew.samples[i] = asnew;
            pNew.sampleProbabilities[i] = psnew;
        }
        
        pNew.meanValue = new double[this.meanValue.length];
        for (int i=0; i<pNew.meanValue.length; i++)
        {
            pNew.meanValue[i] = this.meanValue[i];
        }
        
        return pNew;
    }
    
    public ArrayList<Double>[] getSamples()
    {
        return this.samples;
    }
    
    public ArrayList<Double>[] getSampleProbabilities()
    {
        return this.sampleProbabilities;
    }
    
    public double getITsignificance(UnivariatePrototype p, double commonLB, double commonUB, int dim)
    {
        double its1 = 0.0;
        double its2 = 0.0;
        
        for (int i=0; i<this.entries.size(); i++)
        {
            PDF pdf = ((UnivariateEntry)this.entries.get(i)).getUValues()[dim];
            its1 += (pdf.calculateCDF(commonUB)-pdf.calculateCDF(commonLB));
        }
        its1 /= this.entries.size();
        
        for (int i=0; i<p.entries.size(); i++)
        {
            PDF pdf = ((UnivariateEntry)p.entries.get(i)).getUValues()[dim];
            its2 += (pdf.calculateCDF(commonUB)-pdf.calculateCDF(commonLB));
        }
        its2 /= p.entries.size();
        
        double v = (its1+its2)/2;
        
        if (v<0 || v>1)
        {
            throw new RuntimeException("ERROR: IT-SIGNIFICANCE < 0 or > 1");
        }
        
        return v;
    }
       
}























