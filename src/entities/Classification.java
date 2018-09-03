package entities;

public class Classification 
{
    private int[] classInfo;
    
    public Classification (int[] classesInfo)
    {
        this.classInfo = classesInfo;
    }
    
    public int getNumberOfClasses()
    {
        return this.classInfo.length;
    }
    
    public int getNumberOfObjectsInClass(int classID)
    {
        return this.classInfo[classID];
    }
}
