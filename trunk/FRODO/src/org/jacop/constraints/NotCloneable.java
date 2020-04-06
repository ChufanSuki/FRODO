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
import org.jacop.core.StoreCloneable;

/** A cloneable version of the Not constraint
 * @author Thomas Leaute
 */
public class NotCloneable extends Not implements ConstraintCloneableInterface<NotCloneable> {

	/** Constructor
	 * @param c 	the constraint to be negated
	 */
	public NotCloneable(PrimitiveConstraint c) {
		super(c);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@SuppressWarnings("unchecked")
	@Override
	public NotCloneable cloneInto(StoreCloneable targetStore) throws CloneNotSupportedException, FailException {
		
		// Attempt to cast the underlying constraint to CloneableConstraint
		ConstraintCloneableInterface<PrimitiveConstraint> c2;
		try {
			c2 = (ConstraintCloneableInterface<PrimitiveConstraint>) this.c;
		} catch (ClassCastException e) {
			throw new CloneNotSupportedException (this.c.getClass() + " does not implement " + ConstraintCloneableInterface.class);
		}
		
		PrimitiveConstraint clone;
		try {
			clone = c2.cloneInto(targetStore);
			
		} catch (FailException e) { // the constraint is inconsistent, so this constraint is
			return null;
		}
		if (clone == null) // the constraint is consistent, so this constraint isn't 
			throw StoreCloneable.failException;
		
		return new NotCloneable (clone);
	}

}
