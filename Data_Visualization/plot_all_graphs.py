import math

import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import matplotlib.ticker as ticker

import constants
import data_parsing


def plotalg(alg, x, y, xtext, ytext, loglog, errors,loglin=False):
    data = data_parsing.readJSONToPandasFlat()
    print(data.algs.unique())
    data = data[data['algs'] == alg]

    palette = sns.color_palette("plasma", len(constants.samples_order))

    data = data.rename(columns={"param1": "Samples","graphs":'Graph'})

    plt.tight_layout()
    sns.set_theme(font_scale=1.25,style='ticks')  # big
    g = sns.relplot(
        data=data,
        x=x, y=y, hue='Samples', hue_order=constants.samples_order[::-1], palette=palette,height=5,aspect=(2/3),
        col='Graph', col_order=constants.shortGraphNames,

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

    plt.savefig(x + "_" + y + "_" + ('log-log_' if loglog else "") +('log-lin_' if loglin else "")+ 'all' + '.png', bbox_inches="tight")


alg = "GeisbergerBisection"
x = 'time'
y = "percent"
xtext = 'Computation Time (s)'
ytext = 'Top 1% Correctness'
loglog = True
loglin = False
error = False

plotalg(alg, x, y, xtext, ytext, loglog, error,loglin=loglin)
#plt.show()
