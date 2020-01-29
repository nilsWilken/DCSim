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

package serviceRelatedClasses;

import java.util.Comparator;

/**
 * @author University of Mannheim
 *
 */
public class ServiceComparator implements Comparator<Service> {

private int resourceToCompare;
	
	
	public ServiceComparator(int resourceToCompare){
		this.resourceToCompare = resourceToCompare;
	}

	public int compare(Service s1, Service s2) {
		SLA sla1 = s1.getSla();
		SLA sla2 = s2.getSla();
		switch (resourceToCompare) {
		case 1:
			return sla1.getMips() > sla2.getMips() ? 1 : (sla1.getMips() > sla2.getMips() ? -1 : 0);
		case 2:
			return sla1.getPes() > sla2.getPes() ? 1 : (sla1.getPes() > sla2.getPes() ? -1 : 0);
		case 3:
			return sla1.getBw() > sla2.getBw() ? 1 : (sla1.getBw() > sla2.getBw() ? -1 : 0);
		case 4:
			return sla1.getRam() > sla2.getRam() ? 1 : (sla1.getRam() > sla2.getRam() ? -1 : 0);
		case 5:
			return sla1.getHdd() > sla2.getHdd() ? 1 : (sla1.getHdd() > sla2.getHdd() ? -1 : 0);			
		default:
			return 0;			
		}	
	}
	

}
