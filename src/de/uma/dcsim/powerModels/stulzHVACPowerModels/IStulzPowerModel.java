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

package de.uma.dcsim.powerModels.stulzHVACPowerModels;

/**
 * 
 * @author University of Mannheim
 *
 */
public interface IStulzPowerModel {

	public abstract double getLight();

	public abstract void setLight(double light);

	public abstract double getGenerator();

	public abstract void setGenerator(double generator);

	public abstract double getUsv();

	public abstract void setUsv(double usv);

	public abstract double getUtility();

	public abstract void setUtility(double utility);

	public abstract double getCondensators();

	public abstract void setCondensators(double condensators);

	public abstract double getHumidifier();

	public abstract void setHumidifier(double humidifier);

	public abstract double getAc();

	public abstract void setAc(double ac);

	public abstract double getIt();

	public abstract void setIt(double it);

}