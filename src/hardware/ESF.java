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

/**
 * @author University of Mannheim
 */

// THIS CLASS WAS NOT ADJUSTED DURING THE THESIS AND IS THUS NOT COMMENTED!
public class ESF {

	private int id;
	private int currentEC;
//	private List<Integer> ec; // energy consumption
	private List<Integer> util; // utilization in percent (e.g. 68)
	private int efficiency; // efficiency in percent (e.g. 68)
	private double chargingLevel; // in percent (e.g. 68)
	private int chargingInput; // Watt of Input when charging
	private String status; // charging, off, discharging
	private int capacity; // in Wh

	public ESF(int id, List<Integer> ec, List<Integer> util, int efficiency,
			int chargingLevel, int chargingInput, int capacity) {
		super();
		this.setId(id);
//		this.ec = ec;
		this.util = util;
		this.efficiency = efficiency;
		this.chargingLevel = chargingLevel;
		this.chargingInput = chargingInput;
		this.capacity = capacity;
		this.status = "off";
	}
	
	public ESF deepCopy() {
		ESF copy = new ESF(id, new ArrayList<Integer>(), new ArrayList<Integer>(), efficiency, (int)chargingLevel, chargingInput, capacity);
		copy.setStatus(status);
		copy.setCurrentEC(currentEC);
		return copy;
	}

	public int getCurrentUtil() {
		return this.util.get(this.util.size() - 1);
	}

	public double getChargingLevel() {
		return chargingLevel;
	}

	public void update(int overallEC, int schedulingInterval, int serverHVACUtil) {
		if (status.equals("charging")) {
//			ec.add(chargingInput);
			currentEC = chargingInput;
			double currentWhLeftInESF = capacity * chargingLevel / 100;
			currentWhLeftInESF = currentWhLeftInESF
					+ Double.valueOf(chargingInput)
					* Double.valueOf(efficiency) / 100d
					* Double.valueOf(schedulingInterval) / 3600d;
			chargingLevel = currentWhLeftInESF / capacity * 100;
//			util.add(0);
			if (chargingLevel >= 100) {
				chargingLevel = 100;
				status = "off";
			}
		} else if (status.equals("discharging")) {
//			ec.add(overallEC * -1);
			currentEC = overallEC * -1;
			double currentWhLeftInESF = capacity * chargingLevel / 100;
			currentWhLeftInESF = currentWhLeftInESF - overallEC
					* schedulingInterval / 3600d;
			chargingLevel = 100 * currentWhLeftInESF / capacity;
			if (chargingLevel <= 0) {
				chargingLevel = 0;
				status = "charging";
			}
//			util.add(serverHVACUtil);
		} else {
//			ec.add(0);
//			util.add(0);
			currentEC = 0;
		}
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setCurrentEC(int currentEC) {
		this.currentEC = currentEC;
	}

	public int getCurrentEC() {
		return currentEC;
	}

}
