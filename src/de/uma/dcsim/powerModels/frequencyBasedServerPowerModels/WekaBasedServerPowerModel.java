package de.uma.dcsim.powerModels.frequencyBasedServerPowerModels;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import de.uma.dcsim.powerModels.FrequencyBasedPowerModel;
import de.uma.dcsim.simulationControl.Setup;
import weka.classifiers.functions.LinearRegression;
import weka.core.DenseInstance;
import weka.core.Instance;

/**
 * This class implements the FrequencyBasedPowerModel interface and thus can be used as server power consumption model.
 * The model is based on several linear regression models of the following form: P_serv = k_1*f^3 + k_2, 
 * where k_1 is an application and server dependant fitting parameter, f is the current CPU frequency of the server, and k_2
 * is another application and server specific fitting parameter.
 * 
 * The linear regression models are parsed from a file in which they are stored as linear regression model objects of the WEKA framework.
 * 
 * 
 * @author nilsw
 *
 */
public class WekaBasedServerPowerModel implements FrequencyBasedPowerModel {

	/**
	 * Path at which the WEKA model file is located (parsed from the configuration file).
	 */
	private String filePath;
	
	/**
	 * Array that contains the parsed WEKA classifiers.
	 */
	private LinearRegression[] classifiers;
	

	public WekaBasedServerPowerModel() {
		this.filePath = Setup.wekaPowerModelFile;
		this.classifiers = this.parseFile(this.filePath);
	}
	
	public double getPower(double frequency, int clusterNumber) {
		Instance instance = new DenseInstance(1);
		instance.setValue(0, Math.pow(frequency, 3));
		try {
			return this.classifiers[clusterNumber].classifyInstance(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private LinearRegression[] parseFile(String file) {
		LinearRegression[] result;
		
		ObjectInputStream objectReader;
		
		try {
			objectReader = new ObjectInputStream(new FileInputStream(file));
			result = new LinearRegression[objectReader.readInt()];
			
			for(int i=0; i < result.length; i++) {
				result[i] = (LinearRegression)objectReader.readObject();
			}
			objectReader.close();
			
			return result;
		}catch(IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
//	public static void main(String[] args) {
//		WekaBasedServerPowerModel powerModel = new WekaBasedServerPowerModel("models.mod");
//		int clusterNum = 1;
//		
//		double[] frequencies = {2.7, 2.6, 2.5, 2.4, 2.3, 2.2, 2.1, 2.0, 1.9, 1.8, 1.7, 1.6, 1.5, 1.4, 1.3, 1.2};
//		for(double freq : frequencies) {
//			System.out.println(powerModel.getPower(freq, clusterNum));
//		}
//	}
	


}
