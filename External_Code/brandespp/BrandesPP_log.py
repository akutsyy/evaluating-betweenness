###########
##runs BFS on every cluster, then running Dijkstra on the skeleton
##writes resulting data structures to file
##
##input files:
##graphfile: contains edges in nodeid1\tnodeid2\n format. Direction or repetition of edges doesn't matter 
##sourcefile: containes \t separated list of source ids
##clusterfile: contains in row i cluster id of node i
##cluster_dir: name of folder where datastructures are written
##########


import sys
import Queue
import itertools
import pickle
import time, datetime


def readData(sourcefile,clusterfile,clusterdir):
 
    #list of global source nodes
    sources = []
    #read list of sources from file
    f = open(sourcefile,'r')
    l = f.readline()
    a = l.split("\t")
    for i in range(len(a)):
        ai = a[i].strip()
        if len(ai)>0: sources.append(int(ai))
    f.close()
    clusters = set([])
    counter = 1
    #contains for every nodeId the clusterId it belongs to
    clusterIds = {}
    #contains for every cluster and every node in that cluster  dict of [frontierId] = ( dist, numPaths)
    clusterNodes = {}
    with  open(clusterfile,'r') as f:
        for l in f:
            a = l.split()
            a0 = int(a[0].strip())
            if a0 not in clusterNodes: clusterNodes[a0] = {}
            clusters.add(a0)
            if not counter in sources:
                clusterIds[counter] = a0
                clusterNodes[a0][counter] = {}
            counter += 1
     #for every clusterId contains edge list of that cluster
    clusterEdges = {}
    #for every clusterId contains list of frontier nodes in that cluster
    frontierNodes = {}
    maxcluster = 0
    for clusterId in clusters:
        if clusterId>maxcluster: maxcluster = clusterId
        clusterEdges[clusterId] = {}
        frontierNodes[clusterId] = []
    del clusters
    for source in sources:
        maxcluster += 1
        clusterIds[source] = maxcluster
    clusterfile = clusterdir+'/cluster_map'
    f = open(clusterfile,'wb')
    pickle.dump(clusterIds,f)
    f.close()
    return (sources,clusterIds,clusterEdges,clusterNodes,frontierNodes)





