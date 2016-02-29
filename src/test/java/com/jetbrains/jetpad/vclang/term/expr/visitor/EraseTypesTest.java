package com.jetbrains.jetpad.vclang.term.expr.visitor;

import com.jetbrains.jetpad.vclang.term.context.param.DependentLink;
import com.jetbrains.jetpad.vclang.term.expr.Expression;
import org.junit.Test;

import static com.jetbrains.jetpad.vclang.term.expr.ExpressionFactory.*;
import static org.junit.Assert.*;

public class EraseTypesTest {
  @Test
  public void testId() {
    DependentLink x = param("x", Nat());
    Expression expr = Apps(Lam(x, Reference(x)), Suc(Zero()));
    EraseTypesVisitor visitor = new EraseTypesVisitor();
    EraseTypesVisitor.AST ast = expr.accept(visitor, null);
    System.err.println(ast);
    System.err.println(expr);
    System.err.println(visitor.getReachedData());
    assertEquals(Suc(Zero()), expr.normalize(NormalizeVisitor.Mode.NF));
  }

  @Test
  public void testFreeVars() {
    DependentLink x = param("x", Nat());
    DependentLink y = param("y", Nat());

  }
}
