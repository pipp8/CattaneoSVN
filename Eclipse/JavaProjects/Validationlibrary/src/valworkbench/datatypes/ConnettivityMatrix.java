package valworkbench.datatypes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.StringTokenizer;
/**
 * 
 * @author Raffaele Giancarlo
 * @author Filippo Utro
 * @author Davide Scaturro
 * @version 1.0
 *
 */
public class ConnettivityMatrix 
{
	private int nOfItem;
	private int connMatrix[][];

	/**
	 * 
	 */
	public ConnettivityMatrix(int nOfGene)
	{
		this.nOfItem=nOfGene;

		//Instance of Connetivity Matrix
		int tmp[][]=new int [nOfGene][];
	
		for(int i=0; i<this.nOfItem; i++)
		{

			tmp[i]=new int [i+1];
		}
		
		this.connMatrix=tmp;
	}

	/**
	 * Initialize the Connetivity Matrix to zero
	 *
	 */
	public void setConnMatrixtoZero()
	{
		for(int i=0; i<this.nOfItem; i++)
		{
			for (int j=0; j<this.connMatrix[i].length; j++)
			{
				this.connMatrix[i][j]=0;
			}
		}
	}

	/**
	 * Set the element (i,j) of the Connetivity Matrix to 1
	 * @param i index of row
	 * @param j index of column
	 */
	public void setConnMatrixEntry(int i, int j)
	{
		if (j>i)
		{
			int swap=i;
			i=j;
			j=swap;
		}
		this.connMatrix[i][j]=1;
	}

	/**
	 * Get the value of element (i,j) of the Connetivity Matrix 
	 * @param i index of row
	 * @param j index of column
	 * @return the value of Connetivity Matrix
	 */
	public int getConnMatrixEntry(int i, int j)
	{
		return this.connMatrix[i][j];
	}

	/**
	 * Get the i-th row of Connetivity Matrix
	 * @param i index of row
	 * @return a row vector that's contain the element's of the i-th row of Connetivity Matrix
	 */
	public int[] getConnMatrixRow(int i)
	{
	
		int vector[]=new int [i+1];

		for(int j=0;j<i+1;j++)
		{
			vector[i]=getConnMatrixEntry(i,j);
		}
		return vector;
	}

	/**
	 * 
	 * @param clusterGene vector containing the connetivity for i-th gene
	 * @param i index of row
	 */
	public void setConnMatrixRow(int clusterGene[], int i)
	{
		for(int j=0; j<clusterGene.length; j++)
		{
			setConnMatrixEntry(i,clusterGene[j]);
		}
	}

	/**
	 * Sum a Connetivity Matrix
	 * @param connMatrix matrix of sum.
	 */
	public void addConnMatrix(ConnettivityMatrix connMatrix)
	{
		if (this.nOfItem!=connMatrix.getNOfItem())
		{
			System.err.println("Error:The matrixes have different dimensions ");
		}
		else
		{
			for(int i=0;i<this.nOfItem;i++)
			{
				for(int j=0;j<this.connMatrix[i].length;j++)
					this.connMatrix[i][j]+= connMatrix.getConnMatrixEntry(i,j);
			}
		}
	}

	/**
	 * Set the Connetivity Matrix 
	 * @param clusterMatrix cluster matrix on which calcorare Connetivity Matrix
	 */
	public void setConnMatrix(ClusterMatrix clusterMatrix)
	{
		for(int i=0; i<clusterMatrix.getNOfCluster(); i++)
		{
			int vector[]=clusterMatrix.getCMatrixRow(i);
	
			for (int j=0; j<clusterMatrix.getClusterSize(i); j++)
			{
				for(int z=j; z<clusterMatrix.getClusterSize(i); z++)
				{
					setConnMatrixEntry(vector[j],vector[z]);
				}
			}
		}
	}

	/**
	 * 
	 * @return the cluster Matrix
	 */
	public int [][] getConnMatrix() {
		return this.connMatrix;
	}

	/**
	 * Copy the Connetivity Matrix 
	 * @param connMatrix matrix to copy
	 */
	public void copyConnMatrix(ConnettivityMatrix connMatrix) {
		
		for(int i=0;i<this.nOfItem;i++)
		{
				this.connMatrix[i]=connMatrix.getConnMatrixRow(i);
		}
	}
	
	/**
	 * Write in a File the k-th Connettivity MAtrix
	 * 
	 * @param path addres and name of file
	 * @param k number of iteratio (i.e. number of cluster)
	 */
	public void writeConnMatrix(String path, int k) 
	{
		int tmpmatrix[][] = new int[this.nOfItem][this.nOfItem];

		for (int i = 0; i < this.nOfItem; i++) 
		{
			for (int j = 0; j < i + 1; j++) 
			{
				tmpmatrix[i][j] = this.connMatrix[i][j];
				tmpmatrix[j][i] = this.connMatrix[i][j];
			}
		}

		try 
		{
			// Open file in append
			FileOutputStream file = new FileOutputStream(path, true);
			PrintStream Output = new PrintStream(file);

			// write first row
			Output.println("----  Connettivity Matrix: " + k + " ----");

			// write Connettivity Matrix
			for (int i = 0; i < this.nOfItem; i++) 
			{
				for (int j = 0; j < this.nOfItem; j++)
					Output.print(tmpmatrix[i][j] + " ");
				Output.println("");
			}

			// write last row
			Output.println("---- End Connettivity Matrix: " + k + "----");
			// close file
			Output.close();
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Read a k-th Connnettivity Matrix from file
	 * 
	 * @param path address and name of file
	 * @param k number of iteratio (i.e. number of cluster)
	 */
	public void readConnMatrix(String path, int k) 
	{
		try 
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
				for (int i = 0; i < this.nOfItem; i++) 
				{
					linea = br.readLine();
				}
				// Read last row
				linea = br.readLine();
			}
			// Read fistr row
			linea = br.readLine();
			// Read Matrix
			for (int i = 0; i < this.nOfItem; i++) 
			{
				linea = br.readLine();
				StringTokenizer st = new StringTokenizer(linea);
				for (int j = 0; j < i + 1; j++) 
				{
					this.connMatrix[i][j]= Integer.parseInt(st.nextToken());
				}
			}
			// Read last row
			linea = br.readLine();
			fis.close();

		}
		catch (Exception ex) 
		{
			ex.printStackTrace();
		}
	}

	/**
	 *  Get the gene's number
	 * @return the number of gene
	 */
	public int getNOfItem() 
	{
		return this.nOfItem;
	}

	/**
	 * Set the gene's number
	 * @param nofGene
	 */
	public void setNOfItem(int nofGene) 
	{
		this.nOfItem = nofGene;
	}

}
