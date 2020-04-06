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

/** A cloneable version of the Cumulative constraint
 * @author Thomas Leaute
 */
public class CumulativeCloneable extends Cumulative implements ConstraintCloneableInterface<CumulativeCloneable> {

    /**
     * It specifies if the edge finding algorithm should be used.
     */
    private boolean doEdgeFinding = true;

    /**
     * It specifies if the profiles should be computed to propagate
     * onto limit variable.
     */
    private boolean doProfile = true;

    /**
     * It specifies if the data from profiles should be used to propagate
     * onto limit variable.
     */
    private boolean setLimit = true;

	/**
	 * It creates a cumulative constraint.
	 * @param starts variables denoting starts of the tasks.
	 * @param durations variables denoting durations of the tasks.
	 * @param resources variables denoting resource usage of the tasks.
	 * @param limit the overall limit of resources which has to be used.
	 */
	public CumulativeCloneable(ArrayList<? extends IntVarCloneable> starts, ArrayList<? extends IntVarCloneable> durations,
			ArrayList<? extends IntVarCloneable> resources, IntVarCloneable limit) {
		super(starts, durations, resources, limit);
	}

	/**
	 * It creates a cumulative constraint.
	 * @param starts variables denoting starts of the tasks.
	 * @param durations variables denoting durations of the tasks.
	 * @param resources variables denoting resource usage of the tasks.
	 * @param limit the overall limit of resources which has to be used.
	 */
	public CumulativeCloneable(IntVarCloneable[] starts, IntVarCloneable[] durations, IntVarCloneable[] resources, IntVarCloneable limit) {
		super(starts, durations, resources, limit);
	}

	/**
	 * It creates a cumulative constraint.
	 * @param starts variables denoting starts of the tasks.
	 * @param durations variables denoting durations of the tasks.
	 * @param resources variables denoting resource usage of the tasks.
	 * @param limit the overall limit of resources which has to be used.
	 * @param edgeFinding true if edge finding algorithm should be used.
	 */
	public CumulativeCloneable(ArrayList<? extends IntVarCloneable> starts, ArrayList<? extends IntVarCloneable> durations,
			ArrayList<? extends IntVarCloneable> resources, IntVarCloneable limit, boolean edgeFinding) {
		super(starts, durations, resources, limit, edgeFinding);
		this.doEdgeFinding = edgeFinding;
	}

	/**
	 * It creates a cumulative constraint.
	 * @param starts variables denoting starts of the tasks.
	 * @param durations variables denoting durations of the tasks.
	 * @param resources variables denoting resource usage of the tasks.
	 * @param limit the overall limit of resources which has to be used.
	 * @param edgeFinding true if edge finding algorithm should be used.
	 */
	public CumulativeCloneable(IntVarCloneable[] starts, IntVarCloneable[] durations, IntVarCloneable[] resources, IntVarCloneable limit,
			boolean edgeFinding) {
		super(starts, durations, resources, limit, edgeFinding);
		this.doEdgeFinding = edgeFinding;
	}

	/**
	 * It creates a cumulative constraint.
	 * @param starts variables denoting starts of the tasks.
	 * @param durations variables denoting durations of the tasks.
	 * @param resources variables denoting resource usage of the tasks.
	 * @param limit the overall limit of resources which has to be used.
	 * @param doEdgeFinding true if edge finding algorithm should be used.
	 * @param doProfile specifies if the profiles should be computed in order to reduce limit variable.
	 */
	public CumulativeCloneable(IntVarCloneable[] starts, IntVarCloneable[] durations, IntVarCloneable[] resources, IntVarCloneable limit, 
			boolean doEdgeFinding, boolean doProfile) {
		super(starts, durations, resources, limit, doEdgeFinding, doProfile);
		this.doEdgeFinding = doEdgeFinding;
		this.doProfile = doProfile;
	}

	/**
	 * It creates a cumulative constraint.
	 * @param starts variables denoting starts of the tasks.
	 * @param durations variables denoting durations of the tasks.
	 * @param resources variables denoting resource usage of the tasks.
	 * @param limit the overall limit of resources which has to be used.
	 * @param edgeFinding true if edge finding algorithm should be used.
	 * @param profile specifies if the profiles should be computed in order to reduce limit variable.
	 */
	public CumulativeCloneable(ArrayList<? extends IntVarCloneable> starts, ArrayList<? extends IntVarCloneable> durations,
			ArrayList<? extends IntVarCloneable> resources, IntVarCloneable limit, boolean edgeFinding, boolean profile) {
		super(starts, durations, resources, limit, edgeFinding, profile);
		this.doEdgeFinding = edgeFinding;
		this.doProfile = profile;
	}

	/**
	 * It creates a cumulative constraint.
	 * @param starts variables denoting starts of the tasks.
	 * @param durations variables denoting durations of the tasks.
	 * @param resources variables denoting resource usage of the tasks.
	 * @param limit the overall limit of resources which has to be used.
	 * @param doEdgeFinding true if edge finding algorithm should be used.
	 * @param doProfile specifies if the profiles should be computed in order to reduce limit variable.
	 * @param setLimit specifies if limit variable will be prunded.
	 */
	public CumulativeCloneable(IntVarCloneable[] starts, IntVarCloneable[] durations, IntVarCloneable[] resources, IntVarCloneable limit,
			boolean doEdgeFinding, boolean doProfile, boolean setLimit) {
		super(starts, durations, resources, limit, doEdgeFinding, doProfile, setLimit);
		this.doEdgeFinding = doEdgeFinding;
		this.doProfile = doProfile;
		this.setLimit = setLimit;
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public CumulativeCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the the starts array
		IntVarCloneable[] starts2 = new IntVarCloneable [this.starts.length];
		for (int i = this.starts.length - 1; i >= 0; i--) 
			if ((starts2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.starts[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;

		// Clone the the durations array
		IntVarCloneable[] durations2 = new IntVarCloneable [this.durations.length];
		for (int i = this.durations.length - 1; i >= 0; i--) 
			if ((durations2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.durations[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;

		// Clone the the resources array
		IntVarCloneable[] resources2 = new IntVarCloneable [this.resources.length];
		for (int i = this.resources.length - 1; i >= 0; i--) 
			if ((resources2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.resources[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;

		IntVarCloneable limit2 = targetStore.findOrCloneInto((IntVarCloneable) this.limit);
		if (limit2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		return new CumulativeCloneable (starts2, durations2, resources2, limit2, this.doEdgeFinding, this.doProfile, this.setLimit);
	}

}
