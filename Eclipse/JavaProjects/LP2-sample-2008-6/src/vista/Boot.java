package vista;

import javax.swing.JFrame;


public class Boot {

	public static void main(String[] args) {
		FrameVista frame = new FrameVista();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.initPanel();
	}

}
