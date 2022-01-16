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

package org.jacop.constraints;

import java.util.ArrayList;
import java.util.List;

import org.jacop.core.FailException;
import org.jacop.core.IntVar;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the SoftAllDifferent constraint
 * @author Thomas Leaute
 */
public class SoftAlldifferentCloneable extends SoftAlldifferent 
implements ConstraintCloneableInterface<SoftAlldifferentCloneable>, DecomposedConstraintCloneableInterface {

	/** Used to increment the id of constraints */
	private static int idNbr = 0;
	
	/** This constraint's ID/name */
	private final String id;

	/** Constructor
	 * @param xVars 			the variables that should all be different
	 * @param costVar 			the cost variable
	 * @param violationMeasure 	the violation measure
	 */
	public SoftAlldifferentCloneable(IntVarCloneable[] xVars, IntVarCloneable costVar, ViolationMeasure violationMeasure) {
		super(xVars, costVar, violationMeasure);
		this.id = this.getClass().getSimpleName() + idNbr++;
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public SoftAlldifferentCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the IntVar array
		IntVarCloneable[] xVars2 = new IntVarCloneable [this.xVars.length];
		for (int i = this.xVars.length - 1; i >= 0; i--) 
			if ((xVars2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.xVars[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		IntVarCloneable costVar2 = targetStore.findOrCloneInto((IntVarCloneable) this.costVar);
		if (costVar2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		return new SoftAlldifferentCloneable (xVars2, costVar2, this.violationMeasure);
	}

	/** @see org.jacop.constraints.DecomposedConstraintCloneableInterface#arguments() */
	@Override
	public List<IntVar> arguments() {
		
		ArrayList<IntVar> out = new ArrayList<IntVar> (super.xVars.length + 1);
		out.add(super.costVar);
		for (int i = super.xVars.length - 1; i >= 0; i--) 
			out.add(super.xVars[i]);
		
		return out; 
	}

	/** @see org.jacop.constraints.DecomposedConstraintCloneableInterface#id() */
	@Override
	public String id() {
		return this.id;
	}

}
