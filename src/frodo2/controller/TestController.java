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
package frodo2.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import frodo2.algorithms.RandGraphFactory;
import frodo2.communication.Message;
import frodo2.communication.MessageWithPayload;
import frodo2.controller.userIO.UserIO;
import frodo2.daemon.Daemon;
import junit.extensions.RepeatedTest;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Brammert Ottens
 * @author Thomas Leaute
 */
public class TestController extends TestCase {
	
	/** Directory containing the test files. Must end with a slash. */
	private static String testDir = TestController.class.getResource("testFiles").getFile() + "/";
	
	/** Maximum number of variables in the random graph 
	 * @note Must be at least 2. 
	 */
	private final int maxNbrVars = 15;
	
	/** Maximum number of edges in the random graph */
	private final int maxNbrEdges = 50;

	/** Maximum number of agents */
	private final int maxNbrAgents;
	
	/** The controller*/
	ControllerExtension control;
	
	/** The daemon*/
	DaemonExtension daemon;
	
	/** The problem definition corresponding to the graph*/
	Document problem;
	
	/** A timeout on how long the test waits for the end*/
	int timeOut = 10000;
	
	/**
	 * An extension of the controller that allows external objects to
	 *  - set a configuration file
	 *  - start an experiment
	 * @author brammertottens
	 *
	 */
	public class ControllerExtension extends Controller {

		/**
		 * Constructor
		 * @see Controller#Controller(boolean, boolean, String)
		 */
		public ControllerExtension(boolean local, boolean ui, String testDir) {
			super(local, ui, testDir);
		}
		
		/**
		 * @param local (self-explanatory)
		 * @param ui (self-explanatory)
		 * @param testDir (self-explanatory)
		 * @param size  is the number of daemons to be instantiated
		 */
		public ControllerExtension(boolean local, boolean ui, String testDir, int size) {
			super(local, ui, testDir, size);
		}
		
		/**
		 * Configuration file loader. For testing purposes only!
		 * @param configFile 	the configuration file
		 */
		public void setUp(String configFile) {
			MessageWithPayload<String> msg = new MessageWithPayload<String>(UserIO.CONFIGURATION_MSG, configFile);
			controlQueue.sendMessageToSelf(msg);
		}
		
		/** 
		 * Start the experiments. For testing purposes only!
		 */
		public void start() {
			Message msg = new Message(UserIO.START_MSG);
			controlQueue.sendMessageToSelf(msg);
		}
		
		
	}
	
	/** A daemon with an API to pass the configuration file
	 * @author Thomas Leaute
	 */
	private static class DaemonExtension extends Daemon {

		/** Constructor */
		public DaemonExtension() {
			super(Controller.PipeFactoryInstance.getSelfAddress(Controller.PORT), false, testDir);
		}
		
		/** Passes the configuration file to the daemon
		 * @param configFileName 	the name of the configuration file
		 */
		public void setUp (String configFileName) {
			MessageWithPayload<String> msg = new MessageWithPayload<String>(UserIO.CONFIGURATION_MSG, configFileName);
			super.daemonQueue.sendMessageToSelf(msg);
		}

		/** @return whether the daemon is finished */
		public boolean isFinished() {
			return super.isFinished;
		}
	}
	
	/** Constructor
	 * @param name 			the name of the test method
	 */
	public TestController(String name) {
		this(name, 6);
	}
	
	/** Constructor
	 * @param name 			the name of the test method
	 * @param maxNbrAgents 	the maximum number of agents
	 */
	public TestController(String name, int maxNbrAgents) {
		super(name);
		this.maxNbrAgents = maxNbrAgents;
	}
	
