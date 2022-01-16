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

package org.jacop.util.fsm;

import java.util.HashMap;
import java.util.HashSet;

/** A cloneable FSM
 * @author Thomas Leaute
 */
public class FSMCloneable extends FSM implements Cloneable {

	/** Default constructor */
	public FSMCloneable() {
		super();
	}

	/** Constructor
	 * @param initState 	the initial state
	 * @param finalStates 	the final states
	 * @param allStates 	all states
	 */
	public FSMCloneable(FSMState initState, HashSet<FSMState> finalStates, HashSet<FSMState> allStates) {
		super(initState, finalStates, allStates);
	}

	/** @see java.lang.Object#clone() */
	@Override
	public FSMCloneable clone() {
		
		HashSet<FSMState> all2 = new HashSet<FSMState> ();
		FSMState init2 = null;
		HashSet<FSMState> final2 = new HashSet<FSMState> ();
		
		// First clone the states
		FSMState state2;
		HashMap<Integer, FSMState> newStates = new HashMap<Integer, FSMState> (); // indexed by the state ids
		for (FSMState state : this.allStates) {
			all2.add(state2 = new FSMState (state));
			
			newStates.put(state2.id, state2);
			
			if (state.equals(this.initState)) 
				init2 = state2;
			
			if (this.finalStates.contains(state)) 
				final2.add(state2);
		}
		
		// Then clone the transitions
		for (FSMState state : this.allStates) 
			for (FSMTransition trans : state.transitions) 
				newStates.get(state.id).transitions.add(new FSMTransition (trans.domain.cloneLight(), newStates.get(trans.successor.id)));
		
		return new FSMCloneable (init2, final2, all2);
	}

}
