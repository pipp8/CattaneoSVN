package smstest;



import java.awt.Dimension;

import javax.swing.JFrame;

public class SmsTester {

	public static void main(String[] args) {
		SmsFrame frame = new SmsFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(300, 300);
		frame.setVisible(true);
//		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
//		Dimension frameSize = frame.getSize();
//		if (frameSize.height > screenSize.height)
//			frameSize.height = screenSize.height;
//		if (frameSize.width > screenSize.width)
//			frameSize.width = screenSize.width;
//		frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
//		frame.validate();
	}
}

