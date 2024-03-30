package stateful;

import java.util.ArrayList;

public interface Calculator {

	  public double calculate (int start, int end, 
	                  double growthrate, double saving);

	  public ArrayList <Integer> getStarts ();
	  public ArrayList <Integer> getEnds ();
	  public ArrayList <Double> getGrowthrates ();
	  public ArrayList <Double> getSavings ();
	  public ArrayList <Double> getResults ();

}
