/*
FRODO: a FRamework for Open/Distributed Optimization
Copyright (C) 2008-2018  Thomas Leaute, Brammert Ottens & Radoslaw Szymanek

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

package frodo2.algorithms.test;

import java.util.Arrays;
import java.util.Collection;

import org.jdom2.Element;

import frodo2.algorithms.AgentInterface;
import frodo2.communication.IncomingMsgPolicyInterface;
import frodo2.communication.Message;
import frodo2.communication.OutgoingMsgPolicyInterface;
import frodo2.communication.Queue;
import frodo2.controller.Controller;
import frodo2.controller.WhitePages;
import frodo2.daemon.Daemon;
import frodo2.solutionSpaces.DCOPProblemInterface;

/** Prints out all messages received and sent by the agent
 * @author Thomas Leaute
 * @warning Printing out all messages exchanges can be computationally expensive. Only use this feature for debugging purposes. 
 */
public class MessageDebugger implements IncomingMsgPolicyInterface<String>, OutgoingMsgPolicyInterface<String> {
	
	static {
		System.err.println(
				  "************************************************************************\n"
				+ "WARNING! Messages are being printed out, which slows down the algorithms\n"
				+ "************************************************************************");
	}
	
	/** The name of this agent */
	private final String agent;
	
	/** Whether to display system messages */
	private final boolean hideSystemMessages;
	
	/** Constructor
	 * @param problem 	the problem instance
	 * @param params 	the parameters for this module
	 */
	public MessageDebugger (DCOPProblemInterface<?, ?> problem, Element params) {
		this.agent = problem.getAgent();
		
		String str = params == null ? null : params.getAttributeValue("hideSystemMessages");
		this.hideSystemMessages = str == null ? true : Boolean.parseBoolean(str);
	}

	/** @see IncomingMsgPolicyInterface#setQueue(Queue) */
	@Override
	public void setQueue(Queue queue) { }

	/** @see IncomingMsgPolicyInterface#getMsgTypes() */
	@Override
	public Collection<String> getMsgTypes() {
		return Arrays.asList(Queue.ALLMESSAGES);
	}

	/** @see OutgoingMsgPolicyInterface#notifyOut(Message) */
	@Override
	public Decision notifyOut(Message msg) {
		
		// Filter out system messages
		if (this.hideSystemMessages) {
			
			// Don't report messages sent by system agents
			switch (this.agent) {
			case Daemon.DAEMON:
			case Controller.CONTROLLER:
			case AgentInterface.STATS_MONITOR:
				return Decision.DONTCARE;
			}
			
			// Don't report system messages
			switch (msg.getType()) {
			case AgentInterface.AGENT_FINISHED:
			case AgentInterface.LOCAL_AGENT_REPORTING:
			case AgentInterface.AGENT_CONNECTED:
				return Decision.DONTCARE;
			}
		}
		
		System.out.println("Agent `" + this.agent + "' sends the following message:\n" + msg + "\n");
		
		return Decision.DONTCARE;
	}

	/** @see IncomingMsgPolicyInterface#notifyIn(Message) */
	@Override
	public void notifyIn(Message msg) {
		
		// Filter out system messages
		if (this.hideSystemMessages) {
			
			switch (msg.getType()) {
			case WhitePages.CONNECT_AGENT: 
			case AgentInterface.START_AGENT:
			case AgentInterface.AGENT_FINISHED:
			case AgentInterface.ALL_AGENTS_IDLE:
				return;
			}
			
		}
		
		System.out.println("Agent `" + this.agent + "' receives the following message:\n" + msg + "\n");
	}

}
