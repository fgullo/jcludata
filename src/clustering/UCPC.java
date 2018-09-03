package clustering;

import entities.*;
import java.util.ArrayList;


public class UCPC
{
    public int MAX_ITERATIONS = 50;
    private Dataset d;

    private int iterations;

    private double[][] mu;
    private double[][] mu2;
    private double[][] sigma2;

    private double[][] psi;
    private double[][] phi;
    private double[][] upsilon;
    private double[] Qs;

    public UCPC(Dataset d)
    {
        this.d = d;
        initialization();
        //computeGravityCenterED();
    }

    protected Cluster[] run(int k, int[] objToClustAssignments, int[]clustSizes)
    {
        //computing initial Psi, Phi, Upsilon values
        this.psi = new double[k][d.getEntrySize()];
        this.phi = new double[k][d.getEntrySize()];
        this.upsilon = new double[k][d.getEntrySize()];
        this.Qs = new double[k];
        for (int j=0; j<this.psi[0].length; j++)
        {
            for (int i=0; i<this.mu.length; i++)
            {
                this.psi[objToClustAssignments[i]][j] += this.sigma2[i][j];
                this.phi[objToClustAssignments[i]][j] += this.mu2[i][j];
                this.upsilon[objToClustAssignments[i]][j] += this.mu[i][j];
            }

            for (int c=0; c<this.upsilon.length; c++)
            {
                double value = this.upsilon[c][j]*this.upsilon[c][j];
                this.upsilon[c][j] = value;
            }

        }

        double V = 0.0;
        for (int c=0; c<this.psi.length; c++)
        {
            for (int j=0; j<this.psi[0].length; j++)
            {
                double VC = this.psi[c][j]/clustSizes[c]+this.phi[c][j]-this.upsilon[c][j]/clustSizes[c];
                if (Double.isInfinite(VC) || Double.isNaN(VC) || VC < -0.000001)
                {
                    throw new RuntimeException("Invalid value for VC: VC="+VC);
                }

                if (VC > 0.0)
                {
                    this.Qs[c] += VC;
                    V += VC;
                }
            }
        }

        iterations = 1;
        //System.out.println("\nRVUA started...\n");
        int notRelocatedObjects = 0;
        while (notRelocatedObjects < d.getSize()-1 && iterations<=this.MAX_ITERATIONS)
        {
            //System.out.println("RVUA  IT="+iterations);

            //System.out.println("V="+V);

             //relocation phase
             for (int i=0; i<objToClustAssignments.length && notRelocatedObjects<d.getSize()-1; i++)
             {
                 int assignment = objToClustAssignments[i];

                 if (clustSizes[assignment] > 1)
                 {
                     double minV = V;
                     int newAssignment = -1;

                     double[] psiPlus = null;
                     double[] psiMinus = null;
                     double[] phiPlus = null;
                     double[] phiMinus = null;
                     double[] upsilonPlus = null;
                     double[] upsilonMinus = null;
                     double Qplus = -1;
                     double Qminus = -1;

                     for (int c=0; c<k; c++)
                     {
                         if (c != assignment)
                         {
                             double[] psiPlusTmp = new double[d.getEntrySize()];
                             double[] psiMinusTmp = new double[d.getEntrySize()];
                             double[] phiPlusTmp = new double[d.getEntrySize()];
                             double[] phiMinusTmp = new double[d.getEntrySize()];
                             double[] upsilonPlusTmp = new double[d.getEntrySize()];
                             double[] upsilonMinusTmp = new double[d.getEntrySize()];
                             double QplusTmp = 0.0;
                             double QminusTmp = 0.0;

                             //compute new Psi, Phi, Upsilon values
                             for (int j=0; j<psiPlusTmp.length; j++)
                             {
                                psiPlusTmp[j] = this.psi[c][j]+this.sigma2[i][j];
                                //throwsException(psiPlusTmp[j]/(clustSizes[c]+1));
                                QplusTmp += psiPlusTmp[j]/(clustSizes[c]+1);
                                psiMinusTmp[j] = this.psi[assignment][j]-this.sigma2[i][j];
                                QminusTmp += psiMinusTmp[j]/(clustSizes[assignment]-1);
                                //throwsException(psiMinusTmp[j]/(clustSizes[ass]-1));

                                phiPlusTmp[j] = this.phi[c][j]+this.mu2[i][j];
                                //throwsException(phiPlusTmp[j]);
                                QplusTmp += phiPlusTmp[j];
                                phiMinusTmp[j] = this.phi[assignment][j]-this.mu2[i][j];
                                //throwsException(phiMinusTmp[j]);
                                QminusTmp += phiMinusTmp[j];
                                
                                double x = Math.sqrt(this.upsilon[c][j])+this.mu[i][j];
                                upsilonPlusTmp[j] = x*x;
                                //throwsException(upsilonPlusTmp[j]/(clustSizes[c]+1));
                                QplusTmp -= upsilonPlusTmp[j]/(clustSizes[c]+1);
                                double y = Math.sqrt(this.upsilon[assignment][j])-this.mu[i][j];
                                upsilonMinusTmp[j] = y*y;
                                //throwsException(upsilonMinusTmp[j]/(clustSizes[ass]-1));
                                QminusTmp -= upsilonMinusTmp[j]/(clustSizes[assignment]-1);
                             }

                             double Vtmp = V - this.Qs[assignment] - this.Qs[c] + QplusTmp + QminusTmp;

                             if (Vtmp < minV)
                             {
                                minV = Vtmp;
                                newAssignment = c;

                                psiPlus = psiPlusTmp;
                                psiMinus = psiMinusTmp;
                                phiPlus = phiPlusTmp;
                                phiMinus = phiMinusTmp;
                                upsilonPlus = upsilonPlusTmp;
                                upsilonMinus = upsilonMinusTmp;
                                Qplus = QplusTmp;
                                Qminus = QminusTmp;
                             }
                         }
                     }

                     if (newAssignment != -1)
                     {
                         notRelocatedObjects = 0;

                         //perform new assignment and recompute centroid
                         objToClustAssignments[i] = newAssignment;
                         clustSizes[assignment]--;
                         clustSizes[newAssignment]++;

                         psi[newAssignment] = psiPlus;
                         psi[assignment] = psiMinus;
                         phi[newAssignment] = phiPlus;
                         phi[assignment] = phiMinus;
                         upsilon[newAssignment] = upsilonPlus;
                         upsilon[assignment] = upsilonMinus;
                         Qs[newAssignment] = Qplus;
                         Qs[assignment] = Qminus;

                         V = minV;

                         double check = 0.0;
                         for (int h=0; h<Qs.length; h++)
                         {
                             check += Qs[h];
                         }

                         double xxx = Math.abs(check-V);

                         if (xxx > 00000000.1)
                         {
                             throw new RuntimeException("ERROR: global V must be equal to the sum of single Qs---V="+V+", sumOfSingleQs="+check);
                         }
                     }
                     else
                     {
                         notRelocatedObjects++;
                     }
                 }
                 else
                 {
                     notRelocatedObjects++;
                 }
             }

            iterations++;
        }


        //build clusters
        ArrayList<Entry>[] clusters = new ArrayList[k];
        for (int c=0; c<k; c++)
        {
            clusters[c] = new ArrayList<Entry>(clustSizes[c]);
        }

        for (int i=0; i<objToClustAssignments.length; i++)
        {
            int assignment = objToClustAssignments[i];
            clusters[assignment].add(d.getEntry(i));
        }

        Cluster[] clustering = new Cluster[k];
        for (int c=0; c<clusters.length; c++)
        {
            clustering[c] = new Cluster(clusters[c], c);
        }

        return clustering;
    }

