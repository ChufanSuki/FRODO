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

package org.jacop.constraints.regular;

import org.jacop.constraints.ConstraintCloneableInterface;
import org.jacop.core.FailException;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;
import org.jacop.util.fsm.FSMCloneable;

/** A cloneable version of the Regular constraint
 * @author Thomas Leaute
 */
public class RegularCloneable extends Regular implements ConstraintCloneableInterface<RegularCloneable> {

	/** Constructor
	 * @param fsm 	the finite-state machine
	 * @param vars 	the variables
	 */
	public RegularCloneable(FSMCloneable fsm, IntVarCloneable[] vars) {
		super(fsm, vars);
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public RegularCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the list of variables
		IntVarCloneable[] list2 = new IntVarCloneable [this.list.length];
		for (int i = this.list.length - 1; i >= 0; i--) 
			if ((list2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.list[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		return new RegularCloneable (((FSMCloneable) this.fsm).clone(), list2);
	}

}
