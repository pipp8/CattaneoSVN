package valworkbench.graphics;

import java.io.File;
import java.util.StringTokenizer;

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

import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.MeasureVector;

/**
 * Creates a Line Chart figure, obtained from Measure Values
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 *
 */
public class VisualLineChart extends JFrame{

	private static final long serialVersionUID = 1L;
	private MeasureVector lineVector;
	private HeaderData header;
	private JFreeChart chart;
		
	/**
	 * Creates the Line Chart
	 * 
	 * @param title the chart title
	 * @param outputPath Measure Values and Measure Header files path 
	 */
	public VisualLineChart(String title, String outputPath) 
	{
		super(title);
		String pathMeasure;
		String pathHeader;
		String measureName;
				
		if(title.contentEquals("Null Measure Generic Precision")
		|| title.contentEquals("Null Measure Hierarchical Precision")
		|| title.contentEquals("Null Measure H. Init. Precision"))
		{
			pathMeasure = outputPath+File.separator+"AdjRand_Values.txt";
			pathHeader = outputPath+File.separator+"Measure_Header.vhf";
						
			//reads dk
			lineVector = new MeasureVector();
			header = new HeaderData();
			
			try
			{
				lineVector.loadFromFile(pathMeasure);
				header.loadFromFile(pathHeader);
			}
			catch(Exception exc)
			{
				exc.printStackTrace();
			}
			
			measureName = "Adjusted Rand Index";
		}
		else if(title.contentEquals("Null Measure Generic Precision F")
				|| title.contentEquals("Null Measure Hierarchical Precision F")
				|| title.contentEquals("Null Measure H. Init. Precision F"))
		{
			pathMeasure = outputPath+File.separator+"FIndex_Values.txt";
			pathHeader = outputPath+File.separator+"Measure_Header.vhf";
						
			//reads dk
			lineVector = new MeasureVector();
			header = new HeaderData();
			
			try
			{
				lineVector.loadFromFile(pathMeasure);
				header.loadFromFile(pathHeader);
			}
			catch(Exception exc)
			{
				exc.printStackTrace();
			}
			
			measureName = "F-Index";
		}
		else if(title.contentEquals("Null Measure Generic Precision FM")
				|| title.contentEquals("Null Measure Hierarchical Precision FM")
				|| title.contentEquals("Null Measure H. Init. Precision FM"))
		{
			pathMeasure = outputPath+File.separator+"FMIndex_Values.txt";
			pathHeader = outputPath+File.separator+"Measure_Header.vhf";
						
			//reads dk
			lineVector = new MeasureVector();
			header = new HeaderData();
			
			try
			{
				lineVector.loadFromFile(pathMeasure);
				header.loadFromFile(pathHeader);
			}
			catch(Exception exc)
			{
				exc.printStackTrace();
			}
			
			measureName = "FM-Index";
		}
		else
		{
			pathMeasure = outputPath+File.separator+"Measure_Values.txt";
			pathHeader = outputPath+File.separator+"Measure_Header.vhf";
			
			//reads dk
			lineVector = new MeasureVector();
			header = new HeaderData();
			
			try
			{
				lineVector.loadFromFile(pathMeasure);
				header.loadFromFile(pathHeader);
			}
			catch(Exception exc)
			{
				exc.printStackTrace();
			}
			
			StringTokenizer st = new StringTokenizer(header.getMeasureName());
			measureName = st.nextToken();
							
		}
				
		XYSeriesCollection dataset=new XYSeriesCollection(readMeasure(lineVector, header, measureName));
		//TimeSeriesCollection dataset = new TimeSeriesCollection(); 
		//TimeSeries dataset = new TimeSeries(readMeasure(lineVector, header));		
		
		//create chart
		JFreeChart chart = createLineChart(dataset, header, measureName);
		
		//add the chart to a panel...
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
	}
	
