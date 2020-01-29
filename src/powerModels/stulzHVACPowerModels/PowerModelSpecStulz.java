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

package powerModels.stulzHVACPowerModels;

import powerModels.PowerModelSpecHVAC;

/**
 * 
 * @author University of Mannheim
 * 
 *         This class contains the power representation of a STULZ DX HWAC
 *         System without free air cooling. All data is a number being the
 *         percentage of power consumption in relation to the IT equipment.
 * 
 */
public class PowerModelSpecStulz extends PowerModelSpecHVAC implements IStulzPowerModel {

	private double light; 
	private double lightChangeFactor;
	private double generator;
	private double generatorChangeFactor;
	private double usv;
	private double usvChangeFactor;
	private double utility;
	private double utilityChangeFactor;
	private double condensators;
	private double condensatorsChangeFactor;
	private double humidifier;
	private double humidifierChangeFactor;
	private double ac;
	private double acChangeFactor;
	private double it;
	private double maxITPower;
	private double minITPower;

	public double getMaxITPower() {
		return maxITPower;
	}

	public void setMaxITPower(double maxITPower) {
		this.maxITPower = maxITPower;
	}

	public double getMinITPower() {
		return minITPower;
	}

	public void setMinITPower(double minITPower) {
		this.minITPower = minITPower;
	}

	
	/**
	 * @param light
	 *            A fixed parameter concerning the energy consumption of light
	 *            (in percent) based on the average energy consumption of the
	 *            data center
	 * @param generator
	 *            A fixed parameter concerning the energy consumption of
	 *            generator (in percent) based on the average energy consumption
	 *            of the data center
	 * @param usv
	 *            A fixed parameter concerning the energy consumption of the USV
	 *            (in percent) based on the average energy consumption of the
	 *            data center
	 * @param utility
	 *            A fixed parameter concerning the energy consumption of the
	 *            utility system (in percent) based on the average energy
	 *            consumption of the data center
	 * @param humidifier
	 *            A fixed parameter concerning the energy consumption of the
	 *            humidifier (in percent) based on the average energy
	 *            consumption of the data center
	 * @param ac
	 *            A fixed parameter concerning the energy consumption of the AC
	 *            system itself (in percent) based on the average energy
	 *            consumption of the data center
	 * @param it
	 *            A fixed parameter concerning the energy consumption of the IT
	 *            equipment (in percent) based on the average energy consumption
	 *            of the data center
	 */
	public PowerModelSpecStulz(double light, double generator, double usv,
			double utility, double condensators, double humidifier, double ac,
			double it) {
		super();
		this.light = light;
		this.lightChangeFactor = 0.05;
		this.generator = generator;
		this.generatorChangeFactor = 0.05;
		this.usv = usv;
		this.usvChangeFactor = 0.2;
		this.utility = utility;
		this.utilityChangeFactor = 0.2;
		this.condensators = condensators;
		this.condensatorsChangeFactor = 1;
		this.humidifier = humidifier;
		this.humidifierChangeFactor = 1;
		this.ac = ac;
		this.acChangeFactor = 1;
		this.it = it;
	}
	
	public PowerModelSpecStulz(double light, double lightCF, double generator, double generatorCF, double usv, double usvCF,
			double utility, double utilityCF, double condensators, double condensatorsCF, double humidifier, double humidifierCF, double ac, double acCF,
			double it) {
		super();
		this.light = light;
		this.lightChangeFactor = lightCF;
		this.generator = generator;
		this.generatorChangeFactor = generatorCF;
		this.usv = usv;
		this.usvChangeFactor = usvCF;
		this.utility = utility;
		this.utilityChangeFactor = utilityCF;
		this.condensators = condensators;
		this.condensatorsChangeFactor = condensatorsCF;
		this.humidifier = humidifier;
		this.humidifierChangeFactor = humidifierCF;
		this.ac = ac;
		this.acChangeFactor = acCF;
		this.it = it;
	}
	