#builds skeleton and supernodes    
def buildGraph(graphfile,clusterdir,sources,clusterIds,clusterEdges,clusterNodes,frontierNodes):
    # for every skeleton node contains edgelist with dict of [neighborId]= (dist, numPaths)
    skeletonEdges = {} 
    #contains for every skeletonNode list of [sourceId] =(dist, numPaths)
    skeletonNodes = {}
    #read graph from file
    #while reading create local edge list for clusters and inter cluster edges in skeletonEdges
    with open(graphfile,'r') as f:
        for l in f:
            a = l.split("\t")
            a0 = int(a[0].strip())
            a1 = int(a[1].strip())
            c0 = clusterIds[a0]
            c1 = clusterIds[a1]
            if not a0 in sources and not a0 in clusterEdges[c0]:
                clusterEdges[c0][a0] = []
            if not a1 in sources and not a1 in clusterEdges[c1]:
                clusterEdges[c1][a1] = []
            if c0 == c1:
                if not a1 in clusterEdges[c0][a0]:
                    clusterEdges[c0][a0].append(a1)
                if not a0 in  clusterEdges[c1][a1]:
                    clusterEdges[c1][a1].append(a0)
            else:
                if not a0 in skeletonEdges: 
                    skeletonEdges[a0] = {}
                    if not a0 in sources:
                        frontierNodes[c0].append(a0)
                skeletonEdges[a0][a1] = (1,1)
                if not a1 in skeletonEdges: 
                    skeletonEdges[a1] = {}
                    if not a1 in sources: 
                        frontierNodes[c1].append(a1)
                skeletonEdges[a1][a0]= (1,1)
                if a0 not in skeletonNodes: skeletonNodes[a0] = {}
                if a1 not in skeletonNodes: skeletonNodes[a1] = {}
    for source in sources:
        if not source in skeletonEdges: skeletonEdges[source] = {}
        skeletonEdges[source][source] = (0,1)
        if not source  in skeletonNodes: skeletonNodes[source] = {}
    for clusterId in clusterEdges:
        clusterNodes[clusterId] = BFS(clusterId,clusterNodes[clusterId], clusterEdges[clusterId], frontierNodes[clusterId])
        clusterfile = clusterdir+'/nodes_C'+str(clusterId)
        f = open(clusterfile,'wb')
        pickle.dump(clusterNodes[clusterId],f)
        f.close()
        clusterfile = clusterdir+'/edges_C'+str(clusterId)
        f = open(clusterfile,'wb')
        pickle.dump(clusterEdges[clusterId],f)
        f.close()
        clusterfile = clusterdir+'/frontiers_C'+str(clusterId)
        f = open(clusterfile,'wb')
        pickle.dump(frontierNodes[clusterId],f)
        f.close()
        for (frontierId,frontierNeighbor) in itertools.combinations(frontierNodes[clusterId],2):
            if frontierNeighbor not in clusterNodes[clusterId][frontierId]:continue
            skeletonEdges[frontierId][frontierNeighbor] = clusterNodes[clusterId][frontierId][frontierNeighbor]
            skeletonEdges[frontierNeighbor][frontierId] = clusterNodes[clusterId][frontierNeighbor][frontierId]
    skeletonfile = clusterdir+'/skeleton_edges'
    f = open(skeletonfile,'wb')
    pickle.dump(skeletonEdges,f)
    f.close()
    return (clusterEdges,clusterNodes,frontierNodes,skeletonEdges,skeletonNodes)


#BFS algorithm on (unweighted) cluster run from every frontier node in cluster as source
def BFS(clusterId,lclusterNodes, lclusterEdges, lfrontierNodes):
    for source in lfrontierNodes:
        nextChildList = []
        nextChildList.append(source)
        lclusterNodes[source][source] = (0,1)
        while len(nextChildList)>0:
            parentNode = nextChildList.pop(0)
            #we stop shortest paths computations if we reach one of the frontier nodes
            if parentNode in lfrontierNodes and parentNode != source: continue
            for childNode in lclusterEdges[parentNode]:
                if source not in lclusterNodes[childNode]:
                    lclusterNodes[childNode][source] = (lclusterNodes[parentNode][source][0]+1, lclusterNodes[parentNode][source][1])
                    nextChildList.append(childNode)
                elif lclusterNodes[childNode][source][0] ==  lclusterNodes[parentNode][source][0] + 1:
                    lclusterNodes[childNode][source] = (lclusterNodes[childNode][source][0], lclusterNodes[childNode][source][1] + lclusterNodes[parentNode][source][1])
    return lclusterNodes



#read input and then create supernodes and skeleton
def run(filenames):
    (sources,clusterIds,clusterEdges,clusterNodes,frontierNodes) = readData(filenames[1],filenames[2],filenames[3])
    (clusterEdges,clusterNodes,frontierNodes,skeletonEdges,skeletonNodes) = buildGraph(filenames[0],filenames[3],sources,clusterIds,clusterEdges,clusterNodes,frontierNodes)

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
    #file containing the cluster of every node
    if len(args)>2:
         filenames.append(args[2])
    else: filenames.append('example_input/cluster.txt')
    #name of folder where information on supernodes and local computations can be stored.
    if len(args)>3:
        filenames.append(args[3])
    else: filenames.append('example_input/cluster_dir')
    if len(args)<4:
        sys.stderr.write('usage of this file:\n')
        sys.stderr.write('python BrandesPP_log.py graphfile sourcefile clusterfile clusterdir\n')
    run(filenames)

if __name__ == "__main__":
        main(sys.argv[1:])

