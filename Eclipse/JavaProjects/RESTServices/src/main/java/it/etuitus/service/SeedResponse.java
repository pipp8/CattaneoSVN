package it.etuitus.service;

public class SeedResponse {
	
	private String seed;
	
	private int seedLenght;
	
	private long currentTime;

	public SeedResponse() {
		seed = "31323334353637383930313233343536373839303132333435363738393031323334353637383930313233343536373839303132333435363738393031323334";
		seedLenght = 512;	// lunghezza in bit del seed
		currentTime = System.currentTimeMillis() / 1000; // ora corrente UTC in secondi
	}

	public String getSeed() {
		return seed;
	}

	public void setSeed(String seed) {
		this.seed = seed;
	}

	public int getSeedLenght() {
		return seedLenght;
	}

	public void setSeedLenght(int seedLenght) {
		this.seedLenght = seedLenght;
	}

	public long getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(long currentTime) {
		this.currentTime = currentTime;
	}
	
	
}
