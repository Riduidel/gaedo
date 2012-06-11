package com.dooapp.gaedo.finders;

import com.dooapp.gaedo.finders.expressions.QueryExpressionVisitor;
import com.dooapp.gaedo.patterns.Visitable;

public interface QueryExpression extends Visitable<QueryExpressionVisitor> {
}
