/*
 * $Header: /cygdrive/C/CVSNT/cvsroot/Cattaneo/Sources/J2ME/SudokuME/src/sudoku/Table.java,v 1.2 2007-09-09 18:08:14 cattaneo Exp $
 */

package sudoku;

import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;

import com.nokia.mmedia.control.STempoControl;


/**
 *
 * @version 2.0
 */
public class Table extends Canvas implements CommandListener {
    private static final Command INS_VALUE = new Command("Inserisci Valore", Command.ITEM, 5);
    private static final Command INS_VINCOLI = new Command("Inserisci Vincolo", Command.ITEM, 5);
    private static final Command CMD_RUN = new Command("Calcola Soluzione", Command.ITEM, 10);
    private static final Command CMD_BACK = new Command("Back", Command.BACK, 50);
    private static final Command CMD_CANCEL= new Command("Cancel", Command.CANCEL, 1);
    private static final Command CMD_OK = new Command("OK", Command.OK, 1);

    private Display display;
    private Sudoku parent;
    private GameLogic game;
    private List lstValues;
    private int rows = 9;
    private int cols = 9;
    private int dx = 0;
    private int dy = 0;
    private int xoff = 0;
    private int yoff = 0;
    private int currentX = 0;
    private int currentY = 0;
    private int ppainted = 0;
    private String[][] data = new String[rows][cols];
    private boolean readVincoli = false;
    private Font fntBold;
    private Font fntHuge;
    private Font fntPlain;
    int width;
    int height;
  
    // Traversal stuff     
    // indicating support of horizontal traversal internal to the CustomItem
    boolean horz;

    // indicating support for vertical traversal internal to the CustomItem.
    boolean vert;

