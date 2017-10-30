package edu.berkeley.nlp.assignments.parsing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.berkeley.nlp.util.CollectionUtils;
import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.Indexer;

/**
 * Calculates and provides accessors for the REFLEXIVE, TRANSITIVE closure of
 * the unary rules in the provided Grammar. Each rule in this closure stands for
 * zero or more unary rules in the original grammar. Use the getPath() method to
 * retrieve the full sequence of symbols (from parent to child) which support
 * that path.
 */
public class UnaryClosure
{

	public static class PositiveCycleException extends RuntimeException {

		/**
		 * @param message
		 */
		public PositiveCycleException(String message) {
			super(message);
			// TODO Auto-generated constructor stub
		}
	}

	List<UnaryRule>[] closedUnaryRulesByChild;
  List<UnaryRule>[] closedUnaryRulesByParent;
	Map<UnaryRule, List<Integer>> pathMap = new HashMap<UnaryRule, List<Integer>>();

	public List<UnaryRule> getClosedUnaryRulesByChild(int child) {
	  return closedUnaryRulesByChild[child];
	}

	public List<UnaryRule> getClosedUnaryRulesByParent(int parent) {
    return closedUnaryRulesByParent[parent];
	}

	public List<Integer> getPath(UnaryRule unaryRule) {
		return pathMap.get(unaryRule);
	}
  
  public Map<UnaryRule, List<Integer>> getPathMap() {
    return pathMap;
  }

//	public String toString() {
//		StringBuilder sb = new StringBuilder();
//		for (String parent : closedUnaryRulesByParent.keySet()) {
//			for (UnaryRule unaryRule : getClosedUnaryRulesByParent(parent)) {
//				List<String> path = getPath(unaryRule);
//				//          if (path.size() == 2) continue;
//				sb.append(unaryRule);
//				sb.append("  ");
//				sb.append(path);
//				sb.append("\n");
//			}
//		}
//		return sb.toString();
//	}

	public UnaryClosure(Indexer<String> stateIndexer, Collection<UnaryRule> unaryRules) {
	  this.closedUnaryRulesByChild = new List[stateIndexer.size()];
	  this.closedUnaryRulesByParent = new List[stateIndexer.size()];
	  for (int i = 0; i < stateIndexer.size(); i++) {
	    this.closedUnaryRulesByChild[i] = new ArrayList<UnaryRule>();
	    this.closedUnaryRulesByParent[i] = new ArrayList<UnaryRule>();
	  }
		Map<UnaryRule, List<Integer>> closureMap = computeUnaryClosure(unaryRules, stateIndexer.size());
		for (UnaryRule unaryRule : closureMap.keySet()) {
			addUnary(unaryRule, closureMap.get(unaryRule));
		}
	}

	private void addUnary(UnaryRule unaryRule, List<Integer> path) {
	  closedUnaryRulesByChild[unaryRule.getChild()].add(unaryRule);
	  closedUnaryRulesByParent[unaryRule.getParent()].add(unaryRule);
		pathMap.put(unaryRule, path);
	}

