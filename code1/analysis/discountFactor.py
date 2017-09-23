import sys
import re
import matplotlib.pyplot as plt


# python discountFactor.py ../discount_factor_log ../plot/discount_factor_log

f=open(sys.argv[1],"r")
discountFactor=[]
BLEU=[]

for line in f.readlines():
	if re.match(r"^BLEU score on test data was BLEU\(([\.0-9]+)\)",line):
		BLEU+=[float(re.match(r"^BLEU score on test data was BLEU\(([\.0-9]+)\)",line).group(1))]
	elif re.match(r"^discount factor ([\.0-9]+)",line):
		discountFactor+=[float(re.match(r"^discount factor ([\.0-9]+)",line).group(1))]

print len(BLEU)
print len(discountFactor)
print discountFactor

BLEU=[x for _,x in sorted(zip(discountFactor,BLEU))]
discountFactor=sorted(discountFactor)
print BLEU,discountFactor

fig=plt.figure()
ax=fig.add_subplot(111)

ax.set_xlabel("Discount Factor")
ax.set_ylabel("BLEU")
ax.grid()
ax.plot(discountFactor,BLEU,"b.-")

plt.savefig(sys.argv[2])
plt.show()




