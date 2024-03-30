/*
 * $Header: /cygdrive/C/CVSNT/cvsroot/Cattaneo/Sources/J2ME/SudokuME/src/sudoku/GameLogic.java,v 1.2 2007-09-09 18:08:15 cattaneo Exp $
 */

package sudoku;

import java.util.Random;
import java.util.Vector;

public class GameLogic {

	private int[][] campo;
	private int[][] constraints;
	private int[][] correct;
	boolean [][] suggested;
	private boolean fine;
	private Random rn; 
	private Sudoku parent;
	private Table table;
	private int punti;
	private boolean automatic = false;
	
    public GameLogic( Sudoku s)  {
    	parent = s;
    	table = s.getTable();
        campo = new int[9][9];
        constraints = new int[9][9];
        correct = new int[9][9];
        suggested = new boolean[9][9];
        table.setGame(this);
        punti = 0;
    	rn = new Random();
    	rn.setSeed(System.currentTimeMillis());
        
        // Reset();
    }

    public void Start() {
    	PrintConstraints();
        // copia i vincoli
    	for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
            	campo[i][j] = constraints[i][j];
            }
        }

        fine = false;
        if (FillSudoku(0,0))
            PrintResult();
        else
            System.out.println("Nessuna Soluzione ammissibile");
    }
    
    // restituisce vero sse è stato aggiunto un nuovo numero falso se fallisce
    private boolean FillSudoku(int x, int y) {
        boolean ok = false;
        int nx, ny;
        if (x == 9)
                return true;

        // calcola la prossima casella da riempire
        if (y == 8 || fine == true) {
            ny = 0; nx = x + 1; // inizia una nuova riga
        }
        else {
            ny = y + 1; nx = x; // continua sulla stessa riga
        }

        if (constraints[x][y] != 0) {
            campo[x][ y] = constraints[x][ y];    // già fatto
            if (verifyConstraints(x, y) == true)
                return FillSudoku(nx, ny);
            else
                return false;   // occorre rivedere gli altri piazzamenti
        }
        else {   
       		while (campo[x][y] < 9) {
                campo[x][y]++;	// cerca la prossima soluzione (per default parte da 0
                if (verifyConstraints(x, y) == true) {
                    ok = FillSudoku(nx, ny);
                    if (ok)
                        return true; // tutti i figli sono stati piazzati
                }
            }
//            Application.DoEvents(); // processa i messaggi nella coda
            campo[x][y] = 0;         // resetta la condizione della cella per le prossime ricerche
            return false; 		    // nessuna soluzione e' stata trovata (campo[x][y] > 9)
        }
    }

    private boolean verifyConstraints(int x, int y)
    {
        // System.out.println("Checking: " + campo[x][y] + " in [" +x+"]["+y+"]");
        return checkRow(x, y) && checkCol(x, y) && checkSquare(x, y);
    }

    private boolean checkRow(int x, int y)
    {
        for (int i = 0; i < 9; i++) // fino a 9 e non fino y per tenere conto anche dei vincoli
        {
            if (i != y && campo[x][i] == campo[x][y])
                return false; // è già stato inserito
        }
        return true;
    }
    
    private boolean checkCol(int x, int y)
    {
        for (int i = 0; i < 9; i++) // fino a 9 e non fino y per tenere conto anche dei vincoli
        {
            if (i != x && campo[i][y] == campo[x][y])
                return false; // è già stato inserito
        }
        return true;
    }
    private boolean checkSquare(int x, int y)
    {
        int r1 = x - x % 3;
        int r2 = r1 + 3;
        int c1 = y - y % 3;
        int c2 = c1 + 3;

        for (int i = r1; i < r2; i++)
        {
            for (int j = c1; j < c2; j++)
            {
                if (campo[i][j] == campo[x][y])	// è già stato inserito
                    if ((i != x) || (j != y))	// se non e' la stessa locazione che stiamo verificando 
                        return false; 			// allora il test fallisce
            }
        }
        return true;
    }

    public void PrintConstraints() {
    	System.out.println("Vincoli:");
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
            	table.setData(String.valueOf(campo[i][j]), i,j);
                System.out.print(constraints[i][j] + " ");
            }
            System.out.println();
        }
        table.refresh();
    }

    public void PrintCampo() {
    	System.out.println("Valori");
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                System.out.print(campo[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public void PrintResult() {
        PrintCampo();
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
            	table.setData(String.valueOf(campo[i][j]), i,j);
            }
        }
        table.refresh();
    }

    public void Reset() {
        for (int i = 0; i < 9; i++)  {
            for (int j = 0; j < 9; j++)  {
                campo[i][ j] = 0;
                constraints[i][ j] = 0;
                table.setData("", i, j);
                suggested[i][j] = false;
            }
        }
        table.refresh();
    }
    
    public void Risolvi() {
    	automatic = true;
    	Reset();
    }

    public void NewGame() {
    	automatic = false; 	// flag per indicare se sta in modalità automatica (true) o manuale (false)
    	Vector v = new Vector();
    	for(int i = 1; i < 10; i++)
    		v.addElement(new Integer(i));
    	
    	Reset();
    	punti = 1000;
    	for(int i = 0; i < 9; i++) {
    		int col = rn.nextInt(9);
    		int ndx = rn.nextInt(v.size());
    		constraints[i][col] = ((Integer) v.elementAt(ndx)).intValue();
    		v.removeElementAt(ndx);
    		campo[i][col] = constraints[i][col];
    		if (!verifyConstraints(i, col))
    			System.out.println("Errore nella creazione del campo");
    	}
    	PrintConstraints();
        fine = false;
        if (FillRandomSudoku(0,0))
            PrintResult();
        else
            System.out.println("Nessuna Soluzione ammissibile");
        try {
        	Thread.sleep(2000);
        }catch (Exception e) {}
        
        selectConstraints();
    }

    
    // restituisce vero sse è stato aggiunto un nuovo numero falso se fallisce
    private boolean FillRandomSudoku(int x, int y) {
        int nx, ny, ndx;
        if (x == 9)
                return true;

        // calcola la prossima casella da riempire
        if (y == 8 || fine == true) {
            ny = 0; nx = x + 1; // inizia una nuova riga
        }
        else {
            ny = y + 1; nx = x; // continua sulla stessa riga
        }

        if (constraints[x][y] != 0)  {
            campo[x][ y] = constraints[x][ y];    // già fatto
            if (verifyConstraints(x, y) == true)
                return FillSudoku(nx, ny);
            else
                return false;   // occorre rivedere gli altri piazzamenti
        }
        else {
        	Vector v = new Vector();
        	for(int i = 1; i < 10; i++)
        		v.addElement(new Integer(i));
        	
        	for(ndx = 0; ndx < 9; ndx++) {
        		int pos = rn.nextInt(v.size());
                campo[x][y] = ((Integer) v.elementAt( pos)).intValue();	// cerca la prossima soluzione (per default parte da 0
                v.removeElementAt(pos);
                if (verifyConstraints(x, y) == true) {
                    if (FillRandomSudoku(nx, ny))
                        return true; // tutti i figli sono stati piazzati
                }
            }
//            Application.DoEvents(); // processa i messaggi nella coda
            campo[x][y] = 0;         // resetta la condizione della cella per le prossime ricerche
            return false; 		    // nessuna soluzione e' stata trovata (campo[x][y] > 9)
        }
    }

    private void selectConstraints() {
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                constraints[i][ j] = 0;
        
        switch (parent.getOptions().getLevel()) {
        case 0:	// Livello Principiante
        	setConstraints_lev0();
        	break;
        	
        case 1:	// Livello Semplice

        	setConstraints_lev1();
        	break;

        case 2:	// Livello Medio
        	setConstraints_lev2();
        	break;

        case 3:	// Livello  Avanzato

        	setConstraints_lev3();
        	break;

        case 4:	// Livello Molto difficile
        	setConstraints_lev4();
        	break;

        case 5:  // Livello Improbabile
        	setConstraints_lev5();
        	break;

        case 6:  // Livello libero (nessun vincolo preimpostato)
        	setConstraints_lev6();
        	break;
        }
    
        
        UpdateData();
    }
    
    private void UpdateData() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
            	correct[i][j] = campo[i][j];
            	campo[i][j] = 0;
            	if (constraints[i][j] == 0)
            		table.setData("", i,j);
            	else {
            		table.setData(String.valueOf(constraints[i][j]), i,j);
            		campo[i][j] = constraints[i][j];
            	}
            }
        }        
        table.refresh();    	
    }
    
    public boolean[] getPossibleValues(int i, int j) {
    	boolean[] res = new boolean[10];
		int save = campo[i][j];
		for(int x = 1; x < 10; x++) {
			campo[i][j] = x;
			res[x] = verifyConstraints(i, j);
		}
		campo[i][j] = save;
    	return res;
    }
    
    public boolean isConstraint(int i, int j) {
    	return ((constraints[i][j] != 0) ? true : false);
    }

    
    public boolean isCorrect(int val, int i, int j) {
    	return ((campo[i][j] == val) ? true : false);
    }
    
    public boolean setConstraint(int val, int i, int j) {
    	int save = campo[i][j];
    	campo[i][j] = val;
    	if (verifyConstraints(i, j)) {
    		constraints[i][j] = val;
    		return true;
    	}
    	else {
    		campo[i][j] = save;
    		return false;
    	}
    }

    public void setValue(int val, int i, int j) {
    	campo[i][j] = val;
    }

    // produce uno schema casuale con un numero di vincoli pari a n.
    private void randomSchemas(int n) {
    	int i;
    	Vector v = new Vector(81);
    	for(i = 0; i < 81; i++)
    		v.addElement(new Integer(i));
    	
    	for(i = 0; i < n; i++) {
    		int ndx = rn.nextInt( v.size());
    		int pos = ((Integer) v.elementAt(ndx)).intValue();
    		int col = pos % 9;
    		int row = pos / 9;
    		constraints[row][col] = campo[row][col];
    	}
    }
    
    // Livello Principiante
    private void setConstraints_lev0() {
    	randomSchemas(46);
    	punti = punti - 46 * 10;
    }

	// Livello Semplice
    private void setConstraints_lev1() {
    	randomSchemas(40);
    	punti = punti - 40 * 10;
    }

    private void setConstraints_lev2() {
    	switch (rn.nextInt(4)) {
    	case 0:	setConstraints_lev21(); break;

    	case 1:	setConstraints_lev22(); break;
    	
    	case 2:	setConstraints_lev23(); break;
    	
    	case 3:	setConstraints_lev24(); break;
    	}
    }
    
	// Livello Medio 34 vincoli predefiniti
    private void setConstraints_lev21() {
	    constraints[0][6] = campo[0][6];
	    constraints[1][1] = campo[1][1];
	    constraints[1][2] = campo[1][2];
	    constraints[1][4] = campo[1][4];
	    constraints[1][5] = campo[1][5];
	    constraints[1][7] = campo[1][7];
	    constraints[2][1] = campo[2][1];
	    constraints[2][2] = campo[2][2];
	    constraints[2][6] = campo[2][6];
	    constraints[2][7] = campo[2][7];
	    constraints[3][2] = campo[3][2];
	    constraints[3][3] = campo[3][3];
	    constraints[3][4] = campo[3][4];
	    constraints[3][7] = campo[3][7];
	    constraints[4][0] = campo[4][0];
	    constraints[4][1] = campo[4][1];
	    constraints[4][2] = campo[4][2];
	    constraints[4][6] = campo[4][6];
	    constraints[4][7] = campo[4][7];
	    constraints[4][8] = campo[4][8];
	    constraints[5][1] = campo[5][1];
	    constraints[5][4] = campo[5][4];
	    constraints[5][5] = campo[5][5];
	    constraints[5][6] = campo[5][6];
	    constraints[6][1] = campo[6][1];
	    constraints[6][2] = campo[6][2];
	    constraints[6][6] = campo[6][6];
	    constraints[6][7] = campo[6][7];
	    constraints[7][1] = campo[7][1];
	    constraints[7][3] = campo[7][3];
	    constraints[7][4] = campo[7][4];
	    constraints[7][6] = campo[7][6];
	    constraints[7][7] = campo[7][7];
	    constraints[8][2] = campo[8][2];
    	punti = punti - 34 * 10;
    }

	// Livello Medio 33 vincoli predefiniti
    private void setConstraints_lev22() {
	    constraints[0][0] = campo[0][0];
	    constraints[0][1] = campo[0][1];
	    constraints[0][3] = campo[0][3];
	    constraints[0][5] = campo[0][5];
	    constraints[0][8] = campo[0][8];
	    constraints[1][0] = campo[1][0];
	    constraints[1][2] = campo[1][2];
	    constraints[1][6] = campo[1][6];
	    constraints[2][1] = campo[2][1];
	    constraints[2][7] = campo[2][7];
	    constraints[3][0] = campo[3][0];
	    constraints[3][1] = campo[3][1];
	    constraints[3][4] = campo[3][4];
	    constraints[3][7] = campo[3][7];
	    constraints[4][1] = campo[4][1];
	    constraints[4][2] = campo[4][2];
	    constraints[4][4] = campo[4][4];
	    constraints[4][6] = campo[4][6];
	    constraints[4][7] = campo[4][7];
	    constraints[5][1] = campo[5][1];
	    constraints[5][4] = campo[5][4];
	    constraints[5][7] = campo[5][7];
	    constraints[5][8] = campo[5][8];
	    constraints[6][1] = campo[6][1];
	    constraints[6][7] = campo[6][7];
	    constraints[7][2] = campo[7][2];
	    constraints[7][6] = campo[7][6];
	    constraints[7][8] = campo[7][8];
	    constraints[8][0] = campo[8][0];
	    constraints[8][3] = campo[8][3];
	    constraints[8][5] = campo[8][5];
	    constraints[8][7] = campo[8][7];
	    constraints[8][8] = campo[8][8];
    	punti = punti - 33 * 10;
    }

	// Livello Medio 32 vincoli predefiniti
    private void setConstraints_lev23() {
	    constraints[0][0] = campo[0][0];
	    constraints[0][3] = campo[0][3];
	    constraints[0][4] = campo[0][4];
	    constraints[0][7] = campo[0][7];
	    constraints[1][2] = campo[1][2];
	    constraints[1][4] = campo[1][4];
	    constraints[1][6] = campo[1][6];
	    constraints[1][8] = campo[1][8];
	    constraints[2][2] = campo[2][2];
	    constraints[2][3] = campo[2][3];
	    constraints[2][7] = campo[2][7];
	    constraints[3][0] = campo[3][0];
	    constraints[3][4] = campo[3][4];
	    constraints[3][5] = campo[3][5];
	    constraints[3][6] = campo[3][6];
	    constraints[3][7] = campo[3][7];
	    constraints[5][1] = campo[5][1];
	    constraints[5][2] = campo[5][2];
	    constraints[5][3] = campo[5][3];
	    constraints[5][4] = campo[5][4];
	    constraints[5][8] = campo[5][8];
	    constraints[6][1] = campo[6][1];
	    constraints[6][4] = campo[6][4];
	    constraints[6][6] = campo[6][6];
	    constraints[7][0] = campo[7][0];
	    constraints[7][2] = campo[7][2];
	    constraints[7][4] = campo[7][4];
	    constraints[7][6] = campo[7][6];
	    constraints[8][1] = campo[8][1];
	    constraints[8][4] = campo[8][4];
	    constraints[8][5] = campo[8][5];
	    constraints[8][8] = campo[8][8];
    	punti = punti - 32 * 10;
    }

	// Livello Medio 34 vincoli predefiniti
    private void setConstraints_lev24() {
	    constraints[0][2] = campo[0][2];
	    constraints[0][4] = campo[0][4];
	    constraints[0][7] = campo[0][7];
	    constraints[0][8] = campo[0][8];
	    constraints[1][0] = campo[1][0];
	    constraints[1][2] = campo[1][2];
	    constraints[1][6] = campo[1][6];
	    constraints[1][8] = campo[1][8];
	    constraints[2][0] = campo[2][0];
	    constraints[2][5] = campo[2][5];
	    constraints[2][7] = campo[2][7];
	    constraints[3][0] = campo[3][0];
	    constraints[3][2] = campo[3][2];
	    constraints[3][4] = campo[3][4];
	    constraints[3][6] = campo[3][6];
	    constraints[3][8] = campo[3][8];
	    constraints[4][3] = campo[4][3];
	    constraints[4][5] = campo[4][5];
	    constraints[5][0] = campo[5][0];
	    constraints[5][2] = campo[5][2];
	    constraints[5][4] = campo[5][4];
	    constraints[5][6] = campo[5][6];
	    constraints[5][8] = campo[5][8];
	    constraints[6][1] = campo[6][1];
	    constraints[6][3] = campo[6][3];
	    constraints[6][8] = campo[6][8];
	    constraints[7][0] = campo[7][0];
	    constraints[7][2] = campo[7][2];
	    constraints[7][6] = campo[7][6];
	    constraints[7][8] = campo[7][8];
	    constraints[8][0] = campo[8][1];
	    constraints[8][1] = campo[8][1];
	    constraints[8][4] = campo[8][4];
	    constraints[8][6] = campo[8][6];
    	punti = punti - 34 * 10;
    }

    private void setConstraints_lev3() {
    	switch (rn.nextInt(4)) {
    	case 0:	setConstraints_lev31(); break;

    	case 1:	setConstraints_lev32(); break;
    	
    	case 2:	setConstraints_lev33(); break;
    	
    	case 3:	setConstraints_lev34(); break;
    	}
    }
    
	// Livello  Avanzato 28 vincoli predefiniti
    private void setConstraints_lev31() {
	    constraints[0][1] = campo[0][1];
	    constraints[0][4] = campo[0][4];
	    constraints[1][0] = campo[1][0];
	    constraints[1][1] = campo[1][1];
	    constraints[2][1] = campo[2][1];
	    constraints[2][4] = campo[2][4];
	    constraints[2][6] = campo[2][6];
	    constraints[3][0] = campo[3][0];
	    constraints[3][1] = campo[3][1];
	    constraints[3][3] = campo[3][3];
	    constraints[3][4] = campo[3][4];
	    constraints[3][8] = campo[3][8];
	    constraints[4][1] = campo[4][1];
	    constraints[4][4] = campo[4][4];
	    constraints[4][7] = campo[4][7];
	    constraints[5][0] = campo[5][0];
	    constraints[5][4] = campo[5][4];
	    constraints[5][5] = campo[5][5];
	    constraints[5][7] = campo[5][7];
	    constraints[5][8] = campo[5][8];
	    constraints[6][1] = campo[6][1];
	    constraints[6][2] = campo[6][2];
	    constraints[6][4] = campo[6][4];
	    constraints[6][7] = campo[6][7];
	    constraints[7][7] = campo[7][7];
	    constraints[7][8] = campo[7][8];
	    constraints[8][4] = campo[8][4];
	    constraints[8][7] = campo[8][7];
    	punti = punti - 28 * 10;
    }

	// Livello  Avanzato 27 vincoli predefiniti
    private void setConstraints_lev32() {
	    constraints[0][1] = campo[0][1];
	    constraints[0][3] = campo[0][3];
	    constraints[0][5] = campo[0][5];
	    constraints[0][7] = campo[0][7];
	    constraints[1][4] = campo[1][4];
	    constraints[2][1] = campo[2][1];
	    constraints[2][3] = campo[2][3];
	    constraints[2][7] = campo[2][7];
	    constraints[3][0] = campo[3][0];
	    constraints[3][6] = campo[3][6];
	    constraints[3][8] = campo[3][8];
	    constraints[4][1] = campo[4][1];
	    constraints[4][3] = campo[4][3];
	    constraints[4][4] = campo[4][4];
	    constraints[4][5] = campo[4][5];
	    constraints[4][7] = campo[4][7];
	    constraints[5][0] = campo[5][0];
	    constraints[5][2] = campo[5][2];
	    constraints[5][8] = campo[5][8];
	    constraints[6][1] = campo[6][1];
	    constraints[6][5] = campo[6][5];
	    constraints[6][7] = campo[6][7];
	    constraints[7][4] = campo[7][4];
	    constraints[8][1] = campo[8][1];
	    constraints[8][3] = campo[8][3];
	    constraints[8][5] = campo[8][5];
	    constraints[8][7] = campo[8][7];
    	punti = punti - 27 * 10;
    }

	// Livello  Avanzato 26 vincoli predefiniti
    private void setConstraints_lev33() {
	    constraints[0][5] = campo[0][5];
	    constraints[1][1] = campo[1][1];
	    constraints[1][2] = campo[1][2];
	    constraints[1][3] = campo[1][3];
	    constraints[1][5] = campo[1][5];
	    constraints[1][6] = campo[1][6];
	    constraints[1][7] = campo[1][7];
	    constraints[2][2] = campo[2][2];
	    constraints[2][6] = campo[2][6];
	    constraints[3][0] = campo[3][0];
	    constraints[3][1] = campo[3][1];
	    constraints[3][4] = campo[3][4];
	    constraints[3][7] = campo[3][7];
	    constraints[5][1] = campo[5][0];
	    constraints[5][4] = campo[5][4];
	    constraints[5][7] = campo[5][7];
	    constraints[5][8] = campo[5][8];
	    constraints[6][2] = campo[6][2];
	    constraints[6][6] = campo[6][6];
	    constraints[7][1] = campo[7][1];
	    constraints[7][2] = campo[7][2];
	    constraints[7][3] = campo[7][3];
	    constraints[7][5] = campo[7][5];
	    constraints[7][6] = campo[7][6];
	    constraints[7][7] = campo[7][7];
	    constraints[8][3] = campo[8][3];
    	punti = punti - 26 * 10;
    }

	// Livello  Avanzato 27 vincoli predefiniti
    private void setConstraints_lev34() {
	    constraints[0][5] = campo[0][5];
	    constraints[0][8] = campo[0][8];
	    constraints[1][0] = campo[1][0];
	    constraints[1][2] = campo[1][2];
	    constraints[1][7] = campo[1][7];
	    constraints[2][3] = campo[2][3];
	    constraints[2][6] = campo[2][6];
	    constraints[2][7] = campo[2][7];
	    constraints[2][8] = campo[2][8];
	    constraints[3][3] = campo[3][3];
	    constraints[3][4] = campo[3][4];
	    constraints[3][7] = campo[3][7];
	    constraints[4][0] = campo[4][0];
	    constraints[4][4] = campo[4][4];
	    constraints[4][8] = campo[4][8];
	    constraints[5][1] = campo[5][1];
	    constraints[5][4] = campo[5][4];
	    constraints[5][5] = campo[5][5];
	    constraints[6][0] = campo[6][0];
	    constraints[6][1] = campo[6][1];
	    constraints[6][2] = campo[6][2];
	    constraints[6][5] = campo[6][5];
	    constraints[7][1] = campo[7][1];
	    constraints[7][6] = campo[7][6];
	    constraints[7][8] = campo[7][8];
	    constraints[8][0] = campo[8][0];
	    constraints[8][3] = campo[8][3];
    	punti = punti - 27 * 10;
    }

	// Livello Molto difficile 24 vinoli predefiniti
	private void setConstraints_lev4() {
		randomSchemas(24);
    	punti = punti - 24 * 10;
	}
    
	// Livello Improbabile
    private void setConstraints_lev5() {
    	randomSchemas(20);
    	punti = punti - 20 * 10;
    }

	// Livello schema libero
    private void setConstraints_lev6() {
    	// nessun vincolo preimpostato ... l'utente potrà scegliere le locazioni che preferisce
    }

	public int getCorrect(int i, int j) {
		suggested[i][j] = true;
		punti = punti - 50;
		return correct[i][j];
	}

	public boolean isSuggested(int i, int j) {
		return suggested[i][j];
	}

	public boolean isAutomatic() {
		return automatic;
	}

	public int getPunti() {
		return punti;
	}
}