###########
##Computes the betweenness centrality of all nodes in a graph by
##by running Brandes' 2001 algorithm
##Keeps every data in memory
##
##input files:
##graphfile: contains edges in nodeid1\tnodeid2\n format. Direction or repetition of edges doesn't matter 
##sourcefile: containes \t separated list of source ids
##outputfile: file, where the resulting betweenness centrality is written
##########


import sys
import Queue
import itertools
import datetime,time
import PriorityDictionary as PD


#read id of source nodes
def readSource(sourcefile):
    #list of source node ids
    sources = []
    #read list of sources from file
    f = open(sourcefile,'r')
    l = f.readline()
    a = l.split("\t")
    for i in range(len(a)):
        ai = a[i].strip()
        if len(ai)>0: sources.append(int(ai))
    f.close()
    return (sources)

#read graph from file
def buildGraph(graphfile,sources,w=0):
    # for every nodeId contains list of neighbors
    edges = {} 
    #for every NodeId contains dict of [source] = (dist,numpaths)
    nodes = {}
    #read graph from file
    #while reading create edge list
    with open(graphfile,'r') as f:
        for l in f:
            a = l.split("\t")
            a0 = int(a[0].strip())
            a1 = int(a[1].strip())
            if w ==1:
                a2 = int(a[2].strip())
            else:
                a2 = 1
            if not a0 in edges:
                edges[a0] = {}
            if not a1 in edges:
                edges[a1] = {}
            if not a1 in edges[a0]: edges[a0][a1] = (a2,1)
            if not a0 in edges[a1]: edges[a1][a0] = (a2,1)
    sourcedict = {}
    for source in sources: sourcedict[source] = (sys.maxint,0)
    for nodeId in edges:
        nodes[nodeId] = sourcedict.copy()
    return (edges,nodes)


#Ulrik Brandes' original algorithm, implemented using the same data structures as Brandes++
def Brandes(sources,edges,nodes):
    centrality= {}
    for nodeId in nodes: centrality[nodeId] = 0
     #dummy node to make sure all sources are frontier nodes
    maxNode = -1
    centrality[maxNode] = 0
    nodes[maxNode] = {}
    for source in sources:
        #priority dictionary containing distances.
        Q = PD.priorityDictionary()
        #keep track in which order nodes are fixed in Dijkstra. Used to traverse
        #shortest paths tree in reversed order
        discoveryList = []
        #contains list of parent nodes for every node
        parentDict = {}
        Q[source] = 0
        numPaths = {}
        delta = {}
        #initialize data structures
        for nodeId in nodes:
            Q[nodeId] = sys.maxint
            delta[nodeId] = 0
            parentDict[nodeId] = []
            numPaths[nodeId] = 0
        numPaths[source] = 1
        Q[source] = 0
        for currentNode in Q:
            if currentNode == maxNode:continue
            minDist = Q[currentNode]
            nodes[currentNode][source] = (Q[currentNode],numPaths[currentNode])
            discoveryList.append(currentNode)
            #find children of currentNode
            for childNode in edges[currentNode]:
                if childNode == maxNode:continue
                dist = edges[currentNode][childNode][0]
                if childNode in Q and dist+minDist < Q[childNode]:
                    Q[childNode] = dist+minDist
                    parentDict[childNode] = [currentNode]
                    numPaths[childNode] = numPaths[currentNode]*edges[currentNode][childNode][1]
                elif childNode in Q and dist+minDist == Q[childNode] :
                    if childNode == currentNode:continue
                    parentDict[childNode].append(currentNode)
                    numPaths[childNode] += numPaths[currentNode]*edges[currentNode][childNode][1]
        while len(discoveryList)>0:
            childNode = discoveryList.pop()
            if childNode == source:
                centrality[source] += delta[source]/2.0
                continue
            if childNode in sources:delta[childNode] += 1
            centrality[childNode] += delta[childNode]/2.0
            for parentNode in parentDict[childNode]:
                if parentNode == maxNode:continue
                 #formula is modified from original Brandes to accomodate subset of nodes as sources
                if numPaths[childNode]>0:
                    delta[parentNode] += delta[childNode]*edges[parentNode][childNode][1]*float(numPaths[parentNode])/float(numPaths[childNode])
    return (centrality,nodes,edges)



#write centrality of every node to outputfile
def writeToFile(outputfile, centrality):
    of = open(outputfile,'w')
    for nodeId in centrality:
        of.write(str(nodeId)+"\t"+str(centrality[nodeId])+"\n")
    of.close()

#run Brandes algorithm
def run(filenames):
    (sources) = readSource(filenames[1])
    (edges,nodes) = buildGraph(filenames[0],sources)
    (centrality,nodes,edges) = Brandes(sources,edges,nodes)
    writeToFile(filenames[2],centrality)

#read command line arguments and then run()
def main(args=[]):
    filenames = []
    #graphfile
    if len(args)>0:
        filenames.append(args[0])
    else: filenames.append('example_input/graph.txt')
    #sourcefile
    if len(args)>1:
        filenames.append(args[1])
    else: filenames.append('example_input/source.txt')
        #outputfile
    if len(args)>2:
        filenames.append(args[2])
    else: filenames.append('centrality_brandes.txt')
    if len(args)<3:
        sys.stderr.write('usage of this file:\n')
        sys.stderr.write('python Brandes.py graphfile sourcefile outputfile\n')
    run(filenames)

if __name__ == "__main__":
        main(sys.argv[1:])