	/** @return the test suite for this test */
	public static TestSuite suite () {
		TestSuite testSuite = new TestSuite ("Tests for Controller - Daemon - Agent interaction");
		
		TestSuite testLocal = new TestSuite("Test on a local problem");
		testLocal.addTest(new RepeatedTest (new TestController ("testControllerOnLocalProblem"), 50));
		testSuite.addTest(testLocal);
		
		TestSuite testDistributed = new TestSuite("Test on a distributed problem passed to the Controller");
		testDistributed.addTest(new RepeatedTest (new TestController ("testOmniscientControllerOnDistributedProblem"), 50));
		testSuite.addTest(testDistributed);
		
		testDistributed = new TestSuite("Test on a distributed problem passed to the Daemon");
		testDistributed.addTest(new RepeatedTest (new TestController ("testNonOmniscientControllerOnDistributedProblem", 1), 50));
		testSuite.addTest(testDistributed);
		
		return testSuite;
	}

	/** 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() {
		problem = frodo2.algorithms.test.AllTests.generateProblem(RandGraphFactory.getRandGraph(maxNbrVars, maxNbrEdges, maxNbrAgents), false);
	}

	/** 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() {
		control.exit(false);
		if(daemon != null) {
			daemon.exit(false);
		}
		new File (testDir + "randomProblem.xml").delete();
	}
	
	/**
	 * Tests the controller when it simultaneously functions
	 * as a daemon that spawns agents
	 * @throws Exception 	if an error occurs
	 */
	public void testControllerOnLocalProblem() throws Exception {
		control = new ControllerExtension(true, false, testDir);
		String problemFile = testDir + "randomProblem.xml";
		
		// create the problem files
		BufferedWriter bw = new BufferedWriter(new FileWriter(problemFile));

		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		out.output(problem, bw);
		
		control.setUp("testRandomConfig.xml");
		
		Thread.sleep(500);
		
		long startTime = System.currentTimeMillis();
		
		control.start();
		
		boolean stop = false;
		
		
		while(!stop && (System.currentTimeMillis() - startTime) < timeOut) {
			synchronized (control.isFinishedSync) {
				if(control.isFinished) {
					stop = true;
				}
			}
		}
		
		assertTrue(control.isFinished);
	}
	
	/**
	 * This test function generates random
	 * graphs, lets the controller/daemon set up
	 * the agent structure and tests whether it has been properly
	 * set up.
	 * @throws Exception 	if an error occurs
	 */
	public void testOmniscientControllerOnDistributedProblem() throws Exception {
		String problemFile = testDir + "randomProblem.xml";
		control = new ControllerExtension(false, false, testDir);
		daemon = new DaemonExtension();

		BufferedWriter bw = new BufferedWriter(new FileWriter(problemFile));

		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		out.output(problem, bw);
		
		control.setUp("testRandomConfig.xml");
		
		Thread.sleep(500);
		
		long startTime = System.currentTimeMillis();
		
		control.start();
		
		while(!control.isFinished && (System.currentTimeMillis() - startTime) < timeOut) {
			Thread.sleep(10);
		}
		
		assertTrue(control.isFinished);
	}
	
	/** Test method for the distributed submode, in which the configuration file is passed to the daemons
	 * @throws Exception 	if an error occurs
	 */
	public void testNonOmniscientControllerOnDistributedProblem() throws Exception {
		
		// Instantiate the controller and one daemon
		control = new ControllerExtension(false, false, testDir);
		daemon = new DaemonExtension();

		// Create the problem instance for only one agent
		/// @todo Test with more than one agent
		/// @todo Test with more than one problem instance in the same experiment
		String problemFile = testDir + "randomProblem.xml";
		BufferedWriter bw = new BufferedWriter(new FileWriter(problemFile));
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		problem.getRootElement().getChild("agents").setAttribute("self", "a0");
		out.output(problem, bw);

		// Pass the configuration file to the daemon
		daemon.setUp("testRandomConfig.xml");
		Thread.sleep(500);
		
		// Start the experiment
		long startTime = System.currentTimeMillis();
		control.start();
		
		// Wait until the controller is finished
		while(!daemon.isFinished() && (System.currentTimeMillis() - startTime) < timeOut) {
			Thread.sleep(10);
		}
		assertTrue("Daemon timeout", daemon.isFinished());
	}

}
