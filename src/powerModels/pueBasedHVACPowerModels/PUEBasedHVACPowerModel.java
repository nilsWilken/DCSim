package powerModels.pueBasedHVACPowerModels;

public interface PUEBasedHVACPowerModel {
	
	double getCoolingPower(double pue, double itPower);

}
