"""
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
"""

# For each algorithm, this script compares the performance of its current version (in the src/ folder) against its old version (from a JAR)

# Add the FRODO benchmarks folder to the Python path and import the frodo2 module
import sys
sys.path.append("..")
import frodo2

# The command to call java and the JVM parameters
root = "../../../../"
java = "java"
javaParamsOld = [
            "-Xmx2G", 
            "-classpath", root + "frodo2.18.jar", # includes a (presumably older) version of FRODO from a JAR
            ]
javaParamsNew = [
            "-Xmx2G", 
            "-classpath", root + "bin:" + root + "lib/jacop-4.7.0.jar:" + root + "lib/jdom-2.0.6.jar", # includes the current version of FRODO
            ]

# Partly define the problem generator (the input parameters will depend on the algorithm)
generator = "frodo2.benchmarks.graphcoloring.GraphColoring"
problemFile = "graphColoring.xml"

# Define the experiments to run, and on which problem sizes
# Each experiment is [algoName, genParams, [algo_version_1, algo_version_2...]]
# where algo_version_i is [algoName, solverClassName, agentConfigFilePath, inputProblemFilePath, javaParams]
experiments = [
    ["ADOPT", ["-soft", list(range(10, 15)), .4, .0, 3], [
        ["ADOPT 2.18", "frodo2.algorithms.adopt.ADOPTsolver", root + "agents/ADOPT/ADOPTagent.xml", problemFile, javaParamsOld], 
        ["ADOPT 2.x", "frodo2.algorithms.adopt.ADOPTsolver", root + "agents/ADOPT/ADOPTagent.xml", problemFile, javaParamsNew], 
            ]], 
    ["ADOPT+JaCoP", ["-i", "-soft", list(range(10, 15)), .4, .0, 3], [
        ["ADOPT+JaCoP 2.18", "frodo2.algorithms.adopt.ADOPTsolver", root + "agents/ADOPT/ADOPTagentJaCoP.xml", problemFile, javaParamsOld], 
        ["ADOPT+JaCoP 2.x", "frodo2.algorithms.adopt.ADOPTsolver", root + "agents/ADOPT/ADOPTagentJaCoP.xml", problemFile, javaParamsNew], 
            ]], 
    ["AFB", ["-soft", list(range(15, 22)), .4, .0, 3], [
        ["AFB 2.18", "frodo2.algorithms.afb.AFBsolver", root + "agents/AFB/AFBagent.xml", problemFile, javaParamsOld], 
        ["AFB 2.x", "frodo2.algorithms.afb.AFBsolver", root + "agents/AFB/AFBagent.xml", problemFile, javaParamsNew], 
            ]],
    ["AFB+JaCoP", ["-i", "-soft", list(range(15, 21)), .4, .0, 3], [
        ["AFB+JaCoP 2.18", "frodo2.algorithms.afb.AFBsolver", root + "agents/AFB/AFBagentJaCoP.xml", problemFile, javaParamsOld], 
        ["AFB+JaCoP 2.x", "frodo2.algorithms.afb.AFBsolver", root + "agents/AFB/AFBagentJaCoP.xml", problemFile, javaParamsNew], 
            ]],
    ["DPOP", ["-soft", list(range(14, 20)), .4, .0, 3], [
        ["DPOP 2.18", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/DPOPagent.xml", problemFile, javaParamsOld], 
        ["DPOP 2.x", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/DPOPagent.xml", problemFile, javaParamsNew], 
            ]], 
    ["DPOP+JaCoP", ["-i", "-soft", list(range(14, 19)), .4, .0, 3], [
        ["DPOP+JaCoP 2.18", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/DPOPagentJaCoP.xml", problemFile, javaParamsOld], 
        ["DPOP+JaCoP 2.x", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/DPOPagentJaCoP.xml", problemFile, javaParamsNew], 
            ]], 
    ["ASO-DPOP", ["-soft", list(range(14, 20)), .4, .0, 3], [
        ["ASO-DPOP 2.18", "frodo2.algorithms.asodpop.ASODPOPsolver", root + "agents/DPOP/ASO-DPOP/ASO-DPOPagent.xml", problemFile, javaParamsOld], 
        ["ASO-DPOP 2.x", "frodo2.algorithms.asodpop.ASODPOPsolver", root + "agents/DPOP/ASO-DPOP/ASO-DPOPagent.xml", problemFile, javaParamsNew], 
            ]], 
    ["ASO-DPOP+JaCoP", ["-i", "-soft", list(range(14, 19)), .4, .0, 3], [
        ["ASO-DPOP+JaCoP 2.18", "frodo2.algorithms.asodpop.ASODPOPsolver", root + "agents/DPOP/ASO-DPOP/ASO-DPOPagentJaCoP.xml", problemFile, javaParamsOld], 
        ["ASO-DPOP+JaCoP 2.x", "frodo2.algorithms.asodpop.ASODPOPsolver", root + "agents/DPOP/ASO-DPOP/ASO-DPOPagentJaCoP.xml", problemFile, javaParamsNew], 
            ]], 
    ["MB-DPOP", ["-soft", list(range(15, 22)), .4, .0, 3], [
        ["MB-DPOP 2.18", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/MB-DPOP/MB-DPOPagent.xml", problemFile, javaParamsOld], 
        ["MB-DPOP 2.x", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/MB-DPOP/MB-DPOPagent.xml", problemFile, javaParamsNew], 
            ]], 
    ["MB-DPOP+JaCoP", ["-i", "-soft", list(range(15, 21)), .4, .0, 3], [
        ["MB-DPOP+JaCoP 2.18", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/MB-DPOP/MB-DPOPagentJaCoP.xml", problemFile, javaParamsOld], 
        ["MB-DPOP+JaCoP 2.x", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/MB-DPOP/MB-DPOPagentJaCoP.xml", problemFile, javaParamsNew], 
            ]], 
    ["O-DPOP", ["-soft", list(range(14, 19)), .4, .0, 3], [
        ["O-DPOP 2.18", "frodo2.algorithms.odpop.ODPOPsolver", root + "agents/DPOP/O-DPOP/O-DPOPagent.xml", problemFile, javaParamsOld], 
        ["O-DPOP 2.x", "frodo2.algorithms.odpop.ODPOPsolver", root + "agents/DPOP/O-DPOP/O-DPOPagent.xml", problemFile, javaParamsNew], 
            ]], 
    ["O-DPOP+JaCoP", ["-i", "-soft", list(range(14, 19)), .4, .0, 3], [
        ["O-DPOP+JaCoP 2.18", "frodo2.algorithms.odpop.ODPOPsolver", root + "agents/DPOP/O-DPOP/O-DPOPagentJaCoP.xml", problemFile, javaParamsOld], 
        ["O-DPOP+JaCoP 2.x", "frodo2.algorithms.odpop.ODPOPsolver", root + "agents/DPOP/O-DPOP/O-DPOPagentJaCoP.xml", problemFile, javaParamsNew], 
            ]], 
    ["P-DPOP", ["-soft", list(range(9, 14)), .4, .0, 3], [
        ["P-DPOP 2.18", "frodo2.algorithms.dpop.privacy.P_DPOPsolver", root + "agents/DPOP/P-DPOP/P-DPOPagent.xml", problemFile, javaParamsOld], 
        ["P-DPOP 2.x", "frodo2.algorithms.dpop.privacy.P_DPOPsolver", root + "agents/DPOP/P-DPOP/P-DPOPagent.xml", problemFile, javaParamsNew], 
            ]],
    ["P-DPOP+JaCoP", ["-i", "-soft", list(range(9, 14)), .4, .0, 3], [
        ["P-DPOP+JaCoP 2.18", "frodo2.algorithms.dpop.privacy.P_DPOPsolver", root + "agents/DPOP/P-DPOP/P-DPOPagentJaCoP.xml", problemFile, javaParamsOld], 
        ["P-DPOP+JaCoP 2.x", "frodo2.algorithms.dpop.privacy.P_DPOPsolver", root + "agents/DPOP/P-DPOP/P-DPOPagentJaCoP.xml", problemFile, javaParamsNew], 
            ]],
    ["P1.5-DPOP", ["-soft", list(range(5, 11)), .4, .0, 3], [
        ["P3/2-DPOP 2.18", "frodo2.algorithms.dpop.privacy.P3halves_DPOPsolver", root + "agents/DPOP/P-DPOP/P1.5-DPOPagent.xml", problemFile, javaParamsOld], 
        ["P3/2-DPOP 2.x", "frodo2.algorithms.dpop.privacy.P3halves_DPOPsolver", root + "agents/DPOP/P-DPOP/P1.5-DPOPagent.xml", problemFile, javaParamsNew], 
            ]],
    ["P1.5-DPOP+JaCoP", ["-i", "-soft", list(range(5, 11)), .4, .0, 3], [
        ["P3/2-DPOP+JaCoP 2.18", "frodo2.algorithms.dpop.privacy.P3halves_DPOPsolver", root + "agents/DPOP/P-DPOP/P1.5-DPOPagentJaCoP.xml", problemFile, javaParamsOld], 
        ["P3/2-DPOP+JaCoP 2.x", "frodo2.algorithms.dpop.privacy.P3halves_DPOPsolver", root + "agents/DPOP/P-DPOP/P1.5-DPOPagentJaCoP.xml", problemFile, javaParamsNew], 
            ]],
    ["P2-DPOP", ["-soft", list(range(3, 6)), .4, .0, 3], [
        ["P2-DPOP 2.18", "frodo2.algorithms.dpop.privacy.P2_DPOPsolver", root + "agents/DPOP/P-DPOP/P2-DPOPagent.xml", problemFile, javaParamsOld],
        ["P2-DPOP 2.x", "frodo2.algorithms.dpop.privacy.P2_DPOPsolver", root + "agents/DPOP/P-DPOP/P2-DPOPagent.xml", problemFile, javaParamsNew],
            ]],
    ["P2-DPOP+JaCoP", ["-i", "-soft", list(range(3, 6)), .4, .0, 3], [
        ["P2-DPOP+JaCoP 2.18", "frodo2.algorithms.dpop.privacy.P2_DPOPsolver", root + "agents/DPOP/P-DPOP/P2-DPOPagentJaCoP.xml", problemFile, javaParamsOld],
        ["P2-DPOP+JaCoP 2.x", "frodo2.algorithms.dpop.privacy.P2_DPOPsolver", root + "agents/DPOP/P-DPOP/P2-DPOPagentJaCoP.xml", problemFile, javaParamsNew],
            ]],
    ["Complete-E-DPOP", ["-soft", list(range(22, 27)), .4, .0, 3, .3], [
        ["Complete-E-DPOP 2.18", "frodo2.algorithms.dpop.stochastic.Complete_E_DPOPsolver", root + "agents/DPOP/StochDCOP/Complete-E-DPOP.xml", problemFile, javaParamsOld],
        ["Complete-E-DPOP 2.x", "frodo2.algorithms.dpop.stochastic.Complete_E_DPOPsolver", root + "agents/DPOP/StochDCOP/Complete-E-DPOP.xml", problemFile, javaParamsNew],
            ]],
    ["E-DPOP", ["-soft", list(range(26, 31)), .4, .0, 3, .3], [
        ["E-DPOP 2.18", "frodo2.algorithms.dpop.stochastic.E_DPOPsolver", root + "agents/DPOP/StochDCOP/E-DPOP.xml", problemFile, javaParamsOld],
        ["E-DPOP 2.x", "frodo2.algorithms.dpop.stochastic.E_DPOPsolver", root + "agents/DPOP/StochDCOP/E-DPOP.xml", problemFile, javaParamsNew],
            ]],
    ["Robust-E-DPOP", ["-soft", list(range(27, 32)), .4, .0, 3, .3], [
        ["Robust-E-DPOP 2.18", "frodo2.algorithms.dpop.stochastic.E_DPOPsolver", root + "agents/DPOP/StochDCOP/Robust-E-DPOP.xml", problemFile, javaParamsOld],
        ["Robust-E-DPOP 2.x", "frodo2.algorithms.dpop.stochastic.E_DPOPsolver", root + "agents/DPOP/StochDCOP/Robust-E-DPOP.xml", problemFile, javaParamsNew],
            ]],
    ["DUCT", ["-soft", list(range(10, 51, 10)), .4, .0, 3], [
        ["DUCT 2.18", "frodo2.algorithms.duct.DUCTsolver", root + "agents/DUCT/DUCTagent.xml", problemFile, javaParamsOld], 
        ["DUCT 2.x", "frodo2.algorithms.duct.DUCTsolver", root + "agents/DUCT/DUCTagent.xml", problemFile, javaParamsNew], 
            ]], 
    ["DSA", ["-soft", list(range(20, 35)), .4, .0, 3], [
        ["DSA 2.18", "frodo2.algorithms.localSearch.dsa.DSAsolver", root + "agents/DSA/DSAagent.xml", problemFile, javaParamsOld], 
        ["DSA 2.x", "frodo2.algorithms.localSearch.dsa.DSAsolver", root + "agents/DSA/DSAagent.xml", problemFile, javaParamsNew], 
            ]],
    ["DSA+JaCoP", ["-i", "-soft", list(range(20, 35)), .4, .0, 3], [
        ["DSA+JaCoP 2.18", "frodo2.algorithms.localSearch.dsa.DSAsolver", root + "agents/DSA/DSAagentJaCoP.xml", problemFile, javaParamsOld], 
        ["DSA+JaCoP 2.x", "frodo2.algorithms.localSearch.dsa.DSAsolver", root + "agents/DSA/DSAagentJaCoP.xml", problemFile, javaParamsNew], 
            ]],
    ["MaxSum", ["-soft", list(range(15, 21)), .4, .0, 3], [
        ["MaxSum 2.18", "frodo2.algorithms.maxsum.MaxSumSolver", root + "agents/MaxSum/MaxSumAgentPerturbed.xml", problemFile, javaParamsOld], 
        ["MaxSum 2.x", "frodo2.algorithms.maxsum.MaxSumSolver", root + "agents/MaxSum/MaxSumAgentPerturbed.xml", problemFile, javaParamsNew], 
            ]],
    ["MaxSum+JaCoP", ["-soft", list(range(15, 21)), .4, .0, 3], [
        ["MaxSum+JaCoP 2.18", "frodo2.algorithms.maxsum.MaxSumSolver", root + "agents/MaxSum/MaxSumAgentPerturbedJaCoP.xml", problemFile, javaParamsOld], 
        ["MaxSum+JaCoP 2.x", "frodo2.algorithms.maxsum.MaxSumSolver", root + "agents/MaxSum/MaxSumAgentPerturbedJaCoP.xml", problemFile, javaParamsNew], 
            ]],
    ["MGM", ["-soft", list(range(20, 35)), .4, .0, 3], [
        ["MGM 2.18", "frodo2.algorithms.localSearch.mgm.MGMsolver", root + "agents/MGM/MGMagent.xml", problemFile, javaParamsOld], 
        ["MGM 2.x", "frodo2.algorithms.localSearch.mgm.MGMsolver", root + "agents/MGM/MGMagent.xml", problemFile, javaParamsNew], 
            ]],
    ["MGM+JaCoP", ["-i", "-soft", list(range(20, 35)), .4, .0, 3], [
        ["MGM+JaCoP 2.18", "frodo2.algorithms.localSearch.mgm.MGMsolver", root + "agents/MGM/MGMagentJaCoP.xml", problemFile, javaParamsOld], 
        ["MGM+JaCoP 2.x", "frodo2.algorithms.localSearch.mgm.MGMsolver", root + "agents/MGM/MGMagentJaCoP.xml", problemFile, javaParamsNew], 
            ]],
    ["MGM2", ["-soft", list(range(20, 35)), .4, .0, 3], [
        ["MGM2 2.18", "frodo2.algorithms.localSearch.mgm.mgm2.MGM2solver", root + "agents/MGM/MGM2agent.xml", problemFile, javaParamsOld], 
        ["MGM2 2.x", "frodo2.algorithms.localSearch.mgm.mgm2.MGM2solver", root + "agents/MGM/MGM2agent.xml", problemFile, javaParamsNew], 
            ]],
    ["MGM2+JaCoP", ["-i", "-soft", list(range(20, 35)), .4, .0, 3], [
        ["MGM2+JaCoP 2.18", "frodo2.algorithms.localSearch.mgm.mgm2.MGM2solver", root + "agents/MGM/MGM2agentJaCoP.xml", problemFile, javaParamsOld], 
        ["MGM2+JaCoP 2.x", "frodo2.algorithms.localSearch.mgm.mgm2.MGM2solver", root + "agents/MGM/MGM2agentJaCoP.xml", problemFile, javaParamsNew], 
            ]],
    ["MPC-DisCSP4", [list(range(2, 7)), .4, .0, 3], [
        ["MPC-DisCSP4 2.18", "frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver", root + "agents/MPC/MPC-DisCSP4.xml", problemFile, javaParamsOld],
        ["MPC-DisCSP4 2.x", "frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver", root + "agents/MPC/MPC-DisCSP4.xml", problemFile, javaParamsNew],
            ]],
    ["MPC-DisCSP4+JaCoP", ["-i", list(range(2, 7)), .4, .0, 3], [
        ["MPC-DisCSP4+JaCoP 2.18", "frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver", root + "agents/MPC/MPC-DisCSP4_JaCoP.xml", problemFile, javaParamsOld],
        ["MPC-DisCSP4+JaCoP 2.x", "frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver", root + "agents/MPC/MPC-DisCSP4_JaCoP.xml", problemFile, javaParamsNew],
            ]],
    ["MPC-DisWCSP4", ["-soft", list(range(2, 6)), .4, .0, 3], [
        ["MPC-DisWCSP4 2.18", "frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver", root + "agents/MPC/MPC-DisWCSP4.xml", problemFile, javaParamsOld],
        ["MPC-DisWCSP4 2.x", "frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver", root + "agents/MPC/MPC-DisWCSP4.xml", problemFile, javaParamsNew],
            ]],    
    ["MPC-DisWCSP4+JaCoP", ["-i", "-soft", list(range(2, 6)), .4, .0, 3], [
        ["MPC-DisWCSP4+JaCoP 2.18", "frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver", root + "agents/MPC/MPC-DisWCSP4_JaCoP.xml", problemFile, javaParamsOld],
        ["MPC-DisWCSP4+JaCoP 2.x", "frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver", root + "agents/MPC/MPC-DisWCSP4_JaCoP.xml", problemFile, javaParamsNew],
            ]],    
    ["SynchBB", ["-soft", list(range(16, 22)), .4, .0, 3], [
        ["SynchBB 2.18", "frodo2.algorithms.synchbb.SynchBBsolver", root + "agents/SynchBB/SynchBBagent.xml", problemFile, javaParamsOld], 
        ["SynchBB 2.x", "frodo2.algorithms.synchbb.SynchBBsolver", root + "agents/SynchBB/SynchBBagent.xml", problemFile, javaParamsNew], 
            ]],
    ["SynchBB+JaCoP", ["-i", "-soft", list(range(16, 21)), .4, .0, 3], [
        ["SynchBB+JaCoP 2.18", "frodo2.algorithms.synchbb.SynchBBsolver", root + "agents/SynchBB/SynchBBagentJaCoP.xml", problemFile, javaParamsOld], 
        ["SynchBB+JaCoP 2.x", "frodo2.algorithms.synchbb.SynchBBsolver", root + "agents/SynchBB/SynchBBagentJaCoP.xml", problemFile, javaParamsNew], 
            ]],
]
timeout = 500 # in seconds
nbrProblems = 101

# Run each experiment nbrProblems times
outputs = [] # the names of the CSV output files
interrupted = False
for i in range(0, nbrProblems):
    print("\n" + str(i + 1) + "/" + str(nbrProblems))
    
    # Run each experiment
    for exp in experiments: 
        if not interrupted: 
            print("\n" + exp[0]) # prints the algorithm
        
        # The CSV file to which the statistics should be written
        output = exp[0] + ".csv"
        outputs += [output]
        
        # Run the experiment
        if not interrupted: 
            interrupted = frodo2.run(java, javaParamsNew, generator, exp[1], 1, exp[2], timeout, output, saveProblems = False)

    if interrupted: 
        break

# Plot the results for each experiment
inputCol = 7 # number of nodes
for j in range(0, len(experiments)): 
    exp = experiments[j]

    # The CSV file to which the statistics should have been written
    output = outputs[j]

    # Plot curves with x = problem size and y = performance of each algorithm
#     frodo2.plot(output, xCol = inputCol, yCol = 16, block = False) # 16 = message size
#     frodo2.plot(output, xCol = inputCol, yCol = 14, block = (j == len(experiments)-1)) # 14 = runtime

    # Scatter plot with one data point per instance, x = old algorithm and y = new algorithm
    xAlgo = exp[2][0][0]
    yAlgo = exp[2][1][0]
    frodo2.plotScatter(output, xAlgo, yAlgo, metricsCol = 20, inputCol = inputCol, timeouts = False, block = False, loglog = False, annotate = False) # 20 = solution quality 
    frodo2.plotScatter(output, xAlgo, yAlgo, metricsCol = 16, inputCol = inputCol, timeouts = True, block = False, annotate = False) # 16 = message size
    frodo2.plotScatter(output, xAlgo, yAlgo, metricsCol = 16, inputCol = inputCol, timeouts = False, block = False, annotate = False) # 16 = message size
    frodo2.plotScatter(output, xAlgo, yAlgo, metricsCol = 14, inputCol = inputCol, timeouts = True, block = False, annotate = False) # 14 = runtime
    frodo2.plotScatter(output, xAlgo, yAlgo, metricsCol = 14, inputCol = inputCol, timeouts = False, block = (j == len(experiments)-1), annotate = False) # 14 = runtime
