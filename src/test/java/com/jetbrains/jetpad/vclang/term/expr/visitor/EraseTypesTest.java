package com.jetbrains.jetpad.vclang.term.expr.visitor;

import com.jetbrains.jetpad.vclang.normalization.Untyped;
import com.jetbrains.jetpad.vclang.term.Abstract;
import com.jetbrains.jetpad.vclang.term.context.param.DependentLink;
import com.jetbrains.jetpad.vclang.term.definition.ClassDefinition;
import com.jetbrains.jetpad.vclang.term.definition.Definition;
import com.jetbrains.jetpad.vclang.term.definition.FunctionDefinition;
import com.jetbrains.jetpad.vclang.term.expr.Expression;
import com.jetbrains.jetpad.vclang.term.pattern.elimtree.ElimTreeNode;
import com.jetbrains.jetpad.vclang.term.pattern.elimtree.LeafElimTreeNode;
import org.junit.Test;

import static com.jetbrains.jetpad.vclang.term.expr.ExpressionFactory.*;
import static com.jetbrains.jetpad.vclang.typechecking.TypeCheckingTestCase.typeCheckDef;
import static com.jetbrains.jetpad.vclang.typechecking.TypeCheckingTestCase.typeCheckClass;
import static org.junit.Assert.*;

public class EraseTypesTest {
  @Test
  public void testId() {
    DependentLink x = param("x", Nat());
    Expression expr = Apps(Lam(x, Reference(x)), Suc(Zero()));
    EraseTypesVisitor visitor = new EraseTypesVisitor();
    Untyped ast = expr.accept(visitor, null);
    System.err.println(ast);
    System.err.println(expr);
    assertEquals(Suc(Zero()), expr.normalize(NormalizeVisitor.Mode.NF));
  }

  @Test
  public void testFreeVars() {
    DependentLink x = param("x", Nat());
    DependentLink y = param("y", Nat());

  }

  @Test
  public void testElim() {
    FunctionDefinition def = (FunctionDefinition) typeCheckDef("\\function lol (n : Nat) : Nat <= \\elim n | zero => 1 | suc n' => 0");
    ElimTreeNode leaf = def.getElimTree();
    System.err.println(leaf);
    System.err.println(FunCall(def).normalize(NormalizeVisitor.Mode.NF));
    Expression expr = FunCall(def);
    EraseTypesVisitor visitor = new EraseTypesVisitor();
    Untyped untyped = expr.accept(visitor, null);
    System.err.println(untyped);
    System.err.println(visitor.getFunctions());
  }
}
