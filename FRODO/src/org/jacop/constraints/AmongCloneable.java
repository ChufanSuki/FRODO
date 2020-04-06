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
import org.jacop.core.IntervalDomain;
import org.jacop.core.Store;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the Among constraint
 * @author Thomas Leaute
 */
public class AmongCloneable extends Among implements ConstraintCloneableInterface<AmongCloneable> {

	/**
	 * It constructs an Among constraint.
	 * @param list variables which are compared to Kset
	 * @param kSet set of integer values against which we check if variables are equal to.
	 * @param n number of possible variables equal to a value from Kset.
	 */
	public AmongCloneable(IntVarCloneable[] list, IntervalDomain kSet, IntVarCloneable n) {
		super(list, kSet, n);
	}

	/**
	 * It constructs an Among constraint.
	 * @param list variables which are compared to Kset
	 * @param kSet set of integer values against which we check if variables are equal to.
	 * @param n number of possible variables equal to a value from Kset.
	 */
	public AmongCloneable(ArrayList<IntVarCloneable> list, IntervalDomain kSet, IntVarCloneable n) {
		this(list.toArray(new IntVarCloneable [list.size()]), kSet, n);
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public AmongCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the X
		IntVarCloneable[] list2 = new IntVarCloneable [this.list.length];
		for (int i = this.list.length - 1; i >= 0; i--) 
			if ((list2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.list[i])).dom().isEmpty()) 
				throw Store.failException;
		
		IntVarCloneable n2 = targetStore.findOrCloneInto((IntVarCloneable) this.n); 
		if (n2.dom().isEmpty()) 
			throw Store.failException;
		
		return new AmongCloneable (list2, this.kSet.clone(), n2);
	}
	
}
