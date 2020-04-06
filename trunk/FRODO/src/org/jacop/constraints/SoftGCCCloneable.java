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
import java.util.Arrays;
import java.util.List;

import org.jacop.core.FailException;
import org.jacop.core.IntVar;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the SoftGCC constraint
 * @author Thomas Leaute
 */
public class SoftGCCCloneable extends SoftGCC implements ConstraintCloneableInterface<SoftGCCCloneable>, DecomposedConstraintCloneableInterface {

	/** Used to increment the id of constraints */
	private static int idNbr = 0;
	
	/** This constraint's ID/name */
	private final String id;

	/**
	 * It specifies soft-GCC constraint. 
	 * 
	 * @param xVars variables over which counting takes place.
	 * @param hardCounters counter variables for different values being counted. (hard)
	 * @param softCounters counter variables that may be violated.
	 * @param costVar a cost variable specifying the cost of violations.
	 * @param violationMeasure it is only accepted to use Value_Based violation measure.
	 */
	public SoftGCCCloneable(IntVarCloneable[] xVars, IntVarCloneable[] hardCounters, IntVarCloneable[] softCounters, IntVarCloneable costVar, 
			ViolationMeasure violationMeasure) {
		super(xVars, hardCounters, softCounters, costVar, violationMeasure);
		this.id = this.getClass().getSimpleName() + idNbr++;
	}

	/**
	 * It specifies soft-GCC constraint. 
	 * 
	 * @param xVars variables over which counting takes place.
	 * @param hardCounters counter variables for different values being counted. (hard)
	 * @param countedValue it specifies values which occurrence is being counted.
	 * @param softCounters counter variables for different values being counted. (soft)
	 * @param costVar a cost variable specifying the cost of violations.
	 * @param violationMeasure it is only accepted to use Value_Based violation measure.
	 */
	public SoftGCCCloneable(IntVarCloneable[] xVars, IntVarCloneable[] hardCounters, int[] countedValue, IntVarCloneable[] softCounters,
			IntVarCloneable costVar, ViolationMeasure violationMeasure) {
		super(xVars, hardCounters, countedValue, softCounters, costVar, violationMeasure);
		this.id = this.getClass().getSimpleName() + idNbr++;
	}

	/**
	 * It specifies soft-GCC constraint. 
	 * 
	 * @param xVars variables over which counting takes place.
	 * @param hardCounters counter variables for different values being counted. (hard)
	 * @param softLowerBound it specifies constraint what is the minimal number of occurrences. (soft)
	 * @param softUpperBound it specifies constraint what is the maximal number of occurrences. (soft)
	 * @param costVar a cost variable specifying the cost of violations.
	 * @param violationMeasure it is only accepted to use Value_Based violation measure.
	 */	
	public SoftGCCCloneable(IntVarCloneable[] xVars, IntVarCloneable[] hardCounters, int[] softLowerBound, int[] softUpperBound, IntVarCloneable costVar, 
			ViolationMeasure violationMeasure) {
		super(xVars, hardCounters, softLowerBound, softUpperBound, costVar, violationMeasure);
		this.id = this.getClass().getSimpleName() + idNbr++;
	}

	/**
	 * It specifies soft-GCC constraint. 
	 * 
	 * @param xVars variables over which counting takes place.
	 * @param hardLowerBound it specifies constraint what is the minimal number of occurrences. (hard)
	 * @param hardUpperBound it specifies constraint what is the maximal number of occurrences. (hard)
	 * @param softCounters counter variables for different values being counted. (soft)
	 * @param costVar a cost variable specifying the cost of violations.
	 * @param violationMeasure it is only accepted to use Value_Based violation measure.
	 */
	public SoftGCCCloneable(IntVarCloneable[] xVars, int[] hardLowerBound, int[] hardUpperBound, IntVarCloneable[] softCounters, IntVarCloneable costVar, 
			ViolationMeasure violationMeasure) {
		super(xVars, hardLowerBound, hardUpperBound, softCounters, costVar, violationMeasure);
		this.id = this.getClass().getSimpleName() + idNbr++;
	}

