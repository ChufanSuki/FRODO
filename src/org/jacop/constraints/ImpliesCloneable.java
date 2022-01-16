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

/** A cloneable version of the Implies constraint
 * @author Thomas Leaute
 */
public class ImpliesCloneable extends Implies implements ConstraintCloneableInterface<PrimitiveConstraint> {

    /**
     * It constructs Implies constraint.
     *
     * @param b the variable of the implied constraint.
     * @param c the constraint which must hold if the variable is 1.
     */
	public ImpliesCloneable(IntVarCloneable b, PrimitiveConstraint c) {
		super(b, c);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@SuppressWarnings("unchecked")
	@Override
	public PrimitiveConstraint cloneInto(StoreCloneable targetStore) 
			throws CloneNotSupportedException, FailException {
		
		IntVarCloneable b2 = targetStore.findOrCloneInto((IntVarCloneable) this.b);
		if (b2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		ConstraintCloneableInterface<PrimitiveConstraint> c2;
		try {
			c2 = (ConstraintCloneableInterface<PrimitiveConstraint>) this.c;
		} catch (ClassCastException e) {
			throw new CloneNotSupportedException (this.c.getClass() + " does not implement " + ConstraintCloneableInterface.class);
		}
		
		PrimitiveConstraint thenClone;
		try {
			thenClone = c2.cloneInto(targetStore);
			
		} catch (FailException e) { // THEN is inconsistent, so the variable cannot be 1
			return new XneqCCloneable (b2, 1);
		}
		if (thenClone == null) // THEN is consistent, so this constraint is consistent
			return null;
		
		return new ImpliesCloneable (b2, thenClone);
	}

}
