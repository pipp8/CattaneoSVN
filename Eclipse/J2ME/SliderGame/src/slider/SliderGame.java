/*
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package slider;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;


/**
 *
 * @version 2.0
 */
public class SliderGame extends MIDlet implements CommandListener {
    private static final Command CMD_EXIT = new Command("Esci", Command.EXIT, 10);
    private static final Command CMD_START = new Command("Start", Command.ITEM, 10);
    private Display display;
    private GameLogic game;
    private Table table;
    private StringItem label;
    private boolean firstTime;
    private Form mainForm;

    public SliderGame() {
        firstTime = true;
        mainForm = new Form("Slider Game");
    }

    protected void startApp() {
        if (firstTime) {
            display = Display.getDisplay(this);

 
            table = new Table("", this);
            mainForm.append(table);

            label = new StringItem("# mosse: ", "");
            label.setPreferredSize( mainForm.getWidth() - 10, 15);
            label.setLayout(Item.LAYOUT_NEWLINE_BEFORE);
            mainForm.append(label);

            mainForm.addCommand(CMD_EXIT);
            mainForm.addCommand(CMD_START);
            
            mainForm.setCommandListener(this);

            game = new GameLogic(this);
            
            firstTime = false;
        }
        display.setCurrent(mainForm);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == CMD_EXIT) {
            destroyApp(false);
            notifyDestroyed();
        }
        if (c == CMD_START) {
            game.initialize();
        }
    }

    protected void destroyApp(boolean unconditional) {
    }

    protected void pauseApp() {
    }

	public Display getDisplay() {
		return display;
	}

	public Form getMainForm() {
		return mainForm;
	}

	public Table getTable() {
		return table;
	}

	public StringItem getLabel() {
		return label;
	}
}
