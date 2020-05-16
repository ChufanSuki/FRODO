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
            "-classpath", root + "frodo2.18.1.jar", # includes a (presumably older) version of FRODO from a JAR
            ]
javaParamsNew = [
            "-Xmx2G", 
            "-classpath", root + "bin:" + root + "lib/jacop-4.7.0.jar:" + root + "lib/jdom-2.0.6.jar", # includes the current version of FRODO
            ]

# Partly define the problem generator (the input parameters will depend on the algorithm)
generator = "frodo2.benchmarks.meetings.MeetingScheduling"
problemFileEAV = "meetingScheduling_EAV.xml"
problemFilePEAV = "meetingScheduling_PEAV.xml"

# Define the experiments to run, and on which problem sizes
# Each experiment is [algoName, genParams, [algo_version_1, algo_version_2...]]
# where algo_version_i is [algoName, solverClassName, agentConfigFilePath, inputProblemFilePath, javaParams]
experiments = [
    ["ADOPT", ["-PEAV", "-maxCost", 10, 3, list(range(1, 6)), 2, 8], [
        ["ADOPT 2.18", "frodo2.algorithms.adopt.ADOPTsolver", root + "agents/ADOPT/ADOPTagent.xml", problemFilePEAV, javaParamsOld], 
        ["ADOPT 2.x", "frodo2.algorithms.adopt.ADOPTsolver", root + "agents/ADOPT/ADOPTagent.xml", problemFilePEAV, javaParamsNew], 
            ]], 
    ["ADOPT+JaCoP", ["-i", "-PEAV", "-maxCost", 10, 3, list(range(1, 5)), 2, 8], [
        ["ADOPT+JaCoP 2.18", "frodo2.algorithms.adopt.ADOPTsolver", root + "agents/ADOPT/ADOPTagentJaCoP.xml", problemFilePEAV, javaParamsOld], 
        ["ADOPT+JaCoP 2.x", "frodo2.algorithms.adopt.ADOPTsolver", root + "agents/ADOPT/ADOPTagentJaCoP.xml", problemFilePEAV, javaParamsNew], 
            ]], 
    ["AFB", ["-PEAV", "-maxCost", 10, 3, list(range(1, 9)), 2, 8], [
        ["AFB 2.18", "frodo2.algorithms.afb.AFBsolver", root + "agents/AFB/AFBagent.xml", problemFilePEAV, javaParamsOld], 
        ["AFB 2.x", "frodo2.algorithms.afb.AFBsolver", root + "agents/AFB/AFBagent.xml", problemFilePEAV, javaParamsNew], 
            ]],
    ["AFB+JaCoP", ["-i", "-PEAV", "-maxCost", 10, 3, list(range(1, 8)), 2, 8], [
        ["AFB+JaCoP 2.18", "frodo2.algorithms.afb.AFBsolver", root + "agents/AFB/AFBagentJaCoP.xml", problemFilePEAV, javaParamsOld], 
        ["AFB+JaCoP 2.x", "frodo2.algorithms.afb.AFBsolver", root + "agents/AFB/AFBagentJaCoP.xml", problemFilePEAV, javaParamsNew], 
            ]],
    ["DPOP", ["-PEAV", "-maxCost", 10, 3, list(range(1, 8)), 2, 8], [
        ["DPOP 2.18", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/DPOPagent.xml", problemFilePEAV, javaParamsOld], 
        ["DPOP 2.x", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/DPOPagent.xml", problemFilePEAV, javaParamsNew], 
            ]], 
    ["DPOP+JaCoP", ["-i", "-PEAV", "-maxCost", 10, 3, list(range(1, 8)), 2, 8], [
        ["DPOP+JaCoP 2.18", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/DPOPagentJaCoP.xml", problemFilePEAV, javaParamsOld], 
        ["DPOP+JaCoP 2.x", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/DPOPagentJaCoP.xml", problemFilePEAV, javaParamsNew], 
            ]], 
    ["ASO-DPOP", ["-PEAV", "-maxCost", 10, 3, list(range(1, 7)), 2, 8], [
        ["ASO-DPOP 2.18", "frodo2.algorithms.asodpop.ASODPOPsolver", root + "agents/DPOP/ASO-DPOP/ASO-DPOPagent.xml", problemFilePEAV, javaParamsOld], 
        ["ASO-DPOP 2.x", "frodo2.algorithms.asodpop.ASODPOPsolver", root + "agents/DPOP/ASO-DPOP/ASO-DPOPagent.xml", problemFilePEAV, javaParamsNew], 
            ]], 
    ["ASO-DPOP+JaCoP", ["-i", "-PEAV", "-maxCost", 10, 3, list(range(1, 7)), 2, 8], [
        ["ASO-DPOP+JaCoP 2.18", "frodo2.algorithms.asodpop.ASODPOPsolver", root + "agents/DPOP/ASO-DPOP/ASO-DPOPagentJaCoP.xml", problemFilePEAV, javaParamsOld], 
        ["ASO-DPOP+JaCoP 2.x", "frodo2.algorithms.asodpop.ASODPOPsolver", root + "agents/DPOP/ASO-DPOP/ASO-DPOPagentJaCoP.xml", problemFilePEAV, javaParamsNew], 
            ]], 
    ["MB-DPOP", ["-PEAV", "-maxCost", 10, 3, list(range(1, 8)), 2, 8], [
        ["MB-DPOP 2.18", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/MB-DPOP/MB-DPOPagent.xml", problemFilePEAV, javaParamsOld], 
        ["MB-DPOP 2.x", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/MB-DPOP/MB-DPOPagent.xml", problemFilePEAV, javaParamsNew], 
            ]], 
    ["MB-DPOP+JaCoP", ["-i", "-PEAV", "-maxCost", 10, 3, list(range(1, 7)), 2, 8], [
        ["MB-DPOP+JaCoP 2.18", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/MB-DPOP/MB-DPOPagentJaCoP.xml", problemFilePEAV, javaParamsOld], 
        ["MB-DPOP+JaCoP 2.x", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/MB-DPOP/MB-DPOPagentJaCoP.xml", problemFilePEAV, javaParamsNew], 
            ]], 
    ["O-DPOP", ["-PEAV", "-maxCost", 10, 3, list(range(1, 8)), 2, 8], [
        ["O-DPOP 2.18", "frodo2.algorithms.odpop.ODPOPsolver", root + "agents/DPOP/O-DPOP/O-DPOPagent.xml", problemFilePEAV, javaParamsOld], 
        ["O-DPOP 2.x", "frodo2.algorithms.odpop.ODPOPsolver", root + "agents/DPOP/O-DPOP/O-DPOPagent.xml", problemFilePEAV, javaParamsNew], 
            ]], 
    ["O-DPOP+JaCoP", ["-i", "-PEAV", "-maxCost", 10, 3, list(range(1, 7)), 2, 8], [
        ["O-DPOP+JaCoP 2.18", "frodo2.algorithms.odpop.ODPOPsolver", root + "agents/DPOP/O-DPOP/O-DPOPagentJaCoP.xml", problemFilePEAV, javaParamsOld], 
        ["O-DPOP+JaCoP 2.x", "frodo2.algorithms.odpop.ODPOPsolver", root + "agents/DPOP/O-DPOP/O-DPOPagentJaCoP.xml", problemFilePEAV, javaParamsNew], 
            ]], 
    ["P-DPOP", ["-PEAV", "-maxCost", 10, 3, list(range(1, 7)), 2, 8], [
        ["P-DPOP 2.18", "frodo2.algorithms.dpop.privacy.P_DPOPsolver", root + "agents/DPOP/P-DPOP/P-DPOPagent.xml", problemFilePEAV, javaParamsOld], 
        ["P-DPOP 2.x", "frodo2.algorithms.dpop.privacy.P_DPOPsolver", root + "agents/DPOP/P-DPOP/P-DPOPagent.xml", problemFilePEAV, javaParamsNew], 
            ]],
    ["P-DPOP+JaCoP", ["-i", "-PEAV", "-maxCost", 10, 3, list(range(1, 7)), 2, 8], [
        ["P-DPOP+JaCoP 2.18", "frodo2.algorithms.dpop.privacy.P_DPOPsolver", root + "agents/DPOP/P-DPOP/P-DPOPagentJaCoP.xml", problemFilePEAV, javaParamsOld], 
        ["P-DPOP+JaCoP 2.x", "frodo2.algorithms.dpop.privacy.P_DPOPsolver", root + "agents/DPOP/P-DPOP/P-DPOPagentJaCoP.xml", problemFilePEAV, javaParamsNew], 
            ]],
    ["P1.5-DPOP", ["-PEAV", "-maxCost", 10, 3, list(range(1, 5)), 2, 8], [
        ["P3/2-DPOP 2.18", "frodo2.algorithms.dpop.privacy.P3halves_DPOPsolver", root + "agents/DPOP/P-DPOP/P1.5-DPOPagent.xml", problemFilePEAV, javaParamsOld], 
        ["P3/2-DPOP 2.x", "frodo2.algorithms.dpop.privacy.P3halves_DPOPsolver", root + "agents/DPOP/P-DPOP/P1.5-DPOPagent.xml", problemFilePEAV, javaParamsNew], 
            ]],
    ["P1.5-DPOP+JaCoP", ["-i", "-PEAV", "-maxCost", 10, 3, list(range(1, 5)), 2, 8], [
        ["P3/2-DPOP+JaCoP 2.18", "frodo2.algorithms.dpop.privacy.P3halves_DPOPsolver", root + "agents/DPOP/P-DPOP/P1.5-DPOPagentJaCoP.xml", problemFilePEAV, javaParamsOld], 
        ["P3/2-DPOP+JaCoP 2.x", "frodo2.algorithms.dpop.privacy.P3halves_DPOPsolver", root + "agents/DPOP/P-DPOP/P1.5-DPOPagentJaCoP.xml", problemFilePEAV, javaParamsNew], 
            ]],
    ["P2-DPOP", ["-PEAV", "-maxCost", 10, 3, list(range(1, 5)), 2, 4], [
        ["P2-DPOP 2.18", "frodo2.algorithms.dpop.privacy.P2_DPOPsolver", root + "agents/DPOP/P-DPOP/P2-DPOPagent.xml", problemFilePEAV, javaParamsOld],
        ["P2-DPOP 2.x", "frodo2.algorithms.dpop.privacy.P2_DPOPsolver", root + "agents/DPOP/P-DPOP/P2-DPOPagent.xml", problemFilePEAV, javaParamsNew],
            ]],
    ["P2-DPOP+JaCoP", ["-i", "-PEAV", "-maxCost", 10, 3, list(range(1, 5)), 2, 4], [
        ["P2-DPOP+JaCoP 2.18", "frodo2.algorithms.dpop.privacy.P2_DPOPsolver", root + "agents/DPOP/P-DPOP/P2-DPOPagentJaCoP.xml", problemFilePEAV, javaParamsOld],
        ["P2-DPOP+JaCoP 2.x", "frodo2.algorithms.dpop.privacy.P2_DPOPsolver", root + "agents/DPOP/P-DPOP/P2-DPOPagentJaCoP.xml", problemFilePEAV, javaParamsNew],
            ]],
    ["DUCT", ["-PEAV", "-infinity", 1000, "-maxCost", 10, 3, list(range(1, 17, 5)), 2, 8], [
        ["DUCT 2.18", "frodo2.algorithms.duct.DUCTsolver", root + "agents/DUCT/DUCTagent.xml", problemFilePEAV, javaParamsOld], 
        ["DUCT 2.1x", "frodo2.algorithms.duct.DUCTsolver", root + "agents/DUCT/DUCTagent.xml", problemFilePEAV, javaParamsNew], 
            ]],
    ["DSA", ["-PEAV", "-infinity", 1000, "-maxCost", 10, 3, list(range(1, 37, 5)), 2, 8], [
        ["DSA 2.18", "frodo2.algorithms.localSearch.dsa.DSAsolver", root + "agents/DSA/DSAagent.xml", problemFilePEAV, javaParamsOld], 
        ["DSA 2.x", "frodo2.algorithms.localSearch.dsa.DSAsolver", root + "agents/DSA/DSAagent.xml", problemFilePEAV, javaParamsNew], 
            ]],
    ["DSA+JaCoP", ["-PEAV", "-infinity", 1000, "-maxCost", 10, 3, list(range(1, 18, 2)), 2, 8], [
        ["DSA+JaCoP 2.18", "frodo2.algorithms.localSearch.dsa.DSAsolver", root + "agents/DSA/DSAagentJaCoP.xml", problemFilePEAV, javaParamsOld], 
        ["DSA+JaCoP 2.x", "frodo2.algorithms.localSearch.dsa.DSAsolver", root + "agents/DSA/DSAagentJaCoP.xml", problemFilePEAV, javaParamsNew], 
            ]],
    ["MaxSum", ["-PEAV", "-infinity", 1000, "-maxCost", 10, 3, list(range(1, 10, 2)), 2, 8], [
        ["MaxSum 2.18", "frodo2.algorithms.maxsum.MaxSumSolver", root + "agents/MaxSum/MaxSumAgentPerturbed.xml", problemFilePEAV, javaParamsOld], 
        ["MaxSum 2.x", "frodo2.algorithms.maxsum.MaxSumSolver", root + "agents/MaxSum/MaxSumAgentPerturbed.xml", problemFilePEAV, javaParamsNew], 
            ]],
    ["MaxSum+JaCoP", ["-PEAV", "-infinity", 1000, "-maxCost", 10, 3, list(range(1, 8)), 2, 8], [
        ["MaxSum+JaCoP 2.18", "frodo2.algorithms.maxsum.MaxSumSolver", root + "agents/MaxSum/MaxSumAgentPerturbedJaCoP.xml", problemFilePEAV, javaParamsOld], 
        ["MaxSum+JaCoP 2.x", "frodo2.algorithms.maxsum.MaxSumSolver", root + "agents/MaxSum/MaxSumAgentPerturbedJaCoP.xml", problemFilePEAV, javaParamsNew], 
            ]],
    ["MGM", ["-PEAV", "-infinity", 1000, "-maxCost", 10, 3, list(range(1, 17, 3)), 2, 8], [
        ["MGM 2.18", "frodo2.algorithms.localSearch.mgm.MGMsolver", root + "agents/MGM/MGMagent.xml", problemFilePEAV, javaParamsOld], 
        ["MGM 2.x", "frodo2.algorithms.localSearch.mgm.MGMsolver", root + "agents/MGM/MGMagent.xml", problemFilePEAV, javaParamsNew], 
            ]],
    ["MGM+JaCoP", ["-PEAV", "-infinity", 1000, "-maxCost", 10, 3, list(range(1, 8)), 2, 8], [
        ["MGM+JaCoP 2.18", "frodo2.algorithms.localSearch.mgm.MGMsolver", root + "agents/MGM/MGMagentJaCoP.xml", problemFilePEAV, javaParamsOld], 
        ["MGM+JaCoP 2.x", "frodo2.algorithms.localSearch.mgm.MGMsolver", root + "agents/MGM/MGMagentJaCoP.xml", problemFilePEAV, javaParamsNew], 
            ]],
    ["MGM2", ["-PEAV", "-infinity", 1000, "-maxCost", 10, 3, list(range(1, 20, 3)), 2, 8], [
        ["MGM2 2.18", "frodo2.algorithms.localSearch.mgm.mgm2.MGM2solver", root + "agents/MGM/MGM2agent.xml", problemFilePEAV, javaParamsOld], 
        ["MGM2 2.x", "frodo2.algorithms.localSearch.mgm.mgm2.MGM2solver", root + "agents/MGM/MGM2agent.xml", problemFilePEAV, javaParamsNew], 
            ]],
    ["MGM2+JaCoP", ["-PEAV", "-infinity", 1000, "-maxCost", 10, 3, list(range(1, 7)), 2, 8], [
        ["MGM2+JaCoP 2.18", "frodo2.algorithms.localSearch.mgm.mgm2.MGM2solver", root + "agents/MGM/MGM2agentJaCoP.xml", problemFilePEAV, javaParamsOld], 
        ["MGM2+JaCoP 2.x", "frodo2.algorithms.localSearch.mgm.mgm2.MGM2solver", root + "agents/MGM/MGM2agentJaCoP.xml", problemFilePEAV, javaParamsNew], 
            ]],
    ["MPC-DisCSP4", ["-EAV", 3, list(range(1, 6)), 2, 8], [
        ["MPC-DisCSP4 2.18", "frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver", root + "agents/MPC/MPC-DisCSP4.xml", problemFileEAV, javaParamsOld],
        ["MPC-DisCSP4 2.x", "frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver", root + "agents/MPC/MPC-DisCSP4.xml", problemFileEAV, javaParamsNew],
            ]],
    ["MPC-DisCSP4+JaCoP", ["-i", "-EAV", 3, list(range(1, 6)), 2, 8], [
        ["MPC-DisCSP4+JaCoP 2.18", "frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver", root + "agents/MPC/MPC-DisCSP4_JaCoP.xml", problemFileEAV, javaParamsOld],
        ["MPC-DisCSP4+JaCoP 2.x", "frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver", root + "agents/MPC/MPC-DisCSP4_JaCoP.xml", problemFileEAV, javaParamsNew],
            ]],
    ["MPC-DisWCSP4", ["-EAV", "-maxCost", 10, 3, list(range(1, 4)), 2, 8], [
        ["MPC-DisWCSP4 2.18", "frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver", root + "agents/MPC/MPC-DisWCSP4.xml", problemFileEAV, javaParamsOld],
        ["MPC-DisWCSP4 2.x", "frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver", root + "agents/MPC/MPC-DisWCSP4.xml", problemFileEAV, javaParamsNew],
            ]],    
    ["MPC-DisWCSP4+JaCoP", ["-i", "-EAV", "-maxCost", 10, 3, list(range(1, 4)), 2, 8], [
        ["MPC-DisWCSP4+JaCoP 2.18", "frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver", root + "agents/MPC/MPC-DisWCSP4_JaCoP.xml", problemFileEAV, javaParamsOld],
        ["MPC-DisWCSP4+JaCoP 2.x", "frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver", root + "agents/MPC/MPC-DisWCSP4_JaCoP.xml", problemFileEAV, javaParamsNew],
            ]],    
    ["SynchBB", ["-PEAV", "-maxCost", 10, 3, list(range(1, 9)), 2, 8], [
        ["SynchBB 2.18", "frodo2.algorithms.synchbb.SynchBBsolver", root + "agents/SynchBB/SynchBBagent.xml", problemFilePEAV, javaParamsOld], 
        ["SynchBB 2.x", "frodo2.algorithms.synchbb.SynchBBsolver", root + "agents/SynchBB/SynchBBagent.xml", problemFilePEAV, javaParamsNew], 
            ]],
    ["SynchBB+JaCoP", ["-i", "-PEAV", "-maxCost", 10, 3, list(range(1, 8)), 2, 8], [
        ["SynchBB+JaCoP 2.18", "frodo2.algorithms.synchbb.SynchBBsolver", root + "agents/SynchBB/SynchBBagentJaCoP.xml", problemFilePEAV, javaParamsOld], 
        ["SynchBB+JaCoP 2.x", "frodo2.algorithms.synchbb.SynchBBsolver", root + "agents/SynchBB/SynchBBagentJaCoP.xml", problemFilePEAV, javaParamsNew], 
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
inputCol = 10 # number of meetings
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
