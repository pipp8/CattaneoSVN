package sessionstateless;

import javax.ejb.Stateless;
import javax.ejb.Local;
import javax.ejb.Remote;

@Stateless(name="CalculatorBean")
@Local ({LocalCalculator.class})
@Remote ({RemoteCalculator.class})
public class CalculatorBean implements LocalCalculator, RemoteCalculator {
	public double calculate (int start, int end, double growthrate, double saving) {
		double tmp = Math.pow(1. + growthrate / 12., 12. * (end - start) + 1);
		return saving * 12. * (tmp - 1) / growthrate;
	}
}
