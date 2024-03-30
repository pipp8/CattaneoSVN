package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

public class PannelloVista extends JPanel implements  Runnable {

	private String msg = "Valentino ha vinto 90 gran premi";
	private String display;
	private int xStart, yStart;
	private int ndx;
	private int delay = 300;
	private int fntSize = 30;
	private String fntFamily;
	private Font fntTesto;
	private int avCharWidth;
	private boolean bClear = false;
	
	public PannelloVista () {

		xStart = 100;
		yStart = 100;
		display = msg;
		fntFamily = "Times";
		this.setPreferredSize(new Dimension(200, 100));
	}
	
	public void run () {
		xStart = this.getWidth();
		// yStart = this.getHeight(); inizializzata da changeFont()
		setFntFamily(fntFamily);
		
		for( ; true; ) {
			// passo 1
			for( xStart = this.getWidth(); xStart > 0; xStart -= avCharWidth) {
				display = msg;
				repaint();
				try {
					Thread.sleep( delay);
				}
				catch(Exception e ) {}
			}		
			// passo 2
			for( ndx = 0; ndx < msg.length(); ndx++ ){
				xStart = 0;
				display = msg.substring(ndx);
				repaint();
				try {
					Thread.sleep( delay);
				}
				catch(Exception e ) {}
			}
		}
	}

	public void clear() {
		bClear = true;
		display = "";
		repaint();
		bClear = false;
	}
	
	public void paintComponent (Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		super.paintComponent(g);
		
		if (bClear) {
			g2.clearRect(0, 0, getWidth(), getHeight());
		}
		else {
			g2.setFont( fntTesto);
			g2.drawString( display, xStart, yStart);
		}
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getFntSize() {
		return fntSize;
	}

	public void setFntSize(int fntSize) {
		this.fntSize = fntSize;
		changeFont();
	}

	public String getFntFamily() {
		return fntFamily;
	}

	public void setFntFamily(String fntFamily) {
		this.fntFamily = fntFamily;
		changeFont();
	}

	private void changeFont() {
		fntTesto = new Font( fntFamily, Font.ITALIC, fntSize);
		Graphics2D g = (Graphics2D) this.getGraphics();
		if (g != null) {
			FontRenderContext context = g.getFontRenderContext();
			LineMetrics metrics = fntTesto.getLineMetrics(msg, context);
			int descent = (int) metrics.getDescent();
	
			yStart = this.getHeight() - descent - 5 ;
			avCharWidth = (int) fntTesto.getStringBounds("O", context).getWidth();
		}
	}
}













