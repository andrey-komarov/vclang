package com.jetbrains.jetpad.vclang.typechecking.nameresolver.listener;

import com.jetbrains.jetpad.vclang.term.Abstract;
import com.jetbrains.jetpad.vclang.term.Concrete;
import com.jetbrains.jetpad.vclang.term.definition.BaseDefinition;
import com.jetbrains.jetpad.vclang.term.definition.Condition;

public class ConcreteResolveListener implements ResolveListener {
  @Override
  public void nameResolved(Abstract.DefCallExpression defCallExpression, BaseDefinition definition) {
    ((Concrete.DefCallExpression) defCallExpression).setResolvedDefinition(definition);
  }

  @Override
  public void moduleResolved(Abstract.ModuleCallExpression moduleCallExpression, BaseDefinition module) {
    ((Concrete.ModuleCallExpression) moduleCallExpression).setModule(module);
  }

  @Override
  public void nsCmdResolved(Abstract.NamespaceCommandStatement nsCmdStatement, BaseDefinition definition) {
    ((Concrete.NamespaceCommandStatement) nsCmdStatement).setResolvedClass(definition);
  }

  @Override
  public Abstract.BinOpExpression makeBinOp(Abstract.BinOpSequenceExpression binOpExpr, Abstract.Expression left, BaseDefinition binOp, Abstract.DefCallExpression var, Abstract.Expression right) {
    return ((Concrete.BinOpSequenceExpression) binOpExpr).makeBinOp(left, binOp, var, right);
  }

  @Override
  public Abstract.Expression makeError(Abstract.BinOpSequenceExpression binOpExpr, Abstract.SourceNode node) {
    return ((Concrete.BinOpSequenceExpression) binOpExpr).makeError(node);
  }

  @Override
  public void replaceBinOp(Abstract.BinOpSequenceExpression binOpExpr, Abstract.Expression expression) {
    ((Concrete.BinOpSequenceExpression) binOpExpr).replace(expression);
  }

  @Override
  public void replaceWithConstructor(Abstract.PatternArgument patternArg) {
    ((Concrete.PatternArgument) patternArg).replaceWithConstructor();
  }

  @Override
  public void replaceWithConstructor(Abstract.PatternContainer container, int index) {
    ((Concrete.PatternContainer) container).replaceWithConstructor(index);
  }
}
