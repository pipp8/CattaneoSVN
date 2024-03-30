package valworkbench.graphics;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import valworkbench.datatypes.ConsensusMatrix;
import valworkbench.datatypes.InputMeasure;

/**
 * Create a figure of cdf value, calculated of Consensus Cluster.
 * 
 * 
 * @author Raffaele Giancarlo
 * @author Filippo Utro
 * @author Davide Scaturro
 * @version 1.0
 */
public class VisualConsensusClusterCdf extends JFrame 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int kmin, N, kmax;
	private String pathelements;
	private JFreeChart chart;
	

	/**
	 * Create the chart of cdf values
	 * 
	 * @param title is a title of chart
	 * @param pathcdf is a path file containing the cdf value
	 * @param pathelements is a path file containing the elements value
	 * @param N is number of gene the data matrix
	 * @param kmax maximun number of cluster
	 * @param kmin minimum number of cluster
	 */ 
	public VisualConsensusClusterCdf(String title, String pathcdf, String pathelements, int N, int kmax, int kmin) throws FileNotFoundException, IOException, NumberFormatException,	Exception 
	{
		super(title);
		
		this.kmin=kmin;
		this.N=N;
		this.kmax=kmax;
		this.pathelements=pathelements;
		
		JFreeChart chart=createCDFChart(pathcdf);
		
		//add the chart to a panel...
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
	}
	
	/**
	 * Create the chart of cdf values 
	 * 
	 * @param title is a title of chart
	 * @param nameCdf is a path file containing the cdf value
	 * @param nameElements is a path file containing the elements value
	 * @param parameters is a array of input measuer containing the parameters of compute measure
	 * @see InputMeasure
	 * @param N is number of gene the data matrix
	 */
	public VisualConsensusClusterCdf(String title, String nameCdf, String nameElements, InputMeasure parameters, int N) throws FileNotFoundException, IOException, NumberFormatException,	Exception 
	{
		super(title);
		
		this.N=N;
		this.kmin=parameters.getKMin();		
		this.kmax=parameters.getKMax();
		this.pathelements=parameters.getCurrentAuxDir()+File.separator+nameElements;
		
		JFreeChart chart = createCDFChart(parameters.getCurrentAuxDir()+File.separator+nameCdf);
		
		//add the chart to a panel...
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		
		setContentPane(chartPanel);
	
	}
	
	/**
	 * Reads of elements of consensus matrix (i.e. elements.txt)
	 * 
	 * @param path is a path file containing the elements value
	 * @param k is a number of cluster
	 * @return a array of double containing the sort elements
	 */
	public double[] readIndex(String path, int k) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		double index[]=new double[(this.N*this.N+1)/2];

		// Create a hash table
	//    Map map = new HashMap();    // hash table
	 //   map = new TreeMap();        // sorted map

	    ConsensusMatrix tmp=new ConsensusMatrix(this.N);
	    
	    List<Double> elements=tmp.getElemnts(path, k, this.kmin);
	    
	    int i=0;
	    for(Double c:elements)
	    {
	    	index[i]=c;
	    	i++;
	    }
	    /*
	    i=0;
		Iterator it = map.keySet().iterator();
	    while (it.hasNext()) 
	    {
	        // Get key
	    	Object key = it.next();	    
	    	index[i]=(Double.parseDouble(map.get(key).toString()));
			i++;
	    }
	      */
	    return index;
	}
	
	/**
	 * Reads of cdf value of to file
	 * 
	 * @param path is a file path cof cdf value
	 * @param k number of cluster
	 * @param kmin minimum number of cluster
	 * @return a double array containing the cdf value
	 */
	public double[] readCDF(String path, int k, int kmin) throws FileNotFoundException, IOException, NumberFormatException,	Exception 
	{

		int n;
		n = (this.N * this.N + 1) / 2;
		double[] cdfVector = new double[n];

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
		if (!st.nextToken().equals("*"))
		{
			for (int i = 0; i < n; i++) 
			{
				cdfVector[i] = Double.parseDouble(st.nextToken());
			}
		}
		else
		{
			for (int i = 0; i < n; i++) 
			{
				cdfVector[i] = 0;
			}
			st.nextToken();
		}
		// Read last row
		linea = br.readLine();

		fis.close();

		return cdfVector;
	}

	/**
	 * create a series of cdf value
	 * 
	 * @param path is a file path cof cdf value
	 * @return a series of XYSeries type
	 */
	private XYSeriesCollection CDFValue(String path) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		
		   XYSeriesCollection dataset = new XYSeriesCollection();
		   
		   double cdf[];
		   
		   ConsensusMatrix tmp=new ConsensusMatrix(this.N);
		   
		   for(int i=0;i<this.kmax-this.kmin+1;i++)
		   {
			   double index[]=readIndex(this.pathelements, i+this.kmin);
			   XYSeries series = new XYSeries("Cluster "+Integer.toString(i+this.kmin));
			   
			   cdf=tmp.getCDFFromFile(path, this.kmin, i+this.kmin);
			   if (cdf!=null)
			   {
				   for(int j=0;j<cdf.length;j++)
				   {
					   series.add(index[j], cdf[j]);
				   }
				   dataset.addSeries(series);
			   }
		   }
		  
		
		   return dataset;
	}

	/**
	 * Create the chart of cdf values
	 * 
	 * @param path file path of cdf value
	 * @return a graph of cdf value
	 */
	public JFreeChart createCDFChart(String path) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	 {

		//reads cdf
		XYSeriesCollection dataset=CDFValue(path);
			   
	   //Generate the graph
	    this.chart = ChartFactory.createXYLineChart(
	   "Consensus Matrix CDF", // Title
	   "consensus index value", // x-axis Label
	   "CDF", // y-axis Label
	   dataset, // Dataset
	   PlotOrientation.VERTICAL, // Plot Orientation
	   true, // Show Legend
	   true, // Use tooltips
	   false // Configure chart to generate URLs?
	   );

	   return this.chart;
	 }
	
	/**
	 * Accessor method to chart object
	 * 
	 * @return the chart object associated to the graph 
	 */
	public JFreeChart getChart() {
		return chart;
	}
}
