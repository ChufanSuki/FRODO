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

import org.jacop.core.FailException;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the Subcircuit constraint
 * @author Thomas Leaute
 */
public class SubcircuitCloneable extends Subcircuit implements ConstraintCloneableInterface<SubcircuitCloneable> {

	/** Constructor
	 * @param vars 	the list of variables
	 */
	public SubcircuitCloneable(IntVarCloneable[] vars) {
		super(vars);
	}

	/** Constructor
	 * @param vars 	the list of variables
	 */
	public SubcircuitCloneable(ArrayList<IntVarCloneable> vars) {
		super(vars.toArray(new IntVarCloneable [vars.size()]));
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public SubcircuitCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the list
		IntVarCloneable[] list2 = new IntVarCloneable [this.list.length];
		for (int i = this.list.length - 1; i >= 0; i--) 
			if ((list2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.list[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		return new SubcircuitCloneable (list2);
	}

}
