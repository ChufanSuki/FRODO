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

/** A cloneable version of the Min constraint 
 * @author Thomas Leaute
 */
public class MinCloneable extends Min implements ConstraintCloneableInterface<MinCloneable> {

	/** Constructor 
	 * @param vars 	the list of variables
	 * @param min	the min variable
	 */
	public MinCloneable(IntVarCloneable[] vars, IntVarCloneable min) {
		super(vars, min);
	}

	/** Constructor 
	 * @param vars 	the list of variables
	 * @param min	the min variable
	 */
	public MinCloneable(ArrayList<? extends IntVarCloneable> vars, IntVarCloneable min) {
		super(vars, min);
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public MinCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the IntVar array
		IntVarCloneable[] list2 = new IntVarCloneable [this.list.length];
		for (int i = this.list.length - 1; i >= 0; i--) 
			if ((list2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.list[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		IntVarCloneable min2 = targetStore.findOrCloneInto((IntVarCloneable) this.min);
		if (min2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		return new MinCloneable (list2, min2);
	}

}