    public Table(String title, Sudoku parent) {
    	setFullScreenMode(true);
    	setTitle( title);
        display = parent.getDisplay();
        this.parent = parent;
      
        setCommands(false);
        setCommandListener(this);
              
        data = new String[rows][cols];
        for(int i = 0; i < rows; i++)
            for(int j = 0; j < cols; j++)
            	data[i][j] = "";
        
        fntBold = Font.getFont( Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
        fntHuge = Font.getFont( Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
        fntPlain= Font.getFont( Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
               
        //	per leggere i valori
        lstValues = new List("Scegli", Choice.IMPLICIT);
        lstValues.addCommand(CMD_OK);
        lstValues.addCommand(CMD_CANCEL);
        lstValues.setCommandListener(this);
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

 /*   protected void paint2(Graphics g) {
      	String s = "Width = ";
    	s = s + String.valueOf(width);
    	g.drawString(s , 2, 20, Graphics.BOTTOM | Graphics.LEFT);
    	s = "height = ";
    	s = s + String.valueOf(height);
    	g.drawString(s , 2, 50, Graphics.BOTTOM | Graphics.LEFT);
    	s = "dx = ";
    	s = s + String.valueOf(dx);
    	g.drawString(s , 2, 70, Graphics.BOTTOM | Graphics.LEFT);
    	s = "dy = ";
    	s = s + String.valueOf(dy);
    	g.drawString(s , 2, 90, Graphics.BOTTOM | Graphics.LEFT);
    }
 */   
    protected void paint(Graphics g) {
        int oldColor = g.getColor();

        if (dx == 0) {
	        dx = (getWidth() - 2) / cols;
	        dy = (getHeight() - 10) / rows;
	        if (dx > dy)
	        	dx = dy;
	        else	
	        	dy = dx;
	        xoff = (getWidth() - (dx * cols)) / 2;
	        yoff = 3; // (getHeight() - (dy * rows)) / 2;
        }
        
        for (int i = 0; i <= rows; i++) {
        	g.setStrokeStyle((i % 3 == 0) ? Graphics.SOLID : Graphics.DOTTED);
            g.drawLine(xoff, yoff + i * dy, xoff + cols * dx, yoff + i * dy);
        }

        for (int i = 0; i <= cols; i++) {
        	g.setStrokeStyle((i % 3 == 0) ? Graphics.SOLID : Graphics.DOTTED);
            g.drawLine(xoff + i * dx, yoff, xoff + i * dx, yoff + rows * dy);
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int f1 = i / 3;
                int f2 = j / 3;

                int col = (((f1 + f2) % 2) == 0) ? 0x00FFFFB0 : 0x00FFFFFF;
           	 	// coloro di giallo tutti i sottoquadrati in posizioni le cui coordinate sommano ad un indice pari
                g.setColor( col);
                g.fillRect((i * dx) + xoff + 1, (j * dy) + yoff + 1, dx - 1, dy - 1);
            }
        }
            	
        g.setColor(0x00D0D0D0);
        g.fillRect((currentX * dx) + xoff + 1, (currentY * dy) + yoff + 1, dx - 1, dy - 1);
        g.setColor(oldColor);

        if ((game.getPunti() > 0) && (game.getPunti() != ppainted)) {
        	ppainted = game.getPunti();
        	String msg = String.valueOf(ppainted);
        	g.setFont(fntHuge);
		    int h = fntHuge.getHeight() + 2;
//		    int w = fntHuge.stringWidth(msg) + 10;
		    int w = getWidth();
		    int sx = getWidth() / 2;
		    int sy = getHeight();
		    g.setColor(0x00ffffff);
		    g.fillRect(0, sy - h, w, h);
		    g.setColor( 0x0);
		    g.drawString( "tot: " + String.valueOf(parent.getTotalePunti()), 3, sy, Graphics.BOTTOM | Graphics.LEFT);
		    g.drawString( "cur:" + msg, w / 2 + 10, sy, Graphics.BOTTOM | Graphics.LEFT);
        }
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (data[i][j] != null && data[i][j].length() > 0) {
                    // store clipping properties
                    int oldClipX = g.getClipX();
                    int oldClipY = g.getClipY();
                    int oldClipWidth = g.getClipWidth();
                    int oldClipHeight = g.getClipHeight();
                    g.setClip((j * dx) + xoff + 1, i * dy + yoff, dx - 1, dy - 1);
                    g.setColor(oldColor);
                    if (game.isConstraint(i, j) == true)
                    	g.setFont(fntBold);
                    else if (game.isSuggested(i,j)) {
                    	g.setFont(fntBold);
                    	g.setColor(0x00ff0000);
                    }
                    else
                    	g.setFont(fntPlain);
                
                    g.drawString(data[i][j], (j * dx) + dx / 2 + xoff, ((i + 1) * dy)  + yoff - 2,
                        Graphics.BOTTOM | Graphics.HCENTER);
                    // restore clipping properties
                    g.setClip(oldClipX, oldClipY, oldClipWidth, oldClipHeight);
                }
            }
        }
    }

 
    protected void keyPressed(int keyCode) {
    	int dir = getGameAction(keyCode);
    	
    	switch (dir) {
            case Canvas.DOWN:

            	if (currentY < (rows - 1)) {
                    currentY++;
                    repaint(currentX * dx + xoff, (currentY - 1) * dy + yoff, dx, dy);
                    repaint(currentX * dx + xoff, currentY * dy + yoff, dx, dy);
                }
                break;

            case Canvas.UP:

                if (currentY > 0) {
                    currentY--;
                    repaint(currentX * dx + xoff, (currentY + 1) * dy + yoff, dx, dy);
                    repaint(currentX * dx + xoff, currentY * dy + yoff, dx, dy);
                }
                break;

            case Canvas.LEFT:

                if (currentX > 0) {
                    currentX--;
                    repaint((currentX + 1) * dx + xoff, currentY * dy + yoff, dx, dy);
                    repaint(currentX * dx + xoff, currentY * dy + yoff, dx, dy);
                }
                break;

            case Canvas.RIGHT:

                if (currentX < (cols - 1)) {
                    currentX++;
                    repaint((currentX - 1) * dx + xoff, currentY * dy + yoff, dx, dy);
                    repaint(currentX * dx + xoff, currentY * dy + yoff, dx, dy);
                }
            }
        }
    
    public void refresh() {
    	super.repaint();
    }

    public void setValue(String text) {
       	if (readVincoli) {
    		game.setConstraint( Integer.parseInt(text), currentY, currentX);
       	}
       	else {
       		game.setValue(Integer.parseInt(text), currentY, currentX);
    	}
        data[currentY][currentX] = text;
        repaint(currentY * dx + xoff, currentX * dy + yoff, dx, dy);
    }
    
    public void setData(String val, int i, int j) {
        data[i][j] = val;
    }
    
    
    public void commandAction(Command c, Displayable d) {
    	if (d.equals(lstValues)) {
    		if (c == CMD_OK) {
    			if ((lstValues.getSelectedIndex() == 0) && !readVincoli)  {
        			setValue(String.valueOf(game.getCorrect( currentY, currentX)));    				
    			}
    			else {
    				setValue(lstValues.getString(lstValues.getSelectedIndex()));
    			}
    			display.setCurrent(this);
    		}
    		if (c == CMD_CANCEL) {
    			display.setCurrent(this);
    		}
    	}
    	else if(d == this) {
	        if (c == CMD_BACK) {
	        	display.setCurrent(parent.getSplashScreen());
	        }
	        else if (c == INS_VALUE) {
	        	if (game.isConstraint(currentY, currentX)) {
	        		Beep();
	        		return;
	        	}
	        	readVincoli  = false;
	        	boolean[] p = game.getPossibleValues(currentY, currentX);
	        	lstValues.deleteAll();
	        	lstValues.append("Suggerisci", null);
	        	for(int v = 1; v < 10; v++) {
	        		if (p[v])
	        			lstValues.append(String.valueOf(v), null);
	        	}
	            display.setCurrent(lstValues);
	        }
	        else if (c == INS_VINCOLI) {
	        	readVincoli  = true;
	        	boolean[] p = game.getPossibleValues(currentY, currentX);
	        	lstValues.deleteAll();
	        	for(int v = 1; v < 10; v++) {
	        		if (p[v])
	        			lstValues.append(String.valueOf(v), null);
	        	}
	            display.setCurrent(lstValues);
	        }
	        else if (c == CMD_RUN) {
	        	this.removeCommand(CMD_RUN);
	        	this.removeCommand(INS_VINCOLI);
	        	game.Start();
	        }
    	}
    }

    public void setCommands(boolean automatic) {
		this.addCommand(CMD_BACK);
    	if (automatic) {
        	this.removeCommand(INS_VALUE);
	        this.addCommand(INS_VINCOLI);
	        this.addCommand(CMD_RUN);
    	}
    	else {
        	this.removeCommand(CMD_RUN);
        	this.removeCommand(INS_VINCOLI);
	        this.addCommand(INS_VALUE);
    	}
    }

    public void Beep() {
	    try {
	        Manager.playTone(60, 400, 90);
	    } catch (MediaException ex) {
	        System.out.println("can't play tone");
	    }
    }
}
