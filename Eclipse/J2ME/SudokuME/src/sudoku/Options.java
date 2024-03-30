/*
 * $Header: /cygdrive/C/CVSNT/cvsroot/Cattaneo/Sources/J2ME/SudokuME/src/sudoku/Options.java,v 1.2 2007-09-09 18:08:15 cattaneo Exp $
 */

package sudoku;

import javax.microedition.lcdui.*;


public class Options extends Form implements CommandListener {
	private int level;
	
	private Command CMD_OK;
	private Command CMD_CANCEL;
	private Display dpy;
	private Displayable prev;
	private Sudoku parent;
	private ChoiceGroup cg1;
	private ChoiceGroup cg2;

    Options( Sudoku parent) {
        super("Options");
        this.parent = parent;
        dpy = parent.getDisplay();
        prev = parent.getSplashScreen();

        // set up default values
        level = 2;

        cg2 = new ChoiceGroup("Livello di gioco", Choice.EXCLUSIVE);
        cg2.append("Principiante", null);
        cg2.append("Semplice", null);
        cg2.append("Medio", null);
        cg2.append("Avanzato", null);
        cg2.append("Molto difficile", null);
        cg2.append("Improbabile", null);
        cg2.append("libero", null);
        cg2.setSelectedIndex(level, true);
        append(cg2);

        CMD_OK = new Command("OK", Command.OK, 0);
        CMD_CANCEL = new Command("Cancel", Command.CANCEL, 1);

        addCommand(CMD_OK);
        addCommand(CMD_CANCEL);
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == CMD_OK) {
        	level = cg2.getSelectedIndex();
        }
        else if (c == CMD_CANCEL) {
        	//
        }
        dpy.setCurrent(prev);
    }

	public int getLevel() {
		return level;
	}
}
