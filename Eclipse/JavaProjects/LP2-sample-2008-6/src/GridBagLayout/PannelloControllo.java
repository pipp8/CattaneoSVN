package GridBagLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.sun.corba.se.impl.encoding.CodeSetConversion.BTCConverter;

import vista.PannelloVista;

public class PannelloControllo extends JPanel {

	PannelloVista vista = null;
	Thread bRun = null;
	JTextArea txtMessage = null;
	JButton btStart, btStop, btPausa;

	public PannelloControllo (PannelloVista vista) {
		this.vista = vista;
		
		GridBagLayout gbl= new GridBagLayout();
		this.setLayout(gbl);
		
		JLabel lblTesto= new JLabel();
		lblTesto.setText("Testo Messaggio");
		GridBagConstraints ct1= new GridBagConstraints();
		ct1.gridx=0;
		ct1.gridy=0;
		ct1.gridheight=1;
		ct1.gridwidth=3;
		ct1.anchor=GridBagConstraints.SOUTH;
		this.add(lblTesto, ct1);
		
		txtMessage= new JTextArea(4,20);
		txtMessage.setLineWrap(true);
		txtMessage.getDocument().addDocumentListener( new MessageChangedListener());
		JScrollPane spMessage = new JScrollPane( txtMessage);
		spMessage.createVerticalScrollBar();
		GridBagConstraints ct2= new GridBagConstraints();
		ct2.gridx=0;
		ct2.gridy=1;
		ct2.gridheight=5;
		ct2.gridwidth=3;
		ct2.anchor=GridBagConstraints.NORTHWEST;
		ct2.weightx=1;
		ct2.weighty=1;
		ct2.insets= new Insets(0,10,10,10);
		ct2.fill=GridBagConstraints.BOTH;
		this.add(spMessage, ct2);
		
		JLabel lblCombo= new JLabel("Font Family"); 
		GridBagConstraints ct3= new GridBagConstraints();
		ct3.gridx=3;
		ct3.gridy=0;
		ct3.gridheight=1;
		ct3.gridwidth=1;
		ct3.insets= new Insets(0,4,4,4);
		ct3.anchor=GridBagConstraints.SOUTH;
		this.add(lblCombo, ct3);
		
		JComboBox cmbFontFamily= new JComboBox();
		cmbFontFamily.addItemListener(new FontFamilyChangeSelection());
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String [] fn = ge.getAvailableFontFamilyNames();
		for(int i = 0; i < fn.length; i++)
			cmbFontFamily.addItem( fn[i]);
		cmbFontFamily.setEditable(false);
		
		GridBagConstraints ct4= new GridBagConstraints();
        ct4.gridx=3;
		ct4.gridy=1;
		ct4.gridheight=1;
		ct4.gridwidth=1;
		ct4.anchor=GridBagConstraints.NORTHEAST;
		ct4.ipadx=50;
		ct4.fill=GridBagConstraints.HORIZONTAL;
		this.add(cmbFontFamily, ct4);
		
		JLabel lblSliderSpeed= new JLabel("Velocit� di Scorrimento"); 
		GridBagConstraints ct5= new GridBagConstraints();
		ct5.gridx=3;
		ct5.gridy=2;
		ct5.gridheight=1;
		ct5.gridwidth=1;
		ct5.weighty=1;
		ct5.anchor=GridBagConstraints.SOUTH;
        this.add(lblSliderSpeed, ct5);
        
        JSlider sldSpeed= new JSlider();
        sldSpeed.addChangeListener(new SpeedChangeListener());
        sldSpeed.setValue(vista.getDelay());
        sldSpeed.setOrientation(JSlider.HORIZONTAL);
        sldSpeed.setMaximum(1000);
        sldSpeed.setMinimum(100);
        sldSpeed.setMajorTickSpacing(200);
        sldSpeed.setMinorTickSpacing(50);
        sldSpeed.setPaintTicks(true);
        sldSpeed.setPaintLabels(true);
        sldSpeed.createStandardLabels(100);
        
        GridBagConstraints ct6= new GridBagConstraints();
		ct6.gridx=3;
		ct6.gridy=3;
		ct6.gridheight=1;
		ct6.gridwidth=1;
		ct6.weighty=1;
		ct6.fill = GridBagConstraints.HORIZONTAL;
		ct6.anchor=GridBagConstraints.NORTH;
		this.add(sldSpeed, ct6);
		
		JLabel lblSliderFont= new JLabel("Dimensione Font"); 
		GridBagConstraints ct7= new GridBagConstraints();
		ct7.gridx=3;
		ct7.gridy=4;
		ct7.gridheight=1;
		ct7.gridwidth=1;
		ct7.anchor=GridBagConstraints.SOUTH;
        this.add(lblSliderFont, ct7);
        
        JSlider sldFont = new JSlider();
        sldFont.setOrientation(JSlider.HORIZONTAL);
        sldFont.setMaximum(100);
        sldFont.setMinimum(10);
        sldFont.setPaintLabels(true);
        sldFont.setPaintTicks(true);
        sldFont.setMajorTickSpacing(20);
        sldFont.setMinorTickSpacing(5);
        sldFont.createStandardLabels(100);
        sldFont.addChangeListener(new FontSizeChangeListener());
        sldFont.setValue( vista.getFntSize());
 
        GridBagConstraints ct8= new GridBagConstraints();
		ct8.gridx=3;
		ct8.gridy=5;
		ct8.gridheight=1;
		ct8.gridwidth=1;
		ct8.fill = GridBagConstraints.HORIZONTAL;
		ct8.anchor=GridBagConstraints.NORTH;
		this.add(sldFont, ct8);
		
		ButtonListener btl = new ButtonListener();
		
		btStart= new JButton("START");
		btStart.addActionListener(btl);
		GridBagConstraints ct9= new GridBagConstraints();
		ct9.gridx=0;
		ct9.gridy=6;
		ct9.gridheight=1;
		ct9.gridwidth=1;
		ct9.weightx=1;
		ct9.anchor=GridBagConstraints.WEST;
		ct9.insets=new Insets(2,4,2,4);
		this.add(btStart, ct9);
		
		btPausa= new JButton("PAUSA");
		btPausa.addActionListener(btl);
		btPausa.setEnabled(false);
		GridBagConstraints ct10= new GridBagConstraints();
		ct10.gridx=1;
		ct10.gridy=6;
		ct10.gridheight=1;
		ct10.gridwidth=1;
		ct10.weightx=1;
		ct10.anchor=GridBagConstraints.CENTER;
		ct10.insets=new Insets(2,4,2,4);
		this.add(btPausa, ct10);

		btStop= new JButton("STOP");
		btStop.setEnabled(false);
		btStop.addActionListener(btl);
		GridBagConstraints ct11= new GridBagConstraints();
		ct11.gridx=2;
		ct11.gridy=6;
		ct11.gridheight=1;
		ct11.gridwidth=1;
		ct11.weightx=1;
		ct11.anchor=GridBagConstraints.EAST;
		ct11.insets=new Insets(2,4,2,4);
		this.add(btStop, ct11);
		
		
	}
	public void init () { 
	}
	
