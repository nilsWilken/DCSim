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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uma.dcsim.hardware.Server;

/**
 * @author University of Mannheim
 *
 */
public class Optimizer {

	/**
	 * This method executes an internal DC optimization. This comprises VM
	 * allocations to as few as possible servers and switching off unused
	 * servers.
	 * 
	 * @param server
	 *            The list of all servers in a DC
	 * @param services
	 *            The list of all services located in a DC
	 * @return True if the optimization was successful; false otherwise.
	 */
	public boolean optimize(List<Server> server, List<Service> services) {
		double vMips = Integer.MAX_VALUE;
		double vPes = Integer.MAX_VALUE;
		double vBw = Integer.MAX_VALUE;
		double vRam = Integer.MAX_VALUE;
		double vHdd = Integer.MAX_VALUE;
		boolean isOK = true;

		double mips = Integer.MAX_VALUE;
		double pes = Integer.MAX_VALUE;
		double bw = Integer.MAX_VALUE;
		double ram = Integer.MAX_VALUE;
		double hdd = Integer.MAX_VALUE;

		for (Server s : server) {
			if (s.getBw() < bw) {
				bw = s.getBw();
			}
			if (s.getHdd() < hdd) {
				hdd = s.getHdd();
			}
			if (s.getMips() < mips) {
				mips = s.getMips();
			}
			if (s.getPes() < pes) {
				pes = s.getPes();
			}
			if (s.getRam() < ram) {
				ram = s.getRam();
			}
		}
		for (Service s : services) {
			SLA sla = s.getSla();
			if (sla.getBw() < vBw) {
				vBw = sla.getBw();
			}
			if (sla.getHdd() < vHdd) {
				vHdd = sla.getHdd();
			}
			if (sla.getMips() < vMips) {
				vMips = sla.getMips();
			}
			if (sla.getPes() < vPes) {
				vPes = sla.getPes();
			}
			if (sla.getRam() < vRam) {
				vRam = sla.getRam();
			}
		}

		int scarceResource = 0;
		double sMips = vMips / mips;
		double sPes = vPes / pes;
		double sBw = vBw / bw;
		double sRam = vRam / ram;
		double sHdd = vHdd / hdd;

		if (sMips >= sPes && sMips >= sBw && sMips >= sRam && sMips >= sHdd) {
			scarceResource = 1;
		} else if (sPes >= sMips && sPes >= sBw && sPes >= sRam && sPes >= sHdd) {
			scarceResource = 2;
		} else if (sBw >= sMips && sBw >= sPes && sBw >= sRam && sBw >= sHdd) {
			scarceResource = 3;
		} else if (sRam >= sMips && sRam >= sPes && sRam >= sBw && sRam >= sHdd) {
			scarceResource = 4;
		} else if (sHdd >= sMips && sHdd >= sPes && sHdd >= sBw && sHdd >= sRam) {
			scarceResource = 5;
		}

		// Sorting in ascending order:
		Collections.sort(server, new ServerComparator(scarceResource));
		Collections.sort(services, new ServiceComparator(scarceResource));
		List<VM> allVMs = new ArrayList<VM>();
		for (Service s : services) {
			for (VM vm : s.getVms())
				if (vm.getStatus().equals("running")){
					allVMs.add(vm);
				}
				else
					vm.setServerID(server.get(0).getId());
		}
		for (int j = server.size() - 1; j >= 0; j--) {
			Server s = server.get(j);
			double s2Mips = s.getMips();
			double s2Pes = s.getPes();
			double s2Bw = s.getBw();
			double s2Ram = s.getRam();
			double s2Hdd = s.getHdd();

			List<VM> serverVM;
			serverVM = new ArrayList<VM>();
			for (int i = allVMs.size() - 1; i >= 0; i--) {
				switch (scarceResource) {
				case 1:
					if (s2Mips >= allVMs.get(i).getMips()) {
						allVMs.get(i).setServerID(s.getId());
						s2Mips -= allVMs.get(i).getMips();
						serverVM.add(allVMs.get(i));
						allVMs.remove(i);
					}
					break;
				case 2:
					if (s2Pes >= allVMs.get(i).getPes()) {
						allVMs.get(i).setServerID(s.getId());
						s2Pes -= allVMs.get(i).getPes();
						serverVM.add(allVMs.get(i));
						allVMs.remove(i);
					}
					break;
				case 3:
					if (s2Bw >= allVMs.get(i).getBw()) {
						allVMs.get(i).setServerID(s.getId());
						s2Bw -= allVMs.get(i).getBw();
						serverVM.add(allVMs.get(i));
						allVMs.remove(i);
					}
					break;
				case 4:
					if (s2Ram >= allVMs.get(i).getRam()) {
						allVMs.get(i).setServerID(s.getId());
						s2Ram -= allVMs.get(i).getRam();
						serverVM.add(allVMs.get(i));
						allVMs.remove(i);
					}
					break;
				case 5:
					if (s2Hdd >= allVMs.get(i).getHdd()) {
						allVMs.get(i).setServerID(s.getId());
						s2Hdd -= allVMs.get(i).getHdd();
						serverVM.add(allVMs.get(i));
						allVMs.remove(i);
					}
					break;
				}
			}
			s.setVms(serverVM);
			if (allVMs.isEmpty())
				break;
		}
		if (!allVMs.isEmpty()) {
			isOK = false;
			System.out.println("Could not allocate " + allVMs.size()
					+ " VMs. Possibly too many Services started in DC.");
		}

		return isOK;
	}

