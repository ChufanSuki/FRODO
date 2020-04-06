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

/** A cloneable version of the DisjointConditional constraint
 * @author Thomas Leaute
 */
public class DisjointConditionalCloneable extends DisjointConditional implements ConstraintCloneableInterface<DisjointConditionalCloneable> {

	/**
	 * It specifies a diff constraint. 
	 * @param rectangles list of rectangles which can not overlap in at least one dimension.
	 * @param exclusionList it is a list of exclusive items. Each item consists of two ints and a variable.
	 * @param doProfile should the constraint compute and use the profile functionality.
	 * 
	 */
	public DisjointConditionalCloneable(Rectangle[] rectangles, ExclusiveList exclusionList, boolean doProfile) {
		super(rectangles, exclusionList, doProfile);
	}

	/**
	 * It creates Disjoint conditional constraint.
	 * @param rectangles the rectangles within a constraint.
	 * @param exceptionIndices a list of pairs of conditionally overlaping rectangles.
	 * @param exceptionCondition a variable specifying if a corresponding pair is nonoverlapping.
	 * 
	 * @warning The variables must be of type IntVarCloneable
	 */
	public DisjointConditionalCloneable(List<List<? extends IntVar>> rectangles, List<List<Integer>> exceptionIndices,
			ArrayList<? extends IntVarCloneable> exceptionCondition) {
		super(rectangles, exceptionIndices, exceptionCondition);
	}

	/**
	 * It creates Disjoint conditional constraint.
	 * @param rectangles the rectangles within a constraint.
	 * @param exceptionIndices list of rectangles that may not be considered
	 * @param exceptionCondition conditions for rectangles that may not be considered
	 */
	public DisjointConditionalCloneable(IntVarCloneable[][] rectangles, List<List<Integer>> exceptionIndices,
			ArrayList<? extends IntVarCloneable> exceptionCondition) {
		super(rectangles, exceptionIndices, exceptionCondition);
	}

	/**
	 * It creates Disjoint conditional constraint.
	 * @param rectangles the rectangles within a constraint.
	 * @param exceptionIndices it specifies a list of pairs, where each pair specifies two rectangles which conditionally overlap. 
	 * @param exceptionCondition a variable specifying if a corresponding pair is nonoverlapping.
	 * @param profile it specifies if the profiles are used and computed within the constraint.
	 * 
	 * @warning The variables must be of type IntVarCloneable
	 */
	public DisjointConditionalCloneable(List<List<? extends IntVar>> rectangles,
			List<List<Integer>> exceptionIndices, ArrayList<? extends IntVarCloneable> exceptionCondition,
			boolean profile) {
		super(rectangles, exceptionIndices, exceptionCondition, profile);
	}

	/**
	 * It creates Disjoint conditional constraint.
	 * @param rectangles the rectangles within a constraint.
	 * @param exceptionIndices list of rectangles that may not be considered
	 * @param exceptionCondition conditions for rectangles that may not be considered
	 * @param profile it specifies if the profiles are being computed and used within that constraint.
	 */
	public DisjointConditionalCloneable(IntVarCloneable[][] rectangles, List<List<Integer>> exceptionIndices,
			List<? extends IntVarCloneable> exceptionCondition, boolean profile) {
		super(rectangles, exceptionIndices, exceptionCondition, profile);
	}

	/**
	 * It constructs a disjoint conditional constraint. 
	 * @param o1 variables specifying the origin in the first dimension.
	 * @param o2 variables specifying the origin in the second dimension.
	 * @param l1 variables specifying the length in the first dimension.
	 * @param l2 variables specifying the length in the second dimension.
	 * @param exceptionIndices it specifies a list of pairs, where each pair specifies two rectangles which conditionally overlap. 
	 * @param exceptionCondition a variable specifying if a corresponding pair is nonoverlapping.
	 */
	public DisjointConditionalCloneable(ArrayList<? extends IntVarCloneable> o1, ArrayList<? extends IntVarCloneable> o2,
			ArrayList<? extends IntVarCloneable> l1, ArrayList<? extends IntVarCloneable> l2,
			List<List<Integer>> exceptionIndices, ArrayList<? extends IntVarCloneable> exceptionCondition) {
		super(o1, o2, l1, l2, exceptionIndices, exceptionCondition);
	}

