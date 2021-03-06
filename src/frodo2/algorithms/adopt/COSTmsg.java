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

package frodo2.algorithms.adopt;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import frodo2.communication.Message;
import frodo2.solutionSpaces.Addable;

/**
 * The message used to send a VALUE message to a variable's
 * pseudochildren in ADOPT
 * 
 * @param <Val> the type used for variable values
 * @param <U> the type used for utility values
 */
public class COSTmsg<Val extends Addable<Val>, U extends Addable<U>>
		extends Message implements Externalizable {

	/** The sending variable */
	private String sender;

	/** The receiving variable */
	String receiver;

	/** The context in which this message was created */
	private HashMap<String, Val> context;

	/** The upper bound */
	private U lb;

	/** The lower bound */
	private U ub;
	
	/** Empty constructor */
	public COSTmsg () {
		super (ADOPT.Original.COST_MSG_TYPE);
	}

	/**
	 * Constructor
	 * 
	 * @param sender the sender variable
	 * @param receiver the recipient variable
	 * @param context the context
	 * @param lb  the lower bound
	 * @param ub  the upper bound
	 */
	public COSTmsg(String sender, String receiver,
			HashMap<String, Val> context, U lb, U ub) {
		super(ADOPT.Original.COST_MSG_TYPE);
		this.sender = sender;
		this.receiver = receiver;
		this.context = new HashMap<String, Val> (context);
		this.lb = lb;
		this.ub = ub;
	}
	
	/** @see Message#toString() */
	@Override
	public String toString () {
		
		StringBuilder builder = new StringBuilder (super.toString());
		builder.append("\n\t sender = " + this.sender);
		builder.append("\n\t receiver = " + this.receiver);
		builder.append("\n\t context = " + this.context);
		builder.append("\n\t lb = " + this.lb);
		builder.append("\n\t ub = " + this.ub);
		
		return builder.toString();
	}

	/** @see java.io.Externalizable#writeExternal(java.io.ObjectOutput) */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(this.sender);
		out.writeObject(this.receiver);
		
		// Serialize the context manually
		final int size = this.context.size();
		assert size < Short.MAX_VALUE;
		out.writeShort(size); // size of context
		if (size > 0) {
			Iterator< Map.Entry<String, Val> > iter = this.context.entrySet().iterator();
			Map.Entry<String, Val> entry = iter.next();
			out.writeObject(entry.getKey()); // first variable
			out.writeObject(entry.getValue()); // value of first variable
			final boolean externalize = entry.getValue().externalize();
			
			// Serialize the rest
			while (iter.hasNext()) {
				entry = iter.next();
				out.writeObject(entry.getKey());
				if (externalize) 
					entry.getValue().writeExternal(out);
				else 
					out.writeObject(entry.getValue());
			}
		}
		
		// Write the bounds
		out.writeObject(this.lb);
		if (this.lb.externalize()) 
			this.ub.writeExternal(out);
		else 
			out.writeObject(this.ub);
	}

	/** @see java.io.Externalizable#readExternal(java.io.ObjectInput) */
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.sender = (String) in.readObject();
		this.receiver = (String) in.readObject();
		
		// Read the context
		short nbrVars = in.readShort();
		this.context = new HashMap<String, Val> (nbrVars);
		if (nbrVars > 0) {
			// Read the info about the first variable
			String var = (String) in.readObject();
			Val val = (Val) in.readObject();
			this.context.put(var, val);
			final boolean externalize = val.externalize();
			
			// Read the remaining variables
			for (short i = 1; i < nbrVars; i++) {
				var = (String) in.readObject();
				if (externalize) {
					val = val.getZero();
					val.readExternal(in);
					val = (Val) val.readResolve();
				} else 
					val = (Val) in.readObject();
				this.context.put(var, val);
			}
		}
		
		// Read the bounds
		this.lb = (U) in.readObject();
		if (this.lb.externalize()) {
			this.ub = this.lb.getZero();
			this.ub.readExternal(in);
			this.ub = (U) this.ub.readResolve();
		} else 
			this.ub = (U) in.readObject();
	}

	/** Use for serialization */
	private static final long serialVersionUID = 2153239219905600645L;

	/** @see java.lang.Object#equals(java.lang.Object) */
	@SuppressWarnings("unchecked")
	public boolean equals(Object msg) {
		if (msg == null)
			return false;
		
		Message msgCast2 = (Message) msg;
		if (! this.getType().equals(msgCast2.getType())) 
			return false;
		
		COSTmsg<Val, U> msgCast = (COSTmsg<Val, U>) msg;
		return sender.equals(msgCast.sender)
				&& receiver.equals(msgCast.receiver)
				&& context.equals(msgCast.context)
				&& lb.equals(msgCast.lb) && ub.equals(msgCast.ub);
	}

	/**
	 * Getter function
	 * 
	 * @return the variable that sent this message
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * Getter function
	 * 
	 * @return the variable that is to receive this message
	 */
	public String getReceiver() {
		return receiver;
	}

	/**
	 * Getter function
	 * 
	 * @return the value
	 */
	public HashMap<String, Val> getContext() {
		return context;
	}

	/**
	 * Getter function
	 * 
	 * @return the lower bound reported by the sender
	 */
	public U getLB() {
		return lb;
	}

	/**
	 * Getter function
	 * 
	 * @return the upper bound reported by the sender
	 */
	public U getUB() {
		return ub;
	}

}