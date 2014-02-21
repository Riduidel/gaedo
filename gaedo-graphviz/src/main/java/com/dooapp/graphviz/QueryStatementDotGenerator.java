package com.dooapp.graphviz;

import java.util.Stack;
import java.util.Map.Entry;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.QueryExpressionContainer;
import com.dooapp.gaedo.finders.QueryExpressionContainerVisitor;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.SortingExpression.Direction;
import com.dooapp.gaedo.finders.expressions.AndQueryExpression;
import com.dooapp.gaedo.finders.expressions.AnythingExpression;
import com.dooapp.gaedo.finders.expressions.CollectionContaingExpression;
import com.dooapp.gaedo.finders.expressions.ContainsStringExpression;
import com.dooapp.gaedo.finders.expressions.EndsWithExpression;
import com.dooapp.gaedo.finders.expressions.EqualsExpression;
import com.dooapp.gaedo.finders.expressions.EqualsToIgnoreCaseExpression;
import com.dooapp.gaedo.finders.expressions.GreaterThanExpression;
import com.dooapp.gaedo.finders.expressions.LowerThanExpression;
import com.dooapp.gaedo.finders.expressions.MapContainingKeyExpression;
import com.dooapp.gaedo.finders.expressions.MatchesRegexpExpression;
import com.dooapp.gaedo.finders.expressions.NotQueryExpression;
import com.dooapp.gaedo.finders.expressions.OrQueryExpression;
import com.dooapp.gaedo.finders.expressions.StartsWithExpression;
import com.dooapp.gaedo.finders.informers.MapContainingValueExpression;

/**
 * Generates, from a query statement, a dot file
 * @author Nicolas
 *
 */
public class QueryStatementDotGenerator implements QueryExpressionContainerVisitor {
	private static final String DOT_ARROW = " -> ";

	private static final String QUERY_STATEMENT = "queryStatement";

	private static final String QUERY_EXPRESSION = "QueryExpression";

	private static final String SORTING_EXPRESSION = "SortingExpression";

	private static final String END_SUFFIX = "_end";

	/**
	 * Built String
	 */
	private StringBuilder sOut = new StringBuilder();

	/**
	 * Stack of ndoes, to allow and operations
	 */
	private Stack<String> nodes = new Stack<String>();

	/**
	 * Global index of node (to ensure no name gets duplicated)
	 */
	private int nodeIndex;

	/**
	 * Contains a graph title
	 */
	private String title;

	public QueryStatementDotGenerator(String id) {
		this.title = id;
		if(title!=null) {
			sOut.append("\tlabel=\"").append(id).append("\";\n");
		}
	}

	@Override
	public void endVisit(QueryExpressionContainer queryStatement) {
		createNodes(nodes.pop(), "query statement", "query statement END");
		setAttribute(QUERY_STATEMENT, "shape", "box3d");
		setAttribute(QUERY_STATEMENT+END_SUFFIX, "shape", "box3d");
	}

	/**
	 * Create nodes declaration associated to given id
	 * @param id
	 */
	private void createNodes(String id, String startName, String endName) {
		// Declare input node
		setLabel(id, startName);
		// Declare output node
		setLabel(id+END_SUFFIX, endName);
	}

	/**
	 * Sets node label
	 * @param nodeId
	 * @param label
	 */
	private void setLabel(String nodeId, String label) {
		setAttribute(nodeId, "label", label);
	}

	private void setAttribute(String nodeId, String attributeName, String attributeValue) {
		sOut.append("\t").append(nodeId).append("["+attributeName+"=\""+attributeValue+"\"];").append("\n");
	}

	private void setShape(String nodeId, String shape) {
		setAttribute(nodeId, "shape", shape);
	}

	@Override
	public void startVisit(QueryExpressionContainer queryStatement) {
		addNode(QUERY_STATEMENT, null, null);
	}

	/**
	 * Adds a node id to the stack, and branch both its start and end view to parent nodes.
	 * @param nodeName legible part of node id
	 */
	private void addNode(String nodeName) {
		addNode(nodeName, nodes.peek(), nodes.peek()+END_SUFFIX);
	}

