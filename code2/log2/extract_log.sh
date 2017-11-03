echo $1

grep "Number of symbols" $1
grep "Number of unary" $1
grep "Number of binary" $1
tail -2 $1
echo ""
