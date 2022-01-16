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
import org.jacop.core.Store;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the GCC constraint
 * @author Thomas Leaute
 */
public class GCCCloneable extends GCC implements ConstraintCloneableInterface<GCCCloneable> {

    /**
     * It species variables counters for counting occurences of each possible value from the
     * intial domain of x variables.
     */
    private final IntVarCloneable[] counters;

	/** Constructor
	 * @param x 		the variables whose values are counted
	 * @param counters 	the counter variables
	 */
	public GCCCloneable(IntVarCloneable[] x, IntVarCloneable[] counters) {
		super(x, counters);
		this.counters = counters;
	}

	/** Constructor
	 * @param x 		the variables whose values are counted
	 * @param counters 	the counter variables
	 */
	public GCCCloneable(ArrayList<? extends IntVarCloneable> x, ArrayList<? extends IntVarCloneable> counters) {
		super(x, counters);
		this.counters = counters.toArray(new IntVarCloneable [counters.size()]);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public GCCCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the first IntVar array
		IntVarCloneable[] x2 = new IntVarCloneable [this.x.length];
		for (int i = this.x.length - 1; i >= 0; i--) 
			if ((x2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.x[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		// Clone the second IntVar array
		IntVarCloneable[] counters2 = new IntVarCloneable [this.counters.length];
		for (int i = this.counters.length - 1; i >= 0; i--) 
			if ((counters2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.counters[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		try {
			return new GCCCloneable (x2, counters2);
			
		} catch (IllegalArgumentException e) {
			throw Store.failException;
		}
	}

}
