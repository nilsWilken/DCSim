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
public class SLA {
	
	private int mips; //million instruction per second (CPU)
	private int pes; //cores 
	private int ram; //ram in byte
	private int bw; //bandwidth in bit
	private int hdd; //harddiskspace in byte
	private int availability; //0 == low; 1 == medium; 2 == high
	private int startupTime; //in seconds
	
	
	public SLA(int mips, int pes, int ram, int bw, int hdd, int availability,
			int startupTime) {
		super();
		this.mips = mips;
		this.pes = pes;
		this.ram = ram;
		this.bw = bw;
		this.hdd = hdd;
		this.availability = availability;
		this.startupTime = startupTime;
	}
	
	public int getMips() {
		return mips;
	}


	public void setMips(int mips) {
		this.mips = mips;
	}


	public int getPes() {
		return pes;
	}


	public void setPes(int pes) {
		this.pes = pes;
	}


	public int getRam() {
		return ram;
	}


	public void setRam(int ram) {
		this.ram = ram;
	}


	public int getBw() {
		return bw;
	}


	public void setBw(int bw) {
		this.bw = bw;
	}


	public int getHdd() {
		return hdd;
	}


	public void setHdd(int hdd) {
		this.hdd = hdd;
	}


	public int getAvailability() {
		return availability;
	}


	public void setAvailability(int availability) {
		this.availability = availability;
	}


	public int getStartupTime() {
		return startupTime;
	}


	public void setStartupTime(int startupTime) {
		this.startupTime = startupTime;
	}

}
