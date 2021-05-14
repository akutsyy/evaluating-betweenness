import matplotlib.pyplot as plt

alg_names = ["Brandes", "BrandesAndPich2007", "Bader", "GeisbergerBisection", "GeisbergerBisectionSampling",
             "GeisbergerLinear", "KADABRA","BrandesPP","BrandesSubset"]

metrics = ['max','euclid', 'inversions', 'percent', 'average']
mainseriesNames = ['Maximum Normalized Error','Normalized Euclidean Distance','Percent of Maximum Inversions','Top 1% Correctness','Average Normalized Error']
mainseriesLogLog = [True,True,True,False,True]

shortGraphNames = [
    "com-amazon",
    "slashdot0811",
    "as-caida20071105",
    "ca-astroph",
    "4932-protein",
]

numNodes = {
    "com-amazon":334853,
    "slashdot0811":77360,
    "as-caida20071105":26475,
    "ca-astroph":18772,
    "4932-protein":6574,
}

numEdges = {
    "com-amazon":925872,
    "slashdot0811":905468,
    "as-caida20071105":106762,
    "ca-astroph":396160,
    "4932-protein":1845996,
}

samples_order = ['25','50','100','200','400','800','1600','3200']