	/**
	 * Creates the line Chart
	 * 
	 * @param title the chart title 
	 * @param lineVector the measure vector object containing measures values
	 * @param header the header data object containing experiment information  
	 */
	public VisualLineChart(String title, MeasureVector lineVector, HeaderData header)
	{
		super(title);
		
		String measureName;
		
		if(title.contentEquals("Null Measure Generic Precision")
		|| title.contentEquals("Null Measure Hierarchical Precision")
		|| title.contentEquals("Null Measure H. Init. Precision"))
		{
			this.lineVector = lineVector;
			this.header = header;
						
			measureName = "Adjusted Rand Index";
		}
		else if(title.contentEquals("Null Measure Generic Precision F")
				|| title.contentEquals("Null Measure Hierarchical Precision F")
				|| title.contentEquals("Null Measure H. Init. Precision F"))
		{
			this.lineVector = lineVector;
			this.header = header;
						
			measureName = "F-Index";
		}
		else if(title.contentEquals("Null Measure Generic Precision FM")
				|| title.contentEquals("Null Measure Hierarchical Precision FM")
				|| title.contentEquals("Null Measure H. Init. Precision FM"))
		{
			this.lineVector = lineVector;
			this.header = header;
						
			measureName = "FM-Index";
		}
		else
		{
			this.lineVector = lineVector;
			this.header = header;
						
			StringTokenizer st = new StringTokenizer(header.getMeasureName());
			measureName = st.nextToken();
							
		}
			
		XYSeriesCollection dataset=new XYSeriesCollection(readMeasure(lineVector, header, measureName));
		JFreeChart chart = createLineChart(dataset, header, measureName);
		
		//add the chart to a panel...
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
	}
	
	/**
	 * Reads a measure vector data object and creates a XYSeries object 
	 * 
	 * @param lineVector the measure Vector object containing measure values
	 * @param header the header data object containing experiment information
	 * @return an XYSeries object
	 * 
	 * @see XYSeries
	 * @see MeasureVector
	 * @see HeaderData
	 */	
	private XYSeries readMeasure(MeasureVector lineVector, HeaderData header, String measureName)
	{		
		XYSeries series = new XYSeries(measureName);
		for(int i = 0; i<lineVector.getNOfEntries(); i++)
		{
			if(lineVector.getMeasureValue(i)!= Double.NaN)
				series.add(lineVector.getNOfCluster(i), lineVector.getMeasureValue(i));
		}
		return series;
	}
	
	/**
	 * Creates the line Chart 
	 * 
	 * @param dataset an XYSeriesCollection dataset
	 * @param header a header data Object
	 * @return a JFreeChart object
	 * 
	 * @see XYSeriesCollection
	 * @see HeaderData
	 * @see JFreeChart
	 */	
	public JFreeChart createLineChart(XYSeriesCollection dataset, HeaderData header, String measureName)
	{
		//Generates the graph
		
		 this.chart = ChartFactory.createXYLineChart(
				measureName, // Title
				"Number of cluster", // x-axis Label
				measureName+" Value", // y-axis Label
				dataset, // Dataset
				PlotOrientation.VERTICAL, // Plot Orientation
				true, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
		);
		
		XYPlot plot = this.chart.getXYPlot();
		
		//customise the range axis...
		ValueAxis rAxis = (ValueAxis) plot.getRangeAxis();
		
		double rangeSize = Math.ceil(this.lineVector.getMaxMeasureValue() - this.lineVector.getMinMeasureValue());
		rAxis.resizeRange(rangeSize);
		rAxis.setRange(this.lineVector.getMinMeasureValue()-0.01, this.lineVector.getMaxMeasureValue()+0.01);
				
		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(1, true);
        plot.setRenderer(renderer);

		
		NumberAxis rangeAxis = (NumberAxis) plot.getDomainAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setUpperMargin(0.20);
			
	return chart;
	}
	
	/**
	 * 
	 * @return
	 */
	public JFreeChart getChart() {
		return chart;
	}

}