	/**
	 * It specifies soft-GCC constraint. 
	 * 
	 * @param xVars variables over which counting takes place.
	 * @param hardCounters counter variables for different values being counted. Their domain specify hard constraints on the occurrences.
	 * @param countedValue it specifies values which occurrence is being counted.
	 * @param softLowerBound it specifies constraint what is the minimal number of occurrences.
	 * @param softUpperBound it specifies constraint what is the maximal number of occurrences.
	 * @param costVar a cost variable specifying the cost of violations.
	 * @param violationMeasure it is only accepted to use Value_Based violation measure.
	 */
	public SoftGCCCloneable(IntVarCloneable[] xVars, IntVarCloneable[] hardCounters, int[] countedValue, int[] softLowerBound,
			int[] softUpperBound, IntVarCloneable costVar, ViolationMeasure violationMeasure) {
		super(xVars, hardCounters, countedValue, softLowerBound, softUpperBound, costVar, violationMeasure);
		this.id = this.getClass().getSimpleName() + idNbr++;
	}

	/**
	 * It specifies soft-GCC constraint. 
	 * 
	 * @param xVars variables over which counting takes place.
	 * @param hardLowerBound it specifies constraint what is the minimal number of occurrences. (hard)
	 * @param hardUpperBound it specifies constraint what is the maximal number of occurrences. (hard)
	 * @param countedValue it specifies values which occurrence is being counted.
	 * @param softCounters it specifies the number of occurrences (soft). 
	 * @param costVar a cost variable specifying the cost of violations.
	 * @param violationMeasure it is only accepted to use Value_Based violation measure.
	 */
	public SoftGCCCloneable(IntVarCloneable[] xVars, int[] hardLowerBound, int[] hardUpperBound, int[] countedValue,
			IntVarCloneable[] softCounters, IntVarCloneable costVar, ViolationMeasure violationMeasure) {
		super(xVars, hardLowerBound, hardUpperBound, countedValue, softCounters, costVar, violationMeasure);
		this.id = this.getClass().getSimpleName() + idNbr++;
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public SoftGCCCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the the xVars array
		IntVarCloneable[] xVars2 = new IntVarCloneable [this.xVars.length];
		for (int i = this.xVars.length - 1; i >= 0; i--) 
			if ((xVars2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.xVars[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;

		// Clone the the hardCounters array
		IntVarCloneable[] hardCounters2 = new IntVarCloneable [this.hardCounters.length];
		for (int i = this.hardCounters.length - 1; i >= 0; i--) 
			if ((hardCounters2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.hardCounters[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;

		// Clone the the softCounters array
		IntVarCloneable[] softCounters2 = new IntVarCloneable [this.softCounters.length];
		for (int i = this.softCounters.length - 1; i >= 0; i--) 
			if ((softCounters2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.softCounters[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;

		IntVarCloneable costVar2 = targetStore.findOrCloneInto((IntVarCloneable) this.costVar);
		if (costVar2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		return new SoftGCCCloneable (xVars2, hardCounters2, softCounters2, costVar2, this.violationMeasure);
	}

	/** @see org.jacop.constraints.DecomposedConstraintCloneableInterface#arguments() */
	@Override
	public List<IntVar> arguments() {
		
		ArrayList<IntVar> args = new ArrayList<IntVar> (1 + this.hardCounters.length + this.softCounters.length + this.xVars.length);
		
		args.add(this.costVar);
		args.addAll(Arrays.asList(this.hardCounters));
		args.addAll(Arrays.asList(this.softCounters));
		args.addAll(Arrays.asList(this.xVars));
		
		return args;
	}

	/** @see org.jacop.constraints.DecomposedConstraintCloneableInterface#id() */
	@Override
	public String id() {
		return this.id;
	}

}
