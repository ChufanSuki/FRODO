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

import org.jacop.core.FailException;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the MinSimple constraint
 * @author Thomas Leaute
 */
public class MinSimpleCloneable extends MinSimple implements ConstraintCloneableInterface<MinSimpleCloneable> {

    /**
     * It constructs min constraint.
     *
     * @param min variable denoting the minimum value
     * @param x1  first variable for which a  minimum value is imposed.
     * @param x2  second variable for which a  minimum value is imposed.
     */
	public MinSimpleCloneable(IntVarCloneable x1, IntVarCloneable x2, IntVarCloneable min) {
		super(x1, x2, min);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public MinSimpleCloneable cloneInto(StoreCloneable targetStore) throws FailException {
		
		IntVarCloneable x1clone = targetStore.findOrCloneInto((IntVarCloneable) this.x1);
		if (x1clone.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		IntVarCloneable x2clone = targetStore.findOrCloneInto((IntVarCloneable) this.x2);
		if (x2clone.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		IntVarCloneable minClone = targetStore.findOrCloneInto((IntVarCloneable) this.min);
		if (minClone.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		return new MinSimpleCloneable (x1clone, x2clone, minClone);
	}

}
