/*
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package slider;

import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;


/**
 *
 * @version 2.0
 */
public class Table extends CustomItem  implements ItemCommandListener  {
    private static final Command CMD_MOVE= new Command("Muovi", Command.ITEM, 1);

    private static final int UPPER = 0;
    private static final int IN = 1;
    private static final int LOWER = 2;
    private Display display;
    private GameLogic game;
    private StringItem label;
    private int initColor = 0x00C0C070;
    private int winColor = 0x0010D010;
    private int rows = 4;
    private int cols = 4;
    private int dx = 30;
    private int dy = 30;
    private int location = UPPER;
    private int currentX = 0;
    private int currentY = 0;
    private String[][] data = new String[rows][cols];
    private Font fntHuge;
    
    // Traversal stuff     
    // indicating support of horizontal traversal internal to the CustomItem
    boolean horz;

    // indicating support for vertical traversal internal to the CustomItem.
    boolean vert;

    public Table(String title, SliderGame s) {
        super(title);
        display = s.getDisplay();
        label = s.getLabel();

//        addCommand(CMD_MOVE);
//        setDefaultCommand(CMD_MOVE);
//        setItemCommandListener(this);

        int interactionMode = getInteractionModes();
        horz = ((interactionMode & CustomItem.TRAVERSE_HORIZONTAL) != 0);
        vert = ((interactionMode & CustomItem.TRAVERSE_VERTICAL) != 0);

        dx = (s.getMainForm().getWidth() - 1) / cols;
        int lastRowHeight;
        if (s.getLabel() == null)
        	lastRowHeight = 16;
        else
        	lastRowHeight = s.getLabel().getMinimumHeight() + 1;

        dy = (s.getMainForm().getHeight() - lastRowHeight ) / rows;
        if (dx > dy)
        	dx = dy;
        else
        	dy = dx;
        
        fntHuge = Font.getFont( Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
        
        game = null;
    }

    public void commandAction(Command c, Item i) {
//        if (c == CMD_MOVE) {
//    		if (!game.slide( currentY * cols +  currentX))
//    			display.vibrate(800);
//        }
    }
    
    public void setGame( GameLogic g) {
    	game = g;
    }

    protected int getMinContentHeight() {
        return (rows * dy) + 1;
    }

    protected int getMinContentWidth() {
        return (cols * dx) + 1;
    }

    protected int getPrefContentHeight(int width) {
        return (rows * dy) + 1;
    }

    protected int getPrefContentWidth(int height) {
        return (cols * dx) + 1;
    }
    
    protected void keyPressed(int keyCode) {
    	// System.out.println("Table.keyPressed called, param = " + keyCode);
    }

    protected void keyReleased(int keyCode) {
    	if (keyCode == -5 || keyCode == 50) {
    		if (!game.slide( currentY * cols +  currentX)) {
    			Beep();
    			display.vibrate(800);
    		}
    	}
    }

    protected void paint(Graphics g, int w, int h) {
        for (int i = 0; i <= rows; i++) {
            g.drawLine(0, i * dy, cols * dx, i * dy);
        }

        for (int i = 0; i <= cols; i++) {
            g.drawLine(i * dx, 0, i * dx, rows * dy);
        }
     
        if (game == null)
        	return;
        
        int backcol;
        boolean bGameOver = game.isSolved();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (data[i][j].length() == 0) {
                	backcol = 0x00303030;
                }
                else
                	backcol = ( bGameOver ? winColor : initColor);
            	g.setColor(backcol);
                g.fillRect((j * dx) + 1, (i * dy) + 1, dx - 1, dy - 1);
            }
        }
        g.setColor( 0x00D0D0D0);
        g.fillRect((currentX * dx) + 1, (currentY * dy) + 1, dx - 1, dy - 1);
        g.setColor( bGameOver  ? winColor : initColor);

