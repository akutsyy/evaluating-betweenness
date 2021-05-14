import math

import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import matplotlib.ticker as ticker

import constants
import data_parsing



def plotalg(algs, x, y, xtext, ytext, loglog, errors,loglin=False):
    data = data_parsing.readJSONToPandasFlat()
    for a in constants.alg_names:
        if a not in algs:
            data = data[(data['algs'] != a)]

    palette = sns.color_palette("bright", len(algs))

    bsamples = ['none','1','2','4','8','16']
    data = data.rename(columns={"algs":"Algorithm","param1": "Samples","graphs":'Graph','param2':'Accumulation Samples'})
    size = 20
    plt.tight_layout()
    sns.set_theme(font_scale=1.25,style='ticks')  # big
    g = sns.relplot(
        data=data,
        x=x, y=y, hue='Algorithm', hue_order=algs[::-1],
        palette=palette, height=5,aspect=(2/3),
        col='Graph', col_order=constants.shortGraphNames,
        #size='Accumulation Samples',sizes=[size if a=='none' else size/2 for a in bsamples], size_order=bsamples,
        kind='scatter'
    )

    g.set(xlabel=xtext, ylabel=ytext)
    #g.fig.suptitle('BP2007 on all graphs, ' + xtext + ' vs ' + ytext + (', log-log' if loglog else ""),y=1.05)

    if loglog:
        plt.xscale('log')
        plt.yscale('log')

    if loglin:
        plt.xscale('log')

    if errors:
        errors = data.groupby(['Samples']).agg(mean_x=(x, 'mean'), mean_y=(y, 'mean'), std_x=(x, 'std'), std_y=(y, 'std'))
        combined = data.merge(right=errors, how='outer', on=['Samples'])
        combined = combined.sort_values('Samples')
        g.errorbar(x=combined['mean_x'], y=combined['mean_y'], xerr=combined['std_x'], yerr=combined['std_y'], fmt='none', zorder=0)

    plt.savefig("figs/geisgerger-"+x + "-" + y + "-" + ('log-log-' if loglog else "") +('log-lin-' if loglin else "") + '.png', bbox_inches="tight")


algs = ["BrandesAndPich2007","GeisbergerLinear","GeisbergerBisection"]

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
    #plotalg(algs, x, y, xtext, ytext, loglog,errors=error,loglin=loglin)
    plt.show()
    plt.clf()
