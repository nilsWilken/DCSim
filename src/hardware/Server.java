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

package hardware;

import java.util.ArrayList;
import java.util.List;

import powerModels.PowerModelSelector;
import serviceRelatedClasses.VM;
import utilities.BatchJob;
import utilities.ServerStatus;

/**
 * @author University of Mannheim
 * This class represents a server in the simulation framework.
 */
public class Server {

	/**
	 * Constant that specifies the power consumption  of a server in the idle state.
	 */
	public static final int IDLE_POWER = 49;
	
	/**
	 * Unique ID of the server.
	 */
	private int id;
	
	/**
	 * Current power consumption  of the server.
	 */
	private double currentPC;
	
	/**
	 * Current utilization of the server.
	 */
	private int currentUtil;
	
	/**
	 * List of all VMs that are assigned to the server. (Currently not used)
	 */
	private List<VM> vms;
	
	/**
	 * Current status of the server.
	 */
	private ServerStatus status;
	
	/**
	 * Currently assigned BatchJob of the server (null if the server is in the IDLE status).
	 */
	private BatchJob currentJob;
	
	/**
	 * Indicates whether a server is a deep copy of another server instance.
	 */
	private boolean isCopy;

	// -------------------------
	/**
	 * Hardware related variables (not used in the current version).
	 */
	private int mips; // million instruction per second (CPU)
	private int pes; // cores
	private int ram; // ram in byte
	private int bw; // bandwidth in bit
	private int hdd; // harddiskspace in byte

	public Server(int mips, int pes, int ram, int bw, int hdd, int id,
			List<VM> vms) {
		this.currentPC = 0;
		this.currentUtil = 0;
		this.mips = mips;
		this.pes = pes;
		this.ram = ram;
		this.bw = bw;
		this.hdd = hdd;
		this.setId(id);
		this.vms = vms;
		this.status = ServerStatus.IDLE;
		this.isCopy = false;
		this.update();
	}
	
	/**
	 * Provides a deep copy of the server.
	 * @return Deep copy instance of the Server instance on which the method is called.
	 */
	public Server deepCopy() {
		Server copy = new Server(mips, pes, ram, bw, hdd, id, new ArrayList<VM>());
		switch(status) {
		case OFF:
			copy.setStatus(ServerStatus.OFF);
			copy.update();
			break;
		case IDLE:
			copy.setStatus(ServerStatus.IDLE);
			copy.update();
			break;
		case OCCUPIED:
			copy.setStatus(ServerStatus.OCCUPIED);
			break;
		}
		copy.setIsCopy(true);
		return copy;
	}
	
	/**
	 * Updates the power consumption of the server.
	 * When the server is in the OCCUPIED status, the PowerModelSelector class is used to retrieve the current power consumption
	 * of the server in dependance on the execution frequency and the job class of the currently assigned job.
	 */
	public void update() {
		if(this.status == ServerStatus.IDLE) {
//			currentEC = this.IDLE_POWER;
			this.currentPC = 0;
			this.currentUtil = 0;
		}
		else if(this.status == ServerStatus.OFF) {
			currentPC = 0;
			currentUtil = 0;
		}
		else {
			this.currentPC = PowerModelSelector.getServerPower(this.currentJob.getFrequency(), this.currentJob.getJobClass());
		}
	}

	public List<VM> getVms() {
		return vms;
	}

	public void setVms(List<VM> vms) {
		this.vms = vms;
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

	public double getCurrentPC() {
		return currentPC;
	}

	public void setCurrentPC(int currentEC) {
		this.currentPC = currentEC;
	}

	public int getCurrentUtil() {
		return currentUtil;
	}

	public void setCurrentUtil(int currentUtil) {
		this.currentUtil = currentUtil;
	}
	
	public void setStatus(ServerStatus status) {
		this.status = status;
	}
	
	public ServerStatus getStatus() {
		return this.status;
	}
	
	public void setIsCopy(boolean isCopy) {
		this.isCopy = isCopy;
	}
	
	public boolean isCopy() {
		return this.isCopy;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
	public void setCurrentJob(BatchJob job) {
		this.currentJob = job;
	}
	
	public BatchJob getCurrentJob() {
		return this.currentJob;
	}

}
