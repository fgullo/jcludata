package entities;

public class Dataset 
{
    private Entry[] entries;
    private Classification classification;
    
    public Dataset(Entry[] entries)
    {
        this.entries = entries;
        generateClassification();
    }
            
    public Entry getEntry(int position)
    {
        return entries[position];
    }
    
    public int getSize()
    {
        return entries.length;
    }
    
    public int getEntrySize()
    {
        return entries[0].getValues().length;
    }
    
    public Entry[] getEntries()
    {
        return entries;
    }

    private void generateClassification() 
    {
        int maxLabel = 0;
        for(int i=0 ; i<entries.length ; i++)
        {
            if(entries[i].getClassLabelID()> maxLabel)
            {
                maxLabel = entries[i].getClassLabelID();
            }
        }
        
        int[] classInfo = new int[maxLabel+1];
        for(int i=0 ; i<entries.length ; i++)
        {
            int l = entries[i].getClassLabelID();
            if (l == -1)
            {
                l++;
            }
            classInfo[l]++;
        }
        classification = new Classification(classInfo);
    }
    
    public Classification getClassification()
    {
        return this.classification;
    }
}
