from os import listdir
from os.path import isfile, join
import pandas as pd
import json

import constants


def parse_statistics(filename):
    with open(filename, 'r') as f:
        items = {}
        line = f.readline()
        while line:
            name, value = line.split(" ")
            name = name.strip(":")
            items[name] = float(value)
            line = f.readline()
        return items


def parse_description(filename):
    with open(filename, 'r') as f:
        items = {}
        line = f.readline()
        items['cutoff'] = 'none'
        items['completedIterations'] = 'none'
        while line:
            if "Time taken" in line:
                items["time"] = float(line.strip().split("taken: ")[-1])
            if "Graph accesses" in line:
                items["accesses"] = int(line.strip().split("accesses: ")[1])
            if "Bader" in filename and "cutoff" in line:
                items["cutoff"] = line.strip().split(":")[1].lower() == 'true'
            if "Bader" in filename and "after" in line:
                items["completedIterations"] = int(line.split("iterations: ")[1].split(" ")[0])
            if "KADABRA" in filename and "cutoff" in line:
                items["cutoff"] = line.strip().split(":")[1].lower() == 'true'
                items["completedIterations"] = int(line.split("iterations: ")[1].split(" ")[0])
            if "BrandesPP" in filename and "Times=" in line:
                items["cutoff"] = line.split(" on:")[0].split("Times=")[1]

            line = f.readline()

        return items


def parse_accesses(filename):
    try:
        with open(filename, 'r') as f:
            items = {}
            line = f.readline()
            while line:
                if "Graph accesses" in line:
                    items["accesses"] = int(line.strip().split("accesses: ")[1])
                line = f.readline()
            return items
    except OSError as e:
        print("Can't find " + filename)
        return {}


def mystrip(string, strip):
    if string.endswith(strip):
        string = string[:-len(strip)]
    if string.startswith(strip):
        string = string[len(strip):]
    return string


def parse_all_descripts_and_stats(statsDir, descriptionDir, alg, graph, wantedStats, wantedDescipts):
    items = {}
    for statFile in wantedStats:
        stripped = mystrip(mystrip(mystrip(statFile, alg), "_statistics.txt"), graph)
        iteration = mystrip(stripped, "_").split("_")[-1]
        params = mystrip(stripped[:-3], "_")
        param1 = 'none'
        param2 = 'none'
        if alg in ['BrandesAndPich2008_', 'GeisbergerLinear_', 'GeisbergerBisection_']:
            param1 = params
        if alg in ['Bader_']:
            param1 = params.split("_")[1]  # percentile
            param2 = params.split("_")[2]  # alpha
        if alg in ['GeisbergerBisectionSampled_']:
            param1 = params.split("_")[0]  # samples
            param2 = params.split("_")[1]  # bisectionSamples
        if alg in ['KADABRA_']:
            param1 = params.split("_")[0]  # lambda
            param2 = '0.1'
        if alg in ['BrandesPP_']:
            param1 = params.split("_")[0]  # lambda
            param2 = params.split("_")[1]
        if alg in ['BrandesSubset_']:
            param1 = params.split("_")[0]

        if len([f for f in wantedDescipts if
                mystrip(f, "_descriptor.txt") == mystrip(statFile, "_statistics.txt")]) == 0:
            print(statFile.strip("_statistics.txt"))
            print([f.strip("_descriptor.txt") for f in wantedDescipts])
        matching = [f for f in wantedDescipts if mystrip(f, "_descriptor.txt") == mystrip(statFile, "_statistics.txt")][
            0]
        if param1 not in items:
            items[param1] = {}
        if param2 not in items[param1]:
            items[param1][param2] = {}
        items[param1][param2][iteration] = parse_statistics(join(statsDir, statFile))
        items[param1][param2][iteration].update(parse_description(join(descriptionDir, matching)))
        if "accesses" not in items[param1][param2][iteration]:
            accessFile = mystrip(statFile, "_statistics.txt") + "_accesses.txt"
            items[param1][param2][iteration].update(parse_accesses(join(descriptionDir, "..", "accesses", accessFile)))
    return items


def parse_all_files(statsDir, descriptionDir):
    items = {}
    onlyStats = [f for f in listdir(statsDir) if isfile(join(statsDir, f))]
    onlyDescipts = [f for f in listdir(descriptionDir) if isfile(join(descriptionDir, f))]

    for alg in constants.alg_names:
        a = alg
        if alg == 'BrandesAndPich2007':
            a = 'BrandesAndPich2008'
        if alg == 'GeisbergerBisectionSampling':
            a = 'GeisbergerBisectionSampled'
        items[alg] = {}
        thisAlg = a + "_"
        for graph in constants.shortGraphNames:
            wantedStats = [f for f in onlyStats if thisAlg in f and graph in f]
            wantedDescipts = [f for f in onlyDescipts if thisAlg in f and graph in f]
            items[alg][graph] = parse_all_descripts_and_stats(statsDir, descriptionDir, thisAlg, graph,
                                                              wantedStats,
                                                              wantedDescipts)
    return items


