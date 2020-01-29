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

import powerModels.PowerModelSelector;

/**
 * @author University of Mannheim
 * This class represents a HVAC system in the simulation framework.
 */
public class HVAC {

	/**
	 * Current building temperature setpoint of the HVAC system.
	 */
	private double currentTemperatureSetpoint;
	
	/**
	 * Current building temperature.
	 */
	private double currentTemperature;
	
	/**
	 * Current power consumption  of the HVAC system.
	 */
	private double currentPC;
	
	/**
	 * Current utilization of the HVAC system.
	 */
	private double currentUtil;
	
	
	// the minimal temperature that can be achieved with a 100% hvac util:
	/**
	 * Minimal building temperature that can be reached.
	 */
	private final double minTemp = 5;
	
	/**
	 * Fixed delta temperature that indicates the change of the building temperature per timestep, when the current
	 * temperature setpoint does not match the current building temperature.
	 */
	private final double temperatureChangePerTimeInterval = 0.5; //simple temperature model

	public HVAC(double temp) {
		this.currentTemperature = temp;
		this.currentTemperatureSetpoint = temp;
	}
	
	/**
	 * Provides a deep copy of this HVAC system instance.
	 * @return Deep copy of the HVAC instance on which the method is called.
	 */
	public HVAC deepCopy() {
		HVAC copy = new HVAC(currentTemperatureSetpoint);
		return copy;
	}
	
	
	/**
	 * Updates the current buliding temperature, the current utilization of the HVAC system, and the current power consumption of the HVAC system.
	 * Uses the PowerModelSelector class to retrieve the current HVAC power consumption.
	 * @param overallServerUtil Current utilization of all servers in the DC to which this HVAC system belongs.
	 * @param itPowerConsumption Current IT power consumption of the DC to which this HVAC system belongs.
	 * @param pue Current PUE of the DC to which this HVAC system belongs.
	 */
	public void update(double overallServerUtil, int itPowerConsumption, double pue) {
		if (currentTemperature < currentTemperatureSetpoint) {
			currentTemperature += temperatureChangePerTimeInterval;
			if (currentTemperature > currentTemperatureSetpoint)
				currentTemperature = currentTemperatureSetpoint;
		} else if (currentTemperature > currentTemperatureSetpoint) {
			currentTemperature -= temperatureChangePerTimeInterval;
			if (currentTemperature < currentTemperatureSetpoint)
				currentTemperature = currentTemperatureSetpoint;
		}
		currentUtil = (getUtilizationForGivenServerUtil(overallServerUtil));

//		currentEC = (overallServerEC * (pue - 1) * (1 - ((currentTemperature + minTemp) / 100.0)));

		this.currentPC = PowerModelSelector.getHvacPower(pue, itPowerConsumption);

	}

	public double getCurrentTemperature() {
		return currentTemperature;
	}

	public void setCurrentPC(int currentPC) {
		this.currentPC = currentPC;
	}

	public double getCurrentPC() {
		return currentPC;
	}

	public void setCurrentUtil(int currentUtil) {
		this.currentUtil = currentUtil;
	}

	public double getCurrentUtil() {
		return currentUtil;
	}

	public void changeTemperatur(double temperature) {
		this.currentTemperatureSetpoint = temperature;
	}
	
	private double getUtilizationForGivenServerUtil(double overallServerUtil) {
		return overallServerUtil * (1 - ((currentTemperature + minTemp) / 100));
	}

}
