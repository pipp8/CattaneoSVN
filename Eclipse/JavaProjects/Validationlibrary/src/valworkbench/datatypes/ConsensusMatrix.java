package valworkbench.datatypes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
/**
 * The ConsensusMatrix class provides an object for representing a cosensus value.
 * An object of type ConsensusMatrix contain a Matrix with the following fields: 
 * 'nOfItems'that contain the number of item, that contain for each cell (i,j) value 
 * of consensus from i and j.
 * 
 * @author Raffaele Giancarlo
 * @author Filippo Utro
 * @author Davide Scaturro
 * @version 1.0
 *
 */
//DA AGGIUNGERE LANCIO ALG, RIDUZIONE PATTERN MATRIX NEL RESAMPLE, VISUALIZZAZIONE E OUT
public class ConsensusMatrix 
{
	private int nOfItems;

	private double ConsMatrix[][];

	private double cdfValue[]=null;

	private List<Double> elements=null;

	public ConsensusMatrix(int nOfItems) {
		this.nOfItems = nOfItems;
		double tmp[][] = new double[nOfItems][];
		// Instance of Consensus Matrix
		for (int i = 0; i < this.nOfItems; i++) {
			tmp[i] = new double[i + 1];
		}
		this.ConsMatrix = tmp;
	}

	/**
	 * Initialize the Consensus Matrix to zero
	 * 
	 */
	public void setConsensusMatrixtoZero() 
	{
		for (int i = 0; i < this.nOfItems; i++) 
		{
			for (int j = 0; j < this.ConsMatrix[i].length; j++) 
			{
				this.ConsMatrix[i][j] = 0;
			}
		}
	}

	/**
	 * Set the element (i,j) of the Consensus Matrix to value
	 * 
	 * @param i index of row
	 * @param j index of column
	 * @param value
	 */
	public void setConsensuMatrixEntry(int i, int j, double value)
	{
		this.ConsMatrix[i][j] = value;
	}

	/**
	 * Get the value of element (i,j) of the Consensus Matrix
	 * 
	 * @param i index of row
	 * @param j index of column
	 * @return the value of Consensus Matrix
	 */
	public double getConsensusMatrixEntry(int i, int j) 
	{
		return this.ConsMatrix[i][j];
	}

	/**
	 * Calclute the value, and set, of entry the Consensus Matrix
	 * 
	 * @param cMatrix Connetivity Matrix for corrispondent Consensu Matrix ( i.e. k-cluster)
	 * @param iMatrix Indicator Matrix for corrispondent Consensu Matrix ( i.e. k-cluster)
	 */
	public void setConsensusMatrix(ConnettivityMatrix cMatrix, IndicatorMatrix iMatrix) 
	{
		for (int i = 0; i < this.nOfItems; i++) 
		{
			for (int j = 0; j < this.ConsMatrix[i].length; j++) 
			{
				if (iMatrix.getIMatrixEntry(i, j)>0)
				{
					double c= (double)cMatrix.getConnMatrixEntry(i, j)/(double)iMatrix.getIMatrixEntry(i, j);
					setConsensuMatrixEntry(i, j,c);	
					if (c>1)
					{
						System.out.println("Error in the value:\n Connettivity Matrix="+(double)cMatrix.getConnMatrixEntry(i, j)+"Index Matrix="+(double)iMatrix.getIMatrixEntry(i, j));
						System.out.println("i="+i+" j="+j);
						System.exit(1);
					}
				}

			}
		}
	}

	/**
	 * Get the i-th row of the Consensus Matrix
	 * 
	 * @param i index of row
	 * @return a double vector
	 */
	public double[] getConsensusMatrixRow(int i) 
	{

		double vector[] = new double[i];

		for (int j = 0; j < this.ConsMatrix[i].length; j++) 
		{
			vector[i] = getConsensusMatrixEntry(i, j);
		}
		return vector;
	}

	/**
	 * copy the Consensus Matrix
	 * 
	 * @param consMatrix matrix to copy
	 */
	public void copyConsMatrix(ConsensusMatrix consMatrix) 
	{
		for (int i = 0; i < this.nOfItems; i++) 
		{
			this.ConsMatrix[i] = consMatrix.getConsensusMatrixRow(i);
		}
	}

	/**
	 * 
	 * 
	 * @return The number of Items in Consensus Matrix
	 */
	public int getnOfItems() 
	{
		return this.nOfItems;
	}

