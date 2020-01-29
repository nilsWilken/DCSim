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

import java.util.List;

/**
 * @author University of Mannheim
 *
 */
public class VM {

	private int id;
	private List<Integer> util; // utilization in percent (e.g. 68)

	// -------------------------
	private int mips; // million instruction per second (CPU)
	private int pes; // cores
	private int ram; // ram in byte
	private int bw; // bandwidth in bit
	private int hdd; // harddiskspace in byte

	// -------------------------
	private String status; // paused, running, off
	private int serverID;

	public VM(int id, List<Integer> util, int mips, int pes, int ram, int bw,
			int hdd, String status, int serverID) {
		super();
		this.id = id;
		this.setUtil(util);
		this.setMips(mips);
		this.setPes(pes);
		this.ram = ram;
		this.bw = bw;
		this.hdd = hdd;
		this.setStatus(status);
		this.serverID = serverID;
	}
	
	synchronized private List<Integer> getSetUtilization(List<Integer> util){
		if (util == null){
			return this.util;
		}
		else {
			this.util = util;
		}
		return null;
	}

	public double getUtilization(int clock) {
			return getSetUtilization(null).get(clock % getSetUtilization(null).size());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public int getServerID() {
		return serverID;
	}

	public void setServerID(int serverID) {
		this.serverID = serverID;
	}

	public void setMips(int mips) {
		this.mips = mips;
	}

	public int getMips() {
		return mips;
	}

	public void setPes(int pes) {
		this.pes = pes;
	}

	public int getPes() {
		return pes;
	}

	public void setUtil(List<Integer> util) {
		this.getSetUtilization(util);
	}

	public List<Integer> getUtil() {
		return this.getSetUtilization(null);
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

}
