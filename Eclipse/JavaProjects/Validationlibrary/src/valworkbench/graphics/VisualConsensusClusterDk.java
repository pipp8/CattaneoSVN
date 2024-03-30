package valworkbench.graphics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
/**
 * Create a figure of dk value, calculated of Consensus Cluster.
 * 
 * 
 * @author Raffaele Giancarlo
 * @author Filippo Utro
 * @author Davide Scaturro
 * @version 1.0
 *
 */
public class VisualConsensusClusterDk extends JFrame  
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int kmin, kmax;
	private MeasureVector d_k;
	private JFreeChart chart;

	/**
	 * Create the chart of dk values
	 * 
	 * @param title title of chart
	 * @param path is a path file containing the dk value
	 * @param kmax maximun number of cluster
	 * @param kmin minimum number of cluster
	 */
	public VisualConsensusClusterDk(String title, String path, int kmax, int kmin)  throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		super(title);
		this.kmin=kmin;
		this.kmax=kmax;

		///***************************
		this.d_k = new MeasureVector();
		this.d_k.loadFromFile(path);
		
		XYSeries seriesa = new XYSeries("Best");
		int nOfCluster=0;
		while((nOfCluster<(this.kmax-this.kmin+1))&&(this.d_k.getMeasureValue(nOfCluster)>this.d_k.getMeasureValue(nOfCluster+1)))
			nOfCluster++;

		seriesa.add(this.d_k.getNOfCluster(nOfCluster), this.d_k.getMeasureValue(nOfCluster));
			
		//reads dk
		XYSeriesCollection dataset=new XYSeriesCollection(seriesa);
		
		dataset.addSeries(readDk(path));

		//create chart
		JFreeChart chart=createDkChart(dataset);

		//add the chart to a panel...
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);

	}

	/**
	 * Create the chart of dk values
	 * 
	 * @param title contains title of chart
	 * @param dk a mesaure vector containg the dk value
	 * @see MeasureVector
	 */
	public VisualConsensusClusterDk(String title, MeasureVector dk) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		super(title);

		XYSeries series = new XYSeries("Dk");

		this.d_k = dk;
		
		XYSeries seriesa = new XYSeries("Best");
		int nOfCluster=0;
		while((nOfCluster<this.d_k.getNOfEntries())&&(this.d_k.getMeasureValue(nOfCluster)>this.d_k.getMeasureValue(nOfCluster+1)))
			nOfCluster++;

		seriesa.add(this.d_k.getNOfCluster(nOfCluster), this.d_k.getMeasureValue(nOfCluster));
			
		
		double val[]=dk.getMeasureValue();
		int k[]=dk.getNOfCluster();
		for (int i=0;i<k.length;i++)
		{
			if (val[i]!=Double.NaN)
			{
				series.add(k[i], val[i]);
			}
		}

		XYSeriesCollection dataset=new XYSeriesCollection(seriesa);

		dataset.addSeries(series);
		//create chart
		JFreeChart chart=createDkChart(dataset);
		//add the chart to a panel...
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
		
	}

	/**
	 * Create the chart of dk values
	 * 
	 * @param title contains title of chart
	 * @param namedk is a file path containig the dk valu
	 * @param parameters is a array of input measuer containing the parameters of compute measure
	 * @see InputMeasure
	 */
	public VisualConsensusClusterDk(String title, String namedk, InputMeasure parameters) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		super(title);
		
		this.kmin=parameters.getKMin();
		this.kmax=parameters.getKMax();
		
		this.d_k = new MeasureVector();
		this.d_k.loadFromFile(parameters.getOutputPath()+ File.separator+"Measure_Values.txt");
		
		XYSeries seriesa = new XYSeries("Best");
		int nOfCluster=0;
		while((nOfCluster<(this.kmax-this.kmin+1))&&(this.d_k.getMeasureValue(nOfCluster)>this.d_k.getMeasureValue(nOfCluster+1)))
			nOfCluster++;

		seriesa.add(this.d_k.getNOfCluster(nOfCluster), this.d_k.getMeasureValue(nOfCluster));

		
		//reads dk AGGIUNGERE LA CARTELLA 
		XYSeriesCollection dataset=new XYSeriesCollection(readDk(namedk));
		dataset.addSeries(seriesa);

		//create chart
		JFreeChart chart=createDkChart(dataset);

		//add the chart to a panel...
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);

	}

	/**
	 * create a series for the chart 
	 * 
	 * @param path file path of  dk value
	 * @return a series of XYSeries type, in case to error return a null pointer
	 */
	private XYSeries readDk(String path) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{

		//Create a simple XY chart
		XYSeries series = new XYSeries("Dk");
		
		//readToFile(path);
		
		for (int i=0;i<this.d_k.getNOfEntries();i++)
		{
			series.add(this.d_k.getNOfCluster(i), this.d_k.getMeasureValue(i));
		}
		
		return series;
	}
	
	/**
	 * Create the chart of dk values
	 * 
	 * @param dataset the collection of dk value
	 * @return a chart
	 */
	public JFreeChart createDkChart(XYSeriesCollection dataset) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{

		//Generate the graph
		this.chart = ChartFactory.createXYLineChart(
				"Area under CDF change", // Title
				"Number of cluster", // x-axis Label
				"Delta k", // y-axis Label
				dataset, // Dataset
				PlotOrientation.VERTICAL, // Plot Orientation
				false, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
		);
		
		
		XYPlot plot = this.chart.getXYPlot();

		//customise the range axis...
		ValueAxis rAxis = (ValueAxis) plot.getRangeAxis();
		double rangeSize = Math.ceil(this.d_k.getMeasureValue(0) - this.d_k.getMeasureValue(this.d_k.getNOfEntries()-1));
		rAxis.resizeRange(rangeSize);
				
		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(1, true);
        plot.setRenderer(renderer);
		
		NumberAxis rangeAxis = (NumberAxis) plot.getDomainAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setUpperMargin(0.20);
	
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
