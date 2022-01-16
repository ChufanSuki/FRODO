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
import org.jacop.core.StoreCloneable;

/** A cloneable version of the Or constraint
 * @author Thomas Leaute
 */
public class OrCloneable extends Or implements ConstraintCloneableInterface<OrCloneable> {

	/** Constructor
	 * @param constraints 	the list of constraints
	 */
	public OrCloneable(PrimitiveConstraint[] constraints) {
		super(constraints);
	}

	/** Constructor
	 * @param constraints 	the list of constraints
	 */
	public OrCloneable(ArrayList<PrimitiveConstraint> constraints) {
		super(constraints);
	}

	/** Constructor
	 * @param c1 	the first constraint
	 * @param c2 	the second constraint
	 */
	public OrCloneable(PrimitiveConstraint c1, PrimitiveConstraint c2) {
		super(c1, c2);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@SuppressWarnings("unchecked")
	@Override
	public OrCloneable cloneInto(StoreCloneable targetStore) throws CloneNotSupportedException, FailException {
		
		ArrayList<PrimitiveConstraint> list = new ArrayList<PrimitiveConstraint> (this.listOfC.length);
		ConstraintCloneableInterface<PrimitiveConstraint> c;
		for (PrimitiveConstraint cons : this.listOfC) {
			
			// Attempt to cast the underlying constraint to CloneableConstraint
			try {
				c = (ConstraintCloneableInterface<PrimitiveConstraint>) cons;
			} catch (ClassCastException e) {
				throw new CloneNotSupportedException (cons + " does not implement " + ConstraintCloneableInterface.class);
			}
			
			PrimitiveConstraint clone;
			try {
				clone = c.cloneInto(targetStore);
				
			} catch (FailException e) { // the constraint is inconsistent 
				continue;
			}
			
			if (clone == null) // the constraint is consistent 
				return null;
			
			list.add(clone);
		}
		
		if (list.isEmpty()) // all underlying constraints are inconsistent 
			throw StoreCloneable.failException;
		
		return new OrCloneable (list);
	}

}
