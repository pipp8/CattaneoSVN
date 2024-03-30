package slider;

import java.util.Random;
import java.util.Vector;


public class GameLogic {

	private SliderGame parent;
	private Vector correct;
	private Vector current;

	private int hiddenIndex;
	private int moves = 0;
	private boolean solved;
	
	public GameLogic(SliderGame s) {
		parent = s;
		parent.getTable().setGame(this);
		initialize();
	}
	
	public void initialize() {
		correct = new Vector(16);
		for (int i = 1; i < 16; i++) {
			correct.addElement(String.valueOf(i));
		}
		correct.addElement("");
		
		shuffle();
		// Reset lo stato iniziale
		moves = 0;
		solved = false;
	}

	/**
	 * mischia le posizioni
	 */
	private void shuffle() {
		Random rn = new Random();
		rn.setSeed(System.currentTimeMillis());
		
		Vector src = new Vector(16);
		for (int i = 1; i < 16; i++) {
			src.addElement(String.valueOf(i));
		}
		src.addElement("");
		
		current = new Vector(16);
		// Randomize the order
		for (int i = 0; i < 16; i++) {
			int ndx = rn.nextInt(src.size());
			String key = (String) src.elementAt(ndx);
			if (key.compareTo("") == 0) {
				hiddenIndex = i;
			}
			current.addElement( key);
			src.removeElementAt(ndx);
		}
		
		showCurrent();
		// Reset the status line 
		parent.getLabel().setText(String.valueOf(moves));
	}

	private void showCurrent() {
		parent.getTable().setData(current);
	}
	/**
	 * Check to see if the button at the given index can be moved
	 * @param index the button to check
	 * @return true if the button can move, false otherwise
	 */
	private boolean check(int index) {

		if (index < 0 || index >= correct.size())
			return false;

		boolean valid = false;
		// check up
		if (index > 3) {
			valid = (hiddenIndex == index - 4);
		}
		// check down
		if (valid == false && index <= 11) {
			valid = (hiddenIndex == index + 4);
		}
		// check right
		if (valid == false && (index % 4) != 3) {
			valid = (hiddenIndex == index + 1);
		}
		// check left
		if (valid == false && (index % 4) != 0) {
			valid = (hiddenIndex == index - 1);
		}
		return valid;
	}

	/**
	 * React to the pushing of the given button
	 * 
	 * @param index  The number of the button pushed
	 */
	public boolean slide(int index) {
		// Don't do anything if the puzzle is solved
		if (solved)
			return false;

		// if not a valid click return
		if (!check(index)) {
			return false;
		}

		// swap positions
		current.setElementAt(current.elementAt(index), hiddenIndex);
		current.setElementAt("", index);
		// aggiorna la posizione dello spazio libero
		hiddenIndex = index;
		showCurrent();

		// Increment the number of moves and update status
		moves++;
		parent.getLabel().setText(String.valueOf(moves));

		// if you've won
		boolean finish = true;
		for(int i = 0; i < 16; i++) {
			if (! current.elementAt(i).equals(correct.elementAt(i))) {
				finish = false;
				break;
			}
		}
		if (finish) {
			solved = true;
			// Change the buttons colors to green
			parent.getTable().refresh();
			parent.getTable().getDisplay().flashBacklight(1000);
		}

		return true;
	}

	public boolean isSolved() {
		return solved;
	}
}
