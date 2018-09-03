package utility;

import entities.*;
import statistics.*;
import java.io.*;
import java.util.HashMap;
import java.util.StringTokenizer;

public class DataLoader 
{
    private final int nSamples = 80;
    
    private final int binomialSamples = 20;
    
    private final double rho = 0.5;
    
    private final double lognormalPDFCondition = 1.1;
    private final double gaussianPDFCondition = 0.3989;
    private final double percentage = 0.05;
    private final double percentagePDFCondition = 0.05;
    
    private String dataPath;
    private String infoPath;
    
    private int entries;
    private int rawAttributes;
    
    private int dataAttributes;
    private int classes=1;

    private int classAttributePosition=-1;
    private int idPosition=-1;
    
    private Dataset d;
    
    int failures = 0;
    int totLogNorm = 0;
    
    private Pair[][] minMax; //matrix to generate uncertainty
    
    //load a dataset where uncertainty has already been generated
    public DataLoader()
    {
        
    }
    
    //load a raw dataset
    public DataLoader(String dataPath, String infoPath, boolean univariate)
    {
        this.dataPath = dataPath;
        this.infoPath = infoPath;
        this.d = createDataset(univariate);
    }
    
    public DataLoader(String dataPath, String infoPath, boolean univariate, boolean loadOnlyInfo)
    {
        this.dataPath = dataPath;
        this.infoPath = infoPath;
        if (!loadOnlyInfo)
        {
            this.d = createDataset(univariate);
        }
        else
        {
            readInfo();
        }
    }
    
    private Dataset createDataset(boolean univariate)
    {
        readInfo();
        readData(univariate);
        return d;
    }

    public Dataset createDataset(String dataPath, String infoPath, boolean univariate)
    {
        this.dataPath = dataPath;
        this.infoPath = infoPath;
        return createDataset(univariate);
    }
    
    private void readInfo()
    {
        try
        {
            //reading metadata
            FileReader fr = new FileReader(infoPath);
            BufferedReader br = new BufferedReader(fr);
            String line = null;
            while((line=br.readLine())!=null)
            {
                StringTokenizer st = new StringTokenizer(line," =:,;");
                String propertyName = st.nextToken();
                if (propertyName.equalsIgnoreCase("lines"))
                {
                    String value = st.nextToken();
                    entries = Integer.parseInt(value);                                      
                }
                else if (propertyName.equalsIgnoreCase("attributes"))
                {
                    String value = st.nextToken();
                    rawAttributes = Integer.parseInt(value);
                    dataAttributes = rawAttributes;
                }
                else if (propertyName.equalsIgnoreCase("classes"))
                {
                    String value = st.nextToken();
                    classes = Integer.parseInt(value);                                      
                }
                else if (propertyName.equalsIgnoreCase("classAttributePosition"))
                {
                    String value = st.nextToken();
                    classAttributePosition = Integer.parseInt(value);
                    dataAttributes--;
                }
                
                else if (propertyName.equalsIgnoreCase("idPosition"))
                {
                    String value = st.nextToken();
                    idPosition = Integer.parseInt(value);
                    dataAttributes--;
                }
                
            }           
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }        
    }
    
