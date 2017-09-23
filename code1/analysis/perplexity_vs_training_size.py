import sys
import re
import matplotlib.pyplot as plt


# python perplexity_vs_training_size.py ../perplexity_log ../plot/perplexity_vs_training_size

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
print perplexity,training_size

fig=plt.figure()
ax=fig.add_subplot(111)

ax.set_xlabel("training size")
ax.set_ylabel("perplexity")
ax.grid()
ax.plot(training_size,perplexity,"b.-")

plt.savefig(sys.argv[2])
plt.show()



