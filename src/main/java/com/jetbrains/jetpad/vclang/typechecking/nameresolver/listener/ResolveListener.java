package com.jetbrains.jetpad.vclang.typechecking.nameresolver.listener;

import com.jetbrains.jetpad.vclang.term.Abstract;
import com.jetbrains.jetpad.vclang.term.definition.BaseDefinition;

public interface ResolveListener {
  void nameResolved(Abstract.DefCallExpression defCallExpression, BaseDefinition definition);
  void moduleResolved(Abstract.ModuleCallExpression moduleCallExpression, BaseDefinition definition);
  void nsCmdResolved(Abstract.NamespaceCommandStatement nsCmdStatement, BaseDefinition definition);

  Abstract.BinOpExpression makeBinOp(Abstract.BinOpSequenceExpression binOpExpr, Abstract.Expression left, BaseDefinition binOp, Abstract.DefCallExpression var, Abstract.Expression right);
  Abstract.Expression makeError(Abstract.BinOpSequenceExpression binOpExpr, Abstract.SourceNode node);
  void replaceBinOp(Abstract.BinOpSequenceExpression binOpExpr, Abstract.Expression expression);
  void replaceWithConstructor(Abstract.PatternArgument patternArg);
  void replaceWithConstructor(Abstract.PatternContainer container, int index);
}
