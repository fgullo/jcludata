package utility;

import java.io.*;
import java.util.StringTokenizer;

public class DistancePersistenceManager
{
    private String filePath;

    private char mode;
    private BufferedWriter bw;
    private BufferedReader br;
    
    public DistancePersistenceManager(String filePath, char mode)
    {
        this.filePath = filePath;
        this.mode = mode;
        switch(mode)
        {
            case 'w' :
                try
                {
                    FileWriter fwData = new FileWriter(filePath,true);
                    bw = new BufferedWriter(fwData);            
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                break;
            
            case 'r':
                try
                {
                    FileReader frData = new FileReader(filePath);
                    br = new BufferedReader(frData);            
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                break;
        }
    }
    
    public void writeData(double data)
    {
        try
        {
            bw.write(" "+data);
            bw.flush();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }        
    }
    
    public void writeData(int data)
    {
        try
        {
            bw.write(" "+data);
            bw.flush();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }        
    }
    
    public double[][] readData(String path, int size)
    {
        try
        {
            double [][] matrix = new double[size][size];
                        
            String line=br.readLine();
            int row = 0;
            
            while (row<size)
            {
                if (line.equals(""))
                {
                    matrix[row] = new double[size];
                    line = br.readLine();
                    row++;
                }
                StringTokenizer st = new StringTokenizer(line," ,;");
                double[] array = new double[size];
                int pos = 0;

                while(st.hasMoreTokens() && pos<array.length)
                {
                    double dist = Double.parseDouble(st.nextToken());
                    array[pos] = dist;
                    pos++;
                }
                matrix[row] = array;
                row++;
                line=br.readLine();
            }
            
            return matrix;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }        
    }
    
    public double[][][][] readDataFDBSCAN(String path, int size, int samples)
    {
        int n = size;
        int s = samples;
        
        double[][][][] m = new double[n][s][n][s];
 
        try
        {
            for (int i1=0; i1<n; i1++)
            {
                for (int i2=0; i2<s; i2++)
                {
                    for (int j1=0; j1<n; j1++)
                    {
                        for (int j2=0; j2<s; j2++)
                        {
                            String line = line = br.readLine();
                            m[i1][i2][j1][j2] = Double.parseDouble(line);
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }           
        
        return m;
    }
    
    public void newLine()
    {
        try
        {
            bw.newLine();
            bw.flush();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        } 
    }

    public String getFilePath()
    {
        return filePath;
    }

}
