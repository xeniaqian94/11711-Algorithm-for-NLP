import sys
import re
import matplotlib.pyplot as plt


# python perplexity_BLEU_vs_training_size.py ../perplexity_log_with_BLEU ../plot/perplexity_vs_training_size_with_BLEU

f=open(sys.argv[1],"r")
training_size=[]
perplexity=[]
BLEU=[]

for line in f.readlines():
	if re.match(r"perplexity for training size ([0-9]+) ([0-9]+)",line):
		training_size+=[int(re.match(r"perplexity for training size ([0-9]+) ([0-9]+)",line).group(1))]
		perplexity+=[int(re.match(r"perplexity for training size ([0-9]+) ([0-9]+)",line).group(2))]

	if re.match(r"^BLEU score on test data was BLEU\(([\.0-9]+)\)",line):
		BLEU+=[float(re.match(r"^BLEU score on test data was BLEU\(([\.0-9]+)\)",line).group(1))]

assert len(perplexity)==len(training_size)==len(BLEU)

perplexity=[x for _,x in sorted(zip(training_size,perplexity))]
BLEU=[x for _,x in sorted(zip(training_size,BLEU))]
training_size=sorted(training_size)

print perplexity,training_size,BLEU

fig, ax1 = plt.subplots()
ax1.plot(training_size, perplexity, 'b.-',label="perplexity")
ax1.set_xlabel('Training Size')
# Make the y-axis label, ticks and tick labels match the line color.
ax1.set_ylabel('perplexity',color='b')
ax1.tick_params('y', colors='b')
ax1.legend(loc=4)


ax2 = ax1.twinx()

ax2.plot(training_size,BLEU, 'r^--',label="BLEU")
ax2.set_ylabel('BLEU',color='r')
ax2.tick_params('y', colors='r')
ax1.grid()
ax2.grid()
ax2.legend(loc=1)

fig.tight_layout()
plt.savefig(sys.argv[2])
plt.show()




