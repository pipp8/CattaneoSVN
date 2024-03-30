package sample.classschedule;

public class SchoolClass {
	private String title;
	private int startTime;
	private int endTime;
	private int day;
	
	public SchoolClass(String title, int startTime, int endTime, int day)
	{
		this.title = title;
		this.startTime = startTime;
		this.endTime = endTime;
		this.day = day;
	}

	public String getTitle() {
		return title;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public int getDay() {
		return day;
	}
}
