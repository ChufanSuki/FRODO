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

/** A cloneable version of the And constraint
 * @author Thomas Leaute
 */
public class AndCloneable extends And implements ConstraintCloneableInterface<AndCloneable> {

    /** The constraints */
    private final PrimitiveConstraint listOfC[];

	/** Constructor
	 * @param constraints 	the constraints
	 */
	public AndCloneable(ArrayList<PrimitiveConstraint> constraints) {
		super(constraints);
		this.listOfC = constraints.toArray(new PrimitiveConstraint [constraints.size()]);
	}

	/** Constructor
	 * @param constraints 	the constraints
	 */
	public AndCloneable(PrimitiveConstraint[] constraints) {
		super(constraints);
		this.listOfC = constraints;
	}

	/** Constructor
	 * @param c1 	the first constraint
	 * @param c2 	the second constraint
	 */
	public AndCloneable(PrimitiveConstraint c1, PrimitiveConstraint c2) {
		super(c1, c2);
		this.listOfC = new PrimitiveConstraint[] { c1, c2 };
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@SuppressWarnings("unchecked")
	@Override
	public AndCloneable cloneInto(StoreCloneable targetStore) throws CloneNotSupportedException, FailException {
		
		ArrayList<PrimitiveConstraint> list = new ArrayList<PrimitiveConstraint> (this.listOfC.length);
		ConstraintCloneableInterface<PrimitiveConstraint> c2;
		for (PrimitiveConstraint c : this.listOfC) {
			
			// Attempt to cast the ith underlying constraint to CloneableConstraint
			try {
				c2 = (ConstraintCloneableInterface<PrimitiveConstraint>) c;
			} catch (ClassCastException e) {
				throw new CloneNotSupportedException (c.getClass() + " does not implement " + ConstraintCloneableInterface.class);
			}
			
			PrimitiveConstraint clone = c2.cloneInto(targetStore);
			if (clone != null) 
				list.add(clone);
		}
		
		if (list.isEmpty()) 
			return null; // this constraint is trivially consistent
		
		return new AndCloneable (list);
	}

}