	/**
	 * This method checks if all services can be allocated in a DC or not.
	 * 
	 * @param server
	 *            The list of all servers in a DC
	 * @param services
	 *            The list of all services located in a DC
	 * @return True if the data center is full, that means the VMs could NOT all
	 *         be allocated; false if all VMs could be allocated
	 */
	public boolean isFull(List<Server> server, List<Service> services) {
		boolean isFull = false;
		double vMips = Integer.MAX_VALUE;
		double vPes = Integer.MAX_VALUE;
		double vBw = Integer.MAX_VALUE;
		double vRam = Integer.MAX_VALUE;
		double vHdd = Integer.MAX_VALUE;

		double mips = Integer.MAX_VALUE;
		double pes = Integer.MAX_VALUE;
		double bw = Integer.MAX_VALUE;
		double ram = Integer.MAX_VALUE;
		double hdd = Integer.MAX_VALUE;

		for (Server s : server) {
			if (s.getBw() < bw) {
				bw = s.getBw();
			}
			if (s.getHdd() < hdd) {
				hdd = s.getHdd();
			}
			if (s.getMips() < mips) {
				mips = s.getMips();
			}
			if (s.getPes() < pes) {
				pes = s.getPes();
			}
			if (s.getRam() < ram) {
				ram = s.getRam();
			}
		}
		for (Service s : services) {
			SLA sla = s.getSla();
			if (sla.getBw() < vBw) {
				vBw = sla.getBw();
			}
			if (sla.getHdd() < vHdd) {
				vHdd = sla.getHdd();
			}
			if (sla.getMips() < vMips) {
				vMips = sla.getMips();
			}
			if (sla.getPes() < vPes) {
				vPes = sla.getPes();
			}
			if (sla.getRam() < vRam) {
				vRam = sla.getRam();
			}
		}

		int scarceResource = 0;
		double sMips = vMips / mips;
		double sPes = vPes / pes;
		double sBw = vBw / bw;
		double sRam = vRam / ram;
		double sHdd = vHdd / hdd;

		if (sMips >= sPes && sMips >= sBw && sMips >= sRam && sMips >= sHdd) {
			scarceResource = 1;
		} else if (sPes >= sMips && sPes >= sBw && sPes >= sRam && sPes >= sHdd) {
			scarceResource = 2;
		} else if (sBw >= sMips && sBw >= sPes && sBw >= sRam && sBw >= sHdd) {
			scarceResource = 3;
		} else if (sRam >= sMips && sRam >= sPes && sRam >= sBw && sRam >= sHdd) {
			scarceResource = 4;
		} else if (sHdd >= sMips && sHdd >= sPes && sHdd >= sBw && sHdd >= sRam) {
			scarceResource = 5;
		}

		// Sorting in ascending order:
		Collections.sort(server, new ServerComparator(scarceResource));
		Collections.sort(services, new ServiceComparator(scarceResource));
		List<VM> allVMs = new ArrayList<VM>();
		for (Service s : services) {
			for (VM vm : s.getVms())
				if (vm.getStatus().equals("running"))
					allVMs.add(vm);
				else
					vm.setServerID(server.get(0).getId());
		}
		for (int j = server.size() - 1; j >= 0; j--) {
			Server s = server.get(j);
			double s2Mips = s.getMips();
			double s2Pes = s.getPes();
			double s2Bw = s.getBw();
			double s2Ram = s.getRam();
			double s2Hdd = s.getHdd();

			List<VM> serverVM;
			serverVM = new ArrayList<VM>();
			for (int i = allVMs.size() - 1; i >= 0; i--) {
				switch (scarceResource) {
				case 1:
					if (s2Mips >= allVMs.get(i).getMips()) {
						s2Mips -= allVMs.get(i).getMips();
						allVMs.remove(i);
					}
					break;
				case 2:
					if (s2Pes >= allVMs.get(i).getPes()) {
						s2Pes -= allVMs.get(i).getPes();
						allVMs.remove(i);
					}
					break;
				case 3:
					if (s2Bw >= allVMs.get(i).getBw()) {
						s2Bw -= allVMs.get(i).getBw();
						allVMs.remove(i);
					}
					break;
				case 4:
					if (s2Ram >= allVMs.get(i).getRam()) {
						s2Ram -= allVMs.get(i).getRam();
						allVMs.remove(i);
					}
					break;
				case 5:
					if (s2Hdd >= allVMs.get(i).getHdd()) {
						s2Hdd -= allVMs.get(i).getHdd();
						allVMs.remove(i);
					}
					break;
				}
			}
			s.setVms(serverVM);
			if (allVMs.isEmpty())
				break;
		}
		if (!allVMs.isEmpty()) {
			isFull = true;
		}
		return isFull;
	}

}
