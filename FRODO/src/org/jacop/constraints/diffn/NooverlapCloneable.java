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

package org.jacop.constraints.diffn;

import java.util.List;

import org.jacop.constraints.ConstraintCloneableInterface;
import org.jacop.core.FailException;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the Nooverlap constraint
 * @author Thomas Leaute
 */
public class NooverlapCloneable extends Nooverlap implements ConstraintCloneableInterface<NooverlapCloneable> {

    /**
     * It specifies a diff constraint.
     *
     * @param rectangle list of rectangles which can not overlap in at least one dimension.
     */
	public NooverlapCloneable(IntVarCloneable[][] rectangle) {
		super(rectangle);
	}

    /**
     * It specifies a diff constraint.
     *
     * @param rectangle list of rectangles which can not overlap in at least one dimension.
     * @param strict    true- zero size rectangles need to be between other rectangles; false- these rectangles can be anywhere
     */
	public NooverlapCloneable(IntVarCloneable[][] rectangle, boolean strict) {
		super(rectangle, strict);
	}

    /**
     * It constructs a diff constraint.
     *
     * @param origin1 list of variables denoting origin of the rectangle in the first dimension.
     * @param origin2 list of variables denoting origin of the rectangle in the second dimension.
     * @param length1 list of variables denoting length of the rectangle in the first dimension.
     * @param length2 list of variables denoting length of the rectangle in the second dimension.
     */
	public NooverlapCloneable(IntVarCloneable[] origin1, IntVarCloneable[] origin2, IntVarCloneable[] length1, IntVarCloneable[] length2) {
		super(origin1, origin2, length1, length2);
	}

    /**
     * It constructs a diff constraint.
     *
     * @param origin1 list of variables denoting origin of the rectangle in the first dimension.
     * @param origin2 list of variables denoting origin of the rectangle in the second dimension.
     * @param length1 list of variables denoting length of the rectangle in the first dimension.
     * @param length2 list of variables denoting length of the rectangle in the second dimension.
     * @param strict  true- zero size rectangles need to be between other rectangles; false- these rectangles can be anywhere
     */
	public NooverlapCloneable(IntVarCloneable[] origin1, IntVarCloneable[] origin2, IntVarCloneable[] length1, IntVarCloneable[] length2, boolean strict) {
		super(origin1, origin2, length1, length2, strict);
	}

    /**
     * It specifies a diffn constraint.
     *
     * @param rectangle list of rectangles which can not overlap in at least one dimension.
     */
	public NooverlapCloneable(List<? extends List<? extends IntVarCloneable>> rectangle) {
		super(rectangle);
	}

    /**
     * It specifies a diffn constraint.
     *
     * @param rectangle list of rectangles which can not overlap in at least one dimension.
     * @param strict    true- zero size rectangles need to be between other rectangles; false- these rectangles can be anywhere
     */
	public NooverlapCloneable(List<? extends List<? extends IntVarCloneable>> rectangle, boolean strict) {
		super(rectangle, strict);
	}

    /**
     * It constructs a diff constraint.
     *
     * @param o1 list of variables denoting origin of the rectangle in the first dimension.
     * @param o2 list of variables denoting origin of the rectangle in the second dimension.
     * @param l1 list of variables denoting length of the rectangle in the first dimension.
     * @param l2 list of variables denoting length of the rectangle in the second dimension.
     */
	public NooverlapCloneable(List<? extends IntVarCloneable> o1, List<? extends IntVarCloneable> o2, List<? extends IntVarCloneable> l1,
			List<? extends IntVarCloneable> l2) {
		super(o1, o2, l1, l2);
	}

    /**
     * It constructs a diff constraint.
     *
     * @param o1     list of variables denoting origin of the rectangle in the first dimension.
     * @param o2     list of variables denoting origin of the rectangle in the second dimension.
     * @param l1     list of variables denoting length of the rectangle in the first dimension.
     * @param l2     list of variables denoting length of the rectangle in the second dimension.
     * @param strict true- zero size rectangles need to be between other rectangles; false- these rectangles can be anywhere
     */
	public NooverlapCloneable(List<? extends IntVarCloneable> o1, List<? extends IntVarCloneable> o2, List<? extends IntVarCloneable> l1,
			List<? extends IntVarCloneable> l2, boolean strict) {
		super(o1, o2, l1, l2, strict);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public NooverlapCloneable cloneInto(StoreCloneable targetStore) throws FailException {
		
		// Clone the rectangles
		IntVarCloneable[][] rectangles2 = new IntVarCloneable [this.rectangle.length][4];
		for (int i = this.rectangle.length - 1; i >= 0; i--) {
			Rectangle rect = this.rectangle[i];
			
			IntVarCloneable o1 = targetStore.findOrCloneInto((IntVarCloneable) rect.origin[0]);
			if (o1.dom().isEmpty()) 
				throw StoreCloneable.failException;

			IntVarCloneable o2 = targetStore.findOrCloneInto((IntVarCloneable) rect.origin[1]);
			if (o2.dom().isEmpty()) 
				throw StoreCloneable.failException;

			IntVarCloneable l1 = targetStore.findOrCloneInto((IntVarCloneable) rect.length[0]);
			if (l1.dom().isEmpty()) 
				throw StoreCloneable.failException;

			IntVarCloneable l2 = targetStore.findOrCloneInto((IntVarCloneable) rect.length[1]);
			if (l2.dom().isEmpty()) 
				throw StoreCloneable.failException;
		
			rectangles2[i] = new IntVarCloneable[] {o1, o2, l1, l2};
		}
		
		return new NooverlapCloneable (rectangles2, super.strict);
	}

}
