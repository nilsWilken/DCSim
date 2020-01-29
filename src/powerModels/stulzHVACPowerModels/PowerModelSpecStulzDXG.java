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

package powerModels.stulzHVACPowerModels;

/**
 * Traditional water cooling system 
 * 
 * @author University of Mannheim
 *
 */
public class PowerModelSpecStulzDXG extends PowerModelSpecStulz {

	public PowerModelSpecStulzDXG() {
		super(0.01, 0.01, 0.18, 0.05, 0.33, 0.03, 0.09, 0.3);		
	}

}
