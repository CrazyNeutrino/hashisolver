package org.meb.hashi.engine;

public class StepsCounter {
	
	private int stepsLimit = 0;
	private int stepsCount = 0;
	
	public StepsCounter() {
	}
	
	public StepsCounter(int stepsLimit) {
		this.stepsLimit = stepsLimit;
	}

	public int getStepsLimit() {
		return stepsLimit;
	}
	
	public int getStepsCount() {
		return stepsCount;
	}

	public StepsCounter increase() {
		stepsCount++;
		return this; 
	}
	
	public boolean isLimitReached() {
		return stepsLimit > 0 && stepsCount >= stepsLimit;
	}
}