def verifyDataIntegrity(items):
    for alg in items:
        for graph in items[alg]:
            for param1 in items[alg][graph]:
                for param2 in items[alg][graph][param1]:
                    for iteration in items[alg][graph][param1][param2]:
                        if "inversionPercent" not in items[alg][graph][param1][param2][iteration]:
                            print("Problem: ---------------------" + alg + graph + param1 + "  " + param2 + str(
                                iteration))
                            print(items[alg][graph][param1][param2][iteration])
                        elif items[alg][graph][param1][param2][iteration]["inversionPercent"] < 0:
                            print(str(alg) + str(graph) + str(param1 + "  " + param2) + str(iteration) + str(
                                items[alg][graph][param1][param2][iteration]["inversionPercent"]))

                        if "accesses" not in items[alg][graph][param1][param2][iteration]:
                            print(
                                "Problem accesses missing: ---------------------" + alg + graph + param1 + "  " + param2 + str(
                                    iteration))
                            print(items[alg][graph][param1][param2][iteration])
                        elif items[alg][graph][param1][param2][iteration]["accesses"] < 0:
                            print(str(alg) + str(graph) + str(param1 + "  " + param2) + str(iteration) + str(
                                items[alg][graph][param1][param2][iteration]["accesses"]))

                        if "time" not in items[alg][graph][param1][param2][iteration]:
                            print(
                                "Problem time missing: ---------------------" + alg + graph + param1 + "  " + param2 + str(
                                    iteration))
                            print(items[alg][graph][param1][param2][iteration])


def writeJSON():
    items = parse_all_files("../Algorithm_Testing/statistics", "../Algorithm_Testing/descriptions")
    with open('data.json', 'w') as fp:
        json.dump(items, fp)


def readJSON():
    with open('data.json', 'r') as fp:
        data = json.load(fp)
    return data


# Statistics are: euclideanDistance  inversionPercent  averageError  maxError  percentOfTop  time  accesses
def readJSONToLists():
    items = readJSON()

    algs = []
    graphs = []
    param1s = []
    param2s = []
    iterations = []
    euclid = []
    inversion = []
    average = []
    max = []
    percent = []
    time = []
    accesses = []
    nodes = []
    edges = []
    cutoffs = []
    completedIterations = []
    for alg in items:
        for graph in items[alg]:
            if graph == 'wiki-vote':
                continue
            for param1 in items[alg][graph]:
                for param2 in items[alg][graph][param1]:
                    for iteration in items[alg][graph][param1][param2]:
                        algs.append(alg)
                        graphs.append(graph)
                        param1s.append(param1)
                        param2s.append(param2)
                        iterations.append(iteration)
                        euclid.append(items[alg][graph][param1][param2][iteration]["euclideanDistance"])
                        inversion.append(items[alg][graph][param1][param2][iteration]["inversionPercent"])
                        average.append(items[alg][graph][param1][param2][iteration]["averageError"])
                        max.append(items[alg][graph][param1][param2][iteration]["maxError"])
                        percent.append(items[alg][graph][param1][param2][iteration]["percentOfTop"])
                        time.append(items[alg][graph][param1][param2][iteration]["time"])
                        accesses.append(items[alg][graph][param1][param2][iteration]["accesses"])
                        cutoffs.append(items[alg][graph][param1][param2][iteration]["cutoff"])
                        completedIterations.append(items[alg][graph][param1][param2][iteration]["completedIterations"])

                        nodes.append(constants.numNodes[graph])
                        edges.append(constants.numEdges[graph])

    return algs, graphs, param1s, param2s, iterations, euclid, inversion, average, max, percent, time, accesses, nodes, edges, cutoffs, completedIterations


def readJSONToPandas():
    algs, graphs, param1s, param2s, iterations, euclid, inversion, average, max, percent, time, accesses, nodes, edges, cutoffs, completedIterations = readJSONToLists()
    index = pd.MultiIndex.from_arrays([algs, graphs, param1s, param2s, iterations],
                                      names=('algs', 'graphs', 'param1', 'param2', 'iterations'))
    return pd.DataFrame({"euclid": euclid,
                         "inversions": inversion,
                         "average": average,
                         "max": max,
                         "percent": percent,
                         "time": time,
                         "accesses": accesses,
                         "nodes": nodes,
                         "edges": edges,
                         "cutoff": cutoffs,
                         "completedIterations": completedIterations}, index=index)


def readJSONToPandasFlat():
    algs, graphs, param1s, param2s, iterations, euclid, inversion, average, max, percent, time, accesses, nodes, edges, cutoffs, completedIterations = readJSONToLists()
    return pd.DataFrame({
        'algs': algs,
        'graphs': graphs,
        'param1': param1s,
        'param2': param2s,
        'iterations': iterations,
        "euclid": euclid,
        "inversions": inversion,
        "average": average,
        "max": max,
        "percent": percent,
        "time": time,
        "accesses": accesses,
        "nodes": nodes,
        "edges": edges,
        "cutoff": cutoffs,
        "completedIterations": completedIterations})


def readJSONToPandasAvg():
    data = readJSONToPandas()
    return data.groupby(level=['algs', 'graphs', 'param1', 'param2']).mean()


def readJSONToPandasStats():
    data = readJSONToPandas()
    return data.groupby(level=['algs', 'graphs', 'param1', 'param2']).agg(['mean', 'count', 'std'])


if __name__ == '__main__':
    writeJSON()
    items = readJSON()
    verifyDataIntegrity(items)

