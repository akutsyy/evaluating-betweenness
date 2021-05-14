import math
import scipy
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import matplotlib.ticker as ticker

import constants
import data_parsing


def plotalg(alg,graph,x,y,xtext,ytext,loglog):
    data  = data_parsing.readJSONToPandasFlat()
    print(data.algs.unique())
    data = data[data['algs'] == alg]
    data = data[data['graphs'] == 'com-amazon']

    sns.set_theme(style="ticks")
    palette = sns.color_palette("plasma",len(constants.samples_order))


    data = data.rename(columns={"param1" : "Samples"})

    def r2(x, y):
        return scipy.stats.pearsonr(x, y)[0] ** 2

    print(r2(data[x],data[y]))
    f, ax = plt.subplots(figsize=(6, 4))
    plt.tight_layout()
    g = sns.regplot(
        data=data,
        x=x, y=y,ax=ax

    )


    g.set(xlabel=xtext, ylabel=ytext)
    g.set_title('BP2007 on '+graph+', '+xtext+' vs '+ytext+(', log-log' if loglog else ""),pad=12)

    if loglog:
        g.xaxis.set_major_locator(ticker.MultipleLocator(5))
        g.xaxis.set_major_formatter(ticker.ScalarFormatter())

        g.yaxis.set_major_locator(ticker.MultipleLocator(5))
        g.yaxis.set_major_formatter(ticker.ScalarFormatter())

        g.set(xscale="log")
        g.set(yscale="log")



    plt.savefig(x+"_"+y+"_"+('log-log_' if loglog else "")+graph+'_regression.png', bbox_inches = "tight")


alg = "BrandesAndPich2007"
x = 'time'
y = "accesses"
graph = 'com-amazon'
xtext = 'Computation Time (s)'
ytext = 'Edge Traversals'
loglog = False

plotalg(alg,graph,x,y,xtext,ytext,loglog)
plt.show()