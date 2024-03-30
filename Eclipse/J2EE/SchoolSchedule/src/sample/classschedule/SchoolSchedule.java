package sample.classschedule;

import java.util.ArrayList;
import java.util.List;

public class SchoolSchedule {

		private List classes = new ArrayList();

		public void addClass( SchoolClass cl) {
			classes.add(cl);
		}
		public List getClasses() {
			return classes;
		}
		
}
