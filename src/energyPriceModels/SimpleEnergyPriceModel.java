package energyPriceModels;

/**
 * This class implements the EnergyPriceModel interface and thus can be used as energy price model. This model simply returns a static energy price.
 * @author nilsw
 *
 */
public class SimpleEnergyPriceModel implements EnergyPriceModel {

	@Override
	public double getEnergyPriceInCentPerKWH(int currentTime) {
		return 0.16;
	}

}
