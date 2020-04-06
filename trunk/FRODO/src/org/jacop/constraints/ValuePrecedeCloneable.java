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

import java.util.List;

import org.jacop.core.FailException;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the ValuePrecede constraint
 * @author Thomas Leaute
 */
public class ValuePrecedeCloneable extends ValuePrecede implements ConstraintCloneableInterface<ValuePrecedeCloneable> {

    /** The value occurring first */
    private final int s;
    
    /** The value occurring next */
    private final int t;

   /**
     * It constructs ValuePrecede.
     *
     * @param s value occuring first
     * @param t value occuring next
     * @param x list of arguments x's.
     */
	public ValuePrecedeCloneable(int s, int t, IntVarCloneable[] x) {
		super(s, t, x);
		this.s = s;
		this.t = t;
	}

    /**
     * It constructs ValuePrecede.
     *
     * @param s value occuring first
     * @param t value occuring next
     * @param x list of arguments x's.
     */
	public ValuePrecedeCloneable(int s, int t, List<IntVarCloneable> x) {
		this(s, t, x.toArray(new IntVarCloneable [x.size()]));
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public ValuePrecedeCloneable cloneInto(StoreCloneable targetStore) 
			throws FailException {
		
		// Clone the list of variables
		IntVarCloneable[] x2 = new IntVarCloneable[this.x.length];
		for (int i = this.x.length - 1; i >= 0; i--) 
			if ((x2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.x[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;		
		
		return new ValuePrecedeCloneable (this.s, this.t, x2);
	}

}
