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

package de.uma.dcsim.simulationControl;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

/**
 * @author University of Mannheim
 *
 */
public class SimulationController {
	
	Setup parser;
	
	public void start(InputStream file, double terminationTime) {
		DCSimCore.DCSimCore(file);
		DCSimCore.setLength((int) terminationTime);
		DCSimCore.startSimulation();
	}

	public void start(InputStream file, double terminationTime, Date startTime) {
		Calendar c = Calendar.getInstance();
		c.setTime(startTime);
		DCSimCore.setSimStartTime(c);
		this.start(file, terminationTime);

	}

}
