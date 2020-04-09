package de.uma.dcsim.frequencyTimeSeriesCompletion;

import java.util.Date;

import de.uma.dcsim.utilities.Constants;

public class FileEntry {
	
	private Date time;
	private double frequency;
	private double shiftingFraction;
	
	public FileEntry(Date time, double frequency, double shiftingFraction) {
		this.time = time;
		this.frequency = frequency;
		this.shiftingFraction = shiftingFraction;
	}
	
	public Date getTime() {
		return this.time;
	}
	
	public double getFrequency() {
		return this.frequency;
	}
	
	public double getShiftingFraction() {
		return this.shiftingFraction;
	}
	
	public String toString() {
		return Constants.getDateFormat().format(this.time) + Constants.CSV_SEPARATOR + (""+this.frequency).replace(".", Constants.DECIMAL_SEPARATOR) + Constants.CSV_SEPARATOR + (""+this.shiftingFraction).replace(".", Constants.DECIMAL_SEPARATOR);
	}

}
