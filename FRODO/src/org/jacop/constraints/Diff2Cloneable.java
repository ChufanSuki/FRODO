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
import java.util.List;

import org.jacop.core.FailException;
import org.jacop.core.IntVar;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the Diff2 constraint
 * @author Thomas Leaute
 */
public class Diff2Cloneable extends Diff2 implements ConstraintCloneableInterface<Diff2Cloneable> {

	/**
	 * It creates a diff2 constraint.
	 * @param rectangles list of rectangles with origins and lengths in both dimensions.
	 */
	public Diff2Cloneable(ArrayList<? extends ArrayList<? extends IntVarCloneable>> rectangles) {
		super(rectangles);
	}

	/**
	 * It creates a diff2 constraint.
	 * @param rectangles list of rectangles with origins and lengths in both dimensions.
	 */
	public Diff2Cloneable(IntVarCloneable[][] rectangles) {
		super(rectangles);
	}

	/**
	 * It creates a diff2 constraint.
	 * @param rectangles list of rectangles with origins and lengths in both dimensions.
	 * @param profile specifies if the profile is computed and used.
	 */
	public Diff2Cloneable(ArrayList<? extends ArrayList<? extends IntVarCloneable>> rectangles, boolean profile) {
		super(rectangles, profile);
	}

	/**
	 * It creates a diff2 constraint.
	 * @param rectangles list of rectangles with origins and lengths in both dimensions.
	 * @param profile specifies if the profile is computed and used.
	 */
	public Diff2Cloneable(IntVarCloneable[][] rectangles, boolean profile) {
		super(rectangles, profile);
	}

	/**
	 * Conditional Diff2. The rectangles that are specified on the list
	 * Exclusive are excluded from checking that they must be non-overlapping.
	 * The rectangles are numbered from 1, for example list [[1,3], [3,4]]
	 * specifies that rectangles 1 and 3 as well as 3 and 4 can overlap each
	 * other.
	 * 
	 * @param rect  - list of rectangles, each rectangle represented by a list of variables.
	 * @param exclusiveList - list of rectangles pairs which can overlap.
	 * 
	 * @warning The input variables should be of type IntVarCloneable
	 */
	public Diff2Cloneable(List<List<IntVar>> rect, List<List<Integer>> exclusiveList) {
		super(rect, exclusiveList);
	}

	/**
	 * Conditional Diff2. The rectangles that are specified on the list
	 * Exclusive are excluded from checking that they must be non-overlapping.
	 * The rectangles are numbered from 1, for example list [[1,3], [3,4]]
	 * specifies that rectangles 1 and 3 as well as 3 and 4 can overlap each
	 * other.
	 * 
	 * @param rect  - list of rectangles, each rectangle represented by a list of variables.
	 * @param exclusive - list of rectangles pairs which can overlap.
	 */
	public Diff2Cloneable(IntVarCloneable[][] rect, List<List<Integer>> exclusive) {
		super(rect, exclusive);
	}

	/**
	 * Conditional Diff2. The rectangles that are specified on the list
	 * Exclusive list is specified contains pairs of rectangles 
	 * that are excluded from checking that they must be non-overlapping.
	 * The rectangles are numbered from 1, for example list [1, 3, 3, 4]
	 * specifies that rectangles 1 and 3 as well as 3 and 4 can overlap each
	 * other.
	 * 
	 * @param rectangles a list of rectangles.
	 * @param exclusiveList a list denoting the pair of rectangles, which can overlap
	 * @param doProfile should profile be computed and used.
	 * 
	 */
	public Diff2Cloneable(Rectangle[] rectangles, int[] exclusiveList, boolean doProfile) {
		super(rectangles, exclusiveList, doProfile);
	}

	/**
	 * It creates a diff2 constraint.
	 * @param o1 list of variables denoting the origin in the first dimension.
	 * @param o2 list of variables denoting the origin in the second dimension.
	 * @param l1 list of variables denoting the length in the first dimension.
	 * @param l2 list of variables denoting the length in the second dimension.
	 */
	public Diff2Cloneable(ArrayList<? extends IntVarCloneable> o1, ArrayList<? extends IntVarCloneable> o2,
			ArrayList<? extends IntVarCloneable> l1, ArrayList<? extends IntVarCloneable> l2) {
		super(o1, o2, l1, l2);
	}

	/**
	 * It creates a diff2 constraint.
	 * @param o1 list of variables denoting the origin in the first dimension.
	 * @param o2 list of variables denoting the origin in the second dimension.
	 * @param l1 list of variables denoting the length in the first dimension.
	 * @param l2 list of variables denoting the length in the second dimension.
	 */
	public Diff2Cloneable(IntVarCloneable[] o1, IntVarCloneable[] o2, IntVarCloneable[] l1, IntVarCloneable[] l2) {
		super(o1, o2, l1, l2);
	}

	/**
	 * It creates a diff2 constraint.
	 * @param o1 list of variables denoting the origin in the first dimension.
	 * @param o2 list of variables denoting the origin in the second dimension.
	 * @param l1 list of variables denoting the length in the first dimension.
	 * @param l2 list of variables denoting the length in the second dimension.
	 * @param profile specifies if the profile should be computed.
	 */
	public Diff2Cloneable(ArrayList<IntVarCloneable> o1, ArrayList<IntVarCloneable> o2, ArrayList<IntVarCloneable> l1, ArrayList<IntVarCloneable> l2,
			boolean profile) {
		super(o1.toArray(new IntVarCloneable [o1.size()]), o2.toArray(new IntVarCloneable [o2.size()]), 
				l1.toArray(new IntVarCloneable [l1.size()]), l2.toArray(new IntVarCloneable [l2.size()]), profile);
	}

	/**
	 * It creates a diff2 constraint.
	 * @param o1 list of variables denoting the origin in the first dimension.
	 * @param o2 list of variables denoting the origin in the second dimension.
	 * @param l1 list of variables denoting the length in the first dimension.
	 * @param l2 list of variables denoting the length in the second dimension.
	 * @param profile specifies if the profile should be computed.
	 */	
	public Diff2Cloneable(IntVarCloneable[] o1, IntVarCloneable[] o2, IntVarCloneable[] l1, IntVarCloneable[] l2, boolean profile) {
		super(o1, o2, l1, l2, profile);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public Diff2Cloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		return new Diff2Cloneable (DiffCloneable.cloneRectangles(this.rectangles, targetStore), super.exclusiveList, super.doProfile);
	}

}
