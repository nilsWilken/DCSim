package pueModels;

import java.util.ArrayList;

import utilities.PUE;

/**
 * This class is used by the simulation core to request the PUE value of the DC for a specified point in simulation time.
 * Therefore, if the model is supposed to be changed, it should be changed in this class.
 * 
 * @author nilsw
 *
 */
public class PUEModelSelector {
	
	/**
	 * PUE model that is used to obtain the PUE value of the DC.
	 */
	private static PUEModel pueModel = new TraceBasedPUE();
	
	public static double getPUE(int currentTime) {
		return pueModel.getPUE(currentTime);
	}
	
	/**
	 * Initializes a PUE model. Currently this is only necessary for a trace based model.
	 * @param pues PUE trace values that should be captured by the trace based model.
	 */
	public static void initializePUEModel(ArrayList<PUE> pues) {
		if(pueModel instanceof TraceBasedPUE) {
			((TraceBasedPUE)pueModel).initializeModel(pues);
		}
	}

}
