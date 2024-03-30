package stateful;

import java.io.Serializable;
import java.util.ArrayList;

import javax.ejb.Stateful;
import stateful.StatefulCalculatorRemote;

@Stateful
public class StatefulCalculatorBean implements Calculator, Serializable {

	public ArrayList <Integer> starts = new ArrayList <Integer> ();
	public ArrayList <Integer> ends = new ArrayList <Integer> ();
	public ArrayList <Double> growthrates = new ArrayList <Double> ();
	public ArrayList <Double> savings = new ArrayList <Double> ();
	public ArrayList <Double> results = new ArrayList <Double> ();

	public double calculate (int start, int end, 
						double growthrate, double saving) {
	    
		double tmp = Math.pow(1. + growthrate / 12., 12. * (end - start) + 1);
		double result = saving * 12. * (tmp - 1) / growthrate;
	    
	    starts.add(Integer.valueOf(start));
	    ends.add(Integer.valueOf(end));
	    growthrates.add(Double.valueOf(growthrate));
	    savings.add(Double.valueOf(saving));
	    results.add(Double.valueOf(result));

	    return result;
	}

	public ArrayList <Integer> getStarts () {
	    return starts;
	}

	public ArrayList<Integer> getEnds() {
		return ends;
	}

	public ArrayList<Double> getGrowthrates() {
		return growthrates;
	}

	public ArrayList<Double> getSavings() {
		return savings;
	}

	public ArrayList<Double> getResults() {
		return results;
	}
}
