package statistics;

import umontreal.iro.lecuyer.randvar.BinomialGen;
import umontreal.iro.lecuyer.rng.LFSR113;

public class Binomial extends DiscretePDF
{

    private double mode;
    
    private double p;
    
    private long factN;
    
    private long[] factTable;
        
    public Binomial(double lowerBound, double upperBound, double mode, int n)
    {
        if(lowerBound >=upperBound)
        {
            throw new RuntimeException("ERROR: lower-bound larger than upper-bound.");
        }
        
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.mode = mode;
        this.n = n;
        
        computeP();
        computeMeanValue();
        computeVariance();
        
        computeFactTable();
    }

    public double calculate(double value)
    {
        if(value<this.lowerBound || value>this.upperBound) return 0.0;
        
        int k = (int)Math.rint(n*(value-lowerBound)/(upperBound-lowerBound));
        
        
        return (((double)this.factN)/(this.factTable[k]*this.factTable[n-k]))*Math.pow(p,k)*Math.pow(1-p,n-k);
    }
    
    public double calculateCDF(double value)
    {
        if (value <= this.lowerBound)
        {
            return 0;
        }
        
        if (value >= this.upperBound)
        {
            return 1;
        }
        
        int k = (int)Math.rint(n*(value-lowerBound)/(upperBound-lowerBound));
        
        double v = 0;
        for (int i=1; i<=k; i++)
        {
            v += (((double)this.factN)/(this.factTable[i]*this.factTable[n-i]))*Math.pow(p,i)*Math.pow(1-p,n-i);
        }
        
        if (v > 1 || v < 0)
        {
            throw new RuntimeException("ERROR: value grater than 1 or smaller than 0");
        }
        
        return v;        
    }
    
     
    
    public double getRandomSample()
    {
        int newIndex = BinomialGen.nextInt(new LFSR113(), this.n, this.p);
        return newIndex*(this.upperBound-this.lowerBound)/this.n + this.lowerBound;
    }
    
    
    protected void computeMeanValue()
    {
        this.meanValue = p*(upperBound-lowerBound);
    }
    
    protected void computeVariance()
    {
        this.variance = n*p*(1-p);             
    }
    
    protected void computeSecondOrderMoment()
    {
        computeMeanValue();
        computeVariance();
        
        this.secondOrderMoment = this.variance+this.meanValue*this.meanValue;
    }
    
    private void computeP()
    {
        int modeInt = (int)Math.rint((mode-lowerBound)*n/(upperBound-lowerBound));
        this.p = ((double)modeInt)/(n+1);
    }
    
    private long factorial(int x)
    {
        long val = 1;
        for (int i=1 ; i<=x ; i++)
        {
            val *= i;
        }
        return val;
    }
    
    private void computeFactTable()
    {
        this.factN = factorial(this.n);
        
        this.factTable = new long[this.n+1];
        
        for (int i=0; i<this.factTable.length; i++)
        {
            this.factTable[i] = factorial(i);
        }
    }
            
}