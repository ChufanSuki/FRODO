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

import java.util.List;

import org.jacop.constraints.cumulative.CumulativeUnary;
import org.jacop.core.FailException;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the CumulativeUnary constraint
 * @author Thomas Leaute
 */
public class CumulativeUnaryCloneable extends CumulativeUnary implements ConstraintCloneableInterface<CumulativeUnaryCloneable> {

	/** The start variables originally passed to the constructor */
	final private IntVarCloneable[] starts;
	
	/** The duration variables originally passed to the constructor */
	final private IntVarCloneable[] durations;
	
	/** The resource variables originally passed to the constructor */
	final private IntVarCloneable[] resources;
	
    /** Defines whether to do profile-based propagation */
    final private boolean doProfile;

    /**
     * It creates a cumulative constraint.
     *
     * @param starts    variables denoting starts of the tasks.
     * @param durations variables denoting durations of the tasks.
     * @param resources variables denoting resource usage of the tasks.
     * @param limit     the overall limit of resources which has to be used.
     */
	public CumulativeUnaryCloneable(IntVarCloneable[] starts, IntVarCloneable[] durations, IntVarCloneable[] resources, IntVarCloneable limit) {
		this(starts, durations, resources, limit, false);
	}

    /**
     * It creates a cumulative constraint.
     *
     * @param starts    variables denoting starts of the tasks.
     * @param durations variables denoting durations of the tasks.
     * @param resources variables denoting resource usage of the tasks.
     * @param limit     the overall limit of resources which has to be used.
     * @param doProfile defines whether to do profile-based propagation (true) or not (false); default is false
     */
	public CumulativeUnaryCloneable(IntVarCloneable[] starts, IntVarCloneable[] durations, IntVarCloneable[] resources, IntVarCloneable limit, boolean doProfile) {
		super(starts, durations, resources, limit, doProfile);
		this.starts = starts.clone();
		this.durations = durations.clone();
		this.resources = resources.clone();
        this.doProfile = doProfile;
	}

    /**
     * It creates a cumulative constraint.
     *
     * @param starts    variables denoting starts of the tasks.
     * @param durations variables denoting durations of the tasks.
     * @param resources variables denoting resource usage of the tasks.
     * @param limit     the overall limit of resources which has to be used.
     */
	public CumulativeUnaryCloneable(List<? extends IntVarCloneable> starts, List<? extends IntVarCloneable> durations,
			List<? extends IntVarCloneable> resources, IntVarCloneable limit) {
		this(starts.toArray(new IntVarCloneable [starts.size()]), 
				durations.toArray(new IntVarCloneable [durations.size()]), 
				resources.toArray(new IntVarCloneable [resources.size()]), 
				limit);
	}

    /**
     * It creates a cumulative constraint.
     *
     * @param starts    variables denoting starts of the tasks.
     * @param durations variables denoting durations of the tasks.
     * @param resources variables denoting resource usage of the tasks.
     * @param limit     the overall limit of resources which has to be used.
     * @param doProfile defines whether to do profile-based propagation (true) or not (false); default is false
     */
	public CumulativeUnaryCloneable(List<? extends IntVarCloneable> starts, List<? extends IntVarCloneable> durations,
			List<? extends IntVarCloneable> resources, IntVarCloneable limit, boolean doProfile) {
		this(starts.toArray(new IntVarCloneable [starts.size()]), 
				durations.toArray(new IntVarCloneable [durations.size()]), 
				resources.toArray(new IntVarCloneable [resources.size()]), 
				limit, doProfile);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public CumulativeUnaryCloneable cloneInto(StoreCloneable targetStore) 
			throws FailException {
		
		// Clone the start variables
		IntVarCloneable[] starts2 = new IntVarCloneable [this.starts.length];
		for (int i = this.starts.length - 1; i >= 0; i--) 
			if ((starts2[i] = targetStore.findOrCloneInto(this.starts[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		// Clone the durations variables
		IntVarCloneable[] durations2 = new IntVarCloneable [this.durations.length];
		for (int i = this.durations.length - 1; i >= 0; i--) 
			if ((durations2[i] = targetStore.findOrCloneInto(this.durations[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		// Clone the durations variables
		IntVarCloneable[] resources2 = new IntVarCloneable [this.resources.length];
		for (int i = this.resources.length - 1; i >= 0; i--) 
			if ((resources2[i] = targetStore.findOrCloneInto(this.resources[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		// Clone the limit variable
		IntVarCloneable limit2 = targetStore.findOrCloneInto((IntVarCloneable) this.limit);
		if (limit2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		return new CumulativeUnaryCloneable (starts2, durations2, resources2, limit2, this.doProfile);
	}

}
