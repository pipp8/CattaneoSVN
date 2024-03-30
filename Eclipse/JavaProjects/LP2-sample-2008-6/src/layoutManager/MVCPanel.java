package layoutManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JPanel;

public class MVCPanel extends JPanel {

	private JButton btGiallo;
	private JButton btRosso;
	private JButton btVerde;
	private Color backgroundColor;

	
	public MVCPanel () {
		btGiallo = new JButton("Giallo");
		btRosso = new JButton("Rosso");
		btVerde = new JButton("Verde");
		
		ActionListener al1 = new ButtonListener();
		btGiallo.addActionListener(al1);
		ActionListener al2 = new ButtonListener();
		btRosso.addActionListener(al2);
		ActionListener al3 = new ButtonListener();
		btVerde.addActionListener(al3);

		BorderLayout bl = new BorderLayout();
		this.setLayout(bl);
		
		this.add(btGiallo, BorderLayout.WEST);
		this.add(btRosso, BorderLayout.NORTH);
		this.add(btVerde, BorderLayout.EAST);
	}
	
	public void init () {
	}
	
	public void paintComponent (Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		super.paintComponent(g);
		g2.setBackground(backgroundColor);
		g2.clearRect(30, 30, 100, 100);
	}

	class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("Button ...");
			if (e.getSource() == btGiallo) {
				System.out.println("giallo");
				backgroundColor = Color.yellow;
			}
			else if (e.getSource() == btRosso)
				backgroundColor = Color.red;
			else if (e.getSource() == btVerde)
				backgroundColor = Color.green;
			
			repaint();
		}
	}
}
