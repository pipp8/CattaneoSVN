package vista;

import javax.swing.JFrame;


public class FrameVista extends JFrame {
	PannelloVista panel;
	
	public FrameVista()
	{
		setTitle("Cornice temporanea per la vista della titolatrice");
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		
		panel = new PannelloVista();
		add(panel);
	}
	
	public void initPanel() {
		Thread t = new Thread(panel);
		t.start();
	}
	
	public static final int DEFAULT_WIDTH = 300;
	public static final int DEFAULT_HEIGHT = 200;
}
