###########
##Computes the betweenness centrality of all nodes in a graph by
##first running BFS on every cluster, then running Dijkstra on the skeleton
##finally computing the betweenness of nodes
##Keeps every data in memory
##
##input files:
##graphfile: contains edges in nodeid1\tnodeid2\n format. Direction or repetition of edges doesn't matter 
##sourcefile: containes \t separated list of source ids
##clusterfile: contains in row i cluster id of node i
##outputfile: file, where the resulting betweenness centrality is written
##########


import sys
import Queue
import itertools
import pickle
import time, datetime
import PriorityDictionary as PD




def buildGraph(sourcefile,clusterdir,num_clusters):
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
    # for every skeleton node contains edgelist with triplets (neighborId, dist, numPaths)
    skeletonfile = clusterdir+'/skeleton_edges'
    f = open(skeletonfile,'rb')
    skeletonEdges = pickle.load(f)
    f.close()
    #contains for every skeletonNode list of (sourceId, dist, numPaths)
    skeletonNodes = {}
    for nodeId in skeletonEdges:
        skeletonNodes[nodeId] = {}
    clusterNodes = {}
    clusterEdges = {}
    frontierNodes = {}
    counter = 0 
    for clusterId in range(1,num_clusters+1):
        try:
            clusterfile = clusterdir+'/nodes_C'+str(clusterId)
            f = open(clusterfile,'rb')
            clusterNodes[clusterId] = pickle.load(f)
            f.close()
            clusterfile = clusterdir+'/edges_C'+str(clusterId)
            f = open(clusterfile,'rb')
            clusterEdges[clusterId] = pickle.load(f)
            f.close()
            clusterfile = clusterdir+'/frontiers_C'+str(clusterId)
            f = open(clusterfile,'rb')
            frontierNodes[clusterId] = pickle.load(f)
            f.close()
            counter += 1
        except:
            num_clusters = counter
    clusterfile = clusterdir+'/cluster_map'
    f = open(clusterfile,'rb')
    clusterIds = pickle.load(f)
    f.close()
    return (sources,clusterEdges,clusterNodes,frontierNodes,skeletonEdges,skeletonNodes,clusterIds)


#BFS algorithm on (unweighted) cluster run from every frontier node in cluster as source
def BFS(clusterId,lclusterNodes, lclusterEdges, lfrontierNodes):
    for nId in lclusterNodes:lclusterNodes[nId] = {}
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

def BrandesPP(sources,skeletonEdges,skeletonNodes,clusterNodes,clusterIds,frontierNodes):
    centrality= {}
    for nodeId in clusterIds: centrality[nodeId] = 0
    lnodes = clusterIds.keys()
    for nodeId in skeletonNodes:lnodes.remove(nodeId)
     #dummy node to make sure all sources are frontier nodes
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
        for nodeId in skeletonNodes:
            Q[nodeId] = sys.maxint
            delta[nodeId] = 0
            parentDict[nodeId] = []
            numPaths[nodeId] = 0
        numPaths[source] = 1
        Q[source] = 0
        for currentNode in Q:
            minDist = Q[currentNode]
            skeletonNodes[currentNode][source] = (Q[currentNode],numPaths[currentNode])
            discoveryList.append(currentNode)
            #find children of currentNode
            for childNode in skeletonEdges[currentNode]:
                dist = skeletonEdges[currentNode][childNode][0]
                if childNode in Q and dist+minDist < Q[childNode]:
                    Q[childNode] = dist+minDist
                    parentDict[childNode] = [currentNode]
                    numPaths[childNode] = numPaths[currentNode]*skeletonEdges[currentNode][childNode][1]
                elif childNode in Q and dist+minDist == Q[childNode] :
                    if childNode == currentNode:continue
                    parentDict[childNode].append(currentNode)
                    numPaths[childNode] += numPaths[currentNode]*skeletonEdges[currentNode][childNode][1]
        while len(discoveryList)>0:
            childNode = discoveryList.pop()
            if childNode == source:
                centrality[source] += delta[source]/2.0
                continue
            if childNode in sources:delta[childNode] += 1
            centrality[childNode] += delta[childNode]/2.0
            for parentNode in parentDict[childNode]:
                 #formula is modified from original Brandes to accomodate subset of nodes as sources
                if numPaths[childNode]>0:
                    delta[parentNode] += delta[childNode]*skeletonEdges[parentNode][childNode][1]*float(numPaths[parentNode])/float(numPaths[childNode])
        #compute delta of non-skeleton nodes
        for parentNode in lnodes:
            mindist = sys.maxint
            numpaths = 0
            #check which frontierNodes are on a shortest path between source and nodeId
            for frontierNode in clusterNodes[clusterIds[parentNode]][parentNode]:
                (dist,clusterpaths) = clusterNodes[clusterIds[parentNode]][parentNode][frontierNode]
                if dist + skeletonNodes[frontierNode][source][0] < mindist:
                    mindist = dist + skeletonNodes[frontierNode][source][0]
                    numpaths = skeletonNodes[frontierNode][source][1]*clusterpaths
                elif dist + skeletonNodes[frontierNode][source][0] == mindist:
                    numpaths += skeletonNodes[frontierNode][source][1]*clusterpaths
            for childNode in frontierNodes[clusterIds[parentNode]]:
                if childNode not in clusterNodes[clusterIds[parentNode]][parentNode]:continue
                if skeletonNodes[childNode][source][0] == mindist + clusterNodes[clusterIds[parentNode]][parentNode][childNode][0]:
                    centrality[parentNode] += delta[childNode]*clusterNodes[clusterIds[parentNode]][parentNode][childNode][1]*float(numpaths)/(float(numPaths[childNode])*2.0)
    return (centrality,skeletonNodes,skeletonEdges)



#write centrality of every node to outputfile
def writeToFile(outputfile, centrality):
    of = open(outputfile,'w')
    for nodeId in centrality:
        of.write(str(nodeId)+"\t"+str(centrality[nodeId])+"\n")
    of.close()

#read data from logged files in cluster_dir, then run BrandesPP
def run(filenames,num_clusters):
    (sources,clusterEdges,clusterNodes,frontierNodes,skeletonEdges,skeletonNodes,clusterIds) = buildGraph(filenames[0],filenames[1],num_clusters)
    (centrality,skeletonNodes,skeletonEdges) = BrandesPP(sources,skeletonEdges,skeletonNodes,clusterNodes,clusterIds,frontierNodes)
    writeToFile(filenames[2],centrality)


def main(args=[]):
    filenames = []
    #sourcefile
    if len(args)>0:
        filenames.append(args[0])
    else: filenames.append('example_input/source.txt')
    #cluster_dir
    if len(args)>1:
        filenames.append(args[1])
    else: filenames.append('example_input/cluster_dir')
    #number of clusters in cluster_dir
    #assumes consecutive cluster ids starting from 1
    if len(args)>2:
        num_clusters = int(args[2])
    else:
        num_clusters = 2
    if len(args)>3:
        filenames.append(args[3])
    else: filenames.append('centrality_brandespp.txt')
    if len(args)<4:
        sys.stderr.write('usage of this file:\n')
        sys.stderr.write('python BrandesPP_query.py sourcefile clusterdir numclusters outputfile\n')
    run(filenames,num_clusters)

if __name__ == "__main__":
        main(sys.argv[1:])

