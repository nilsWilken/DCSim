package de.uma.dcsim.testing;

import de.uma.dcsim.SLAModels.SLAModel;
import de.uma.dcsim.SLAModels.StandardSLAModel;
import de.uma.dcsim.simulationControl.Setup;

public class BatchJobSLAFeeTest {

	public static void main(String[] args) {
		System.out.println(calculateSLACosts(Setup.usagePrice));
		System.out.println(getCurrentlyCausedSLACosts(Setup.usagePrice));
	}
	
	public static double calculateSLACosts(double usagePrice) {
		
		SLAModel slaModel = new StandardSLAModel();
		int delay = 200;
		int duration = 100;
		int amountOfServers = 2;
		
		usagePrice = (usagePrice*(double)amountOfServers)*(((double)duration*(double)Setup.secondsPerSimulationTimestep)/3600.0);
		return slaModel.calculateSLAFee(((double)delay/(double)duration), usagePrice);
	}
	
	public static double getCurrentlyCausedSLACosts(double usagePrice) {
		
		int amountOfServers = 2;
		int duration = 100;
		SLAModel slaModel = new StandardSLAModel();
		
		usagePrice = (usagePrice*(double)amountOfServers)*(((double)duration*(double)Setup.secondsPerSimulationTimestep)/3600.0);
		int delay = 200;
		if(delay <= 0) {
			return 0;
		}
		else {
			return slaModel.calculateSLAFee(((double)delay/(double)duration), usagePrice);
		}
	}
	
}
