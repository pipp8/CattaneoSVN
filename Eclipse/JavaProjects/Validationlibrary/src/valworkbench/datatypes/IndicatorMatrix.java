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
 *   
 * 
 * @author Raffaele Giancarlo
 * @author Filippo Utro
 * @author Davide Scaturro
 * @version 1.0
 */
public class IndicatorMatrix 
{
	private int nOfItem;
	private int IndicatorMatrix[][];

	/* settare e prendere una solo riga e solo una colonna, copiare la matrice,
	 * inizializzare la matrice
	 */ 

	public IndicatorMatrix(int nOfGene)
	{
		this.nOfItem=nOfGene;

		//Instance of Connetivity Matrix
		int tmp[][]=new int [nOfGene][];
		
		for(int i=0;i<this.nOfItem;i++)
		{
			tmp[i]=new int [i+1];
		}
		this.IndicatorMatrix=tmp;

	}

	/**
	 * Initialize the Indicator Matrix to zero
	 *
	 */
	public void setIMatrixtoZero()
	{
		for(int i=0;i<this.nOfItem;i++)
		{
			for (int j=0;j<this.IndicatorMatrix[i].length;j++)
			{
				this.IndicatorMatrix[i][j]=0;
			}
		}
	}

	/**
	 * Set the element (i,j) of the Indicator Matrix to 1
	 * @param i index of row
	 * @param j index of column
	 */
	public void setIMatrixEntry(int i, int j)
	{
		if (j>i)
		{
			int swap=i;
			i=j;
			j=swap;
		}
		this.IndicatorMatrix[i][j]=1;
	}

	/**
	 * Get the value of element (i,j) of the Indicator Matrix 
	 * @param i index of row
	 * @param j index of column
	 * @return the value of Indicator Matrix
	 */
	public int getIMatrixEntry(int i, int j)
	{
		return this.IndicatorMatrix[i][j];
	}

	/**
	 * Get the i-th row of Indicator Matrix 
	 * @param i index of row
	 * @return a row vector that's contain the element's of the i-th row of Indicator Matrix 
	 */
	public int[] getIMatrixRow(int i)
	{
		int vector[]=new int [i+1];

		for(int j=0;j<i+1;j++)
		{
			vector[i]=getIMatrixEntry(i,j);
		}
		return vector;
	}

	/**
	 * Set the i-th row of IMatrix 
	 * @param geneData a row vector that's contain the element's of the i-th row of Indicator Matrix
	 * @param i index of row
	 */
	public void setIMatrixRow(int geneData[], int i)
	{
		for(int j=0; j<geneData.length; j++)
		{
			setIMatrixEntry(i,geneData[j]);
		}
	}
	
	/**
	 * Set the IMatrix 
	 * @param geneData vector carrier in which i-th element you count of it the index of the gene that is in the same one dataset
	 */
	public void setIMatrix(int geneData[])
	{
		for(int i=0; i<geneData.length; i++)
		{
			for(int j=i; j<geneData.length; j++)
				setIMatrixEntry(geneData[i], geneData[j]);
		}
	}

	/**
	 * 
	 * @return a reference to Indicator matrix
	 */
	public int[][] getIndicatorMatrix()
	{
		return this.IndicatorMatrix;
	}

	/**
	 *  Sum a Inidcator Matrix
	 * @param indMatrix matrix of sum
	 */
	public void addIMatrix(IndicatorMatrix indMatrix)
	{
		if (this.nOfItem!=indMatrix.getNOfItem())
		{
			System.err.println("Error:The matrixes have different dimensions ");
		}
		else
		{
			for(int i=0; i<this.nOfItem; i++)
			{
				for(int j=0; j<this.IndicatorMatrix[i].length; j++)
					this.IndicatorMatrix[i][j]+= indMatrix.getIMatrixEntry(i, j);
			}
		}
	}

	/**
	 * Copy the Connetivity Matrix 
	 * @param indicatorMatrix matrix to copy
	 */
	public void copyIndicatorMatrix(IndicatorMatrix indicatorMatrix) 
	{
		for (int i=0;i<this.nOfItem;i++)
		{
			this.IndicatorMatrix[i] = indicatorMatrix.getIMatrixRow(i);
		}
	}

	/**
	 * Write in a File the k-th Indicator MAtrix
	 * 
	 * @param path addres and name of file
	 * @param k number of iteratio (i.e. number of cluster)
	 */
	public void writeIMatrix(String path, int k) 
	{
		int tmpmatrix[][] = new int[this.nOfItem][this.nOfItem];

		for (int i = 0; i < this.nOfItem; i++) 
		{
			for (int j = 0; j < i + 1; j++) 
			{
				tmpmatrix[i][j] = this.IndicatorMatrix[i][j];
				tmpmatrix[j][i] = this.IndicatorMatrix[i][j];
			}
		}

		try 
		{
			// Open file in append
			FileOutputStream file = new FileOutputStream(path, true);
			PrintStream Output = new PrintStream(file);

			// write first row
			Output.println("----  Indicator Matrix: " + k + " ----");

			// write Connettivity Matrix
			for (int i = 0; i < this.nOfItem; i++) 
			{
				for (int j = 0; j < this.nOfItem; j++)
					Output.print(tmpmatrix[i][j] + " ");
				Output.println("");
			}

			// write last row
			Output.println("---- End Indicator Matrix: " + k + "----");
			// close file
			Output.close();
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Read a k-th Indicator Matrix from file
	 * 
	 * @param path address and name of file
	 * @param k number of iteratio (i.e. number of cluster)
	 */
	public void readIMatrix(String path, int k) 
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
					this.IndicatorMatrix[i][j]= Integer.parseInt(st.nextToken());
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

	
	public int getNOfItem() {
		return this.nOfItem;
	}

	public void setNOfItem(int nofGene) {
		this.nOfItem = nofGene;
	}

}
