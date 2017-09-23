import sys
import re
import matplotlib.pyplot as plt


# python perplexity_BLEU_vs_training_size.py ../perplexity_log ../plot/perplexity_vs_training_size

f=open(sys.argv[1],"r")
training_size=[]
perplexity=[]

for line in f.readlines():
	matched=re.match(r"perplexity for training size ([0-9]+) ([0-9]+)",line)
	if matched:
		training_size+=[int(matched.group(1))]
		perplexity+=[int(matched.group(2))]

perplexity=[x for _,x in sorted(zip(training_size,perplexity))]
training_size=sorted(training_size)
BLEU=[x for _,x in sorted(zip(training_size,BLEU))]
print perplexity,training_size,BLEU

fig, ax1 = plt.subplots()
ax1.plot(training_size, perplexity, 'b.-')
ax1.set_xlabel('Training Size')
# Make the y-axis label, ticks and tick labels match the line color.
ax1.set_ylabel('perplexity',color='b')
ax1.tick_params('y', colors='b')


ax2 = ax1.twinx()

ax2.plot(training_sizeg,BLEU, 'r^--')
ax2.set_ylabel('BLEU',color='r')
ax2.tick_params('y', colors='r')
ax1.grid()
ax2.grid()

fig.tight_layout()
plt.savefig(sys.argv[2])
plt.show()




