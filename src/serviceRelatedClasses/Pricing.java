/*
 * %%
 * Copyright (C) 2012 University of Mannheim - Chair of Software Engineering
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * 
 */

package serviceRelatedClasses;

/**
 * @author University of Mannheim
 *
 */
public class Pricing {
	
	private double mips = 0d;
	private double pes = 0d;
	private double ram = 0d;
	private double bw = 0d;
	private double storage = 0d;
	private double availabilityLow = 0d;
	private double availabilityMedium = 0d;
	private double availabilityHigh = 0d;
	private double startup = 0d;
	private double dcAdaption = 0d;
	
	public Pricing(double mips, double pes, double ram, double bw,
			double storage, double availabilityLow, double availabilityMedium,
			double availabilityHigh, double startup, double dcAdaption) {
		super();
		this.mips = mips;
		this.pes = pes;
		this.ram = ram;
		this.bw = bw;
		this.storage = storage;
		this.availabilityLow = availabilityLow;
		this.availabilityMedium = availabilityMedium;
		this.availabilityHigh = availabilityHigh;
		this.startup = startup;
		this.dcAdaption = dcAdaption;
	}
	
	public double getMips() {
		return mips;
	}

	public double getPes() {
		return pes;
	}

	public double getRam() {
		return ram;
	}

	public double getBw() {
		return bw;
	}

	public double getStorage() {
		return storage;
	}

	public double getAvailabilityLow() {
		return availabilityLow;
	}

	public double getAvailabilityMedium() {
		return availabilityMedium;
	}

	public double getAvailabilityHigh() {
		return availabilityHigh;
	}

	public double getStartup() {
		return startup;
	}

	public double getDcAdaption() {
		return dcAdaption;
	}

	

}
