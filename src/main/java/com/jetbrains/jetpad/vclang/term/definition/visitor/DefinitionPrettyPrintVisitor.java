package com.jetbrains.jetpad.vclang.term.definition.visitor;

import com.jetbrains.jetpad.vclang.term.Abstract;
import com.jetbrains.jetpad.vclang.term.expr.visitor.PrettyPrintVisitor;

import java.util.List;

import static com.jetbrains.jetpad.vclang.term.expr.arg.Utils.removeFromList;

public class DefinitionPrettyPrintVisitor implements AbstractDefinitionVisitor<Void, Void> {
  private final StringBuilder myBuilder;
  private final List<String> myNames;
  private int myIndent;

  public DefinitionPrettyPrintVisitor(StringBuilder builder, List<String> names, int indent) {
    myBuilder = builder;
    myNames = names;
    myIndent = indent;
  }

  @Override
  public Void visitFunction(Abstract.FunctionDefinition def, Void ignored) {
    myBuilder.append("\\function");
    if (def.getPrecedence() != null && !def.getPrecedence().equals(Abstract.Definition.DEFAULT_PRECEDENCE)) {
      myBuilder.append(" \\infix");
      if (def.getPrecedence().associativity == Abstract.Definition.Associativity.LEFT_ASSOC) myBuilder.append('l');
      if (def.getPrecedence().associativity == Abstract.Definition.Associativity.RIGHT_ASSOC) myBuilder.append('r');
      myBuilder.append(' ');
      myBuilder.append(def.getPrecedence().priority);
    }
    myBuilder.append('\n');
    PrettyPrintVisitor.printIndent(myBuilder, myIndent);

    myBuilder.append(def.getName());
    if (def.getArguments() != null) {
      for (Abstract.Argument argument : def.getArguments()) {
        myBuilder.append(' ');
        argument.prettyPrint(myBuilder, myNames, Abstract.VarExpression.PREC);
      }
    } else {
      myBuilder.append("{!error}");
    }

    if (def.getResultType() != null) {
      myBuilder.append(" : ");
      def.getResultType().accept(new PrettyPrintVisitor(myBuilder, myNames, myIndent), Abstract.Expression.PREC);
    }
    if (!def.isAbstract()) {
      myBuilder.append(def.getArrow() == Abstract.Definition.Arrow.RIGHT ? " => " : " <= ");
      if (def.getTerm() != null) {
        def.getTerm().accept(new PrettyPrintVisitor(myBuilder, myNames, myIndent), Abstract.Expression.PREC);
      } else {
        myBuilder.append("{!error}");
      }
    }

    if (def.getArguments() != null) {
      removeFromList(myNames, def.getArguments());
    }

    if (!def.getFields().isEmpty()) {
      myBuilder.append("\n");
      PrettyPrintVisitor.printIndent(myBuilder, myIndent);
      myBuilder.append("\\where ");
      myIndent += "\\where ".length();
      boolean isFirst = true;
      for (Abstract.Definition nestedDef : def.getFields()) {
        if (!isFirst)
          PrettyPrintVisitor.printIndent(myBuilder, myIndent);
        nestedDef.accept(this, null);
        myBuilder.append("\n");
        isFirst = false;
      }
      myIndent -= "\\where ".length();
    }
    return null;
  }

  @Override
  public Void visitData(Abstract.DataDefinition def, Void ignored) {
    myBuilder.append("\\data ");
    myBuilder.append(def.getName());

    if (def.getParameters() != null) {
      for (Abstract.TypeArgument parameter : def.getParameters()) {
        myBuilder.append(' ');
        parameter.prettyPrint(myBuilder, myNames, Abstract.VarExpression.PREC);
      }
    } else {
      myBuilder.append("{!error}");
    }

    if (def.getUniverse() != null) {
      myBuilder.append(" : ").append(def.getUniverse());
    }
    ++myIndent;
    for (Abstract.Constructor constructor : def.getConstructors()) {
      myBuilder.append('\n');
      PrettyPrintVisitor.printIndent(myBuilder, myIndent);
      myBuilder.append("| ");
      constructor.accept(this, null);
    }
    --myIndent;
    if (def.getParameters() != null) {
      removeFromList(myNames, def.getParameters());
    }
    return null;
  }

  @Override
  public Void visitConstructor(Abstract.Constructor def, Void ignored) {
    myBuilder.append(def.getName());
    if (def.getArguments() == null) {
      myBuilder.append("{!error}");
    } else {
      for (Abstract.TypeArgument argument : def.getArguments()) {
        myBuilder.append(' ');
        argument.prettyPrint(myBuilder, myNames, Abstract.VarExpression.PREC);
      }
      removeFromList(myNames, def.getArguments());
    }
    return null;
  }

  @Override
  public Void visitClass(Abstract.ClassDefinition def, Void ignored) {
    myBuilder.append("\\class ").append(def.getName()).append(" {");
    if (def.getFields() != null) {
      ++myIndent;
      for (Abstract.Definition field : def.getFields()) {
        myBuilder.append('\n');
        PrettyPrintVisitor.printIndent(myBuilder, myIndent);
        field.accept(this, null);
        myBuilder.append('\n');
      }
      --myIndent;
    }
    PrettyPrintVisitor.printIndent(myBuilder, myIndent);
    myBuilder.append("}");
    return null;
  }
}
