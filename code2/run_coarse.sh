ant -f build_assign_parsing.xml
export DATA_PATH="../assign2_data/"
echo "data path is "$DATA_PATH


# export PARSER_TESTER_PATH="edu.berkeley.nlp.assignments.parsing.student.PCFGParserTester3"

# export PARSER_TESTER_PATH="edu.berkeley.nlp.assignments.parsing.student.PCFGParserTester2"
export PARSER_TESTER_PATH="edu.berkeley.nlp.assignments.parsing.PCFGParserTester"

# java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m $PARSER_TESTER_PATH  -path $DATA_PATH -parserType BASELINE

# java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m $PARSER_TESTER_PATH  -path $DATA_PATH -parserType GENERATIVE -test
# java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m $PARSER_TESTER_PATH  -path $DATA_PATH -parserType GENERATIVE  -v 1 -h INF -maxTrainLength 15 -maxTestLength 15

# java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m $PARSER_TESTER_PATH  -path $DATA_PATH -parserType GENERATIVE  -v 1 -h INF 
# java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m $PARSER_TESTER_PATH  -path $DATA_PATH -parserType GENERATIVE 

# java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m edu.berkeley.nlp.assignments.parsing.PCFGParserTester -path $DATA_PATH -parserType BASELINE
# java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m edu.berkeley.nlp.assignments.parsing.PCFGParserTester -path $DATA_PATH -parserType GENERATIVE   # please take a look at PCFGParserTester
java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m $PARSER_TESTER_PATH  -path $DATA_PATH -parserType COARSE_TO_FINE -maxTrainLength 15 -maxTestLength 15


#java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m $PARSER_TESTER_PATH  -path $DATA_PATH -parserType COARSE_TO_FINE  
