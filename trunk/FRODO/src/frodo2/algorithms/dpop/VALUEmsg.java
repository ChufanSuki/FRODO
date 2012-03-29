/*
FRODO: a FRamework for Open/Distributed Optimization
Copyright (C) 2008-2012  Thomas Leaute, Brammert Ottens & Radoslaw Szymanek

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
<http://frodo2.sourceforge.net/>
*/

package frodo2.algorithms.dpop;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

import frodo2.communication.MessageWith3Payloads;
import frodo2.solutionSpaces.Addable;

/** VALUE message
 * @param <Val> the type used for variable values
 */
public class VALUEmsg < Val extends Addable<Val> > 
extends MessageWith3Payloads<String, String[], Val[]> implements Externalizable {
	
	/** Used for serialization */
	private static final long serialVersionUID = 5782176612732670626L;
	
	/** Empty constructor */
	public VALUEmsg () {
		super.type = VALUEpropagation.VALUE_MSG_TYPE;
	}

	/** Constructor 
	 * @param dest 			destination variable
	 * @param variables 	array of variables in \a dest's separator
	 * @param values 		array of values for the variables in \a variables, in the same order
	 */
	public VALUEmsg(String dest, String[] variables, Val[] values) {
		super(VALUEpropagation.VALUE_MSG_TYPE, dest, variables, values);
	}

	/** @see java.io.Externalizable#writeExternal(java.io.ObjectOutput) */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(super.getPayload1());
		out.writeObject(super.getPayload2());
		out.writeObject(super.getPayload3());
	}

	/** @see java.io.Externalizable#readExternal(java.io.ObjectInput) */
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.setPayload1((String) in.readObject());
		super.setPayload2((String[]) in.readObject());
		super.setPayload3((Val[]) in.readObject());
	}

	/** @return the destination variable */
	public String getDest() {
		return super.getPayload1();
	}

	/** @return the separator */
	public String[] getVariables() {
		return super.getPayload2();
	}

	/** @return the values for the variables in the separator */
	public Val[] getValues() {
		return super.getPayload3();
	}
	
	/** @see frodo2.communication.Message#toString() */
	public String toString () {
		return "Message(type = `" + this.getType() + "')\n\tdest: " + super.getPayload1() + "\n\tvars: " + Arrays.toString(super.getPayload2()) + 
		"\n\tvals: " + Arrays.toString(super.getPayload3());
	}
}