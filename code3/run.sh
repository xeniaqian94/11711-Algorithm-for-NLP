ant -f build_assign_rerank.xml
export DATA_PATH="../assign3_data/"
echo "data path is "$DATA_PATH

# export TESTER_PATH="edu.berkeley.nlp.assignments.rerank.ParsingRerankerTester" 
export TESTER_PATH="edu.berkeley.nlp.assignments.rerank.student.ParsingRerankerTester2"
java -cp assign_rerank.jar:assign_rerank-submit.jar -server -mx10000m $TESTER_PATH -path $DATA_PATH -rerankerType AWESOME -verbose 
