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

package frodo2.algorithms.odpop;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import frodo2.communication.MessageWithPayload;

/**
 * This message is used to ask a child for new information
 * @author brammert
 */
public class ASKmsg extends MessageWithPayload<String> implements Externalizable {

	/** Empty constructor */
	public ASKmsg () {
		super.type = UTILpropagation.ASK_MSG;
	}

	/**
	 * A constructor
	 * @param receiver		The recipient of the message
	 */
	public ASKmsg(String receiver) {
		super(UTILpropagation.ASK_MSG, receiver);
	}

	/** @see java.io.Externalizable#writeExternal(java.io.ObjectOutput) */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(super.getPayload());
	}

	/** @see java.io.Externalizable#readExternal(java.io.ObjectInput) */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.setPayload((String) in.readObject());
	}

	/**
	 * Returns the receiver of the message
	 * @return receiver
	 */
	public String getReceiver() {
		return this.getPayload();
	}
}