ant -f build_assign_parsing.xml
export DATA_PATH="../assign2_data/"
echo "data path is "$DATA_PATH


export PARSER_TESTER_PATH="edu.berkeley.nlp.assignments.parsing.student.PCFGParserTester3"

# export PARSER_TESTER_PATH="edu.berkeley.nlp.assignments.parsing.student.PCFGParserTester2"
# export PARSER_TESTER_PATH="edu.berkeley.nlp.assignments.parsing.PCFGParserTester"

# java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m $PARSER_TESTER_PATH  -path $DATA_PATH -parserType BASELINE

# java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m $PARSER_TESTER_PATH  -path $DATA_PATH -parserType GENERATIVE -test
# java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m $PARSER_TESTER_PATH  -path $DATA_PATH -parserType GENERATIVE  -v 1 -h INF -maxTrainLength 15 -maxTestLength 15

# java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m $PARSER_TESTER_PATH  -path $DATA_PATH -parserType GENERATIVE  -v 1 -h INF 
java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m $PARSER_TESTER_PATH  -path $DATA_PATH -parserType GENERATIVE -h 2 -v 2 -maxTrainLength 15 -maxTestLength 15

# java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m edu.berkeley.nlp.assignments.parsing.PCFGParserTester -path $DATA_PATH -parserType BASELINE
# java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m edu.berkeley.nlp.assignments.parsing.PCFGParserTester -path $DATA_PATH -parserType GENERATIVE   # please take a look at PCFGParserTester
# java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m edu.berkeley.nlp.assignments.parsing.PCFGParserTester -path $DATA_PATH -parserType COARSE_TO_FINE







#java -cp assign1.jar:assign1-submit.jar -server -mx2000m edu.berkeley.nlp.assignments.assign1.student.LanguageModelTester2 -path $DATA_PATH -lmType TRIGRAM -noprint -discountFactor 0.3
#for sent in 7189 8986 9073252 7258601 5806881 4645505 3716404 2973123 2378498 1902798 1522239 1217791 974233 779386 623509 498807 399045 319236 255389 204311 163449 130759 104607 83685 66948 53559 42847 34277 27422 21937 17550 14040 11232; do java -cp assign1.jar:assign1-submit.jar -server -mx2500m edu.berkeley.nlp.assignments.assign1.student.LanguageModelTester2 -path $DATA_PATH -lmType TRIGRAM -noprint -calculatePerplexity $sent; done