	/**
	 * 
	 * @param nOfItems the number of items in consensus matrix 
	 */
	public void setnOfItems(int nOfItems) 
	{
		this.nOfItems = nOfItems;
	}

	/**
	 * Sort the element of the Consensus Matrix and it inserts them in a array
	 * 
	 * @return a vector double
	 */
	public double[] vectorSortElement() 
	{
		int n = (this.nOfItems *(this.nOfItems + 1)) / 2;

		double[] vector = new double[n];

		int index = 0;

		for (int i = 0; i < this.nOfItems; i++) 
		{
			for (int j = 0; j < i+1; j++) 
			{
				vector[index] = getConsensusMatrixEntry(i, j);
				index++;
			}
		}

		Arrays.sort(vector);

		return vector;

	}

	/**
	 * write the elements of consensus matrix in a file
	 * 
	 * @param path file path where store.
	 * @param missing is 0 if the missing value, 0 otherwise
	 */

	public void storeToFileElement(String path, int missing) throws FileNotFoundException, IOException, NumberFormatException,	Exception 
	{
		// Open file in append
		FileOutputStream file = new FileOutputStream(path, true);
		PrintStream Output = new PrintStream(file);

		double vector[]=vectorSortElement();

		if (missing == 1) 
		{
			// write elements
			for (int i = 0; i < vector.length; i++) 
			{
				Output.print(vector[i] + "\t");
			}
			Output.println("");
		} 
		else 
		{
			Output.println("* Missing");
		}
		// write last row
		Output.println("-----------");
		// close file
		Output.close();


	}


	/**
	 * Calculate the value of CDF
	 * 
	 * @param c threshold for the calculate cdf value
	 * @return the CDF value
	 */
	public double calculateCDF(double c)
	{

		double j = 0;

		for(int z=0; z<this.nOfItems;z++)
			for (int i=0;i<z+1;i++)
			{
				if (this.ConsMatrix[z][i]< c) 
				{
					j++;
				}	
			}

		double cdf=j / (double)((this.nOfItems*(this.nOfItems+1))/2);
		return cdf;
	}

	/**
	 * Calculate the value of CDF
	 * 
	 * @param c threshold for the calculate cdf value
	 * @param elements is a vector of the sorted elements of the consensus matrix
	 * @return the CDF value
	 */
	public double calculateCDF(double c, double[]elements)
	{

		double j = 0;
		int z=0;
		
		while((z<elements.length)&&(elements[z++]<c))
		{
			j++;
		}
		

		/*for(int z=0; z<this.nOfItems;z++)
			for (int i=0;i<z+1;i++)
			{
				if (this.ConsMatrix[z][i]< c) 
				{
					j++;
				}	
			}
*/
		double cdf=j / (double)((this.nOfItems*(this.nOfItems+1))/2);
		return cdf;
	}
	
	/**
	 * compute the CDF vector
	 * @param elements is a vector of the sorted elements of the consensus matrix
	 * @return the CDF vector
	 */
	public double[] calculateCDF(double[]elements)
	{
		double[]cdf= new double[elements.length];
	
		cdf[0]=0;
		
		for(int z=1; z<elements.length; z++)
		{
			if(elements[z-1]<elements[z])
			{
				cdf[z]=z/(double)((this.nOfItems*(this.nOfItems+1))/2);
			}
			else if(elements[z-1]==elements[z])
			{
				cdf[z]=cdf[z-1];
			}
		}

		return cdf;
	}
	
	
	/**
	 * wirte in a file the CDF
	 * 
	 * @param path addres and name the file
	 * @param k number of cluster correspondent for the CDF
	 * @param cdfVector array contenets the value of cdf
	 */
	public void storeToFileCDF(String path, int k, double cdfVector[], int missing) throws FileNotFoundException, IOException, NumberFormatException,	Exception 
	{
		// Open file in append
		FileOutputStream file = new FileOutputStream(path, true);
		PrintStream Output = new PrintStream(file);

		// write first row
		Output.println("-----------CDF cluster " + k + " ------ ");

		if (missing == 1) 
		{
			// write CDF
			for (int i = 0; i < cdfVector.length; i++) 
			{
				Output.print(cdfVector[i] + "\t");
			}
			Output.println("");
		} 
		else 
		{
			Output.println("* Missing");
		}
		// write last row
		Output.println("-----------Fine CDF cluster " + k + " -----+");
		// close file
		Output.close();
	}

