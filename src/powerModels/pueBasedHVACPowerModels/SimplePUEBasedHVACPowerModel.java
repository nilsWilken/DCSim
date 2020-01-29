package powerModels.pueBasedHVACPowerModels;

public class SimplePUEBasedHVACPowerModel implements PUEBasedHVACPowerModel{

	@Override
	public double getCoolingPower(double pue, double itPower) {
		return (pue*itPower)-itPower;
	}

}
