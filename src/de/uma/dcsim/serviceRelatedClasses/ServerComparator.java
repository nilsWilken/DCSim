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

package de.uma.dcsim.serviceRelatedClasses;

import java.util.Comparator;

import de.uma.dcsim.hardware.Server;

/**
 * @author University of Mannheim
 *
 */
public class ServerComparator implements Comparator<Server>{
	
	private int resourceToCompare;
	
	
	public ServerComparator(int resourceToCompare){
		this.resourceToCompare = resourceToCompare;
	}

	public int compare(Server s1, Server s2) {
		switch (resourceToCompare) {
		case 1:
			return s1.getMips() > s2.getMips() ? 1 : (s1.getMips() > s2.getMips() ? -1 : 0);
		case 2:
			return s1.getPes() > s2.getPes() ? 1 : (s1.getPes() > s2.getPes() ? -1 : 0);
		case 3:
			return s1.getBw() > s2.getBw() ? 1 : (s1.getBw() > s2.getBw() ? -1 : 0);
		case 4:
			return s1.getRam() > s2.getRam() ? 1 : (s1.getRam() > s2.getRam() ? -1 : 0);
		case 5:
			return s1.getHdd() > s2.getHdd() ? 1 : (s1.getHdd() > s2.getHdd() ? -1 : 0);			
		default:
			return 0;			
		}	
	}
	
	

}