	/**
	 * Method to get the current energy consumption of the rest of the DC
	 * infrastructure with respect to a certain IT utilization rate.
	 * 
	 * @param utilizationRate
	 *            The IT utilization rate
	 * @param powerModel
	 *            Indicates whether the linear or cubic power calculation model
	 *            should be used.
	 * @return The current energy consumption of the DC WITHOUT the IT energy
	 *         consumption
	 */
	public double getAdditionalEnergyConsumption(double utilizationRate,
			String powerModel) {
		int energyConsumption = 0;
		if (powerModel.equals("linear")){
			energyConsumption += getPowerOfComponentLinear(utilizationRate,
					light, lightChangeFactor);
			energyConsumption += getPowerOfComponentLinear(utilizationRate,
					generator, generatorChangeFactor);
			energyConsumption += getPowerOfComponentLinear(utilizationRate,
					usv, usvChangeFactor);
			energyConsumption += getPowerOfComponentLinear(utilizationRate,
					utility, utilityChangeFactor);
			energyConsumption += getPowerOfComponentLinear(utilizationRate,
					condensators, condensatorsChangeFactor);
			energyConsumption += getPowerOfComponentLinear(utilizationRate,
					humidifier, humidifierChangeFactor);
			energyConsumption += getPowerOfComponentLinear(utilizationRate, ac,
					acChangeFactor);
		} else if (powerModel.equals("cubic")){
			energyConsumption += getPowerOfComponentCubic(utilizationRate,
					light, lightChangeFactor);
			energyConsumption += getPowerOfComponentCubic(utilizationRate,
					generator, generatorChangeFactor);
			energyConsumption += getPowerOfComponentCubic(utilizationRate, usv,
					usvChangeFactor);
			energyConsumption += getPowerOfComponentCubic(utilizationRate,
					utility, utilityChangeFactor);
			energyConsumption += getPowerOfComponentCubic(utilizationRate,
					condensators, condensatorsChangeFactor);
			energyConsumption += getPowerOfComponentCubic(utilizationRate,
					humidifier, humidifierChangeFactor);
			energyConsumption += getPowerOfComponentCubic(utilizationRate, ac,
					acChangeFactor);
		}
		return energyConsumption;
	}

	/**
	 * This methods returns the power consumption of a component wth respect to
	 * the current utilization rate. It is being calculated with the help of the
	 * linear regression model
	 * 
	 * @param utilization
	 *            The utilization of the IT infrastructure in the data center
	 * @param componentFactor
	 *            The percentage of average power consumption of the
	 *            corresponding component (e.g. 0.01 (=1%) for the energy
	 *            consumption of light)
	 * @param changeFactor
	 *            A factor describing the influence of the IT utilization rate.
	 *            1 means it changes proportional to the IT utilization rate; 0
	 *            means it wont change at all.
	 * @return
	 */
	private double getPowerOfComponentLinear(double utilization,
			double componentFactor, double changeFactor) {
		double avg = ((maxITPower - minITPower) / 2) * componentFactor;
		double min = minITPower * componentFactor;
		double max = maxITPower * componentFactor;
		PowerModelLinear pml = new PowerModelLinear(avg
				- (changeFactor * (avg - min)), avg
				+ (changeFactor * (max - avg)));
		return pml.getPower(utilization);
	}

	/**
	 * This methods returns the power consumption of a component wrt the current
	 * utilization rate. It is being calculated with the help of the cubic
	 * regression model
	 * 
	 * @param utilization
	 *            The utilization of the IT infrastructure in the data center
	 * @param componentFactor
	 *            The percentage of average power consumption of the
	 *            corresponding component (e.g. 0.01 (=1%) for the energy
	 *            consumption of light)
	 * 
	 * @param changeFactor
	 *            A factor describing the influence of the IT utilization rate.
	 *            1 means it changes proportional to the IT utilization rate; 0
	 *            means it wont change at all.
	 * @return
	 */
	private double getPowerOfComponentCubic(double utilization,
			double componentFactor, double changeFactor) {
		double avg = ((maxITPower - minITPower) / 2) * componentFactor;
		double min = minITPower * componentFactor;
		double max = maxITPower * componentFactor;
		PowerModelCubic pmc = new PowerModelCubic(avg
				- (changeFactor * (avg - min)), avg
				+ (changeFactor * (max - avg)));
		return pmc.getPower(utilization);
	}

	public double getLight() {
		return light;
	}

	
	public void setLight(double light) {
		this.light = light;
	}


	public double getGenerator() {
		return generator;
	}

	
	public void setGenerator(double generator) {
		this.generator = generator;
	}

	
	public double getUsv() {
		return usv;
	}

	
	public void setUsv(double usv) {
		this.usv = usv;
	}

	
	public double getUtility() {
		return utility;
	}

	
	public void setUtility(double utility) {
		this.utility = utility;
	}

	
	public double getCondensators() {
		return condensators;
	}


	public void setCondensators(double condensators) {
		this.condensators = condensators;
	}


	public double getHumidifier() {
		return humidifier;
	}


	public void setHumidifier(double humidifier) {
		this.humidifier = humidifier;
	}


	public double getAc() {
		return ac;
	}


	public void setAc(double ac) {
		this.ac = ac;
	}


	public double getIt() {
		return it;
	}


	public void setIt(double it) {
		this.it = it;
	}

	public double getPowerData(double itUtilization, double standardTempDiscrepancy) {
		return (itUtilization * getEfficiencyFactor())*Math.pow(1.02, standardTempDiscrepancy);
	}
	
	
	private double getEfficiencyFactor(){
		double eff = 0;
		eff = (condensators + humidifier + ac)/it;
		return eff;
	}

	@Override
	protected double getPowerData(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

}
