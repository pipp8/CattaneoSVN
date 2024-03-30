/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.unisa.segnapunti;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;

/**
 *
 * @author pipp8
 */
public class SelfAdjustingPanel extends JPanel {
    
    private JLabel lblMsg;
    private JLabel lblIcon = null;
    
    private Image imgServe = null;
    private int imgWidth, imgHeight;
    
    private boolean serving = false;
    
    
    public SelfAdjustingPanel( Color background, Color foreground) 
    {
        this.setBackground(background);
        this.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        this.lblMsg = new JLabel();
        this.lblMsg.setText("-");
        this.lblMsg.setHorizontalAlignment(SwingConstants.LEFT);
        this.lblMsg.setForeground(foreground);
        this.setLayout(new BorderLayout());
        this.add(lblMsg, BorderLayout.CENTER);
        
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                PanelResized(evt);
            }
        });
    }
    
    
       
    public SelfAdjustingPanel( Color background, Color foreground, String iconPath) 
    {
        this.setBackground(background);
        this.setOpaque(true);
        this.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        serving = false;
        
        try {
            imgWidth = this.getWidth() - 3 * 2;
            imgHeight = this.getHeight() - 3 * 2;
            
            lblIcon = new JLabel();
            imgServe = ImageIO.read(getClass().getResource(iconPath));
//            lblIcon.setIcon( new ImageIcon(imgServe.getScaledInstance( imgWidth, imgHeight, Image.SCALE_SMOOTH)));
//            lblIcon.setBounds(4, 4, imgWidth, imgHeight);
            lblIcon.setBackground(new Color( 10, 10, 100));
            lblIcon.setOpaque(true);

            this.setLayout(new OverlayLayout(this));
            this.add(lblIcon);
        }
        catch(Exception Ex) {
            Ex.printStackTrace();
        }
        
                
        lblMsg = new JLabel();
        
        lblMsg.setText("-");
        lblMsg.setFont(new Font("Arial narrow", 0, 36)); // NOI18N);
        lblMsg.setHorizontalAlignment(SwingConstants.LEFT);
        lblMsg.setForeground(foreground);
        lblMsg.setBounds(0, 0, this.getWidth(), this.getHeight());
        lblMsg.setBackground(new Color( 10, 100, 100));
        lblMsg.setOpaque(false);
        
        this.setLayout(new BorderLayout());
        this.add(lblMsg);

        this.setComponentZOrder(lblMsg, 0);
        this.setComponentZOrder(lblIcon, 1);
          
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                PanelResized(evt);
            }
        });
    }
    
    
    protected void paintComponent(Graphics g) {  
//        super.paintComponent(g); 
   
//        Graphics2D g2 = (Graphics2D)g;  
//        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  
//                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);  
//        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,  
//                            RenderingHints.VALUE_FRACTIONALMETRICS_ON); 
//        
//        
//        Font font = g2.getFont().deriveFont(36f);  
//        g2.setFont(font);  
//        String msg = lblMsg.getText();
//        FontRenderContext frc = g2.getFontRenderContext();  
//        LineMetrics metrics = font.getLineMetrics(msg, frc);  
//        // Try omitting the descent from the height variable.  
//        float height = metrics.getAscent() + metrics.getDescent();  
//        double width = font.getStringBounds(msg, frc).getWidth();  
//        int w = getWidth();  
//        int h = getHeight();  
//        double xScale = w/width;  
//        double yScale = (double)h/height;  
//        double x = (w - xScale*width)/2;  
//        double y = (h + yScale*height)/2 - yScale*metrics.getDescent();  
//        AffineTransform at = AffineTransform.getTranslateInstance(x, y);  
//        at.scale(xScale, yScale);  
//        lblMsg.setFont(font.deriveFont(at)); 
//        lblMsg.setBounds(0,0, w, h);
//        if (imgServe != null) {
//            int x = this.getParent().getWidth()/2 - imgWidth;
//            int y = this.getParent().getHeight()/2 - imgHeight;
//            g.drawImage(imgServe, x, y, this);
//        }
        // g2.drawString(lblMsg, 0, 0);
        // this.paintChildren(g);        
    }  

    /**
     * @return the msg
     */
    public String getMsg() {
        return lblMsg.getText();
    }

    /**
     * @param msg the msg to set
     */
    public void setMsg(String msg) {
        this.lblMsg.setText(msg);
        PanelResized(null);
    }
    
    
    public void setMsg(int val) {
        this.setMsg(String.valueOf(val));
    }
  
    
    private void  PanelResized(ComponentEvent evt) {
        
        Graphics2D g2 = (Graphics2D) this.getGraphics();  
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);  
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,  
                            RenderingHints.VALUE_FRACTIONALMETRICS_ON); 
     
        Font font = new Font("Arial narrow", 0, 36); // ( g2.getFont().deriveFont(36f);  
        g2.setFont(font);  
        String msg = lblMsg.getText();
        FontRenderContext frc = g2.getFontRenderContext();  
        LineMetrics metrics = font.getLineMetrics(msg, frc);  
        // Try omitting the descent from the height variable.  
        float height = metrics.getAscent() + metrics.getDescent();  
        double width = font.getStringBounds(msg, frc).getWidth();  
        int w = getWidth() - 5;  
        int h = getHeight() - 5;  
        double xScale =  w / width;  
        double yScale =  (double) h / height;  
        AffineTransform at = new AffineTransform();  
        at.scale(xScale, yScale); 

        lblMsg.setFont(font.deriveFont(at)); 
        // lblMsg.setBounds(0,0, w, h);

        imgWidth = this.getWidth() - 4 * 2;
        imgHeight = this.getHeight() - 4 * 2;

        if (serving && imgServe != null) {
            lblIcon.setIcon( new ImageIcon(imgServe.getScaledInstance( imgWidth, imgHeight, Image.SCALE_SMOOTH)));
            lblIcon.setBounds(4, 4, imgWidth, imgHeight);
        }
        repaint();
    }
    
    public boolean setActiveBackground(boolean newValue) {
        boolean oldValue = serving;
        serving = newValue;

        lblIcon.setVisible(serving);
        PanelResized(null);
        
        return oldValue;
    }

    public boolean getActiveBackground() {
        return serving;
    }
}
