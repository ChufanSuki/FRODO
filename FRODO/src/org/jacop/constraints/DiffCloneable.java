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
import org.jacop.core.StoreCloneable;

/** A cloneable version of the Diff constraint
 * @author Thomas Leaute
 */
public class DiffCloneable extends Diff implements ConstraintCloneableInterface<DiffCloneable> {

	/**
	 * It specifies a diff constraint. 
	 * @param rectangles list of rectangles which can not overlap in at least one dimension.
	 */
	public DiffCloneable(IntVarCloneable[][] rectangles) {
		super(rectangles);
	}

	/**
	 * It specifies a diffn constraint. 
	 * @param rectangles list of rectangles which can not overlap in at least one dimension.
	 */
	public DiffCloneable(ArrayList<? extends ArrayList<? extends IntVarCloneable>> rectangles) {
		super(rectangles);
	}

	/**
	 * It specifies a diff constraint. 
	 * @param rectangles list of rectangles which can not overlap in at least one dimension.
	 * @param doProfile should the constraint compute and use the profile functionality.
	 */
	public DiffCloneable(Rectangle[] rectangles, boolean doProfile) {
		super(rectangles, doProfile);
	}

	/**
	 * It specifies a diff constraint. 
	 * @param profile specifies is the profiles are used.
	 * @param rectangles list of rectangles which can not overlap in at least one dimension.
	 */
	public DiffCloneable(ArrayList<? extends ArrayList<? extends IntVarCloneable>> rectangles, boolean profile) {
		super(rectangles, profile);
	}

	/**
	 * It specifies a diff constraint. 
	 * @param profile specifies is the profiles are used.
	 * @param rectangles list of rectangles which can not overlap in at least one dimension.
	 */
	public DiffCloneable(IntVarCloneable[][] rectangles, boolean profile) {
		super(rectangles, profile);
	}

	/**
	 * It constructs a diff constraint.
	 * @param origin1 list of variables denoting origin of the rectangle in the first dimension.
	 * @param origin2 list of variables denoting origin of the rectangle in the second dimension.
	 * @param length1 list of variables denoting length of the rectangle in the first dimension.
	 * @param length2 list of variables denoting length of the rectangle in the second dimension.
	 */
	public DiffCloneable(IntVarCloneable[] origin1, IntVarCloneable[] origin2, IntVarCloneable[] length1, IntVarCloneable[] length2) {
		super(origin1, origin2, length1, length2);
	}

	/**
	 * It constructs a diff constraint.
	 * @param o1 list of variables denoting origin of the rectangle in the first dimension.
	 * @param o2 list of variables denoting origin of the rectangle in the second dimension.
	 * @param l1 list of variables denoting length of the rectangle in the first dimension.
	 * @param l2 list of variables denoting length of the rectangle in the second dimension.
	 */
	public DiffCloneable(ArrayList<? extends IntVarCloneable> o1, ArrayList<? extends IntVarCloneable> o2, ArrayList<? extends IntVarCloneable> l1,
			ArrayList<? extends IntVarCloneable> l2) {
		super(o1, o2, l1, l2);
	}

	/**
	 * It constructs a diff constraint.
	 * @param o1 list of variables denoting origin of the rectangle in the first dimension.
	 * @param o2 list of variables denoting origin of the rectangle in the second dimension.
	 * @param l1 list of variables denoting length of the rectangle in the first dimension.
	 * @param l2 list of variables denoting length of the rectangle in the second dimension.
	 * @param profile it specifies if the profile should be computed and used.
	 */
	public DiffCloneable(IntVarCloneable[] o1, IntVarCloneable[] o2, IntVarCloneable[] l1, IntVarCloneable[] l2, boolean profile) {
		super(o1, o2, l1, l2, profile);
	}

	/**
	 * It constructs a diff constraint.
	 * @param o1 list of variables denoting origin of the rectangle in the first dimension.
	 * @param o2 list of variables denoting origin of the rectangle in the second dimension.
	 * @param l1 list of variables denoting length of the rectangle in the first dimension.
	 * @param l2 list of variables denoting length of the rectangle in the second dimension.
	 * @param profile it specifies if the profile should be computed and used.
	 */
	public DiffCloneable(ArrayList<? extends IntVarCloneable> o1, ArrayList<? extends IntVarCloneable> o2, ArrayList<? extends IntVarCloneable> l1,
			ArrayList<? extends IntVarCloneable> l2, boolean profile) {
		super(o1, o2, l1, l2, profile);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public DiffCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		return new DiffCloneable (cloneRectangles(this.rectangles, targetStore), this.doProfile);
	}
	
	/** Clones the rectangles into the target store
	 * @param rectangles 		the rectangles to be cloned
	 * @param targetStore 		the target store
	 * @return cloned rectangles
	 * @throws FailException 	if one of the variables has an empty domain
	 */
	static Rectangle[] cloneRectangles (Rectangle[] rectangles, StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the rectangles
		Rectangle[] rectangles2 = new Rectangle [rectangles.length];
		for (int i = rectangles.length - 1; i >= 0; i--) {
			Rectangle rectangle = rectangles[i];
			
			// Clone the origins
			IntVarCloneable[] origin2 = new IntVarCloneable [rectangle.origin.length];
			for (int j = origin2.length - 1; j >= 0; j--) 
				if ((origin2[j] = targetStore.findOrCloneInto((IntVarCloneable) rectangle.origin[j])).dom().isEmpty()) 
					throw StoreCloneable.failException;
			
			// Clone the lengths
			IntVarCloneable[] length2 = new IntVarCloneable [rectangle.length.length];
			for (int j = length2.length - 1; j >= 0; j--) 
				if ((length2[j] = targetStore.findOrCloneInto((IntVarCloneable) rectangle.length[j])).dom().isEmpty()) 
					throw StoreCloneable.failException;
			
			rectangles2[i] = new Rectangle (origin2, length2);
		}
		
		return rectangles2;
	}

}
