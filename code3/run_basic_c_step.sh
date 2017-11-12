ant -f build_assign_rerank.xml
export DATA_PATH="../assign3_data/"
echo "data path is "$DATA_PATH

# export TESTER_PATH="edu.berkeley.nlp.assignments.rerank.ParsingRerankerTester" 
export TESTER_PATH="edu.berkeley.nlp.assignments.rerank.student.ParsingRerankerTester2"


for c in 0.1 1; do for step in 0.1 0.05 0.01; do java -cp assign_rerank.jar:assign_rerank-submit.jar -server -mx10000m $TESTER_PATH -path $DATA_PATH -rerankerType BASIC -verbose -C $c -step $step >> SVM_${c}_${step}.out; done done

# for c in 0.1 1; do for step in 0.1 0.05 0.01; do nohup java -cp assign_rerank.jar:assign_rerank-submit.jar -server -mx10000m $TESTER_PATH -path $DATA_PATH -rerankerType BASIC -verbose -C $c -step $step >> SVM_${c}_${step}.out & done done

# for c in 0.1 0.01 0.001 0.0001; do for step in 0.1 0.01 0.001 0.0001; do nohup java -cp assign_rerank.jar:assign_rerank-submit.jar -server -mx10000m $TESTER_PATH -path $DATA_PATH -rerankerType BASIC -verbose -C $c -step $step >> SVM_$c_$step.out & done done  
