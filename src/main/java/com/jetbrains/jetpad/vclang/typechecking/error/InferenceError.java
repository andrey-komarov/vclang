package com.jetbrains.jetpad.vclang.typechecking.error;

import com.jetbrains.jetpad.vclang.term.Abstract;
import com.jetbrains.jetpad.vclang.term.context.binding.Binding;
import com.jetbrains.jetpad.vclang.term.context.binding.InferenceBinding;
import com.jetbrains.jetpad.vclang.term.expr.Expression;

import java.util.ArrayList;
import java.util.List;

public class InferenceError extends TypeCheckingError {
  private final List<Binding> myVars;

  public InferenceError(Abstract.SourceNode expression, List<Binding> context, int from) {
    super("Cannot infer expressions", expression);
    myVars = context.subList(0, from);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(printHeader()).append(getMessage()).append(":\n");
    for (Binding var : myVars) {
      builder.append('\t');
      if (var instanceof InferenceBinding) {
        builder.append('?');
      }
      builder.append(var.getName() == null ? "_" : var.getName()).append(" : ");
      Expression type = var.getType();
      if (type != null) {
        type.prettyPrint(builder, new ArrayList<String>(), Abstract.Expression.PREC);
      } else {
        builder.append("{!error}");
      }
    }
    return builder.toString();
  }
}
