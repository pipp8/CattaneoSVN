package valworkbench.graphics;

import java.awt.Dimension;
import java.util.StringTokenizer;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.MeasureVector;
/**
 * Creates a Bar Chart figure, obtained from Gap Statistic values
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 */
public class VisualBarChart extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MeasureVector lineVector = new MeasureVector();
	private HeaderData header = new HeaderData();
	private JFreeChart chart;
	
	/**
	 *  Creates the Bar Chart loading information from file
	 * 
	 * @param title the chart title
	 * @param pathMeasure path for measure values file
	 * @param pathHeader path for header file
	 */
	public VisualBarChart(String title, String pathMeasure, String pathHeader)
	{
		super(title);
				
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
		String name = st.nextToken();
		
		CategoryDataset measureDataset = createMeasureDataset(lineVector, name);
		
		JFreeChart chart = ChartFactory.createBarChart(
				header.getMeasureName(), // chart title
				"Number of clusters", // domain axis label
				name, // range axis label
				measureDataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);
		ChartPanel chartPanel = new ChartPanel(chart, false);
		chartPanel.setPreferredSize(new Dimension(500, 270));
		setContentPane(chartPanel);
		
	}
	/**
	 * Creates the Bar Chart loading information from Measure_Vector and Header_Data Object
	 *  
	 * @param title the chart title
	 * @param lineVector Measure Vector object containing measure values
	 * @param header Header object containing experiment informations
	 * @see MeasureVector
	 * @see HeaderData 
	 */
	public VisualBarChart(String title, MeasureVector lineVector, HeaderData header)
	{
		super(title);
		this.createVisualBarChart(lineVector, header);
	}
	
	/**
	 * 
	 * @return
	 */
	private JFreeChart createVisualBarChart(MeasureVector lineVector, HeaderData header)
	{
		this.lineVector = lineVector;
		this.header = header;
		
		StringTokenizer st = new StringTokenizer(header.getMeasureName());
		String name = st.nextToken();
		
		CategoryDataset measureDataset = createMeasureDataset(lineVector, name);
		
		this.chart = ChartFactory.createBarChart(
				header.getMeasureName(), // chart title
				"Number of clusters", // domain axis label
				name, // range axis label
				measureDataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);
		ChartPanel chartPanel = new ChartPanel(this.chart, false);
		chartPanel.setPreferredSize(new Dimension(500, 270));
		setContentPane(chartPanel);
		
	return this.chart;
	}
		
	/**
	 * Creates the Categorydataset for Bar Chart
	 * 
	 * @param mVector measure vector object containing measure values
	 * @param name the measure name
	 * @return a DefaultCategoryDataset object 
	 * 
	 * @see DefaultCategoryDataset
	 * @see CategoryDatatset 
	 */
	private CategoryDataset createMeasureDataset(MeasureVector mVector, String name)
	{
		DefaultCategoryDataset result = new DefaultCategoryDataset();
		
		//row keys...
		String series1 = name;
		
		//Column keys...
		String type[] = new String[mVector.getNOfEntries()];
		
		//Load x Axis String
        for(int i = 0; i < mVector.getNOfEntries(); i++){
        	type[i] = Integer.toString(mVector.getNOfCluster(i)); 
        }
                        
        //Load Measure Values 
        for(int i = 0; i < mVector.getNOfEntries(); i++){
        	result.addValue(mVector.getMeasureValue(i), series1, type[i]);
        }
		
		return result;
	}
	
	/**
	 * 
	 * @return
	 */
	public JFreeChart getChart() {
		return chart;
	}
	
}
