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
import java.util.Calendar;
import java.util.List;

//import eu.a4g.dcSim.DCSimCore_old;

/**
 * @author University of Mannheim
 *
 */
public class Service {

	private int id;
	private String name;
	private List<VM> vms;
	private String status; // paused, running, off
	private SLA sla;
	private boolean modified;
	private int schedulingInterval;
	private Calendar startTime;
	private int dcID;

	public Service(int id, String name, String status, SLA sla, List<VM> vms,
			int schedulingInterval, Calendar startTime, int dcID) {
		super();
		this.setId(id);
		this.setName(name);
		this.sla = sla;
		this.vms = vms;
		this.setModified(false);
		this.setStatus(status);
		this.schedulingInterval = schedulingInterval;
		this.startTime = startTime;
		this.dcID = dcID;
	}

//	/**
//	 * Here, the Service is updated. It should be called every scheduling
//	 * interval. Concretely, the method checks if the SLA availability
//	 * parameters have changed in order to switch on more or switch off
//	 * redundant VMs. Furthermore, it checks whether the service is going to be
//	 * restarted (i.e. the utilization is 0 in this iteration but != 0 in the
//	 * next) in the next scheduling interval. If it is it sends an allocation
//	 * request to A4G.
//	 * 
//	 * @param clock
//	 *            The current internal clock. It is 0 when the simulator is
//	 *            started 1 after the first scheduling interval 2 after the next
//	 *            etc.
//	 */
//	public void update(int clock) {
//
//		// Check if the service is restarting i.e. now utilization is 0 and next
//		// scheduling interval the utilization is != 0
//		VM aVM = vms.get(0);
//		double nowUtil = aVM.getUtilization(clock);
//		double nextUtil = aVM.getUtilization(clock + 1);
//		if (nowUtil == 0 && nextUtil != 0) {
//			boolean nextIsNotZero = true;
//			int i = 2;
//			while (nextIsNotZero) {
//				if (aVM.getUtilization(clock + i) == 0) {
//					nextIsNotZero = false;
//				} else if (i > aVM.getUtil().size())
//					nextIsNotZero = false;
//				else
//					i++;
//			}
//			Calendar startTemp = ((Calendar) (startTime.clone()));
//			startTemp.add(Calendar.SECOND, (clock + 1) * schedulingInterval);
//			Calendar endTemp = ((Calendar) (startTime.clone()));
//			endTemp.add(Calendar.SECOND, (clock + i - 1) * schedulingInterval);
//
//			// TODO: Check if this is the format the DCAgents requires
//			String start = startTemp.getTime().toString();
//			String end = endTemp.getTime().toString();
//			String result = this.getITServiceAllocationAdvice(String.valueOf(this.getId()), start, end);
//			DCSimCore_old.getFederationOfDC(this.dcID).moveService(this.getId(),
//					Integer.valueOf(result));
//		}
//
//		// Check status and availability options
//		if (vms.size() != 0
//				&& (!(this.status.equals("off") && !this.status
//						.equals("paused")))) {
//			if (vms.get(vms.size() - 1).getUtilization(clock) == 0
//					&& vms.get(vms.size() - 1).getStatus().equals("running")) {
//				for (VM vm : vms) {
//					vm.setStatus("off");
//				}
//				modified = true; // trigger for a later internal
//								 // optimization action
//			} else if (vms.get(vms.size() - 1).getUtilization(clock) != 0
//					&& (vms.get(vms.size() - 1).getStatus().equals("off"))) {
//				// SLA dependent				
//				if (this.sla.getAvailability() == 0) {
//					for (int i = ((vms.size() / 2)); i < vms.size(); i++) {
//						vms.get(i).setStatus("running");
//					}
//				} else if (this.sla.getAvailability() == 1) {
//					for (int i = (int) (((vms.size() / 2) * 1.1d)); i < vms
//							.size(); i++) {
//						vms.get(i).setStatus("running");
//					}
//				} else {
//					for (VM vm : vms) {
//						vm.setStatus("running");
//					}
//				}
//				modified = true; // trigger for a later internal
//									// optimization action
//			} else if (vms.size() != 0
//					&& (this.status.equals("running"))){
//				if (vms.get(vms.size() - 1).getUtilization(clock) != 0
//						&& !(vms.get(vms.size() - 1).getStatus().equals("running"))) {
//					// SLA dependent					
//					if (this.sla.getAvailability() == 0) {
//						for (int i = ((vms.size() / 2)); i < vms.size(); i++) {
//							vms.get(i).setStatus("running");
//						}
//					} else if (this.sla.getAvailability() == 1) {
//						for (int i = (int) (((vms.size() / 2) * 1.1d)); i < vms
//								.size(); i++) {
//							vms.get(i).setStatus("running");
//						}
//					} else {
//						for (VM vm : vms) {
//							vm.setStatus("running");
//						}
//					}
//					modified = true; // trigger for a later internal
//										// optimization action
//				}
//			}
//		}
//	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

