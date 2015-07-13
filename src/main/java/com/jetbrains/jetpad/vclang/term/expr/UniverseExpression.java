package com.jetbrains.jetpad.vclang.term.expr;

import com.jetbrains.jetpad.vclang.term.Abstract;
import com.jetbrains.jetpad.vclang.term.definition.Universe;
import com.jetbrains.jetpad.vclang.term.expr.visitor.AbstractExpressionVisitor;
import com.jetbrains.jetpad.vclang.term.expr.visitor.ExpressionVisitor;

import java.util.List;

public class UniverseExpression extends Expression implements Abstract.UniverseExpression {
  private final Universe myUniverse;

  public UniverseExpression(Universe universe) {
    myUniverse = universe;
  }

  @Override
  public Universe getUniverse() {
    return myUniverse;
  }

  @Override
  public <T> T accept(ExpressionVisitor<? extends T> visitor) {
    return visitor.visitUniverse(this);
  }

  @Override
  public UniverseExpression getType(List<Expression> context) {
    return new UniverseExpression(myUniverse.succ());
  }

  @Override
  public <P, R> R accept(AbstractExpressionVisitor<? super P, ? extends R> visitor, P params) {
    return visitor.visitUniverse(this, params);
  }
}
