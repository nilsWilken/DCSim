/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package de.uma.dcsim.powerModels;

/**
 * The PowerModel interface needs to be implemented in order to provide a model of power consumption
 * depending on utilization for system components.
 * 
 * If you are using any algorithms, policies or workload included in the power package, please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience, ISSN: 1532-0626, Wiley
 * Press, New York, USA, 2011, DOI: 10.1002/cpe.1867
 * 
 * @author Anton Beloglazov
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public interface UtilizationBasedPowerModel {

	/**
	 * Get power consumption by the utilization percentage according to the power model.
	 * 
	 * @param utilization the utilization
	 * @return power consumption
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	double getPower(double utilization) throws IllegalArgumentException;

}