	/**
	 * Read the File the CDF elements
	 * 
	 * @param path addres and name the file
	 * @return an array double contents the CDF elements
	 * 
	 */
	public double[] getCDFFromFile(String path) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{

		loadFromFileCDF(path);


		return this.cdfValue;
	}

	/**
	 * Read the File the CDF elements for the k-th iteratio (i.e. number of
	 * cluster)
	 * 
	 * @param path addres and name the file
	 * @param k number of iteratio (i.e. number of cluster)
	 * @return array double contents the CDF elements
	 */
	public double[] getCDFFromFile(String path,int kmin, int k) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{

		loadFromFileCDF(path,k,kmin);


		return this.cdfValue;
	}


	/**
	 * Read the File the CDF elements
	 * 
	 * @param path addres and name the file
	 * 
	 */
	private void loadFromFileCDF(String path) throws FileNotFoundException, IOException, NumberFormatException,	Exception 
	{

		int n;
		n = (this.nOfItems * this.nOfItems - 1) / 2;
		this.cdfValue = new double[n];
		File f = new File(path);
		FileInputStream fis = new FileInputStream(f);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);

		// Read Fistr row
		String linea = br.readLine();

		// Read CDF vector
		linea = br.readLine();

		StringTokenizer st = new StringTokenizer(linea);
		if (!st.nextToken().equals("*"))
		{

			for (int i = 0; i < n; i++) 
			{
				this.cdfValue[i] = Double.parseDouble(st.nextToken());
			}
		}
		else
		{
			for (int i = 0; i < n; i++) 
			{
				this.cdfValue[i] = 0;
				
			}
			st.nextToken();
			st.nextToken();
		}
		fis.close();

	}

	/**
	 * Read the File the CDF elements for the k-th iteratio (i.e. number of
	 * cluster)
	 * 
	 * @param path addres and name the file
	 * @param k number of iteratio (i.e. number of cluster)
	 */
	private void loadFromFileCDF(String path, int k, int kmin) throws FileNotFoundException, IOException, NumberFormatException,	Exception 
	{


		int n;
		n = (this.nOfItems * this.nOfItems + 1) / 2;
		this.cdfValue = new double[n];

		File f = new File(path);
		FileInputStream fis = new FileInputStream(f);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		String linea;
		for (int z = 0; z < k - kmin; z++) 
		{
			// Read fistr row
			linea = br.readLine();

			// Read CDF line
			linea = br.readLine();

			// Read last row
			linea = br.readLine();
		}
		// Read fistr row
		linea = br.readLine();
		// Read Matrix
		linea = br.readLine();

		StringTokenizer st = new StringTokenizer(linea);
		String a = st.nextToken();
		if (!a.equals("*"))
		{
			this.cdfValue[0] = Double.parseDouble(a);
			for (int i = 1; i < n; i++) 
			{
				this.cdfValue[i] = Double.parseDouble(st.nextToken());
			}
		}
		else
		{
			for (int i = 0; i < n; i++) 
			{
				this.cdfValue[i] = 0;
			}
		}
		// Read last row
		linea = br.readLine();

		fis.close();


	}

	/**
	 * Write in a File the k-th Consesus MAtrix
	 * 
	 * @param path addres and name of file
	 * @param k number of iteratio (i.e. number of cluster)
	 */
	public void storeToFileConsensusMatrix(String path, int k, int missing) throws FileNotFoundException, IOException, NumberFormatException,	Exception 
	{

		double tmpmatrix[][] = new double[this.nOfItems][this.nOfItems];

		if (missing == 1) 
		{
			for (int i = 0; i < this.nOfItems; i++) 
			{
				for (int j = 0; j < i + 1; j++) 
				{
					tmpmatrix[i][j] = this.ConsMatrix[i][j];
					tmpmatrix[j][i] = this.ConsMatrix[i][j];
				}
			}
		}

		// Open file in append
		FileOutputStream file = new FileOutputStream(path, true);
		PrintStream Output = new PrintStream(file);

		// write first row
		Output.println("----  Consensus Matrix: " + k + " ----");

		// write Consensus Matrix
		for (int i = 0; i < this.nOfItems; i++) 
		{
			if (missing==1)
			{
				for (int j = 0; j < this.nOfItems; j++)
					Output.print(tmpmatrix[i][j] + " ");
				Output.println("");
			}
			else
			{
				Output.println("* Missing");
			}

		}

		// write last row
		Output.println("---- End Consensus Matrix: " + k + "----");
		// close file
		Output.close();
	}

	/**
	 * Read a Consensus Matrix from file
	 * 
	 * @param path address and name of file
	 */
	public void loadFromFileConsensusMatrix(String path) throws FileNotFoundException, IOException, NumberFormatException,	Exception 
	{
		File f = new File(path);
		FileInputStream fis = new FileInputStream(f);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		// Read Fistr row
		String linea = br.readLine();
		boolean flag=true;
		StringTokenizer st =null;
		for (int i = 0; i < this.nOfItems; i++) 
		{
			linea = br.readLine();
			if (flag)
			{
				st= new StringTokenizer(linea);
			}
			if (!st.nextToken().equals("*"))
			{
				for (int j = 0; j < i + 1; j++) 
				{
					setConsensuMatrixEntry(i, j, Double.parseDouble(st.nextToken()));
				}
			}
			else
			{
				for (int j = 0; j < i + 1; j++) 
				{
					setConsensuMatrixEntry(i, j, 0.0);
				}
				flag=false;
			}
		}
		fis.close();

	}

	/**
	 * Read a k-th Consensus Matrix from file
	 * 
	 * @param path address and name of file
	 * @param k number of iteratio (i.e. number of cluster)
	 */
	public void loadFromFileConsensusMatrix(String path, int k) throws FileNotFoundException, IOException, NumberFormatException,	Exception 
	{
		File f = new File(path);
		FileInputStream fis = new FileInputStream(f);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		String linea;
		for (int z = 0; z < k - 1; z++) 
		{
			// Read fistr row
			linea = br.readLine();
			for (int i = 0; i < this.nOfItems; i++) 
			{
				linea = br.readLine();
			}
			// Read last row
			linea = br.readLine();
		}
		// Read fistr row
		linea = br.readLine();
		// Read Matrix
		boolean flag=true;
		StringTokenizer st =null;
		for (int i = 0; i < this.nOfItems; i++) 
		{
			linea = br.readLine();
			if (flag)
			{
				st= new StringTokenizer(linea);
			}
			if (!st.nextToken().equals("*"))
			{
				for (int j = 0; j < i + 1; j++) 
				{
					setConsensuMatrixEntry(i, j, Double.parseDouble(st.nextToken()));
				}
			}
			else
			{
				for (int j = 0; j < i + 1; j++) 
				{
					setConsensuMatrixEntry(i, j, 0.0);
				}
				flag=false;
			}
		}

		// Read last row
		linea = br.readLine();
		fis.close();
	}

	/**
	 * read elements of k-th matrix form file  
	 * @param path file path of file
	 * @param k numeber of cluster 
	 * @param kmin minimun number of cluster
	 * @return a List of Double, in case to error return a pointer to null 
	 */
	public List<Double> getElemnts(String path,int k, int kmin) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{

		loadFromFileElements(path, k, kmin);

		return this.elements;
	}


	/**
	 * read elements of k-th matrix form file  
	 * @param path file path of file
	 * @param k numeber of cluster 
	 * @param kmin minimun number of cluster
	 * @return an int for ceck to correct execution 
	 */
	private void loadFromFileElements(String path,int k, int kmin) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{

		this.elements = new ArrayList<Double>();   

		//open elements file

		// Open file in read
		File f = new File(path);
		FileInputStream fis = new FileInputStream(f);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		String linea; 
		for(int i=0; i<k-kmin;i++)
		{
			//Read Fistr row
			linea= br.readLine();
			//Read separeted row
			linea= br.readLine();
		}

		//read elements
		linea= br.readLine();
		StringTokenizer st = new StringTokenizer(linea);
		String c=null;
		int nn=(this.nOfItems*this.nOfItems+1)/2;
		boolean flag=true;
		for (int j=0; j<nn;j++)
		{
			if (flag)
			{
				c=st.nextToken();
			}
			if (!c.equals("*"))
			{
				this.elements.add(Double.parseDouble(c));
			}
			else
			{
				this.elements.add(new Double(0));
				flag=false;
			}
		}
		fis.close();
		isr.close();
		br.close();
	}

}