    private void readData(boolean univariate)
    {
        try
        {
            //reading dataset
            Entry[] objects = new Entry[entries];
            
            FileReader fr = new FileReader(dataPath);
            BufferedReader br = new BufferedReader(fr);
            String line = null;
            int idEntry = 0;
            
            while((line=br.readLine())!=null && idEntry<this.entries)
            {
                StringTokenizer st = new StringTokenizer(line," ,;");
                int pos = 0;
                int indexValore = 0;
                //variables for a new entry
                double[] values = new double[dataAttributes];
                
                PDF[] uValues = null;
                MultivariatePDF uValuesMV = null;               
                
                if (univariate)
                {
                    uValues = new PDF[dataAttributes];
                }

                
                String classLabel = "none";
                String id=""+idEntry;
                //#############################################                
                while(st.hasMoreElements())
                {
                    String attribute = st.nextToken();
                    if(pos==classAttributePosition-1)
                    {
                        classLabel = attribute;
                    }
                    else if(pos==idPosition-1)
                    {
                        //id = attribute;
                    }                    
                    else
                    {
                        double val = Double.parseDouble(attribute);
                        values[indexValore] = val;
                        indexValore++;                        
                    }
                    pos++;
                } 
                             
                //building a new Entry
                Entry e  = null;
                if (univariate)
                {
                    e = new UnivariateEntry(id,classLabel,values,uValues);           
                }
                else
                {
                    e = new MultivariateEntry(id,classLabel,values,uValuesMV);           
                }
                //adding the Entry to the Dataset
                objects[idEntry]=e;
                idEntry++;                
            }
            
            String[] classesLabel = new String[this.classes];
            int posClass = 0;
            int classLabelID = -1;

            for (int i=0 ; i<objects.length ; i++)
            {
                Entry e = objects[i];

                if(!ePresente(classesLabel,e.getClassLabel()))
                {
                    classLabelID++;
                    e.setClassLabelID(classLabelID);
                    classesLabel[classLabelID] = e.getClassLabel();

                }
                else
                {
                    e.setClassLabelID(classLabelID);
                }
            }
            //###############################################################
            
            d=new Dataset(objects);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void generateUncertainty(String pdfName, String boundType, String lowerPath, String upperPath, String devStdPath)
    {
        if(boundType.equals("random"))
        {
            this.generateRandomUncertainty(pdfName);
        }
        else if (boundType.equals("range"))
        {
            this.generateRangedUncertainty(pdfName, lowerPath, upperPath);
        }
        else if (boundType.equals("devStd"))
        {
            this.generateStdDevUncertainty(devStdPath);
        }
        else
        {
            throw new RuntimeException("Bound type unknown---type="+boundType);
        }
    }
    
    
    public void generateUncertaintyLowMemory(String pdfName, String boundType, String lowerPath, String upperPath, String devStdPath, String saveDataPath, String saveInfoPath, int bound, boolean compact)
    {
        if(boundType.equals("random"))
        {
            this.generateRandomUncertaintyLowMemory(pdfName, this.dataPath, saveDataPath, saveInfoPath, bound, compact, false);
        }
        else if (boundType.equals("range"))
        {
            throw new RuntimeException("Not implemented---boundType="+boundType);
        }
        else if (boundType.equals("devStd"))
        {
            throw new RuntimeException("Not implemented---boundType="+boundType);
        }
        else
        {
            throw new RuntimeException("Bound type unknown---boundType="+boundType);
        }
    }
    
    public void generateUncertaintyLowMemory(String pdfName, String boundType, String lowerPath, String upperPath, String devStdPath, String saveDataPath, String saveInfoPath, boolean compact)
    {
        if(boundType.equals("random"))
        {
            this.generateRandomUncertaintyLowMemory(pdfName, this.dataPath, saveDataPath, saveInfoPath, Integer.MAX_VALUE, compact, false);
        }
        else if (boundType.equals("range"))
        {
            throw new RuntimeException("Not implemented---boundType="+boundType);
        }
        else if (boundType.equals("devStd"))
        {
            throw new RuntimeException("Not implemented---boundType="+boundType);
        }
        else
        {
            throw new RuntimeException("Bound type unknown---boundType="+boundType);
        }
    }
    
    public void generateUncertaintyLowMemoryClassByClass(String pdfName, String boundType, String lowerPath, String upperPath, String devStdPath, String saveDataPath, String saveInfoPath, boolean compact)
    {
        if(boundType.equals("random"))
        {
            String rdp = this.dataPath.substring(0, this.dataPath.length()-5);
            String sdp = saveDataPath.substring(0, saveDataPath.length()-5);
            String sip = saveInfoPath.substring(0, saveInfoPath.length()-5);
            
            for (int i=1; i<this.classes; i++)
            {
                this.generateRandomUncertaintyLowMemory(pdfName, rdp+i+".data", sdp+i+".data", sip+i+".info", Integer.MAX_VALUE, compact, true);
            }
        }
        else if (boundType.equals("range"))
        {
            throw new RuntimeException("Not implemented---boundType="+boundType);
        }
        else if (boundType.equals("devStd"))
        {
            throw new RuntimeException("Not implemented---boundType="+boundType);
        }
        else
        {
            throw new RuntimeException("Bound type unknown---boundType="+boundType);
        }
    }
    
    
    public void generateRandomUncertaintyLowMemory(String pdfName, String readDataPath, String saveDataPath, String saveInfoPath, int bound, boolean compact, boolean ignoreFirstLine)
    {
        HashMap<String,Integer> classLabelToInt = computeAttributesRangeFromFile(bound, readDataPath, ignoreFirstLine);

            //#################################################
        
        saveDataPath += "TMP";
        saveInfoPath += "TMP";
        
        
        if (pdfName.substring(pdfName.length()-2, pdfName.length()).equals("MV"))
        {
            //generateRandomMultivariateUncertainty(pdfName.substring(0, pdfName.length()-2));
            throw new RuntimeException("ERROR: Not Implemented");
        }
        else
        {
            try
            {
                FileWriter fwData = new FileWriter(saveDataPath);
                FileWriter fwInfo = new FileWriter(saveInfoPath);

                BufferedWriter bwData = new BufferedWriter(fwData);
                BufferedWriter bwInfo = new BufferedWriter(fwInfo);
                
                
                FileReader fr = new FileReader(readDataPath);
                BufferedReader br = new BufferedReader(fr);
                int size = this.entries;
                if (ignoreFirstLine)
                {
                    size = Integer.parseInt(br.readLine());
                    bwData.write(size);
                    bwData.newLine();
                }
                
                if (this.entries < bound)
                {
                    bwInfo.write(size+","+this.dataAttributes);
                }
                else
                {
                    bwInfo.write(bound+","+this.dataAttributes);
                }
                bwInfo.newLine();
                bwInfo.flush();
                
                String line = br.readLine();
                int count = 0;
                while (line != null && count<bound)
                {                    
                    //reading raw object
                    double[] values = new double[this.dataAttributes];
                    int iValues = 0;
                    int pos = 0;
                    int classInt = -1;
                    
                    StringTokenizer st = new StringTokenizer(line,",;");
                    while (st.hasMoreTokens())
                    {
                        if (pos != this.classAttributePosition-1)
                        {
                            values[iValues] = Double.parseDouble(st.nextToken());
                            iValues++;
                        }
                        else
                        {
                            classInt = Integer.parseInt(st.nextToken());
                        }
                        pos++;
                    }
                    
                    Entry e = new UnivariateEntry(""+count,""+classInt,values,new PDF[this.dataAttributes]);
                    e.setClassLabelID(classInt);

                    //generating uncertainty randomly
                    for(int j=0 ; j<values.length ; j++)
                    {
                        double percentage = 0.05;

                        Pair c = minMax[classInt][j];
                        double lowerBound = c.getLowerBound();
                        double upperBound = c.getUpperBound();

                        double offset = 0.0;
                        if((upperBound-lowerBound)==0.0)
                        {
                            do
                            {
                                offset = Math.random();
                            }
                            while(offset==0.0);
                        }
                        else
                        {
                            offset = (upperBound-lowerBound)*percentage;
                        }

                        lowerBound -= offset;
                        upperBound += offset;

                        double value = values[j];


                        if(pdfName.equalsIgnoreCase("lognormal"))
                        {
                            double sigma = -1;
                            Lognormal lg = null;

                            double randomLower = 0.0;
                            double randomUpper = 0.0;
                            double newLower = (value-lowerBound)*randomLower+lowerBound;
                            double newUpper = (upperBound-value)*randomUpper+value;

                            do
                            {
                                randomLower = Math.random();
                                randomUpper = Math.random();

                                newLower = (value-lowerBound)*randomLower+lowerBound;
                                newUpper = (upperBound-value)*randomUpper+value;
                                lg = new Lognormal(newLower, newUpper, value, nSamples);
                                sigma = lg.getSigma();
                            }
                            while( sigma <= 0.0);

                            offset = (upperBound-lowerBound)*percentagePDFCondition;

                            while(lg.getMu() <= this.lognormalPDFCondition*((sigma*sigma/2)-Math.log(sigma*Math.sqrt(2*Math.PI))) )
                            {
                                newLower -= offset;
                                newUpper += offset;

                                lg = new Lognormal(newLower, newUpper, value, nSamples);                        
                            }                        

                            totLogNorm++;
                            if(lg.calculate(value)>=1)
                            {
                                failures++;
                            }

                            ((UnivariateEntry)e).setPDF(lg, j);
                        }
                        else if(pdfName.equalsIgnoreCase("gaussian"))
                        {
                            double distLower = value-lowerBound;
                            double distUpper = upperBound-value;

                            percentage = 0.05;

                            double ramo = (distLower+distUpper)/2.0;
                            double percValue = ramo*percentage;

                            double newLower = value-ramo;
                            double newUpper = value+ramo;

                            Gaussian g = new Gaussian(newLower, newUpper, nSamples);
                            while (Math.sqrt(g.getVariance())<=this.gaussianPDFCondition)
                            {
                                ramo += percValue;
                                newLower = value-ramo;
                                newUpper = value+ramo;
                                g = new Gaussian(newLower, newUpper, nSamples);
                            }                   

                            totLogNorm++;
                            if(g.calculate(value)>=1)
                            {
                                failures++;
                            }
                            ((UnivariateEntry)e).setPDF(g, j);
                        }
                        else if(pdfName.equalsIgnoreCase("binomial"))
                        {                                       
                            double randomLower = Math.random();
                            double randomUpper = Math.random(); 
                            double newLower = (value-lowerBound)*randomLower+lowerBound;
                            double newUpper = (upperBound-value)*randomUpper+value;

                            Binomial b = new Binomial(newLower, newUpper, value, binomialSamples);
                            ((UnivariateEntry)e).setPDF(b, j);
                        }
                        else if(pdfName.equalsIgnoreCase("uniform"))
                        {
                            double randomLower = Math.random();
                            double randomUpper = Math.random();

                            percentage = 0.2;

                            double newLower = value-(value-lowerBound)*randomLower;
                            double newUpper = value+(upperBound-value)*randomUpper;

                            double percValue = (newUpper-newLower)*percentage;

                            while((newUpper-newLower)<=1.0)
                            {
                                newUpper += percValue;
                                newLower -= percValue;

                                percValue = (newUpper-newLower)*percentage;
                            }

                            Uniform u = new Uniform(newLower, newUpper, nSamples);
                            ((UnivariateEntry)e).setPDF(u, j);
                        }
                    }
                    
                    
                    //saving uncertain object
                    if (e instanceof UnivariateEntry)
                    {   
                        UnivariateEntry ue = (UnivariateEntry)e;

                        bwInfo.write(count+","+e.getClassLabel()+","+e.getClassLabelID()+","+e.getValues().length);
                        bwInfo.newLine();

                        for(int j=0 ; j<e.getValues().length ; j++)
                        {
                            String pdf = "";
                            String pdfType = "";
                            double lower=0.0;
                            double upper=0.0;

                            if(ue.getUValues()[j] instanceof Lognormal)
                            {
                                pdf = "lognormal";
                                //pdf = "l";
                                pdfType = "c";
                                Lognormal lg = (Lognormal)ue.getUValues()[j];
                                lower = lg.getLowerBound();
                                upper = lg.getUpperBound();
                            }
                            else if(ue.getUValues()[j] instanceof Gaussian)
                            {
                                pdf = "gaussian";
                                //pdf = "g";
                                pdfType = "c";
                                Gaussian g = (Gaussian)ue.getUValues()[j];
                                lower = g.getLowerBound();
                                upper = g.getUpperBound();
                            }
                            else if(ue.getUValues()[j] instanceof Binomial)
                            {
                                pdf = "binomial";
                                //pdf = "b";
                                pdfType = "d";
                                Binomial b = (Binomial)ue.getUValues()[j];
                                lower = b.getLowerBound();
                                upper = b.getUpperBound();
                            }
                            else if(ue.getUValues()[j] instanceof Uniform)
                            {
                                pdf = "uniform";
                                //pdf = "u";
                                pdfType = "c";
                                Uniform u = (Uniform)ue.getUValues()[j];
                                lower = u.getLowerBound();
                                upper = u.getUpperBound();
                            }
                            
                            if (count == 0 && j==0 && compact)
                            {
                                bwData.write("compact");
                                bwData.newLine();
                                bwData.write(pdfName+","+pdfType);
                                bwData.newLine();
                            }
                            
                            bwData.write(""+e.getValues()[j]);
                            bwData.write(",");
                            if (!compact)
                            {
                                bwData.write(pdf);
                                bwData.write(",");
                                bwData.write(pdfType);
                                bwData.write(",");
                            }
                            bwData.write(""+lower);
                            bwData.write(",");
                            bwData.write(""+upper);                    
                            
                            if(j!=e.getValues().length-1)
                            {
                                bwData.write(";");
                            }                        
                        }
                        bwData.newLine();

                        bwInfo.flush();
                        bwData.flush();
                    }
                    else
                    {
                        throw new RuntimeException("ERROR: not implemented");
                    }
                    
                    line = br.readLine();
                    count++;
                    
                    if (count % 20000 == 0)
                    {
                        System.out.println(count);
                    }
                }
                
                bwInfo.flush();
                bwData.flush();               
            
                bwInfo.close();
                bwData.close(); 
                
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
                
 
            System.out.println("Failures : "+failures+"\nTot : "+totLogNorm); 
        }                      
    }
    
    public void generateRandomUncertainty(String pdfName)
    {
        computeAttributesRange();
        
        if (pdfName.substring(pdfName.length()-2, pdfName.length()).equals("MV"))
        {
            generateRandomMultivariateUncertainty(pdfName.substring(0, pdfName.length()-2));
        }
        else
        {
            for(int i=0 ; i<d.getSize() ; i++)
            {
                Entry e = d.getEntry(i);
                for(int j=0 ; j<e.getValues().length ; j++)
                {
                    double percentage = 0.05;

                    Pair c = minMax[e.getClassLabelID()][j];
                    double lowerBound = c.getLowerBound();
                    double upperBound = c.getUpperBound();

                    double offset = 0.0;
                    if((upperBound-lowerBound)==0.0)
                    {
                        do
                        {
                            offset = Math.random();
                        }
                        while(offset==0.0);
                    }
                    else
                    {
                        offset = (upperBound-lowerBound)*percentage;
                    }

                    lowerBound -= offset;
                    upperBound += offset;

                    double value = e.getValues()[j];

                    if(pdfName.equalsIgnoreCase("lognormal"))
                    {
                        double sigma = -1;
                        Lognormal lg = null;

                        double randomLower = 0.0;
                        double randomUpper = 0.0;
                        double newLower = (value-lowerBound)*randomLower+lowerBound;
                        double newUpper = (upperBound-value)*randomUpper+value;

                        do
                        {
                            randomLower = Math.random();
                            randomUpper = Math.random();

                            newLower = (value-lowerBound)*randomLower+lowerBound;
                            newUpper = (upperBound-value)*randomUpper+value;
                            lg = new Lognormal(newLower, newUpper, value, nSamples);
                            sigma = lg.getSigma();
                        }
                        while( sigma <= 0.0);

                        offset = (upperBound-lowerBound)*percentagePDFCondition;

                        while(lg.getMu() <= this.lognormalPDFCondition*((sigma*sigma/2)-Math.log(sigma*Math.sqrt(2*Math.PI))) )
                        {
                            newLower -= offset;
                            newUpper += offset;

                            lg = new Lognormal(newLower, newUpper, value, nSamples);                        
                        }                        

                        totLogNorm++;
                        if(lg.calculate(value)>=1)
                        {
                            failures++;
                        }

                        ((UnivariateEntry)e).setPDF(lg, j);
                    }
                    else if(pdfName.equalsIgnoreCase("gaussian"))
                    {
                        double distLower = value-lowerBound;
                        double distUpper = upperBound-value;

                        percentage = 0.05;

                        double leg = (distLower+distUpper)/2.0;
                        double percValue = leg*percentage;

                        double newLower = value-leg;
                        double newUpper = value+leg;

                        Gaussian g = new Gaussian(newLower, newUpper, nSamples);
                        while (Math.sqrt(g.getVariance())<=this.gaussianPDFCondition)
                        {
                            leg += percValue;
                            newLower = value-leg;
                            newUpper = value+leg;
                            g = new Gaussian(newLower, newUpper, nSamples);
                        }                   

                        totLogNorm++;
                        if(g.calculate(value)>=1)
                        {
                            failures++;
                        }
                        ((UnivariateEntry)e).setPDF(g, j);
                    }
                    else if(pdfName.equalsIgnoreCase("binomial"))
                    {                                       
                        double randomLower = Math.random();
                        double randomUpper = Math.random(); 
                        double newLower = (value-lowerBound)*randomLower+lowerBound;
                        double newUpper = (upperBound-value)*randomUpper+value;

                        Binomial b = new Binomial(newLower, newUpper, value, binomialSamples);
                        ((UnivariateEntry)e).setPDF(b, j);
                    }
                    else if(pdfName.equalsIgnoreCase("uniform"))
                    {
                        double randomLower = Math.random();
                        double randomUpper = Math.random();

                        percentage = 0.2;

                        double newLower = value-(value-lowerBound)*randomLower;
                        double newUpper = value+(upperBound-value)*randomUpper;

                        double percValue = (newUpper-newLower)*percentage;

                        while((newUpper-newLower)<=1.0)
                        {  
                            newUpper += percValue;
                            newLower -= percValue;
                            
                            percValue = (newUpper-newLower)*percentage;
                        }

                        Uniform u = new Uniform(newLower, newUpper, nSamples);
                        ((UnivariateEntry)e).setPDF(u, j);
                    }
                }
            }
        
            System.out.println("Failures: "+failures+"\nTot : "+totLogNorm); 
        }                      
    }

    public void generateRandomMultivariateUncertainty(String pdfName)
    {
        for(int i=0 ; i<d.getSize() ; i++)
        {
            Entry e = d.getEntry(i);
            
            double[] minValues = new double[e.getValues().length];
            double[] maxValues = new double[e.getValues().length];
            
            MultivariatePDF mvPdf = null;
            
            for(int j=0 ; j<e.getValues().length ; j++)
            {
                double percentage = 0.05;

                Pair c = minMax[e.getClassLabelID()][j];
                double lowerBound = c.getLowerBound();
                double upperBound = c.getUpperBound();

                double offset = 0.0;
                if((upperBound-lowerBound)==0.0)
                {
                    do
                    {
                        offset = Math.random();
                    }
                    while(offset==0.0);
                }
                else
                {
                    offset = (upperBound-lowerBound)*percentage;
                }

                lowerBound -= offset;
                upperBound += offset;

                double value = e.getValues()[j];

                if(pdfName.equalsIgnoreCase("gaussian"))
                {
                    double distLower = value-lowerBound;
                    double distUpper = upperBound-value;

                    percentage = 0.05;

                    double ramo = (distLower+distUpper)/2.0;
                    double percValue = ramo*percentage;

                    double newLower = value-ramo;
                    double newUpper = value+ramo;

                    Gaussian g = new Gaussian(newLower, newUpper, nSamples);
                    while (Math.sqrt(g.getVariance())<=this.gaussianPDFCondition)
                    {
                        ramo += percValue;
                        newLower = value-ramo;
                        newUpper = value+ramo;
                        g = new Gaussian(newLower, newUpper, nSamples);
                    }
                    
                    minValues[j] = newLower;
                    maxValues[j] = newUpper;
                }
                else if(pdfName.equalsIgnoreCase("uniform"))
                {
                    double randomLower = Math.random();
                    double randomUpper = Math.random();

                    percentage = 0.2;
                    
                    double newLower = value-(value-lowerBound)*randomLower;
                    double newUpper = value+(upperBound-value)*randomUpper;

                    double percValue = (newUpper-newLower)*percentage;

                    while((newUpper-newLower)<=1.0)
                    {
                        newUpper += percValue;
                        newLower -= percValue;
                        
                        percValue = percValue = (newUpper-newLower)*percentage;
                    }
                    
                    minValues[j] = newLower;
                    maxValues[j] = newUpper;
                }
            }

            if(pdfName.equalsIgnoreCase("gaussian"))
            {
                mvPdf = new MultivariateGaussian(minValues, maxValues, nSamples*e.getValues().length, rho);
            }
            else if(pdfName.equalsIgnoreCase("uniform"))
            {
                mvPdf = new MultivariateUniform(minValues, maxValues, nSamples*e.getValues().length);
            }
            
            ((MultivariateEntry)e).setMultivariatePDF(mvPdf);
            
        }

                
    }
    
    
    
    /*
     * TO BE USED ONLY FOR Lognormal, Uniform, and Binomial PDFs
     * 
     */
    public void generateRangedUncertainty(String pdfName, String lowerBoundPath, String upperBoundPath)
    {
        try
        {
            FileReader frLower = new FileReader(lowerBoundPath);
            BufferedReader brLower = new BufferedReader(frLower);

            FileReader frUpper = new FileReader(upperBoundPath);
            BufferedReader brUpper = new BufferedReader(frUpper);

            for(int i=0 ; i<d.getSize() ; i++)
            {
                Entry e = d.getEntry(i);

                double percentage = 0.05;

                String lowerLine = brLower.readLine();
                String upperLine = brUpper.readLine();

                StringTokenizer stLower = new StringTokenizer(lowerLine, " ,;");
                StringTokenizer stUpper = new StringTokenizer(upperLine, " ,;");

                stLower.nextToken();//skip gene ID
                stUpper.nextToken();//skip gene ID

                for(int j=0 ; j<e.getValues().length ; j++)
                {

                    double lowerBound = Double.parseDouble(stLower.nextToken());
                    double upperBound = Double.parseDouble(stUpper.nextToken());

                    double value = e.getValues()[j];

                    if(pdfName.equalsIgnoreCase("lognormal"))
                    {
                        double sigma = -1;
                        Lognormal lg = new Lognormal(lowerBound, upperBound, value, nSamples);

                        double offset = (upperBound-lowerBound)*percentagePDFCondition;

                        while(lg.getMu() <= this.lognormalPDFCondition*((sigma*sigma/2)-Math.log(sigma*Math.sqrt(2*Math.PI))) )
                        {
                            lowerBound -= offset;
                            upperBound += offset;

                            lg = new Lognormal(lowerBound, upperBound, value, nSamples);
                        }

                        totLogNorm++;
                        if(lg.calculate(value)>=1)
                        {
                            failures++;
                        }

                        ((UnivariateEntry)e).setPDF(lg, j);
                    }
                    else if(pdfName.equalsIgnoreCase("binomial"))
                    {
                        Binomial b = new Binomial(lowerBound, upperBound, value, binomialSamples);
                        ((UnivariateEntry)e).setPDF(b, j);
                    }
                    else if(pdfName.equalsIgnoreCase("uniform"))
                    {
                        percentage = 0.2;
                        double percValue = (upperBound-lowerBound)*percentage;

                        while((upperBound-lowerBound)<=1.0)
                        {
                            upperBound += upperBound+percValue;
                            lowerBound -= lowerBound-percValue;
                        }

                        Uniform u = new Uniform(lowerBound, upperBound, nSamples);
                        ((UnivariateEntry)e).setPDF(u, j);
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void generateStdDevUncertainty(String devStdPath)
    {
        try
        {
            FileReader frDevStd = new FileReader(devStdPath);
            BufferedReader brDevStd = new BufferedReader(frDevStd);
            
            for(int i=0 ; i<d.getSize() ; i++)
            {
                Entry e = d.getEntry(i);

                double percentage = 0.05;

                String devStdLine = brDevStd.readLine();

                StringTokenizer stDevStd = new StringTokenizer(devStdLine, " ,;");

                stDevStd.nextToken(); //skip gene ID
                
                for(int j=0 ; j<e.getValues().length ; j++)
                {
                    double devStd = Double.parseDouble(stDevStd.nextToken());

                    double value = e.getValues()[j];
                    
                    double lowerBound = value - 3*devStd;
                    double upperBound = value + 3*devStd;

                    if(lowerBound>=upperBound)
                    {
                        System.out.println();
                    }
                    
                    double ramo = (upperBound-lowerBound)/2.0;
                    double percValue = ramo*percentage;
                    
                    Gaussian g = new Gaussian(lowerBound, upperBound, nSamples);
                    
                    while (Math.sqrt(g.getVariance())<=this.gaussianPDFCondition)
                    {
                        ramo += percValue;
                        lowerBound = value-ramo;
                        upperBound = value+ramo;
                        g = new Gaussian(lowerBound, upperBound, nSamples);
                    }

                    ((UnivariateEntry)e).setPDF(g, j);
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void computeAttributesRange()
    {
        minMax = new Pair[this.classes][this.dataAttributes];
     
        for (int i=0 ; i<d.getSize() ; i++)
        {
            Entry e = d.getEntry(i);
            int classID = e.getClassLabelID();
            for (int j=0; j<minMax[classID].length ; j++)
            {
                if (minMax[classID][j]==null)
                {
                    minMax[classID][j] = new Pair(e.getValues()[j],e.getValues()[j]);
                }
                else
                {
                    if(minMax[classID][j].getLowerBound()>e.getValues()[j])
                    {
                        minMax[classID][j].setLowerBound(e.getValues()[j]);
                    }
                    else if (minMax[classID][j].getUpperBound()<e.getValues()[j])
                    {
                        minMax[classID][j].setUpperBound(e.getValues()[j]);
                    }
                }
            }
        }              
    }
    
    private HashMap<String,Integer> computeAttributesRangeFromFile(int bound, String path, boolean ignoreFirstLine)
    {
        if (minMax == null)
        {    
            minMax = new Pair[this.classes][this.dataAttributes];
            for (int i=0; i<minMax.length; i++)
            {
                for (int j=0; j<minMax[i].length; j++)
                {
                    minMax[i][j] = new Pair(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
                }
            }
        }
        
        HashMap<String,Integer> classLabelToInt = new HashMap<String,Integer>();

        try
        {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            if (ignoreFirstLine)
            {
                br.readLine();
            }
            String line = null;
            int idEntry = 0;
            int classId = 0;
            
            while((line=br.readLine())!=null && idEntry<this.entries && idEntry<bound)
            {
                StringTokenizer st = new StringTokenizer(line,",;");
                double[] values = new double[this.dataAttributes];
                int pos = 0;
                int iValues = 0;
               
                String classLabel = "none"; 
                int classLabelInt = -1;
                while(st.hasMoreElements())
                {
                    String attribute = st.nextToken();
                    if(pos==classAttributePosition-1)
                    {
                        classLabel = attribute;

                        if (classLabelToInt.containsKey(classLabel))
                        {
                            classLabelInt = classLabelToInt.get(classLabel);
                        }
                        else
                        {
                            classLabelInt = classId;
                            classId++;
                            classLabelToInt.put(classLabel, classLabelInt);
                        }                 
                    }
                    else
                    {
                        values[iValues] = Double.parseDouble(attribute);
                        iValues++;
                    }
                    pos++;
                }
                
                if (iValues < values.length)
                {
                    throw new RuntimeException("ERROR: all attributes must be considered");
                }
                
                for (int i=0; i<values.length; i++)
                {
                    if (values[i] < minMax[classLabelInt][i].getLowerBound())
                    {
                        minMax[classLabelInt][i].setLowerBound(values[i]);
                    }
                    
                    if (values[i] > minMax[classLabelInt][i].getUpperBound())
                    {
                        minMax[classLabelInt][i].setUpperBound(values[i]);
                    }
                }
                
                idEntry++;
            }
            
            if (bound >= this.entries && !ignoreFirstLine && idEntry<this.entries)
            {
                throw new RuntimeException("ERROR: all objects must be considered");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return classLabelToInt;
    }  
    
    
    private boolean ePresente(String[] a, String value)
    {
        for (int i=0 ; i<a.length ; i++)
        {
            if (a[i]!=null && a[i].equalsIgnoreCase(value))
            {
                return true;
            }
        }
        return false;
    }
    
    public void saveDataset(String saveDataPath, String saveInfoPath)
    {
        try
        {
            FileWriter fwData = new FileWriter(saveDataPath);
            FileWriter fwInfo = new FileWriter(saveInfoPath);
            
            BufferedWriter bwData = new BufferedWriter(fwData);
            BufferedWriter bwInfo = new BufferedWriter(fwInfo);

            bwInfo.write(this.entries+","+this.dataAttributes);
            bwInfo.newLine();
            bwInfo.flush();
            
            for(int i=0 ; i<d.getSize() ; i++)
            {
                Entry e = d.getEntry(i);
                
                if (e instanceof UnivariateEntry)
                {   
                    UnivariateEntry ue = (UnivariateEntry)e;
                    
                    bwInfo.write(e.getId()+","+e.getClassLabel()+","+e.getClassLabelID()+","+e.getValues().length);
                    bwInfo.newLine();

                    for(int j=0 ; j<e.getValues().length ; j++)
                    {
                        String pdfName = "";
                        String pdfType = "";
                        double lower=0.0;
                        double upper=0.0;

                        if(ue.getUValues()[j] instanceof Lognormal)
                        {
                            pdfName = "lognormal";
                            pdfType = "c";
                            Lognormal lg = (Lognormal)ue.getUValues()[j];
                            lower = lg.getLowerBound();
                            upper = lg.getUpperBound();
                        }
                        else if(ue.getUValues()[j] instanceof Gaussian)
                        {
                            pdfName = "gaussian";
                            pdfType = "c";
                            Gaussian g = (Gaussian)ue.getUValues()[j];
                            lower = g.getLowerBound();
                            upper = g.getUpperBound();
                        }
                        else if(ue.getUValues()[j] instanceof Binomial)
                        {
                            pdfName = "binomial";
                            pdfType = "d";
                            Binomial b = (Binomial)ue.getUValues()[j];
                            lower = b.getLowerBound();
                            upper = b.getUpperBound();
                        }
                        else if(ue.getUValues()[j] instanceof Uniform)
                        {
                            pdfName = "uniform";
                            pdfType = "c";
                            Uniform u = (Uniform)ue.getUValues()[j];
                            lower = u.getLowerBound();
                            upper = u.getUpperBound();
                        }                   

                        bwData.write(e.getValues()[j]+","+pdfName+","+pdfType+","+lower+","+upper);                    

                        if(j!=e.getValues().length-1)
                        {
                            bwData.write(";");
                        }                        
                    }
                    bwData.newLine();

                    bwInfo.flush();
                    bwData.flush();
                }
                else if (e instanceof MultivariateEntry)
                {
                    MultivariateEntry ue = (MultivariateEntry)e;
                    
                    bwInfo.write(e.getId()+","+e.getClassLabel()+","+e.getClassLabelID()+","+e.getValues().length);
                    bwInfo.newLine();

                    for(int j=0 ; j<e.getValues().length ; j++)
                    {
                        String pdfName = "";
                        String pdfType = "";
                        double lower=0.0;
                        double upper=0.0;

                        if(ue.getMultivariatePDF() instanceof MultivariateGaussian)
                        {
                            pdfName = "gaussianMV";
                            pdfType = "c";
                            MultivariateGaussian mg = (MultivariateGaussian)ue.getMultivariatePDF();
                            lower = mg.getLowerBound()[j];
                            upper = mg.getUpperBound()[j];
                        }
                        else if(ue.getMultivariatePDF() instanceof MultivariateUniform)
                        {
                            pdfName = "uniformMV";
                            pdfType = "c";
                            MultivariateUniform mu = (MultivariateUniform)ue.getMultivariatePDF();
                            lower = mu.getLowerBound()[j];
                            upper = mu.getUpperBound()[j];
                        }                

                        bwData.write(e.getValues()[j]+","+pdfName+","+pdfType+","+lower+","+upper);                    

                        if(j!=e.getValues().length-1)
                        {
                            bwData.write(";");
                        }                        
                    }
                    bwData.newLine();

                    bwInfo.flush();
                    bwData.flush();
                }

                bwInfo.flush();
                bwData.flush();               
            }

            bwInfo.close();
            bwData.close();             
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void loadDataset(String loadDataPath, String loadInfoPath, double perc)
    {
        try
        {
            boolean compact = false;
            String pdfNameCompact = null;
            String pdfTypeCompact = null;
            
            FileReader frInfo = new FileReader(loadInfoPath);
            FileReader frData = new FileReader(loadDataPath);
            
            BufferedReader brInfo = new BufferedReader(frInfo);
            BufferedReader brData = new BufferedReader(frData);
            
            String preambolo = brInfo.readLine();
            StringTokenizer stPreambolo = new StringTokenizer(preambolo,",");
            
            int totalEntries = Integer.parseInt(stPreambolo.nextToken());
            this.entries = (int)Math.floor(perc*totalEntries);
            this.dataAttributes = Integer.parseInt(stPreambolo.nextToken());            
            
            
            Entry[] objects = new Entry[this.entries];
            boolean univariate = true;
            
            for(int i=0 ; i<this.entries ; i++)
            {
                String lineInfo = brInfo.readLine();
                StringTokenizer stInfo = new StringTokenizer(lineInfo,",");
                
                String ID = stInfo.nextToken();
                String classLabel = stInfo.nextToken();
                int classLabelID = Integer.parseInt(stInfo.nextToken());

                String lineData = brData.readLine();
                if (lineData.equals("compact"))
                {
                    compact = true;
                    StringTokenizer stc = new StringTokenizer(brData.readLine(),",;");
                    pdfNameCompact = stc.nextToken();
                    pdfTypeCompact = stc.nextToken();
                    brData.readLine();
                }
                               
                PDF[] pdfs = new PDF[this.dataAttributes];
                double[] values = new double[this.dataAttributes];
                
                MultivariatePDF mvPDF = null;
                double[] lowerBounds = new double[this.dataAttributes];
                double[] upperBounds = new double[this.dataAttributes];
                
                StringTokenizer stData = new StringTokenizer(lineData,";");
                
                for(int j=0 ; j<this.dataAttributes ; j++)
                {
                    StringTokenizer st = new StringTokenizer(stData.nextToken(),",");
                    
                    double value = Double.parseDouble(st.nextToken());
                    String pdfName = pdfNameCompact;
                    String pdfType = pdfTypeCompact;
                    if (!compact)
                    {
                        pdfName = st.nextToken();
                        pdfType = st.nextToken();
                    }
                    double lowerBound = Double.parseDouble(st.nextToken());
                    double upperBound = Double.parseDouble(st.nextToken());
                                        
                    if(pdfName.equalsIgnoreCase("lognormal"))
                    {
                        univariate = true;
                        pdfs[j] = new Lognormal(lowerBound,upperBound,value,nSamples);
                    }
                    else if(pdfName.equalsIgnoreCase("gaussian"))
                    {
                        univariate = true;
                        pdfs[j] = new Gaussian(lowerBound,upperBound,nSamples);
                    }
                    else if(pdfName.equalsIgnoreCase("binomial"))
                    {
                        univariate = true;
                        pdfs[j] = new Binomial(lowerBound,upperBound,value,binomialSamples);
                    }
                    else if(pdfName.equalsIgnoreCase("uniform"))
                    {
                        univariate = true;
                        pdfs[j] = new Uniform(lowerBound,upperBound,nSamples);
                    }
                    else if(pdfName.equalsIgnoreCase("gaussianMV"))
                    {
                        univariate = false;
                        lowerBounds[j] = lowerBound;
                        upperBounds[j] = upperBound;
                        
                        if (j == lowerBounds.length-1)
                        {
                            mvPDF = new MultivariateGaussian(lowerBounds, upperBounds, nSamples, this.rho);
                        }
                    }
                    else if(pdfName.equalsIgnoreCase("uniformMV"))
                    {
                        univariate = false;
                        lowerBounds[j] = lowerBound;
                        upperBounds[j] = upperBound;
                        
                        if (j == lowerBounds.length-1)
                        {
                            mvPDF = new MultivariateUniform(lowerBounds, upperBounds, nSamples);
                        }                        
                    }                    
                    values[j] = value;                    
                }
                Entry e = null;
                
                if (univariate)
                {
                    e = new UnivariateEntry(ID,classLabel,values,pdfs);
                }
                else
                {
                    e = new MultivariateEntry(ID,classLabel,values,mvPDF);
                }
                e.setClassLabelID(classLabelID);
                objects[i]=e;
            }
            d = new Dataset(objects);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }        
    }
    
    public void loadDatasetClassByClass(String loadDataPath, String loadInfoPath, double perc)
    {
        try
        {
            String baseLDP = loadDataPath.substring(0,loadDataPath.length()-5);
            String baseLIP = loadInfoPath.substring(0,loadInfoPath.length()-5);
            
            boolean compact = false;
            
            int totPercentage = (int)Math.floor(perc*this.entries);
            this.entries = totPercentage;
           
           BufferedReader[] brs = new BufferedReader[classes]; 
           int[] classSizes = new int[classes];
           for (int i=0; i<classSizes.length; i++)
           {
               String path = baseLDP+i+".data";
               BufferedReader br = new BufferedReader(new FileReader(path));
               classSizes[i] = Integer.parseInt(br.readLine());
               brs[i] = br;
           }
           
           int[] sizePerClass = new int[classes];
           int check = 0;
           for (int j=0; j<sizePerClass.length; j++)
           {
               int s = (int)Math.floor(perc*classSizes[j]);
               sizePerClass[j] = s;
               check += s;
           }

           if (check > totPercentage)
           {
               throw new RuntimeException("ERROR!");
           }
           else if (check < totPercentage)
           {
               int diff = totPercentage-check;
               for (int j=0; j<sizePerClass.length && diff > 0; j++)
               {
                   if (classSizes[j]-sizePerClass[j] >= diff)
                   {
                       sizePerClass[j] += diff;
                       diff = 0;
                   }
                   else
                   {
                       int tmp = classSizes[j]-sizePerClass[j];
                       sizePerClass[j] += tmp;
                       diff -= tmp;
                   }
               }              
           }
      
           Entry[] objects = new Entry[this.entries];
           boolean univariate = true;
           int pos = 0;
           for (int k=0; k<sizePerClass.length; k++)
           {
               String lip = baseLIP+k+".info";

               String pdfNameCompact = null;
               String pdfTypeCompact = null;

               FileReader frInfo = new FileReader(lip);

               BufferedReader brInfo = new BufferedReader(frInfo);
               BufferedReader brData = brs[k];

               String header = brInfo.readLine();                  


               int linesToRead = sizePerClass[k];
               for (int l=0; l<linesToRead; l++)
               {
                    String lineInfo = brInfo.readLine();
                    StringTokenizer stInfo = new StringTokenizer(lineInfo,",");

                    String ID = stInfo.nextToken();
                    String classLabel = stInfo.nextToken();
                    int classLabelID = Integer.parseInt(stInfo.nextToken());

                    String lineData = brData.readLine();
                    if (lineData.equals("compact"))
                    {
                        compact = true;
                        StringTokenizer stc = new StringTokenizer(brData.readLine(),",;");
                        pdfNameCompact = stc.nextToken();
                        pdfTypeCompact = stc.nextToken();
                        lineData = brData.readLine();
                    }

                    PDF[] pdfs = new PDF[this.dataAttributes];
                    double[] values = new double[this.dataAttributes];

                    MultivariatePDF mvPDF = null;
                    double[] lowerBounds = new double[this.dataAttributes];
                    double[] upperBounds = new double[this.dataAttributes];

                    StringTokenizer stData = new StringTokenizer(lineData,";");

                    for(int j=0 ; j<this.dataAttributes ; j++)
                    {
                        StringTokenizer st = new StringTokenizer(stData.nextToken(),",");

                        double value = Double.parseDouble(st.nextToken());
                        String pdfName = pdfNameCompact;
                        String pdfType = pdfTypeCompact;
                        if (!compact)
                        {
                            pdfName = st.nextToken();
                            pdfType = st.nextToken();
                        }
                        double lowerBound = Double.parseDouble(st.nextToken());
                        double upperBound = Double.parseDouble(st.nextToken());

                        if(pdfName.equalsIgnoreCase("lognormal"))
                        {
                            univariate = true;
                            pdfs[j] = new Lognormal(lowerBound,upperBound,value,nSamples);
                        }
                        else if(pdfName.equalsIgnoreCase("gaussian"))
                        {
                            univariate = true;
                            pdfs[j] = new Gaussian(lowerBound,upperBound,nSamples);
                        }
                        else if(pdfName.equalsIgnoreCase("binomial"))
                        {
                            univariate = true;
                            pdfs[j] = new Binomial(lowerBound,upperBound,value,binomialSamples);
                        }
                        else if(pdfName.equalsIgnoreCase("uniform"))
                        {
                            univariate = true;
                            pdfs[j] = new Uniform(lowerBound,upperBound,nSamples);
                        }
                        else if(pdfName.equalsIgnoreCase("gaussianMV"))
                        {
                            univariate = false;
                            lowerBounds[j] = lowerBound;
                            upperBounds[j] = upperBound;

                            if (j == lowerBounds.length-1)
                            {
                                mvPDF = new MultivariateGaussian(lowerBounds, upperBounds, nSamples, this.rho);
                            }
                        }
                        else if(pdfName.equalsIgnoreCase("uniformMV"))
                        {
                            univariate = false;
                            lowerBounds[j] = lowerBound;
                            upperBounds[j] = upperBound;

                            if (j == lowerBounds.length-1)
                            {
                                mvPDF = new MultivariateUniform(lowerBounds, upperBounds, nSamples);
                            }                        
                        }                    
                        values[j] = value;                    
                    }

                    Entry e = null;

                    if (univariate)
                    {
                        e = new UnivariateEntry(ID,classLabel,values,pdfs);
                    }
                    else
                    {
                        e = new MultivariateEntry(ID,classLabel,values,mvPDF);
                    }
                    e.setClassLabelID(classLabelID);
                    objects[pos]=e;
                    pos++;
                }                   
           }           

           if (pos != objects.length)
           {
               throw new RuntimeException("ERROR: all objects must be loaded---objects="+objects.length+", loaded="+pos);
           }
           
           d = new Dataset(objects);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }        
    }
    
    
    public void loadDataset(String loadDataPath, String loadInfoPath)
    {
        loadDataset(loadDataPath, loadInfoPath, 1.0);
    }
    
    public void loadDeterministicDataset(String loadDataPath, String loadInfoPath)
    {
        try
        {
            FileReader frInfo = new FileReader(loadInfoPath);
            FileReader frData = new FileReader(loadDataPath);
            
            BufferedReader brInfo = new BufferedReader(frInfo);
            BufferedReader brData = new BufferedReader(frData);
            
            String header = brInfo.readLine();
            StringTokenizer stHeader = new StringTokenizer(header,",");
            
            this.entries = Integer.parseInt(stHeader.nextToken());
            this.dataAttributes = Integer.parseInt(stHeader.nextToken());            
            
            
            Entry[] objects = new Entry[this.entries];
            boolean univariate = true;
            
            for(int i=0 ; i<this.entries ; i++)
            {
                String lineInfo = brInfo.readLine();
                StringTokenizer stInfo = new StringTokenizer(lineInfo,",");
                
                String ID = stInfo.nextToken();
                String classLabel = stInfo.nextToken();
                int classLabelID = Integer.parseInt(stInfo.nextToken());

                String lineData = brData.readLine();

                double[] values = new double[this.dataAttributes];
                
                MultivariatePDF mvPDF = null;
                double[] lowerBounds = new double[this.dataAttributes];
                double[] upperBounds = new double[this.dataAttributes];
                
                StringTokenizer stData = new StringTokenizer(lineData,";");
                
                for(int j=0 ; j<this.dataAttributes ; j++)
                {
                    StringTokenizer st = new StringTokenizer(stData.nextToken(),",");
                    
                    double value = Double.parseDouble(st.nextToken());
                    String pdfName = st.nextToken();
                    String pdfType = st.nextToken();
                    double lowerBound = Double.parseDouble(st.nextToken());
                    double upperBound = Double.parseDouble(st.nextToken());
                                        
                    if(pdfName.equalsIgnoreCase("lognormal"))
                    {
                        univariate = true;
                        PDF pdf = new Lognormal(lowerBound,upperBound,value,nSamples);
                        values[j] = pdf.getRandomSample();
                    }
                    else if(pdfName.equalsIgnoreCase("gaussian"))
                    {
                        univariate = true;
                        PDF pdf = new Gaussian(lowerBound,upperBound,nSamples);
                        values[j] = pdf.getRandomSample();
                    }
                    else if(pdfName.equalsIgnoreCase("binomial"))
                    {
                        univariate = true;
                        PDF pdf = new Binomial(lowerBound,upperBound,value,binomialSamples);
                        values[j] = pdf.getRandomSample();
                    }
                    else if(pdfName.equalsIgnoreCase("uniform"))
                    {
                        univariate = true;
                        PDF pdf = new Uniform(lowerBound,upperBound,nSamples);
                        values[j] = pdf.getRandomSample();
                    }
                    else if(pdfName.equalsIgnoreCase("gaussianMV"))
                    {
                        univariate = false;
                        lowerBounds[j] = lowerBound;
                        upperBounds[j] = upperBound;
                        
                        if (j == lowerBounds.length-1)
                        {
                            mvPDF = new MultivariateGaussian(lowerBounds, upperBounds, nSamples, this.rho);
                            values = mvPDF.getRandomSample();
                        }
                    }
                    else if(pdfName.equalsIgnoreCase("uniformMV"))
                    {
                        univariate = false;
                        lowerBounds[j] = lowerBound;
                        upperBounds[j] = upperBound;
                        
                        if (j == lowerBounds.length-1)
                        {
                            mvPDF = new MultivariateUniform(lowerBounds, upperBounds, nSamples);
                            values = mvPDF.getRandomSample();
                        }                        
                    }                                        
                }
                Entry e = null;

                e = new DeterministicEntry(ID,classLabel,values);

                e.setClassLabelID(classLabelID);
                objects[i]=e;
            }
            d = new Dataset(objects);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public Dataset getDataset()
    {
        return d;
    }
    
    public int getBinomialSamples() 
    {
        return binomialSamples;
    }
    
    public int getClasses() 
    {
        return classes;
    }
}