	public void paintComponent (Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		super.paintComponent(g);
		
	}

	class FontSizeChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent ev) {
			vista.setFntSize( ((JSlider) ev.getSource()).getValue());
			vista.repaint();
		}
	}
	class SpeedChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent ev) {
			vista.setDelay(((JSlider) ev.getSource()).getValue());
			vista.repaint();
		}
	}
	class FontFamilyChangeSelection implements ItemListener {
		public void itemStateChanged(ItemEvent ev) {
			JComboBox src = (JComboBox) ev.getSource();
			String family = (String) src.getSelectedItem();
			vista.setFntFamily(family);
			vista.repaint();
		}
	}
	class StartListener implements ActionListener {
		public void actionPerformed( ActionEvent ev) {
			btStart.setEnabled(false);
			btPausa.setEnabled(true);
			btStop.setEnabled(true);
			bRun = new Thread( vista);
			bRun.start();
		}
	}

	class ButtonListener implements ActionListener {
		public void actionPerformed( ActionEvent ev) {
			if (ev.getSource() == btStart) {
				btStart.setEnabled(false);
				btPausa.setEnabled(true);
				btStop.setEnabled(true);
				bRun = new Thread( vista);
				bRun.start();
			}
			else if (ev.getSource() == btPausa) {
				if ("Pausa".equalsIgnoreCase(btPausa.getText())) {									
					btPausa.setText("Restart");
					bRun.suspend();
				}
				else {
					btPausa.setText("Pausa");
					bRun.resume();
				}
			}
			else if (ev.getSource() == btStop) {
				bRun.stop();
				vista.clear();
				btStop.setEnabled(false);
				btPausa.setEnabled(false);
				btStart.setEnabled(true);
			}
		}
	}
	class MessageChangedListener implements DocumentListener {

		public void changedUpdate(DocumentEvent e) {
			vista.setMsg(txtMessage.getText());
		}

		public void insertUpdate(DocumentEvent e) {
			vista.setMsg(txtMessage.getText());			
		}

		public void removeUpdate(DocumentEvent e) {
			vista.setMsg(txtMessage.getText());
		}
		
	}
}













