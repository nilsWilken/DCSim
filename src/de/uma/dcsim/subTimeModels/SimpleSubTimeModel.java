package de.uma.dcsim.subTimeModels;

import java.util.Date;

public class SimpleSubTimeModel implements SubTimeModel {

	private double maxHoursAhead;
	
	public SimpleSubTimeModel(double maxHoursAhead) {
		this.maxHoursAhead = maxHoursAhead;
	}
	
	@Override
	public Date calculateSubmissionTime(Date originalStartTime) {
		int hoursAhead = (int)(Math.random()*this.maxHoursAhead);
		
		Date submissionTime = new Date();
		submissionTime.setTime(originalStartTime.getTime() - (hoursAhead*3600000));
		return submissionTime;
	}

}