        Font fntTmp = g.getFont();
        g.setFont(fntHuge);
        g.setColor(0x00000000);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (data[i][j] != null) {
                    // store clipping properties
                    int oldClipX = g.getClipX();
                    int oldClipY = g.getClipY();
                    int oldClipWidth = g.getClipWidth();
                    int oldClipHeight = g.getClipHeight();
                    g.setClip((j * dx) + 1, i * dy, dx - 1, dy - 1);
                    g.drawString(data[i][j], (j * dx) + dx / 2, ((i + 1) * dy) - dy / 2 + 8,
                        Graphics.BOTTOM | Graphics.HCENTER);

                    // restore clipping properties
                    g.setClip(oldClipX, oldClipY, oldClipWidth, oldClipHeight);
                }
            }
        }
        g.setFont(fntTmp);
    }

    protected boolean traverse(int dir, int viewportWidth, int viewportHeight, int[] visRect_inout) {
        if (horz && vert) {
            switch (dir) {
            case Canvas.DOWN:

                if (location == UPPER) {
                    location = IN;
                } else {
                    if (currentY < (rows - 1)) {
                        currentY++;
                        repaint(currentX * dx, (currentY - 1) * dy, dx, dy);
                        repaint(currentX * dx, currentY * dy, dx, dy);
                    } else {
                        location = LOWER;

                        return false;
                    }
                }

                break;

            case Canvas.UP:

                if (location == LOWER) {
                    location = IN;
                } else {
                    if (currentY > 0) {
                        currentY--;
                        repaint(currentX * dx, (currentY + 1) * dy, dx, dy);
                        repaint(currentX * dx, currentY * dy, dx, dy);
                    } else {
                        location = UPPER;

                        return false;
                    }
                }

                break;

            case Canvas.LEFT:

                if (currentX > 0) {
                    currentX--;
                    repaint((currentX + 1) * dx, currentY * dy, dx, dy);
                    repaint(currentX * dx, currentY * dy, dx, dy);
                }

                break;

            case Canvas.RIGHT:

                if (currentX < (cols - 1)) {
                    currentX++;
                    repaint((currentX - 1) * dx, currentY * dy, dx, dy);
                    repaint(currentX * dx, currentY * dy, dx, dy);
                }
            }
        } else if (horz || vert) {
            switch (dir) {
            case Canvas.UP:
            case Canvas.LEFT:

                if (location == LOWER) {
                    location = IN;
                } else {
                    if (currentX > 0) {
                        currentX--;
                        repaint((currentX + 1) * dx, currentY * dy, dx, dy);
                        repaint(currentX * dx, currentY * dy, dx, dy);
                    } else if (currentY > 0) {
                        currentY--;
                        repaint(currentX * dx, (currentY + 1) * dy, dx, dy);
                        currentX = cols - 1;
                        repaint(currentX * dx, currentY * dy, dx, dy);
                    } else {
                        location = UPPER;

                        return false;
                    }
                }

                break;

            case Canvas.DOWN:
            case Canvas.RIGHT:

                if (location == UPPER) {
                    location = IN;
                } else {
                    if (currentX < (cols - 1)) {
                        currentX++;
                        repaint((currentX - 1) * dx, currentY * dy, dx, dy);
                        repaint(currentX * dx, currentY * dy, dx, dy);
                    } else if (currentY < (rows - 1)) {
                        currentY++;
                        repaint(currentX * dx, (currentY - 1) * dy, dx, dy);
                        currentX = 0;
                        repaint(currentX * dx, currentY * dy, dx, dy);
                    } else {
                        location = LOWER;

                        return false;
                    }
                }
            }
        } else {
            // In case of no Traversal at all: (horz|vert) == 0
        }

        visRect_inout[0] = currentX;
        visRect_inout[1] = currentY;
        visRect_inout[2] = dx;
        visRect_inout[3] = dy;

        return true;
    }

    public void setText(String text) {
        data[currentY][currentX] = text;
        repaint(currentY * dx, currentX * dy, dx, dy);
    }

    public void refresh() {
    	repaint();
    }
    
    public void setData(String val, int i, int j) {
        data[ i][ j] = val;
        repaint(i * dx, j * dy, dx, dy);
    }

    public void setData(Vector in) {
 
    	if (in.size() < rows * cols)
    		return;
    	
    	for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = (String) in.elementAt(i*cols+j);
            }
    	}
        refresh();
    }

    public void Beep() {
	    try {
	        Manager.playTone(60, 400, 90);
	    } catch (MediaException ex) {
	        System.out.println("can't play tone");
	    }
    }
	public Display getDisplay() {
		return display;
	}
}