	public boolean isModified() {
		return modified;
	}

	public void setStatus(String status) {
		if (status.equals("off") || status.equals("paused")) {
			this.status = status;
			this.modified = true;
			for (VM vm : vms) {
				vm.setStatus(status);
			}
		} else if (status.equals("running")) {
			this.status = status;
			this.modified = true;
		}
	}

	public void changeSLA(SLA sla) {
		if (this.sla.getAvailability() != sla.getAvailability()
				&& this.status.equals("running")) {
			for (int i = 0; i < ((vms.size() / 2)); i++) {
				vms.get(i).setStatus("off");
			}
			if (sla.getAvailability() == 1) {
				for (int i = ((vms.size() / 2)); i < (int) (((vms.size() / 2) * 1.1d)); i++) {
					vms.get(i).setStatus("running");
				}
			} else if (sla.getAvailability() == 2) {
				for (int i = 0; i < ((vms.size() / 2)); i++) {
					vms.get(i).setStatus("running");
				}
			}
			this.modified = true;
		} 
		if (this.sla.getStartupTime() != sla.getStartupTime()) {
			// The assumption here is that the change in startup-time must be
			// higher or equal the scheduling interval to have an effect
			int numberOfZeros = ( this.sla.getStartupTime()-sla
					.getStartupTime()) / schedulingInterval;
			if (numberOfZeros > 0) {				
				List<Integer> util = new ArrayList<Integer>(this.vms.get(0).getUtil());
				List<Integer> index = new ArrayList<Integer>();
				int i = 0;
				int lastUtil = 0;
				for (Integer u : util) {
					if (u == 0 && lastUtil != 0) {
						index.add(i);
					}
					lastUtil = u;
					i++;
				}
				for (int k = index.size() - 1; k >= 0; k--) {
					for (int j = 0; j < numberOfZeros; j++)
						//Should not happen but in case
						if (util.get(index.get(k)) == 0)
							util.remove(index.get(k));
				}
				for (VM vm : vms) {
					vm.setUtil(util);
				}
			} else if (numberOfZeros < 0) {				
				List<Integer> util = new ArrayList<Integer>(this.vms.get(0).getUtil());
				List<Integer> index = new ArrayList<Integer>();
				int i = 0;
				int lastUtil = 0;
				for (Integer u : util) {
					if (u == 0 && lastUtil != 0) {
						index.add(i);
					}
					lastUtil = u;
					i++;
				}
				for (int k = index.size() - 1; k >= 0; k--) {
					for (int j = 0; j < numberOfZeros*-1; j++)	{						
						util.add(index.get(k),0);
				}}
				for (VM vm : vms) {
					vm.setUtil(util);
				}
			}
			this.modified = true;
		}
		if (this.sla.getBw() != sla.getBw()) {
			for (VM vm : vms) {
				vm.setBw(sla.getBw());
			}
			this.modified = true;
		}
		if (this.sla.getHdd() != sla.getHdd()) {
			for (VM vm : vms) {
				vm.setHdd(sla.getHdd());
			}
			this.modified = true;
		}
		if (this.sla.getMips() != sla.getMips()) {
			for (VM vm : vms) {
				vm.setMips(sla.getMips());
			}
			this.modified = true;
		}
		if (this.sla.getPes() != sla.getPes()) {
			for (VM vm : vms) {
				vm.setPes(sla.getPes());
			}
			this.modified = true;
		}
		if (this.sla.getRam() != sla.getRam()) {
			for (VM vm : vms) {
				vm.setRam(sla.getRam());
			}
			this.modified = true;
		}
		this.sla = sla;
	}

	private String getITServiceAllocationAdvice(String serviceID,
			String startTime2, String endTime) {
		// TODO: uncomment the following section to enable the call to the
		// DCAgents
//		try {
//			Advice advice = null;
//			String result = "";
//			try {
//				final DCAgentProxy proxy = ConnectUtil.getDCAgent(null,
//						getID(), true);
//
//				try {
//					advice = proxy.allocationAdvice(serviceID, startTime,
//							endTime);
//				} catch (final Throwable t) {
//					throw ConnectUtil.toRPCError(t);
//				}
//
//			} catch (A4GException e) {
//				e.printStackTrace();
//			}
//
//			LOG.debug("ADVICE: " + advice.toString());
//			result = (advice != null && advice.getSelectedOffer() != null) ? advice
//					.getSelectedOffer().getLocalDatacenterID() : getID();
//
//			LOG.debug("RESULT: " + result);
//			return result;
//		} catch (Exception e) {
			return "-1";
//		}

	}

	public String getStatus() {
		return status;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public List<VM> getVms() {
		return vms;
	}

	public void setVms(List<VM> vms) {
		this.vms = vms;
	}

	public SLA getSla() {
		return sla;
	}

}
