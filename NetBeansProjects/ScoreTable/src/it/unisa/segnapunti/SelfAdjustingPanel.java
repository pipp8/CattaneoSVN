/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.unisa.segnapunti;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 *
 * @author pipp8
 */
public class SelfAdjustingPanel extends BackgroundPanel {
    
    private JLabel lblMsg;
    
    private int imgWidth, imgHeight;
    
    private Image imgServe = null;
    private GradientPaint painter = null;
    
    private boolean serving = false;
    
       
    public SelfAdjustingPanel( Color background, Color foreground, String iconPath) 
    {
        super( (Image) null);
        
        if (iconPath != null) {
            try {
                imgWidth = this.getWidth() - 3 * 2;
                imgHeight = this.getHeight() - 3 * 2;

                imgServe = ImageIO.read(getClass().getResource(iconPath));
                // this.setImage(imgServe);
            }
            catch(Exception Ex) {
                Ex.printStackTrace();
            }
        }
        
        // painter = new GradientPaint(0, 0, Color.BLUE, 600, 0, Color.RED); 
        painter = null;
        this.setPaint(painter);
            
        this.setBackground(background);
        this.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        serving = false;

        lblMsg = new JLabel();
        
        lblMsg.setText("-");
        lblMsg.setFont(new Font("Arial narrow", 0, 36)); // NOI18N);
        lblMsg.setHorizontalAlignment(SwingConstants.LEFT);
        lblMsg.setForeground(foreground);
        lblMsg.setBounds(0, 0, this.getWidth(), this.getHeight());
        
        this.add(lblMsg);

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                PanelResized(evt);
            }
        });
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

//        if (serving && imgServe != null) {
//            lblIcon.setIcon( new ImageIcon(imgServe.getScaledInstance( imgWidth, imgHeight, Image.SCALE_SMOOTH)));
//            lblIcon.setBounds(4, 4, imgWidth, imgHeight);
//        }
        repaint();
    }
    
    public boolean setActiveBackground(boolean newValue) {
        boolean oldValue = serving;
        serving = newValue;
        
        if (serving) {
            this.setImage(imgServe);
        }
        else {
            this.setImage(null);
            this.setPaint(painter);
        }
        repaint();
        return oldValue;
    }

    public boolean getActiveBackground() {
        return serving;
    }
    
    public void setWinner( boolean win) {
        
        if (win) {
            GradientPaint winPainter = new GradientPaint(0, 0, new Color( 251, 255, 80), this.getWidth(), 0, new Color(255, 154, 80)); 
            this.setPaint(winPainter);
        }
        else {
            this.setPaint(null);
        }            
    }
}
