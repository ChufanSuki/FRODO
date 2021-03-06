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

/** The hypercube tests */
package frodo2.solutionSpaces.hypercube.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/** The hypercube tests
 * @author Thomas Leaute
 */
public class AllTestsHypercube {
	
	/** @return the test suite */
	public static Test suite() {
		TestSuite suite = new TestSuite("All tests involving hypercubes");
		//$JUnit-BEGIN$
		suite.addTest(HypercubeTest.suite());
		suite.addTest(HypercubeIterTest.suite());
		suite.addTest(HypercubeIterBestFirstTest.suite());
		//$JUnit-END$
		return suite;
	}
}