	/**
	 * It constructs a disjoint conditional constraint. 
	 * @param o1 variables specifying the origin in the first dimension.
	 * @param o2 variables specifying the origin in the second dimension.
	 * @param l1 variables specifying the length in the first dimension.
	 * @param l2 variables specifying the length in the second dimension.
	 * @param exceptionIndices it specifies a list of pairs, where each pair specifies two rectangles which conditionally overlap. 
	 * @param exceptionCondition a variable specifying if a corresponding pair is nonoverlapping.
	 */
	public DisjointConditionalCloneable(IntVarCloneable[] o1, IntVarCloneable[] o2, IntVarCloneable[] l1, IntVarCloneable[] l2,
			List<List<Integer>> exceptionIndices, ArrayList<? extends IntVarCloneable> exceptionCondition) {
		super(o1, o2, l1, l2, exceptionIndices, exceptionCondition);
	}

	/**
	 * It constructs a disjoint conditional constraint. 
	 * @param o1 variables specifying the origin in the first dimension.
	 * @param o2 variables specifying the origin in the second dimension.
	 * @param l1 variables specifying the length in the first dimension.
	 * @param l2 variables specifying the length in the second dimension.
	 * @param exceptionIndices it specifies a list of pairs, where each pair specifies two rectangles which conditionally overlap. 
	 * @param exceptionCondition a variable specifying if a corresponding pair is nonoverlapping.
	 * @param profile it specifies if the profiles are being computed and used within a constraint.
	 */
	public DisjointConditionalCloneable(ArrayList<? extends IntVarCloneable> o1, ArrayList<? extends IntVarCloneable> o2,
			ArrayList<? extends IntVarCloneable> l1, ArrayList<? extends IntVarCloneable> l2,
			List<List<Integer>> exceptionIndices, ArrayList<? extends IntVarCloneable> exceptionCondition,
			boolean profile) {
		super(o1, o2, l1, l2, exceptionIndices, exceptionCondition, profile);
	}

	/**
	 * It constructs a disjoint conditional constraint. 
	 * @param o1 variables specifying the origin in the first dimension.
	 * @param o2 variables specifying the origin in the second dimension.
	 * @param l1 variables specifying the length in the first dimension.
	 * @param l2 variables specifying the length in the second dimension.
	 * @param exceptionIndices list of rectangles that may not be considered
	 * @param exceptionCondition conditions for rectangles that may not be considered
	 * @param profile it specifies if the profiles are being used and computed within that constraint.
	 */
	public DisjointConditionalCloneable(IntVarCloneable[] o1, IntVarCloneable[] o2, IntVarCloneable[] l1, IntVarCloneable[] l2,
			List<List<Integer>> exceptionIndices, ArrayList<? extends IntVarCloneable> exceptionCondition,
			boolean profile) {
		super(o1, o2, l1, l2, exceptionIndices, exceptionCondition, profile);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public DisjointConditionalCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the exclusion list
		ExclusiveList exclusionList2 = new ExclusiveList ();
		for (ExclusiveItem item : super.exclusionList) {
			
			IntVarCloneable condition2 = targetStore.findOrCloneInto((IntVarCloneable) item.cond);
			if (condition2.dom().isEmpty()) 
				throw StoreCloneable.failException;
			
			exclusionList2.add(new ExclusiveItem (item.i1, item.i2, condition2));
		}
		
		return new DisjointConditionalCloneable (DiffCloneable.cloneRectangles(rectangles, targetStore), exclusionList2, super.doProfile);
	}

}
