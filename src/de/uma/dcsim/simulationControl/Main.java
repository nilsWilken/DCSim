package de.uma.dcsim.simulationControl;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * This class is the entry point for the simulation framework.
 * 
 * @author nilsw
 *
 */
public class Main {

	public static void main(String[] args) {

		try {
			if(args.length == 0) {
				System.out.println("Configuration file path has to be provided as parameter!");
				System.exit(0);
			}
			InputStream in = new BufferedInputStream(new FileInputStream(args[0]));
			SimulationController controller = new SimulationController();
			controller.start(in, 1800);

			while (DCSimCore.isSimulatorIsRunning()) {
				Thread.sleep(100);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
