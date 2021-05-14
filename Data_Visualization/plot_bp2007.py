import math

import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import matplotlib.ticker as ticker

import constants
import data_parsing


def plotalg(alg,graph,x,y,xtext,ytext,loglog,errors):
    data  = data_parsing.readJSONToPandasFlat()
    print(data.algs.unique())
    data = data[data['algs'] == alg]
    data = data[data['graphs'] == 'com-amazon']

    sns.set_theme(style="ticks")
    palette = sns.color_palette("plasma",len(constants.samples_order))


    data = data.rename(columns={"param1" : "Samples"})




    f, ax = plt.subplots(figsize=(6, 4))
    plt.tight_layout()
    g = sns.scatterplot(
        data=data,
        x=x, y=y,hue='Samples',hue_order=constants.samples_order[::-1],palette=palette,ax=ax

    )


    g.set(xlabel=xtext, ylabel=ytext)
    #g.set_title('BP2007 on '+graph+', '+xtext+' vs '+ytext+(', log-log' if loglog else ""),pad=12)

    if loglog:
        g.xaxis.set_major_locator(ticker.MultipleLocator(5))
        g.xaxis.set_major_formatter(ticker.ScalarFormatter())

        g.yaxis.set_major_locator(ticker.MultipleLocator(5))
        g.yaxis.set_major_formatter(ticker.ScalarFormatter())

        g.set(xscale="log")
        g.set(yscale="log")


    if errors:
        errors = data.groupby(['Samples']).agg(mean_x=(x, 'mean'), mean_y=(y, 'mean'), std_x=(x, 'std'), std_y=(y, 'std'))
        combined = data.merge(right=errors,how='outer',on=['Samples'])
        combined = combined.sort_values('Samples')
        g.errorbar(x=combined['mean_x'],y=combined['mean_y'],xerr=combined['std_x'],yerr=combined['std_y'],fmt='none',zorder=0)

    plt.savefig("figs/bp2007-"+x+"-"+y+"-"+('log-log-' if loglog else "")+graph+'.png', bbox_inches = "tight")


alg = "BrandesAndPich2007"
x = 'time'
y = "average"
graph = 'com-amazon'
xtext = 'Computation Time (s)'
ytext = 'Average Normalized Error'
loglog = False
error = False

plotalg(alg,graph,x,y,xtext,ytext,loglog,error)
plt.show()