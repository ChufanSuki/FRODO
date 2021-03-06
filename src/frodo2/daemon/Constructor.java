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

/**
 * 
 */
package frodo2.daemon;

import java.util.ArrayList;
import java.util.Collection;

import org.jdom2.Document;

import frodo2.algorithms.AgentFactory;
import frodo2.algorithms.AgentInterface;
import frodo2.communication.IncomingMsgPolicyInterface;
import frodo2.communication.Message;
import frodo2.communication.MessageType;
import frodo2.communication.MessageWith3Payloads;
import frodo2.communication.MessageWithPayload;
import frodo2.communication.Queue;
import frodo2.communication.QueueOutputPipeInterface;
import frodo2.communication.sharedMemory.QueueIOPipe;
import frodo2.controller.ConfigurationManager;
import frodo2.controller.Controller;
import frodo2.controller.userIO.UserIO;
import frodo2.solutionSpaces.ProblemInterface;

/**
 * @author Brammert Ottens, Thomas Leaute
 *
 */
public class Constructor implements IncomingMsgPolicyInterface<MessageType> {

	/** The daemon queue*/
	Queue queue;
	
	/** The list of messages types this listener wants to be notified of */
	private ArrayList <MessageType> msgTypes = new ArrayList <MessageType> ();
	
	/** The output pipe to communicate with the daemon*/
	private QueueOutputPipeInterface outputToDaemon;
	
	/** Pointer to the daemon. To be used when problem is distributed*/
	private Daemon daemon;
	
	/** Pointer to the controller. To be used when problem is NOT distributed*/
	private Controller controller;
	
	/** Every time an agent is created this number is updated*/
	private int agentPort;
	
	/** Is true when the experiments are run in the same JVM as the controller*/
	private boolean local;
	
	/**
	 * Constructor to be used when this constructor is attached to a daemon
	 * @param local 			whether the experiments are run in the same JVM as the controller
	 * @param outputToDaemon 	the pipe to send messages to the daemon
	 * @param daemon 			the daemon
	 */
	public Constructor(boolean local, QueueIOPipe outputToDaemon, Daemon daemon) {
		msgTypes.add(ConfigurationManager.AGENT_CONFIGURATION_MESSAGE);
		this.outputToDaemon = outputToDaemon;
		this.daemon = daemon;
		agentPort = daemon.getPort() + 1;
		this.local = local;
	}
	
	/**
	 * Constructor to be used when this constructor is attached to a controller
	 * @param local 			whether the experiments are run in the same JVM as the controller
	 * @param outputToDaemon 	the pipe to send messages to the daemon
	 * @param controller 		the controller
	 */
	public Constructor(boolean local, QueueIOPipe outputToDaemon, Controller controller) {
		msgTypes.add(ConfigurationManager.AGENT_CONFIGURATION_MESSAGE);
		this.outputToDaemon = outputToDaemon;
		this.controller = controller;
		agentPort = Controller.PORT + 1;
		this.local = local;
	}
	
	/** 
	 * @see frodo2.communication.IncomingMsgPolicyInterface#getMsgTypes()
	 */
	public Collection<MessageType> getMsgTypes() {
		return msgTypes;
	}
	
	/** 
	 * @see IncomingMsgPolicyInterface#notifyIn(Message)
	 * @author Brammert Ottens, Thomas Leaute
	 */
	@SuppressWarnings("unchecked")
	public void notifyIn(Message msg) {
		MessageType type = msg.getType();
		AgentInterface<?> agent = null;
		
		if(type.equals(ConfigurationManager.AGENT_CONFIGURATION_MESSAGE)) {
			agentPort += 10;
			MessageWith3Payloads<ProblemInterface<?, ?>, Document, Boolean> msgR = 
					(MessageWith3Payloads<ProblemInterface<?, ?>, Document, Boolean>)msg;
			ProblemInterface<?, ?> prob = msgR.getPayload1();
			Document agentDoc = msgR.getPayload2();
			Boolean statsToController = msgR.getPayload3();
			
			// If any of the payloads is missing, then ignore this message; we should wait for the LocalConfigManager's
			if (agentDoc == null || prob == null || statsToController == null) 
				return;
			
			// Check that we are not using the simulated time metric, which is not supported in this mode
			String measureTime = agentDoc.getRootElement().getAttributeValue("measureTime");
			if (Boolean.parseBoolean(measureTime)) { // using the simulated time metric
				System.err.println("The simulated time metric (measureTime = true) is not supported in this mode");
				System.exit(1);
			}
			
			if(local) {
				agent = AgentFactory.createAgent(outputToDaemon, prob, agentDoc, null);
			} else {
				agent = AgentFactory.createAgent(outputToDaemon, queue.getOutputPipe(LocalWhitePages.CONTROLLER_ID), 
						prob, agentDoc, statsToController, agentPort);	
			}
			
			tellUser("Taking ownership of Agent " + agent.getID());
			
			if(local) {
				controller.addAgent(agent.getID(), agent);
			} else {
				daemon.addAgent(agent.getID(), agent);
			}
		}
	}
	
	/**
	 * This function is used to send a message to the user via the UI
	 * @param message the message to be send to the user
	 */
	private void tellUser(String message) {
		Message msg = new MessageWithPayload <String> (UserIO.USER_NOTIFICATION_MSG, message);
		queue.sendMessageToSelf(msg);
	}
	
	/** 
	 * @see frodo2.communication.IncomingMsgPolicyInterface#setQueue(frodo2.communication.Queue)
	 */
	public void setQueue(Queue queue) {
		this.queue = queue;
	}
}
