package valworkbench.graphics;

import java.awt.Color;
import java.awt.GradientPaint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;


/**
 * Create a histogram of elements of the consensus matrix, calculated of Consensus Cluster.
 * 
 * @author Raffaele Giancarlo
 * @author Filippo Utro
 * @author Davide Scaturro
 * @version 1.0
 *
 */
public class VisualConsensusClusterHistogram extends JFrame 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int kmin;
	private Map<String, Integer> map = new HashMap<String, Integer>();    // hash table
	private JFreeChart chart;
	

	
	/**
	 * 
	 * @param title
	 * @param path
	 * @param N
	 * @param k
	 */	
	public VisualConsensusClusterHistogram(String title, String path, int N, int k)  throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		super(title);
		
		this.kmin=2;
		
		DefaultCategoryDataset data = densityValue(path, k, N);
		JFreeChart chart=createElementsHist(data);
		
		//add the chart to a panel...
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
		
	}

	/**
	 * create a dataset for the histogram
	 * 
	 * @param path file path of elements 
	 * @param k number of cluster 
	 * @param N number of gene
	 * @return dataset for the plot
	 */
	@SuppressWarnings("unchecked")
	public DefaultCategoryDataset densityValue(String path, int k, int N) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		DefaultCategoryDataset data = new DefaultCategoryDataset();

		readToFile(path, k,N);
		
		
		
		Iterator it = map.keySet().iterator();
		String series = "First";

		while (it.hasNext()) {
			// Get key
			Object key = it.next();
			data.addValue((Integer)map.get(key), series, (String)key);     
		}
		
		return data;
	}

	private void readToFile(String path, int k, int N) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{

		//Create a hash table
		map = new TreeMap<String, Integer>();        // sorted map
		//open elements file
		
			// Open file in read
			File f = new File(path);
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			String linea; 
			for(int i=0; i<k-this.kmin;i++)
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
			int nn=(N*N+1)/2;
			boolean flag=true;
			for (int j=0; j<nn;j++)
			{
				if (flag)
				{
					c=st.nextToken();
				}
				if (!c.equals("*"))
				{
					c=Double.toString(round(Double.parseDouble(c),1));
					if (map.containsKey(c))
					{
						Integer n = (Integer)map.get(c)+1;		
						map.put(c,n);
					}
					else
						map.put(c,new Integer(1));
				}
				else
				{
					map.put(Integer.toString(0), 0);
					flag=false;
				}
			}
			fis.close();
			isr.close();
			br.close();

	}
	
	/**
	 * create a figure
	 * 
	 * @param dataset containing the elements for the plot
	 * @return a figure.
	 */
	public JFreeChart createElementsHist(CategoryDataset dataset) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		//create the chart...
		this.chart = ChartFactory.createBarChart(
				"Consensus Matrix Histogram", // chart title
				"consensus index value", // domain axis label
				"Density", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL,
				false, // include legend
				true, // tooltips?
				false // URLs?
		);	
		//NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		//set the background color for the chart...
		this.chart.setBackgroundPaint(new Color(0xBBBBDD));
		//get a reference to the plot for further customisation...
		CategoryPlot plot = this.chart.getCategoryPlot();
		//set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		//disable bar outlines...
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);
		//set up gradient paints for series...
		GradientPaint gp0 = new GradientPaint(
				0.0f, 0.0f, Color.blue,
				0.0f, 0.0f, Color.lightGray
		);

		renderer.setSeriesPaint(0, gp0);
		//OPTIONAL CUSTOMISATION COMPLETED.
	

		return this.chart;		
	}
	
	/**
	 * round the value
	 * @param value value of raound
	 * @param decimalPlace precision approximation
	 * @return the customizated value
	 */
	private double round(double value, int decimalPlace) 
	{
		double power_of_ten = 1;
		while (decimalPlace-- > 0)
			power_of_ten *= 10.0;
		return Math.round(value * power_of_ten)/ power_of_ten;
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