	private static Map<UnaryRule, List<Integer>> computeUnaryClosure(Collection<UnaryRule> unaryRules, int numStates) {

		Map<UnaryRule, Integer> intermediateStates = new HashMap<UnaryRule, Integer>();
		Counter<UnaryRule> pathCosts = new Counter<UnaryRule>();
		Map<Integer, List<UnaryRule>> closedUnaryRulesByChild = new HashMap<Integer, List<UnaryRule>>();
		Map<Integer, List<UnaryRule>> closedUnaryRulesByParent = new HashMap<Integer, List<UnaryRule>>();

		Set<Integer> states = new HashSet<Integer>();

		for (UnaryRule unaryRule : unaryRules) {
			relax(pathCosts, intermediateStates, closedUnaryRulesByChild, closedUnaryRulesByParent, new UnaryRule(unaryRule.getParent(), unaryRule.getChild()), null, unaryRule.getScore());
			states.add(unaryRule.getParent());
			states.add(unaryRule.getChild());
		}

		for (Integer intermediateState : states) {
			List<UnaryRule> incomingRules = closedUnaryRulesByChild.get(intermediateState);
			List<UnaryRule> outgoingRules = closedUnaryRulesByParent.get(intermediateState);
			if (incomingRules == null || outgoingRules == null) continue;
			for (UnaryRule incomingRule : incomingRules) {
				for (UnaryRule outgoingRule : outgoingRules) {
					UnaryRule rule = new UnaryRule(incomingRule.getParent(), outgoingRule.getChild());
					double newScore = pathCosts.getCount(incomingRule) + pathCosts.getCount(outgoingRule);
					relax(pathCosts, intermediateStates, closedUnaryRulesByChild, closedUnaryRulesByParent, rule, intermediateState, newScore);
				}
			}
		}

		for (int state=0; state<numStates; ++state) {
			UnaryRule selfLoopRule = new UnaryRule(state, state);
			relax(pathCosts, intermediateStates, closedUnaryRulesByChild, closedUnaryRulesByParent, selfLoopRule, null, 0.0);
		}

		Map<UnaryRule, List<Integer>> closureMap = new HashMap<UnaryRule, List<Integer>>();

		for (UnaryRule unaryRule : pathCosts.keySet()) {
			unaryRule.setScore(pathCosts.getCount(unaryRule));
			List<Integer> path = extractPath(unaryRule, intermediateStates, new HashSet<Integer>());
			closureMap.put(unaryRule, path);
		}

		return closureMap;

	}

	private static List<Integer> extractPath(UnaryRule unaryRule, Map<UnaryRule, Integer> intermediateStates, Set<Integer> exploredStates) {
		List<Integer> path = new ArrayList<Integer>();
		path.add(unaryRule.getParent());
		Integer intermediateState = intermediateStates.get(unaryRule);
		if (intermediateState != null) {
			if (exploredStates.contains(intermediateState)) { throw new PositiveCycleException("Looks like there is a positive cycle of unaries for rule "
				+ unaryRule); }
			exploredStates.add(intermediateState);
			List<Integer> parentPath = extractPath(new UnaryRule(unaryRule.getParent(), intermediateState.intValue()), intermediateStates, exploredStates);
			for (int i = 1; i < parentPath.size() - 1; i++) {
				Integer state = parentPath.get(i);
				path.add(state);
			}
			path.add(intermediateState);
			List<Integer> childPath = extractPath(new UnaryRule(intermediateState.intValue(), unaryRule.getChild()), intermediateStates, exploredStates);
			for (int i = 1; i < childPath.size() - 1; i++) {
				Integer state = childPath.get(i);
				path.add(state);
			}
		}
		if (path.size() == 1 && unaryRule.getParent() == unaryRule.getChild()) return path;
		path.add(unaryRule.getChild());
		return path;
	}

	private static void relax(Counter<UnaryRule> pathCosts, Map<UnaryRule, Integer> intermediateStates, Map<Integer, List<UnaryRule>> closedUnaryRulesByChild,
	                          Map<Integer, List<UnaryRule>> closedUnaryRulesByParent, UnaryRule unaryRule, Integer intermediateState, double newScore) {
		if (intermediateState != null && (intermediateState.intValue() == unaryRule.getParent() || intermediateState.intValue() == unaryRule.getChild())) return;
		boolean isNewRule = !pathCosts.containsKey(unaryRule);
		double oldScore = (isNewRule ? Double.NEGATIVE_INFINITY : pathCosts.getCount(unaryRule));
		if (oldScore > newScore) return;
		if (isNewRule) {
			CollectionUtils.addToValueList(closedUnaryRulesByChild, unaryRule.getChild(), unaryRule);
			CollectionUtils.addToValueList(closedUnaryRulesByParent, unaryRule.getParent(), unaryRule);
		}
		pathCosts.setCount(unaryRule, newScore);
		intermediateStates.put(unaryRule, intermediateState);
	}

}