    public int getIterations()
    {
        return iterations;
    }
    
    public Cluster[] execute(int k)
    {
        //random initialization
        int[] objToClustAssignments = new int[d.getSize()];
        for (int i=0; i<objToClustAssignments.length; i++)
        {
            objToClustAssignments[i] = -1;
        }
        int[] clustSizes = new int[k];
        randomAssignments(objToClustAssignments,clustSizes,k);
        
        return run(k,objToClustAssignments,clustSizes);
    }
    
    public Cluster[] execute(int k,int[] objToClustAssignments,int[] clustSizes)
    {
        return run(k,objToClustAssignments,clustSizes);
    }

    public int[] randomAssignments(int[] assignments, int[] clustSizes, int k)
    {
        double perc = 0.1;
        int percMin = (int)Math.floor(((double)d.getSize())/k*perc);
        if (percMin < 1)
        {
            percMin = 1;
        }

        for (int i=0; i<assignments.length; i++)
        {
            int c = (int)Math.floor(Math.random()*k);
            clustSizes[c]++;
            assignments[i] = c;
        }

        for (int c = 0; c<clustSizes.length; c++)
        {
            if (clustSizes[c] == 0)
            {
                for (int j=0; j<percMin; j++)
                {
                    int obj = (int)Math.floor(Math.random()*((double)d.getSize()));
                    clustSizes[assignments[obj]]--;
                    assignments[obj] = c;
                    clustSizes[c]++;
                }
            }
        }

        int sumCheck = 0;
        for (int c=0; c<clustSizes.length; c++)
        {
            if (clustSizes[c] == 0)
            {
                throw new RuntimeException("ERROR: clusters cannot be empty!");
            }

            sumCheck += clustSizes[c];
        }

        if (sumCheck != d.getSize())
        {
            throw new RuntimeException("ERROR: sum of cluster sizes must be equal to dataset size---clustSizes="+sumCheck+", datasetSize="+d.getSize());
        }

        for (int i=0; i<assignments.length; i++)
        {
            if (assignments[i] < 0 || assignments[i] >= k)
            {
                throw new RuntimeException("ERROR: assignments must be within [0,+"+k+"]---assignment="+assignments[i]);
            }
        }

        return assignments;
    }



