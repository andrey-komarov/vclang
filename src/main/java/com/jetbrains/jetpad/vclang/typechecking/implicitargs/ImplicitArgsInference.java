package com.jetbrains.jetpad.vclang.typechecking.implicitargs;

import com.jetbrains.jetpad.vclang.term.Abstract;
import com.jetbrains.jetpad.vclang.term.expr.Expression;
import com.jetbrains.jetpad.vclang.term.expr.visitor.CheckTypeVisitor;
import com.jetbrains.jetpad.vclang.typechecking.implicitargs.equations.Equations;

public interface ImplicitArgsInference {
  Equations newEquations();
  CheckTypeVisitor.Result infer(Abstract.AppExpression expr, Expression expectedType);
  CheckTypeVisitor.Result infer(Abstract.BinOpExpression expr, Expression expectedType);
  CheckTypeVisitor.Result inferTail(CheckTypeVisitor.Result fun, Expression expectedType, Abstract.Expression expr);
}
