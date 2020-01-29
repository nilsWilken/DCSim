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

package simulationControl;

import java.util.ArrayList;
import java.util.List;

import hardware.DC;

/**
 * @author University of Mannheim
 * 
 */

//THIS CLASS WAS NOT CHANGED DURING THE THESIS AND THEREFORE IS NOT COMMENTED!
public class DCFederation {

	public List<DC> getDcs() {
		return dcs;
	}

	private List<DC> dcs;

	public DCFederation() {
		this.dcs = new ArrayList<DC>();
	}

	public DCFederation(List<DC> dcs) {
		this.dcs = dcs;
	}

	/**
	 * @param dcID
	 */
	public void removeDC(int dcID) {
		int index = -1;
		for (int i = 0; i < dcs.size(); i++) {
			if (dcs.get(i).getId() == dcID)
				index = i;
		}
		if (index >= 0)
			dcs.remove(index);
	}

	/**
	 * @param dc
	 */
	public void addDC(DC dc) {
		this.dcs.add(dc);
	}

//	/**
//	 * @param serviceID
//	 * @param targetDCID
//	 */
//	public void moveService(int serviceID, int targetDCID) {
//		if (targetDCID >= 0) {
//			boolean b = true;
//			int index = 0;
//			int oldServiceIndex = -1;
//			DC sourceDC = null;
//			DC targetDC = null;
//			while (b) {
//				if (index < dcs.size()) {
//					DC dc = dcs.get(index);
//					if (dc.getId() == targetDCID) {
//						targetDC = dc;
//					}
//					if (oldServiceIndex == -1) {
//						for (Service s : dc.getServices()) {
//							if (s.getId() == serviceID) {
//								// b = false;
//								oldServiceIndex = dc.getServices().indexOf(s);
//								sourceDC = dc;
//								break;
//							}
//						}
//					}
//				} else {
//					b = false;
//				}
//				index++;
//			}
//			if (oldServiceIndex != -1 && sourceDC != null && targetDC != null) {
//				Service s = sourceDC.getServices().get(oldServiceIndex);
//				List<Service> services = targetDC.getServices();
//				List<Service> newServiceList = new ArrayList<Service>(services);
//				newServiceList.add(s);
//
//				// Check if it is possible to place the new service in the
//				// target DC
//				// or if it is full
//				if (!new Optimizer().isFull(targetDC.getServer(),
//						newServiceList)) {
//					targetDC.setServices(newServiceList);
//
//					// Remove the Service
//					List<Service> sourceServices = new ArrayList<Service>(
//							sourceDC.getServices());
//					sourceServices.remove(oldServiceIndex);
//					sourceDC.setServices(sourceServices);
//
//					List<VM> vms = s.getVms();
//
//					// Remove the VMs
//					for (Server server : sourceDC.getServer()) {
//						if (server.getVms().size() > 0) {
//							for (VM sVM : server.getVms()) {
//								for (VM vm : vms) {
//									if (vm.getId() == sVM.getId()) {
//										List<VM> newVM = new ArrayList<VM>(server.getVms());
//										newVM.remove(sVM);
//										server.setVms(newVM);										
//									}
//								}
//							}
//						}
//					}
//					targetDC.setChanged(true);
//					sourceDC.setChanged(true);
//				}
//			}
//		}
//	}

}
