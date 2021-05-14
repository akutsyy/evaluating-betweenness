import matplotlib.pyplot as plt
import numpy as np

def parse():
    cMap = {}
    tMap = {}
    dMap = {}
    with open("../Algorithm_Testing/centrality.txt","r") as centrality:
        cString = centrality.read()
        cString = cString.strip("{}")
        cSplit = cString.split(", ")
        for s in cSplit:
            split2 = s.split("=")
            cMap[int(split2[0])] = float(split2[1])

    with open("../Algorithm_Testing/time.txt", "r") as time:
        tString = time.read()
        tString = tString.strip("{}")
        tSplit = tString.split(", ")
        for s in tSplit:
            split2 = s.split("=")
            tMap[int(split2[0])] = float(split2[1])

    with open("../Algorithm_Testing/degree.txt", "r") as degree:
        dString = degree.read()
        dString = dString.strip("{}")
        dSplit = dString.split(", ")
        for s in dSplit:
            split2 = s.split("=")
            dMap[int(split2[0])] = float(split2[1])

    cent_vs_time = {}
    for n in tMap.keys():
        cent_vs_time[cMap[n]] = tMap[n]

        #print(str(tMap[n]) +"   "+ str(cMap[n]))

    names = ['centrality', 'time']
    formats = ['float32', 'int32']
    dtype = dict(names=names, formats=formats)
    array = np.fromiter(cent_vs_time.items(), dtype=dtype, count=len(cent_vs_time))
    array.sort()
    centrality, time = zip(*array)

    cent_vs_degr = {}
    for n in cMap.keys():
        cent_vs_degr[cMap[n]] = dMap[n]

        # print(str(tMap[n]) +"   "+ str(cMap[n]))

    names = ['centrality', 'degree']
    formats = ['float32', 'int32']
    dtype = dict(names=names, formats=formats)
    array = np.fromiter(cent_vs_degr.items(), dtype=dtype, count=len(cent_vs_degr))
    array.sort()
    centrality, degree = zip(*array)

    print(max(time))

    plt.scatter(degree,centrality,s=10)
    #plt.yscale("log")
    #plt.hist(time,bins=30)
    plt.show()

parse()