package layoutManager;

import javax.swing.JFrame;


public class MVCFrame extends JFrame {
	public MVCFrame()
	{
		setTitle("Hello World Sample");
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		
		MVCPanel panel = new MVCPanel();
		add(panel);
	}
	public static final int DEFAULT_WIDTH = 300;
	public static final int DEFAULT_HEIGHT = 200;
}
