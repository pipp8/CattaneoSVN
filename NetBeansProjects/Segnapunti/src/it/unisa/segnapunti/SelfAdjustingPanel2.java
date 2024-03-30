/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.unisa.segnapunti;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;

/**
 *
 * @author pipp8
 */


/*
 *  Support custom painting on a panel in the form of
 *
 *  a) images - that can be scaled, tiled or painted at original size
 *  b) non solid painting - that can be done by using a Paint object
 *
 *  Also, any component added directly to this panel will be made
 *  non-opaque so that the custom painting can show through.
 */

public class SelfAdjustingPanel2 extends JPanel
{
    
    // private String msg = "-";
    private JLabel lblMsg;
    
    private Image imgServe = null;
    private int imgWidth, imgHeight;
    
    private boolean serving = false;
    
    private Paint painter;
    private float alignmentX = 0.5f;
    private float alignmentY = 0.5f;
    private boolean isTransparentAdd = true;


    /*
     *	Set the image used as the background
     */
    public void setImage(Image image)
    {
        this.imgServe = image;
        repaint();
    }


    /*
     *	Set the Paint object used to paint the background
     */
    public void setPaint(Paint painter)
    {
            this.painter = painter;
            repaint();
    }

    
    
       
    public SelfAdjustingPanel2( String msg, Color background, Color foreground, String iconPath) 
    {
        this.setBackground(background);
        this.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));

        if (iconPath != null) {
            try {
                imgWidth = this.getWidth() - 3 * 2;
                imgHeight = this.getHeight() - 3 * 2;

                imgServe = ImageIO.read(getClass().getResource(iconPath));
//                icoServe = new ImageIcon(imgServe.getScaledInstance( imgWidth, imgHeight, Image.SCALE_SMOOTH));
//                lblIcon.setBounds(4, 4, imgWidth, imgHeight);
//                lblIcon.setOpaque(false);
            }
            catch(Exception Ex) {
                Ex.printStackTrace();
            }
        }
        else
            imgServe = null;
        
                
        lblMsg = new JLabel();
        lblMsg.setText(msg);
        lblMsg.setHorizontalAlignment(SwingConstants.LEFT);
        lblMsg.setForeground(foreground);
        // lblMsg.setBounds(0, 0, this.getWidth(), this.getHeight());
        lblMsg.setOpaque(false);
        
        this.setLayout(new BorderLayout());
        this.add(lblMsg);

//        this.setComponentZOrder(lblMsg, 0);
//        if (lblIcon != null)
//            this.setComponentZOrder(lblIcon, 1);
          
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                PanelResized(evt);
            }
        });
    }
    
    
    /*
     *  Add custom painting
     */
    @Override
    protected void paintComponent(Graphics g) 
    {  
        super.paintComponent(g);
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

        //  Invoke the painter for the background

        if (painter != null)
        {
            Dimension d = getSize();
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(painter);
            g2.fill( new Rectangle(0, 0, d.width, d.height) );
        }

        //  Draw the image
       if (serving && imgServe != null) {          
            Dimension d = getSize();
            g.drawImage(imgServe, 0, 0, d.width - 4 * 2, d.height - 4 * 2, null);
            
        }
    }


    
    /*
     *  Specify the horizontal alignment of the image when using ACTUAL style
     */
    public void setImageAlignmentX(float alignmentX)
    {
        this.alignmentX = alignmentX > 1.0f ? 1.0f : alignmentX < 0.0f ? 0.0f : alignmentX;
        repaint();
    }

    /*
     *  Specify the horizontal alignment of the image when using ACTUAL style
     */
    public void setImageAlignmentY(float alignmentY)
    {
        this.alignmentY = alignmentY > 1.0f ? 1.0f : alignmentY < 0.0f ? 0.0f : alignmentY;
        repaint();
    }

    /*
     *  Override method so we can make the component transparent
     */
    public void add(JComponent component)
    {
        add(component, null);
    }

    /*
     *  Override to provide a preferred size equal to the image size
     */
    @Override
    public Dimension getPreferredSize()
    {
            if (imgServe == null)
                    return super.getPreferredSize();
            else
                    return new Dimension(imgServe.getWidth(null), imgServe.getHeight(null));
    }

    /*
     *  Override method so we can make the component transparent
     */
    public void add(JComponent component, Object constraints)
    {
        if (isTransparentAdd)
        {
            makeComponentTransparent(component);
        }

        super.add(component, constraints);
    }

    /*
     *  Controls whether components added to this panel should automatically
     *  be made transparent. That is, setOpaque(false) will be invoked.
     *  The default is set to true.
     */
    public void setTransparentAdd(boolean isTransparentAdd)
    {
            this.isTransparentAdd = isTransparentAdd;
    }

    /*
     *	Try to make the component transparent.
     *  For components that use renderers, like JTable, you will also need to
     *  change the renderer to be transparent. An easy way to do this it to
     *  set the background of the table to a Color using an alpha value of 0.
     */
    private void makeComponentTransparent(JComponent component)
    {
            component.setOpaque( false );

            if (component instanceof JScrollPane)
            {
                    JScrollPane scrollPane = (JScrollPane)component;
                    JViewport viewport = scrollPane.getViewport();
                    viewport.setOpaque( false );
                    Component c = viewport.getView();

                    if (c instanceof JComponent)
                    {
                            ((JComponent)c).setOpaque( false );
                    }
            }
    }
    

    private void  PanelResized(ComponentEvent evt) {
        
        Graphics2D g2 = (Graphics2D) this.getGraphics();  
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);  
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,  
                            RenderingHints.VALUE_FRACTIONALMETRICS_ON); 
     
        Font font = g2.getFont().deriveFont(36f);  
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

        if (serving && imgServe != null) {          
            imgWidth = this.getWidth() - 4 * 2;
            imgHeight = this.getHeight() - 4 * 2;
            
            lblMsg.setIcon( new ImageIcon(imgServe.getScaledInstance( imgWidth, imgHeight, Image.SCALE_SMOOTH)));
            // lblMsg.setBounds(4, 4, imgWidth, imgHeight);
        }
        repaint();
    }
    
    public boolean setActiveBackground(boolean newValue) {
        boolean oldValue = serving;
        serving = newValue;

//        lblIcon.setVisible(serving);
        if (! newValue)
            lblMsg.setIcon(null);
        
        PanelResized(null);
        
        return oldValue;
    }

    public boolean getActiveBackground() {
        return serving;
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
        repaint();
    }
    
    
    public void setMsg(int val) {
        this.setMsg(String.valueOf(val));
    } 
}
