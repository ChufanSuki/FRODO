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

package frodo2.algorithms.duct;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import frodo2.communication.Message;
import frodo2.communication.MessageType;
import frodo2.communication.MessageWith3Payloads;
import frodo2.solutionSpaces.AddableReal;

/**
 * @author Brammert Ottens, 7 jul. 2011
 */
public class COSTBmsg extends MessageWith3Payloads<String, AddableReal, AddableReal> {

	/**
	 * Empty constructor
	 */
	public COSTBmsg() {}
	
	/**
	 * Constructor
	 * 
	 * @param receiver	the name of the variable that is to receive this message
	 * @param cost		the estimated cost for the sampled value
	 * @param bound		the bound of the variable
	 */
	public COSTBmsg(String receiver, AddableReal cost, AddableReal bound) {
		this(Sampling.COST_MSG_TYPE, receiver, cost, bound);
	}
	
	/**
	 * Constructor
	 * 
	 * @param type 		the type of this message 
	 * @param receiver	the name of the variable that is to receive this message
	 * @param cost		the estimated cost for the sampled value
	 * @param bound		the bound of the variable
	 */
	public COSTBmsg(MessageType type, String receiver, AddableReal cost, AddableReal bound) {
		super(type, receiver, cost, bound);
	}
	
	/** @see Message#writeExternal(java.io.ObjectOutput) */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(this.getPayload1());
		if(this.getPayload2() != null) {
			out.writeBoolean(true);
			this.getPayload2().writeExternal(out);
		} else
			out.writeBoolean(true);
		
		if(this.getPayload3() != null) {
			out.writeBoolean(true);
			this.getPayload3().writeExternal(out);
		} else
			out.writeBoolean(true);
	}

	/** @see Message#readExternal(java.io.ObjectInput) */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		super.setPayload1((String) in.readObject());
		if(in.readBoolean()) {
			AddableReal c = new AddableReal(0);
			c.readExternal(in);
			super.setPayload2((AddableReal) c.readResolve());
		}
		if(in.readBoolean()) {
			AddableReal b = new AddableReal(0);
			b.readExternal(in);
			super.setPayload3((AddableReal) b.readResolve());
		}
	}
	
	/**
	 * @author Brammert Ottens, 7 jul. 2011
	 * @return the name of the variable that is to receive this message
	 */
	public String getReceiver() {
		return this.getPayload1();
	}
	
	/**
	 * @author Brammert Ottens, 7 jul. 2011
	 * @return the estimated cost for the sampled value
	 */
	public AddableReal getCost() {
		return this.getPayload2();
	}
	
	/**
	 * @author Brammert Ottens, 23 sep. 2011
	 * @return the bound of the variable
	 */
	public AddableReal getBound() {
		return this.getPayload3();
	}
}
