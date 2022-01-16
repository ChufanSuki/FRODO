/*
FRODO: a FRamework for Open/Distributed Optimization
Copyright (C) 2008-2020  Thomas Leaute, Brammert Ottens & Radoslaw Szymanek

FRODO is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

FRODO is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.


How to contact the authors: 
<https://frodo-ai.tech>
 */

package org.jacop.constraints.geost;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/** A cloneable version of InArea
 * @author Thomas Leaute
 */
public class InAreaCloneable extends InArea implements ExternalConstraintCloneable{

	/**
	 * It constructs an external constraint to enforce that all objects 
	 * within Geost constraint are placed within a specified area with 
	 * holes in that area specfied as well. 
	 * 
	 * @param area the specification of the area within which the objects have to be placed.
	 * @param holes the holes in which the objects can not be placed.
	 */
	public InAreaCloneable(DBox area, Collection<DBox> holes) {
		super(area, holes);
	}

	/** @see org.jacop.constraints.geost.ExternalConstraintCloneable#cloneAndReplace(java.util.Map) */
	@Override
	public InAreaCloneable cloneAndReplace(Map<Integer, GeostObjectCloneable> objMap) {
		
		DBox area2 = new DBox (this.allowedArea.origin.clone(), this.allowedArea.length.clone());
		
		ArrayList<DBox> holes2 = new ArrayList<DBox> (this.holes.size());
		for (DBox box : this.holes) 
			holes2.add(new DBox (box.origin.clone(), box.length.clone()));
		
		return new InAreaCloneable (area2, holes2);
	}

}
