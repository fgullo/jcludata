package utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.StringTokenizer;

public  class Util 
{
    private String basePath;
    
    private int classes;
    private int classAttributePosition;
    private int idPosition;
    private int entries;
    
    private String[] classNames;
    private int[] classIDs;
    
    public Util(String basePath)
    {
        this.basePath = basePath;
    }
    
    public void dataForMatlab(String sourcePath, String infoPath)
    {
        try
        {
            this.readInfo(basePath+infoPath);
            
            FileReader fr = new FileReader(basePath+sourcePath);
            FileWriter fw = new FileWriter(basePath+"matlab"+File.separator+""+sourcePath+".mtl");
            
            BufferedReader br = new BufferedReader(fr);
            BufferedWriter bw = new BufferedWriter(fw);
            
            String line = null;

            classNames = new String[classes];
            classIDs = new int[classes];     
            
            while((line=br.readLine())!=null)
            {
                StringTokenizer st = new StringTokenizer(line," ,;");
                
                int pos = 0;
                String classToken = null;
                
                while(st.hasMoreTokens())
                {
                    String token = "";
                    if(pos==(idPosition-1))
                    {
                        st.nextToken();
                    }
                    else if (pos==(classAttributePosition-1))
                    {
                        classToken = ""+dammiIlClassID(st.nextToken());
                    }
                    else
                    {
                        token = st.nextToken()+" ";
                    }
                    bw.write(token);                    
                    pos++;
                }
                bw.write(classToken);
                bw.newLine();
                bw.flush();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void distanceMatrixSymmetrized(String sourcePath)
    {
        try
        {
            DistancePersistenceManager dpm = new DistancePersistenceManager(sourcePath,'r');
            
            double[][] matrix = dpm.readData(sourcePath, this.entries);
            
            for(int i=1 ; i<matrix.length ; i++)
            {
                for(int j=0 ; j<i ; j++)
                {
                    matrix[j][i] = matrix[i][j];
                }
            }
            
            FileWriter fw = new FileWriter(sourcePath+".mtl");
            BufferedWriter bw = new BufferedWriter(fw);
            for(int i=0 ; i<matrix.length ; i++)
            {
                for(int j=0 ; j<matrix[i].length ; j++)
                {
                    bw.write(""+matrix[i][j]+" ");
                }
                bw.newLine();
                bw.flush();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void readInfo(String infoPath)
    {
        try
        {
            FileReader fr = new FileReader(infoPath);
            BufferedReader br = new BufferedReader(fr);
            String line = null;
            while((line=br.readLine())!=null)
            {
                StringTokenizer st = new StringTokenizer(line," =:,;");
                String propertyName = st.nextToken();
               
                if (propertyName.equalsIgnoreCase("classes"))
                {
                    String value = st.nextToken();
                    classes = Integer.parseInt(value);                                      
                }
                else if (propertyName.equalsIgnoreCase("lines"))
                {
                    String value = st.nextToken();
                    entries = Integer.parseInt(value);                                      
                }
                else if (propertyName.equalsIgnoreCase("classAttributePosition"))
                {
                    String value = st.nextToken();
                    classAttributePosition = Integer.parseInt(value);
                }
                
                else if (propertyName.equalsIgnoreCase("idPosition"))
                {
                    String value = st.nextToken();
                    idPosition = Integer.parseInt(value);
                }                
            }           
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }        
    }
    
    private int dammiIlClassID(String className)
    {
        for(int i=0 ; i<classNames.length ; i++)
        {
            if (classNames[i]!= null && classNames[i].equalsIgnoreCase(className))
            {
                return classIDs[i];
            }
        }
        
        for(int i=0 ; i<classNames.length ; i++)
        {
            if(classNames[i]==null)
            {
                classNames[i]=className;
                int clID = 1;
                for(int j=0 ; j<classIDs.length ; j++)
                {
                    if(classIDs[j]!=0)
                    {
                        clID++;
                    }
                }
                classIDs[i]=clID;
                return clID;
            }
        }
        return -1;
    }
}
