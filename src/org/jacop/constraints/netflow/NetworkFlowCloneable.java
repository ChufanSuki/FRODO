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

package org.jacop.constraints.netflow;

import java.util.HashMap;

import org.jacop.constraints.ConstraintCloneableInterface;
import org.jacop.constraints.netflow.NetworkFlow;
import org.jacop.constraints.netflow.simplex.Arc;
import org.jacop.constraints.netflow.simplex.Node;
import org.jacop.core.FailException;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the NetworkFlow constraint
 * @author Thomas Leaute
 */
public class NetworkFlowCloneable extends NetworkFlow implements ConstraintCloneableInterface<NetworkFlowCloneable> {

	/** Used to increment the id of constraints */
	private static int idNbr = 0;
	
	/** This constraint's ID/name */
	private final String id;

	/** Constructor 
	 * @param builder 	the builder
	 */
	public NetworkFlowCloneable(NetworkBuilder builder) {
		super(builder);
		this.id = this.getClass().getSimpleName() + idNbr++;
	}

	/** @see org.jacop.constraints.netflow.NetworkFlow#id() */
	@Override
	public String id() {
		return this.id;
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public NetworkFlowCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		IntVarCloneable costVariable2 = targetStore.findOrCloneInto((IntVarCloneable) this.costVariable);
		if (costVariable2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		// Reverse-engineer the NetworkBuilder that was used to create this NetworkFlow 
		NetworkBuilder builder = new NetworkBuilder (costVariable2);
		
		// Clone the nodes 
		HashMap<String, Node> nodeMap = new HashMap<String, Node> ();
		for (Node node : this.network.nodes) 
			nodeMap.put(node.name, builder.addNode(node.name, node.balance));
		
		// Clone the arcs
		Node head, tail;
		IntVarCloneable wVar = null, xVar = null;
		for (Arc arc : this.network.allArcs) {
			
			tail = nodeMap.get(arc.tail().name);
			head = nodeMap.get(arc.head.name);
			
			if (arc.companion != null) {
				wVar = (IntVarCloneable) arc.companion.wVar;
				xVar = (IntVarCloneable) arc.companion.xVar;
			}
			
			if (xVar == null) {
				
				// Reverse-engineer the lower and upper capacities 
				int lowerCapacity = arc.companion == null ? 0 : arc.companion.flowOffset;
				int upperCapacity = lowerCapacity + arc.capacity;
				
				if (wVar == null) // wVar == null == xVar
					builder.addArc(tail, head, 
							arc.cost, 
							lowerCapacity, 
							upperCapacity);
				
				else { // xVar == null != wVar

					IntVarCloneable wVar2 = targetStore.findOrCloneInto((IntVarCloneable) wVar);
					if (wVar2.dom().isEmpty()) 
						throw StoreCloneable.failException;
					
					builder.addArc(tail, head, wVar2, lowerCapacity, upperCapacity);
				}
				
			} else if (wVar == null) { // wVar == null != xVar

				IntVarCloneable xVar2 = targetStore.findOrCloneInto((IntVarCloneable) xVar);
				if (xVar2.dom().isEmpty()) 
					throw StoreCloneable.failException;
				
				builder.addArc(tail, head, arc.cost, xVar2);
			}
			
			else { // xVar != null != wVar

				IntVarCloneable wVar2 = targetStore.findOrCloneInto((IntVarCloneable) wVar);
				if (wVar2.dom().isEmpty()) 
					throw StoreCloneable.failException;
				
				IntVarCloneable xVar2 = targetStore.findOrCloneInto((IntVarCloneable) xVar);
				if (xVar2.dom().isEmpty()) 
					throw StoreCloneable.failException;
				
				builder.addArc(tail, head, wVar2, xVar2);
			}
		}
		
		return new NetworkFlowCloneable (builder);
	}

}
