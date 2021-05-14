import math

import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import matplotlib.ticker as ticker

import constants
import data_parsing



def plot_everything_lines(algs,shortnames, x, y, xtext, ytext, loglog, loglin=False, ):

    data  = data_parsing.readJSONToPandasFlat()

    # Filter out unwanted algs
    for a in constants.alg_names:
        if a not in algs:
            data = data[data['algs'] != a]



    def r(row):
        if row['algs'] == 'GeisbergerBisectionSampling':
            return 'Sampling: '+str(row['param2'])
        return shortnames[row['algs']]

    data['Algorithm'] = data.apply (lambda row: r(row), axis=1)

    data = data.rename(columns={"param1": "Samples","graphs":'Graph'})
    mergedErrors = data.groupby(['Samples','Algorithm']).agg(mean_x=(x, 'mean'), mean_y=(y, 'mean'))
    combined = data.merge(right=mergedErrors, how='outer', on=['Samples', 'Algorithm'])
    combined = combined.sort_values(['Samples', 'Algorithm'])


    plt.tight_layout()
    sns.set_theme(font_scale=1.25, style='ticks')  # big

    bsamples = ['1','2','4','8','16']
    order = ['Sampling: '+ n for n in bsamples]+[shortnames[a] for a in algs if a != 'GeisbergerBisectionSampling']
    palette = sns.color_palette("GnBu",len(bsamples)) +sns.color_palette("afmhot",len(algs)-1)[:-1] + ['#fc0303']

    g = sns.relplot(
        data=combined,
        x='mean_x', y=y,
        hue="Algorithm",hue_order=order,
        col="Graph",col_order=constants.shortGraphNames,
        palette=palette,kind="line"
    )

    g.set(xlabel=xtext, ylabel=ytext)


    if loglog:

        g.set(xscale="log")
        g.set(yscale="log")

    if loglin:
        plt.xscale('log')

    # Put the legend out of the figure
    #plt.legend(bbox_to_anchor=(1.05, 1), loc=2, borderaxespad=0.)

    plt.savefig("figs/everything-"+x + "-" + y + "-" + ('log-log-' if loglog else "") +('log-lin-' if loglin else "")+ '.png', bbox_inches="tight")


algs = ["BrandesAndPich2007","GeisbergerLinear","GeisbergerBisection","GeisbergerBisectionSampling","KADABRA"]
shortNames = {'BrandesAndPich2007':'BP2007','GeisbergerLinear':'Linear','GeisbergerBisection':'Bisection',"KADABRA":"KADABRA","GeisbergerBisectionSampling":"Sampling"}

x = 'time'
y = "average"
xtext = 'Computation Time (s)'
ytext = 'Average Normalized Error'
loglog = True
loglin = False
error = False
for i, y in enumerate(constants.metrics):
    ytext = constants.mainseriesNames[i]
    loglog = constants.mainseriesLogLog[i]
    loglin = not loglog
    plot_everything_lines(algs,shortNames, x, y, xtext, ytext, loglog,loglin=loglin)
    #plt.show()
    plt.clf()
