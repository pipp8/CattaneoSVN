package valworkbench.graphics;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import valworkbench.datatypes.HeaderData;
/**
 * Creates a window containing log information about the experiment
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro 
 * @author Filippo Utro
 * @version 1.0 
 *
 */
public class VisualHeader extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel jPanel = null;
	private HeaderData header;
	private JTextPane jTextPane = null;
	
	/**
	 * This method initializes 
	 * 
	 */
	public VisualHeader(String title, HeaderData header) {
		super(title+" Summary Experiment");
		this.header = header;
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(395, 240));
        this.setPreferredSize(new Dimension(395, 240));
        this.setContentPane(getJPanel());
        this.writeHeaderOnTextArea();
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(null);
			jPanel.setPreferredSize(new Dimension(395, 240));
			jPanel.add(getJTextPane(), null);
		}
		return jPanel;
	}
	/**
	 *  Writes experiment informations, contained in a Header_Data type Object, on a JTextArea object
	 *
	 */
	public void writeHeaderOnTextArea()
	{
		String textToWrite;
		textToWrite = "Date:\t"+this.header.getDateExp()+"\n"
					 +"Time:\t"+this.header.getTimeExp()+"\n"
					 +"Algorithm:\t"+this.header.getAlgorithmName()+" - "+this.header.getAlgParameters()+"\n"
					 +"Dataset:\t"+this.header.getDatasetName()+" - "+this.header.getDatasetType()+"\n"
					 +"Measure:\t"+this.header.getMeasureName()+" - "+this.header.getMeasParameters();
		this.getJTextPane().setText(textToWrite);
	}
	/**
	 * This method initializes jTextPane	
	 * 	
	 * @return javax.swing.JTextPane	
	 */
	private JTextPane getJTextPane() {
		if (jTextPane == null) {
			jTextPane = new JTextPane();
			jTextPane.setLocation(new Point(9, 8));
			jTextPane.setSize(new Dimension(370, 182));
		}
		return jTextPane;
	}	
	
	
	public void addToTextArea(String textToAdd)
	{
		String text;
		text = this.jTextPane.getText();
		text = text +"\n"+textToAdd;
		this.jTextPane.setText(text);
	}
	
} 