	/**
	 * Adds a node id to the stack, and branch both its start and end view to given parent nodes.
	 *
	 * @param nodeName node name to add
	 * @param startNode start node, may be null
	 * @param endNode end node may be null
	 */
	private void addNode(String nodeName, String startNode, String endNode) {
		String id = nodeName;
		if(startNode!=null)
			sOut.append("\t").append(startNode).append(DOT_ARROW).append(id).append(";\n");
		if(endNode!=null)
			sOut.append("\t").append(id+END_SUFFIX).append(DOT_ARROW).append(endNode).append(";\n");
		// Push node id
		nodes.push(id);
	}

	@Override
	public void endVisit(OrQueryExpression orQueryExpression) {
		createNodes(nodes.pop(), "OR", "");
	}

	public void addQueryExpression() {
		// Stack only contains query statement, we branch on it, then on SortingExpression
		if(nodes.size()==1) {
			addNode(QUERY_EXPRESSION,QUERY_STATEMENT,SORTING_EXPRESSION);
		} else {
			addNode(QUERY_EXPRESSION+"_"+(nodeIndex++));
		}
		setQueryExpressionAttributes(nodes.peek(), false);
		setQueryExpressionAttributes(nodes.peek()+END_SUFFIX, false);
		setShape(nodes.peek(), "box");
		setShape(nodes.peek()+END_SUFFIX, "box");
	}

	/**
	 * Set common attributes for query expression
	 * @param nodeId
	 * @param terminal
	 */
	private void setQueryExpressionAttributes(String nodeId, boolean terminal) {
		setShape(nodeId, "box");
		setAttribute(nodeId, "style", "filled");
		setAttribute(nodeId, "fontname", "Courier");
		setAttribute(nodeId, "color", terminal ? "#88FF88" : "#00FF00");
	}

	/**
	 * Set common attributes for sorting expression
	 * @param nodeId
	 * @param terminal
	 */
	private void setSortingExpressionAttributes(String nodeId, boolean terminal) {
		setShape(nodeId, "box");
		setAttribute(nodeId, "style", "filled");
		setAttribute(nodeId, "color", terminal ? "#8888AA" : "#8888FF");
	}

	@Override
	public void startVisit(OrQueryExpression orQueryExpression) {
		addQueryExpression();
	}

	@Override
	public void startVisit(AndQueryExpression andQueryExpression) {
		addQueryExpression();
	}

	@Override
	public void endVisit(AndQueryExpression andQueryExpression) {
		createNodes(nodes.pop(), "AND", "");
	}

	@Override
	public void startVisit(NotQueryExpression notQueryExpression) {
		addQueryExpression();
	}

	@Override
	public void endVisit(NotQueryExpression notQueryExpression) {
		createNodes(nodes.pop(), "NOT", "");
	}


	@Override
	public void visit(AnythingExpression expression) {
		String nodeId = putNode("AnythingQuery", QUERY_EXPRESSION);
		setLabel(nodeId, expression.getField().getName()+" * ");
		setQueryExpressionAttributes(nodeId, true);
	}

	@Override
	public void visit(EqualsExpression expression) {
		String nodeId = putNode("EqualsQuery", QUERY_EXPRESSION);
		setLabel(nodeId, expression.getField().getName()+" ==? "+expression.getValue().toString());
		setQueryExpressionAttributes(nodeId, true);
	}

	/**
	 * Put a node atop of node stack without adding it to stack. it is useful for terminal expression nodes
	 * @param nodeName
	 * @param expectedParent TODO
	 * @return the node id
	 */
	private String putNode(String nodeName, String expectedParent) {
		String id = nodeName+"_"+(nodeIndex++);
		if(!nodes.peek().startsWith(expectedParent)) {
			addQueryExpression();
		}
		sOut.append("\t").append(nodes.peek()).append(DOT_ARROW).append(id).append(";\n");
		sOut.append("\t").append(id).append(DOT_ARROW).append(nodes.peek()+END_SUFFIX).append(";\n");
		return id;
	}

