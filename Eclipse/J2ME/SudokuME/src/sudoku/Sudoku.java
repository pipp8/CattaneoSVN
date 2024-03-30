/*
 * $Header: /cygdrive/C/CVSNT/cvsroot/Cattaneo/Sources/J2ME/SudokuME/src/sudoku/Sudoku.java,v 1.2 2007-09-09 18:08:15 cattaneo Exp $
 */

package sudoku;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;


/**
 *
 * @version 2.0
 */
public class Sudoku extends MIDlet implements CommandListener {
    private static final Command CMD_EXIT = new Command("Exit", Command.EXIT, 99);
    private static final Command CMD_RESET = new Command("Risolutore", Command.ITEM, 10);
    private static final Command CMD_PLAY = new Command("Nuovo schema", Command.ITEM, 10);
    private static final Command CMD_OPTION = new Command("Options", Command.ITEM, 20);
 
    private Display display;
    private Options options;
    private Table table;
    private boolean firstTime;
    private Form splashScreen;
    private GameLogic game;
    private int TotalePunti;
    private RecordStore rsPunti = null;
    
    public Sudoku() {
        firstTime = true;
    }

    protected void startApp() {
        if (firstTime) {
        	Image splashImg = null;
        	boolean imageLoaded = false;
        	
            display = Display.getDisplay(this);
            TotalePunti = getPunteggio();
            try {
                splashImg = Image.createImage("/sudoku/images/Sudokuland.png");
                imageLoaded = true;
            } catch (java.io.IOException ex) {
            }
/*
 *          if (imageLoaded) {
        	splashScreen = new Alert("", "", splashScreen, AlertType.INFO);
        	splashScreen.setTimeout(Alert.FOREVER);
        }
        else {
        	splashScreen = new Alert("No resources", "", splashScreen, AlertType.INFO);
        	splashScreen.setTimeout(Alert.FOREVER);
        }
*/
        	splashScreen = new Form("Sudoku");
        	splashScreen.append(new ImageItem("",splashImg, Item.LAYOUT_CENTER, "No Resource available"));

        	splashScreen.addCommand(CMD_EXIT);
            splashScreen.addCommand(CMD_OPTION);            	
            splashScreen.addCommand(CMD_RESET);
            splashScreen.addCommand(CMD_PLAY);

            splashScreen.setCommandListener(this);
   
            // set up options screen
            options = new Options( this);

            // set up the table for sudoku
            table = new Table("", this);
            
            // set up the game logic
            game = new GameLogic(this);
            
            firstTime = false;
        }

        display.setCurrent(splashScreen);
    }

    public void commandAction(Command c, Displayable d) {
        if (d == splashScreen) {
        	if (c == CMD_EXIT) {
	            destroyApp(false);
	            notifyDestroyed();
        	}
	        else if (c == CMD_RESET) {
	        	display.setCurrent(table);
	        	table.setCommands(true);	// sto resettando per inserire i vincoli prima dellarisluzione automatica
	        	game.Risolvi();
	        }
	        else if (c == CMD_PLAY) {
	           	display.setCurrent(table);
	           	table.setCommands(false);	// posso inserire solo valori (NON vincoli)
	           	game.NewGame();
	        }
	        else if (c == CMD_OPTION) {
	        	display.setCurrent(options); 
	        }
        }
    }

    public void setPunteggio(int totale) {

		try {
	    	if (rsPunti == null) {
	    		rsPunti = RecordStore.openRecordStore("Sudoku", true);
	    	}
			if (rsPunti != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream outputStream = new DataOutputStream(baos);
			    outputStream.writeInt(totale);
				// Extract the byte array
				byte[] buf = baos.toByteArray();
				// Add it to the record store
				rsPunti.setRecord(1, buf, 0, buf.length);
			}
		}
		catch (EOFException eofe) {
		    eofe.printStackTrace();
		}
		catch (IOException eofe) {
		    eofe.printStackTrace();
		}
		catch(RecordStoreNotOpenException e) {
			e.printStackTrace();
		}
		catch(RecordStoreException e) {
			e.printStackTrace();
		}
    }

    public void initPunteggio() {
    	int start = 0;
		try {
	    	if (rsPunti == null) {
	    		rsPunti = RecordStore.openRecordStore("Sudoku", true);
	    	}
			if (rsPunti != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream outputStream = new DataOutputStream(baos);
			    outputStream.writeInt(start);
				// Extract the byte array
				byte[] buf = baos.toByteArray();
				// Add it to the record store
				rsPunti.addRecord( buf, 0, buf.length);
			}
		}
		catch (EOFException eofe) {
		    eofe.printStackTrace();
		}
		catch (IOException eofe) {
		    eofe.printStackTrace();
		}
		catch(RecordStoreNotOpenException e) {
			e.printStackTrace();
		}
		catch(RecordStoreException e) {
			e.printStackTrace();
		}
    }

    public int getPunteggio() {
    	int prevPunteggio = 0;
    	byte[] buf = new byte[100];

		try {
	    	if (rsPunti == null) {
	    		rsPunti = RecordStore.openRecordStore("Sudoku", true);
	    	}
			if (rsPunti != null) {
				buf = rsPunti.getRecord(1);	// solo un record
				ByteArrayInputStream bais = new ByteArrayInputStream(buf);
				DataInputStream inputStream = new DataInputStream(bais);
				prevPunteggio = inputStream.readInt();
			}
		}
		catch (InvalidRecordIDException e) {
		    e.printStackTrace();
		    System.out.println("creating record 1");
		    initPunteggio();
		}
		catch (EOFException eofe) {
		    eofe.printStackTrace();
		}
		catch (IOException eofe) {
		    eofe.printStackTrace();
		}
		catch(RecordStoreNotOpenException e) {
			e.printStackTrace();
		}
		catch(RecordStoreException e) {
			e.printStackTrace();
		}
		return prevPunteggio;
    }

    
    
    protected void destroyApp(boolean unconditional) {
    	setPunteggio(TotalePunti + game.getPunti());
		try {
			rsPunti.closeRecordStore();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
    }


    protected void pauseApp() {
    }

	public Display getDisplay() {
		return display;
	}

	public GameLogic getGame() {
		return game;
	}

	public Options getOptions() {
		return options;
	}

	public Table getTable() {
		return table;
	}

	public Form getSplashScreen() {
		return splashScreen;
	}

	public int getTotalePunti() {
		return TotalePunti;
	}
}