    private void initialization()
    {
        //computeMuMu2Sigma2
        this.mu = new double[d.getSize()][d.getEntrySize()];
        this.mu2 = new double[d.getSize()][d.getEntrySize()];
        this.sigma2 = new double[d.getSize()][d.getEntrySize()];

        for (int i=0; i<mu.length; i++)
        {
            double[] mean = d.getEntry(i).getMean();
            double[] mean2 = d.getEntry(i).getSecondOrderMoment();

            for (int j=0; j<mean.length; j++)
            {
                mu[i][j] = mean[j];
                mu2[i][j] = mean2[j];
                double sigma =  mean2[j]-mean[j]*mean[j];

                if (Double.isInfinite(sigma) || Double.isNaN(sigma) || sigma < -0.000001)
                {
                    throw new RuntimeException("Invalid value for variance: sigma="+sigma);
                }

                if (sigma < 0.0)
                {
                    sigma = 0.0;
                }
                sigma2[i][j] = sigma;
            }
        }
    }

    public long[] offlineTime(double[][] muAppr, double[][] mu2Appr, double[][] sigma2Appr, int regionSamples)
    {
        long[] ret = new long[2];

        long start = System.currentTimeMillis();
        double[][][] samples = new double[d.getSize()][regionSamples][d.getEntrySize()+1];
        for (int i=0; i<samples.length; i++)
        {
            Entry e = d.getEntry(i);
            for (int s=0; s<regionSamples; s++)
            {
                samples[i][s] = e.getRandomSample();
            }
        }
        long stop1 = System.currentTimeMillis();
        ret[0] = stop1-start;

        for (int i=0; i<mu.length; i++)
        {
            Entry e1 = d.getEntry(i);

            double prob = 0.0;
            for (int s=0; s<regionSamples-1; s++)
            {
                double[] sample = samples[i][s];

                for (int j=0; j<sample.length-1; j++)
                {
                    muAppr[i][j] += sample[j]*sample[sample.length-1];
                    mu2Appr[i][j] += sample[j]*sample[j]*sample[sample.length-1];
                }

                prob += sample[sample.length-1];
            }

            for (int j=0; j<muAppr[i].length; j++)
            {
                muAppr[i][j] /= prob;
                mu2Appr[i][j] /= prob;
            }
        }

        for (int i=0; i<sigma2Appr.length; i++)
        {
            for (int j=0; j<sigma2Appr[i].length; j++)
            {
                sigma2Appr[i][j] = mu2Appr[i][j]-muAppr[i][j]*muAppr[i][j];
            }
        }

        long stop = System.currentTimeMillis();
        ret[1] = stop-start;
        return ret;
    }

    private void throwsException(double x)
    {
        if (Double.isInfinite(x) || Double.isNaN(x) || x < -0.0000001)
        {
            throw new RuntimeException("Value must be greater than 0---value="+x);
        }
    }

    public void setMaxIterations(int MAX_ITERATIONS) 
    {
        this.MAX_ITERATIONS = MAX_ITERATIONS;
    }
}