	@Override
	public <ComparableType extends Comparable<ComparableType>> void visit(GreaterThanExpression<ComparableType> expression) {
		String nodeId = putNode("GreatherThanQuery", QUERY_EXPRESSION);
		setLabel(nodeId, expression.getField().getName()+(expression.isStrictly() ? " >? " : "=>?")+expression.getValue().toString());
	}

	@Override
	public <ComparableType extends Comparable<ComparableType>> void visit(LowerThanExpression<ComparableType> expression) {
		putNode("LowerThanQuery", QUERY_EXPRESSION);
		String nodeId = putNode("LowerThanQuery", QUERY_EXPRESSION);
		setLabel(nodeId, expression.getField().getName()+(expression.isStrictly() ? " <? " : "=<?")+expression.getValue().toString());
	}

	@Override
	public void visit(EqualsToIgnoreCaseExpression expression) {
		String nodeId = putNode("EqualsToIgnoreCaseQuery", QUERY_EXPRESSION);
		setLabel(nodeId, expression.getField().getName()+" ==(a==A)? "+expression.getCompared());
	}

	@Override
	public void visit(MatchesRegexpExpression expression) {
		String nodeId = putNode("MatchesRegexpQuery", QUERY_EXPRESSION);
		setLabel(nodeId, expression.getField().getName()+" matches? "+expression.getPattern().toString());
	}

	@Override
	public void visit(ContainsStringExpression expression) {
		String nodeId = putNode("ContainsStringQuery", QUERY_EXPRESSION);
		setLabel(nodeId, expression.getField().getName()+" contains? "+expression.getContained());
	}

	@Override
	public void visit(StartsWithExpression expression) {
		String nodeId = putNode("StartsWithQuery", QUERY_EXPRESSION);
		setLabel(nodeId, expression.getField().getName()+" startsWith? "+expression.getStart());
	}

	@Override
	public void visit(EndsWithExpression expression) {
		String nodeId = putNode("EndsWithQuery", QUERY_EXPRESSION);
		setLabel(nodeId, expression.getField().getName()+" contains? "+expression.getEnd());
	}

	@Override
	public void startVisit(SortingExpression sortingExpression) {
		addNode(SORTING_EXPRESSION, null, QUERY_STATEMENT+END_SUFFIX);
		setSortingExpressionAttributes(SORTING_EXPRESSION, false);
		setSortingExpressionAttributes(SORTING_EXPRESSION+END_SUFFIX, false);
		if(!sortingExpression.iterator().hasNext()) {
			sOut.append("\t").append(SORTING_EXPRESSION).append(DOT_ARROW).append(SORTING_EXPRESSION+END_SUFFIX).append(";\n");
		}
	}

	@Override
	public void endVisit(SortingExpression sortingExpression) {
		createNodes(nodes.pop(), "Sorting Expression", "Sorting Expression end");
	}

	@Override
	public void visit(Entry<FieldInformer, Direction> entry) {
		String nodeId = putNode("SortingEntry", QUERY_EXPRESSION);
		setLabel(nodeId, entry.getKey().getField().getName()+" "+entry.getValue().getText());
		setSortingExpressionAttributes(nodeId, true);
	}

	public String getText() {
		return "digraph query {\n"+sOut.toString()+"}\n";
	}

	@Override
	public void visit(CollectionContaingExpression expression) {
		String nodeId = putNode("CollectionContaingQuery", QUERY_EXPRESSION);
		setLabel(nodeId, expression.getField().getName()+" contains? "+expression.getContained());
	}

	@Override
	public void visit(MapContainingValueExpression expression) {
		String nodeId = putNode("MapContaingQuery", QUERY_EXPRESSION);
		setLabel(nodeId, expression.getField().getName()+" containsKey? "+expression.getContained());
	}

	@Override
	public void visit(MapContainingKeyExpression expression) {
		String nodeId = putNode("MapContaingQuery", QUERY_EXPRESSION);
		setLabel(nodeId, expression.getField().getName()+" containsValue? "+expression.getContained());
	}
}
