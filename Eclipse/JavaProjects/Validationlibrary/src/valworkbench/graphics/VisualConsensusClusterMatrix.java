package valworkbench.graphics;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import valworkbench.datatypes.ConsensusMatrix;

/**
 * Create a figure of consensus matrix value, calculated of Consensus Cluster.
 * 
 * 
 * @author Raffaele Giancarlo
 * @author Filippo Utro
 * @author Davide Scaturro
 * @version 1.0
 *
 */
public class VisualConsensusClusterMatrix extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ConsensusMatrix connMatrix;
	private int k;
	private JFreeChart chart;
	
	/**
	 *create a chart of consensus matrix
	 * 
	 * @param title is a title of chart
	 * @param path is a file path containing the consensus matrix
	 * @param k is number of cluster, the consensus matrix that visualize
	 * @param N number of gene
	 */
	public VisualConsensusClusterMatrix(String title, String path, int k, int N) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		super(title);

		this.connMatrix=new ConsensusMatrix(N); 
		this.connMatrix.loadFromFileConsensusMatrix(path, k);
		this.k=k;

		//Load dataset
		XYSeriesCollection dataset=readM();

		//create chart
		JFreeChart chart=createConsensusMatrixChart(dataset);

		//add the chart to a panel...
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 500));
		setContentPane(chartPanel);

	}

	/**
	 *create a chart of consensus matrix
	 * 
	 * @param title is a title of chart
	 * @param connMatrix is the consensus Matrix
	 * @see ConsensusMatrix
	 */
	public VisualConsensusClusterMatrix(String title, ConsensusMatrix connMatrix) throws FileNotFoundException, IOException, NumberFormatException, Exception
	{
		super(title);

		this.connMatrix=new ConsensusMatrix(this.connMatrix.getnOfItems());
		this.connMatrix.copyConsMatrix(connMatrix);
	
		//Caricamento dataset
		XYSeriesCollection dataset=readM();
		
		//create chart
		JFreeChart chart=createConsensusMatrixChart(dataset);

		//add the chart to a panel...
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 500));
		setContentPane(chartPanel);
		
	
	}

	/**
	 * read the consensus matrix to file
	 * 
	 * @return the series of consensus value
	 */
	public XYSeriesCollection readM() throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		XYSeriesCollection dataset= new XYSeriesCollection();
		for (int i=0; i <this.connMatrix.getnOfItems();i++)
		{
			for (int j=0;j<this.connMatrix.getnOfItems();j++)
			{
				XYSeries series = new XYSeries(Integer.toString(i+j));
				series.add(i, j);
				dataset.addSeries(series);
			}
		}
		
		return  dataset;
	}


	/**
	 * create figure of consensus matrix
	 * 
	 * @param dataset series of the consensus matrix
	 * @return a chart of consensus matrix
	 */
	public JFreeChart createConsensusMatrixChart(XYSeriesCollection dataset) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		this.chart = ChartFactory.createScatterPlot(
				"Consensus Matrix "+Integer.toString(this.k), // Title
				"", // x-axis Label
				"", // y-axis Label
				dataset, // Dataset
				PlotOrientation.VERTICAL, // Plot Orientation
				false, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
		);

		XYPlot plot = this.chart.getXYPlot();

		NumberAxis rangeAxis = (NumberAxis) plot.getDomainAxis();
		
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setUpperMargin(0.0);
		rangeAxis.setLowerMargin(0.0);
		rangeAxis.setVisible(false);

		NumberAxis asse= (NumberAxis)plot.getRangeAxis();
		asse.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		asse.setUpperMargin(0.0);
		asse.setLowerMargin(0.0);
		asse.setVisible(false);
		
		
		XYItemRenderer  renderer = (XYItemRenderer) plot.getRenderer();
		int index=0;
		for (int i=0; i <this.connMatrix.getnOfItems();i++)
		{
			for (int j=0;j<this.connMatrix.getnOfItems();j++)
			{
				if (i>j)
					renderer.setSeriesPaint(index, new Color((int)(255*this.connMatrix.getConsensusMatrixEntry(i, j)),0, 0));
				else
					renderer.setSeriesPaint(index, new Color((int)(255*this.connMatrix.getConsensusMatrixEntry(j, i)),0, 0));
				
				index++;		
			}
		}
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
