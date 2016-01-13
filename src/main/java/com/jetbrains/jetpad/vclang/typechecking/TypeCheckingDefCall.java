package com.jetbrains.jetpad.vclang.typechecking;

import com.jetbrains.jetpad.vclang.term.Abstract;
import com.jetbrains.jetpad.vclang.term.definition.*;
import com.jetbrains.jetpad.vclang.term.expr.*;
import com.jetbrains.jetpad.vclang.term.expr.arg.TypeArgument;
import com.jetbrains.jetpad.vclang.term.expr.visitor.CheckTypeVisitor;
import com.jetbrains.jetpad.vclang.term.expr.visitor.NormalizeVisitor;
import com.jetbrains.jetpad.vclang.term.pattern.Utils;
import com.jetbrains.jetpad.vclang.typechecking.error.HasErrors;
import com.jetbrains.jetpad.vclang.typechecking.error.NameDefinedError;
import com.jetbrains.jetpad.vclang.typechecking.error.NotInScopeError;
import com.jetbrains.jetpad.vclang.typechecking.error.TypeCheckingError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import static com.jetbrains.jetpad.vclang.term.expr.ExpressionFactory.*;
import static com.jetbrains.jetpad.vclang.term.expr.ExpressionFactory.Error;
import static com.jetbrains.jetpad.vclang.term.pattern.Utils.expandConstructorParameters;
import static com.jetbrains.jetpad.vclang.term.pattern.Utils.patternMatchAll;
import static com.jetbrains.jetpad.vclang.typechecking.error.TypeCheckingError.getNames;

public class TypeCheckingDefCall {
  private final CheckTypeVisitor myVisitor;
  private ClassDefinition myThisClass;

  public TypeCheckingDefCall(CheckTypeVisitor visitor) {
    myVisitor = visitor;
  }

  public void setThisClass(ClassDefinition thisClass) {
    myThisClass = thisClass;
  }

  public CheckTypeVisitor.Result typeCheckDefCall(Abstract.DefCallExpression expr) {
    if (expr instanceof ConCallExpression) {
      Constructor constructor = ((ConCallExpression) expr).getDefinition();
      CheckTypeVisitor.OKResult result = new CheckTypeVisitor.OKResult(ConCall(constructor), constructor.getBaseType(), null);
      fixConstructorParameters(constructor, result, false);
      if (constructor.getThisClass() != null) {
        result.type = Pi("\\this", ClassCall(constructor.getThisClass()), result.type);
      }
      return result;
    }
    if (expr instanceof DefCallExpression) {
      Definition definition = ((DefCallExpression) expr).getDefinition();
      return new CheckTypeVisitor.OKResult(definition.getDefCall(myVisitor.getDefinition()), definition.getType(), null);
    }

    DefCallResult result = typeCheckNamespace(expr, null);
    if (result == null) {
      return null;
    }
    if (result.baseResult == null) {
      TypeCheckingError error = new TypeCheckingError("'" + result.member.namespace.getFullName() + "' is not available here or not a definition", expr, getNames(myVisitor.getLocalContext()));
      expr.setWellTyped(myVisitor.getLocalContext(), Error(null, error));
      myVisitor.getErrorReporter().report(error);
      return null;
    }

    return result.baseResult;
  }

  private CheckTypeVisitor.OKResult checkDefinition(Definition definition, Abstract.Expression expr) {
    if (definition instanceof FunctionDefinition && ((FunctionDefinition) definition).typeHasErrors() || !(definition instanceof FunctionDefinition) && definition.hasErrors()) {
      TypeCheckingError error = new HasErrors(definition.getName(), expr);
      expr.setWellTyped(myVisitor.getLocalContext(), Error(definition.getDefCall(myVisitor.getDefinition()), error));
      myVisitor.getErrorReporter().report(error);
      return null;
    } else {
      return new CheckTypeVisitor.OKResult(definition.getDefCall(myVisitor.getDefinition()), definition.getBaseType(), null);
    }
  }

  private void fixConstructorParameters(Constructor constructor, CheckTypeVisitor.OKResult result, boolean doSubst) {
    List<TypeArgument> parameters;
    if (constructor.getPatterns() != null) {
      parameters = expandConstructorParameters(constructor, myVisitor.getLocalContext());
    } else {
      parameters = new ArrayList<>(constructor.getDataType().getParameters().size());
      for (TypeArgument argument : constructor.getDataType().getParameters()) {
        parameters.add(argument.toExplicit(false));
      }
    }

    if (!parameters.isEmpty()) {
      result.type = Pi(parameters, result.type);
    }
    if (doSubst && result.expression instanceof ConCallExpression) {
      List<Expression> args = ((ConCallExpression) result.expression).getParameters();
      if (!args.isEmpty()) {
        int argsNumber = args.size() - (constructor.getDataType().getThisClass() != null ? 1 : 0);
        Collections.reverse(args);
        result.type = result.type.splitAt(argsNumber, null, null).subst(args, 0);
        Collections.reverse(args);
      }
    }
  }

  private Expression findParent(ClassDefinition classDefinition, ClassDefinition parent, Expression expr) {
    if (classDefinition == parent) {
      return expr;
    }
    ClassField parentField = classDefinition.getParentField();
    if (parentField == null || !(parentField.getBaseType() instanceof ClassCallExpression)) {
      return null;
    }
    return findParent(((ClassCallExpression) parentField.getBaseType()).getDefinition(), parent, Apps(FieldCall(parentField), expr));
  }

  public CheckTypeVisitor.OKResult getLocalVar(Name name, Abstract.Expression expr) {
    ListIterator<Binding> it = myVisitor.getLocalContext().listIterator(myVisitor.getLocalContext().size());
    int index = 0;
    while (it.hasPrevious()) {
      Binding def = it.previous();
      if (name.name.equals(def.getName() == null ? null : def.getName().name)) {
        return new CheckTypeVisitor.OKResult(Index(index), def.getType(), null);
      }
      ++index;
    }

    TypeCheckingError error = new NotInScopeError(expr, name);
    expr.setWellTyped(myVisitor.getLocalContext(), Error(null, error));
    myVisitor.getErrorReporter().report(error);
    return null;
  }

  private static class DefCallResult {
    CheckTypeVisitor.Result baseResult;
    ClassDefinition baseClassDefinition;
    NamespaceMember member;

    public DefCallResult(CheckTypeVisitor.Result baseResult, ClassDefinition baseClassDefinition, NamespaceMember member) {
      this.baseResult = baseResult;
      this.baseClassDefinition = baseClassDefinition;
      this.member = member;
    }
  }

  private DefCallResult typeCheckName(Abstract.DefCallExpression expr, String next) {
    Name name = expr.getName();
    ResolvedName resolvedName = expr.getResolvedName();
    if (resolvedName == null) {
      CheckTypeVisitor.OKResult result = getLocalVar(name, expr);
      if (result == null) {
        return null;
      }
      result.type = result.type.liftIndex(0, ((IndexExpression) result.expression).getIndex() + 1);
      return new DefCallResult(result, null, null);
    }

    NamespaceMember member = resolvedName.toNamespaceMember();
    if (member == null) {
      assert false;
      TypeCheckingError error = new TypeCheckingError("Internal error: definition '" + name + "' is not available yet", expr, getNames(myVisitor.getLocalContext()));
      expr.setWellTyped(myVisitor.getLocalContext(), Error(null, error));
      myVisitor.getErrorReporter().report(error);
      return null;
    }

    return getDefCallResult(member, expr, next);
  }

  private DefCallResult getDefCallResult(NamespaceMember member, Abstract.Expression expr, String next) {
    Definition definition = member.definition;
    if (definition == null || next != null && (definition instanceof ClassDefinition || definition instanceof FunctionDefinition && member.namespace.getMember(next) != null)) {
      return new DefCallResult(null, null, member);
    }

    if (definition.getThisClass() != null) {
      if (myThisClass != null) {
        assert myVisitor.getLocalContext().size() > 0;
        assert myVisitor.getLocalContext().get(0).getName().name.equals("\\this");
        Expression thisExpr = Index(myVisitor.getLocalContext().size() - 1);
        thisExpr = findParent(myThisClass, definition.getThisClass(), thisExpr);
        if (thisExpr == null) {
          TypeCheckingError error = new TypeCheckingError("Definition '" + definition.getName() + "' is not available in this context", expr, getNames(myVisitor.getLocalContext()));
          expr.setWellTyped(myVisitor.getLocalContext(), Error(null, error));
          myVisitor.getErrorReporter().report(error);
          return null;
        }

        CheckTypeVisitor.OKResult result = checkDefinition(definition, expr);
        if (result == null) {
          return null;
        }
        result.expression = ((DefCallExpression) result.expression).applyThis(thisExpr);
        if (definition instanceof Constructor) {
          fixConstructorParameters((Constructor) definition, result, false);
        }
        result.type = result.type.subst(thisExpr, 0);
        return new DefCallResult(result, null, null);
      } else {
        TypeCheckingError error = new TypeCheckingError("Non-static definitions are not allowed in static context", expr, getNames(myVisitor.getLocalContext()));
        expr.setWellTyped(myVisitor.getLocalContext(), Error(null, error));
        myVisitor.getErrorReporter().report(error);
        return null;
      }
    } else {
      CheckTypeVisitor.OKResult result = checkDefinition(definition, expr);
      if (result == null) {
        return null;
      }
      if (definition instanceof Constructor) {
        fixConstructorParameters((Constructor) definition, result, false);
      }
      return new DefCallResult(result, null, member);
    }
  }

  private DefCallResult nextResult(DefCallResult result, Abstract.DefCallExpression expr, String next) {
    NamespaceMember member;
    ResolvedName resolvedName = expr.getResolvedName();
    if (resolvedName != null) {
      member = result.member.namespace.getMember(resolvedName.name.name);
      if (member == null || resolvedName.parent != member.namespace.getParent()) {
        TypeCheckingError error = new NameDefinedError(false, expr, resolvedName.name, resolvedName.parent.getResolvedName());
        expr.setWellTyped(myVisitor.getLocalContext(), Error(result.baseResult == null ? null : result.baseResult.expression, error));
        myVisitor.getErrorReporter().report(error);
        return null;
      }
    } else {
      Name name = expr.getName();
      member = result.member.namespace.getMember(name.name);
      if (member == null) {
        TypeCheckingError error = new NameDefinedError(false, expr, name, new ResolvedName(result.member.namespace.getParent(), result.member.namespace.getName()));
        expr.setWellTyped(myVisitor.getLocalContext(), Error(result.baseResult == null ? null : result.baseResult.expression, error));
        myVisitor.getErrorReporter().report(error);
        return null;
      }
    }

    if (!(member.definition == null || next != null && (member.definition instanceof ClassDefinition || member.definition instanceof FunctionDefinition && member.namespace.getMember(next) != null))) {
      if (result.baseResult instanceof CheckTypeVisitor.OKResult) {
        if (result.baseClassDefinition != null && result.baseClassDefinition == member.definition.getThisClass()) {
          CheckTypeVisitor.OKResult okResult = checkDefinition(member.definition, expr);
          if (okResult == null) {
            return null;
          }
          okResult.expression = ((DefCallExpression) okResult.expression).applyThis(result.baseResult.expression);
          okResult.equations = result.baseResult.equations;
          if (member.definition instanceof Constructor) {
            fixConstructorParameters((Constructor) member.definition, okResult, false);
          }
          okResult.type = okResult.type.subst(result.baseResult.expression, 0);
          result.baseResult = okResult;
          result.baseClassDefinition = null;
          result.member = null;
          return result;
        } else {
          TypeCheckingError error = new TypeCheckingError("Definition '" + member.definition.getName() + "' cannot be called from here", expr, getNames(myVisitor.getLocalContext()));
          expr.setWellTyped(myVisitor.getLocalContext(), Error(result.baseResult.expression, error));
          myVisitor.getErrorReporter().report(error);
          return null;
        }
      } else {
        return getDefCallResult(member, expr, next);
      }
    }
    if (member.definition == null && next == null) {
      result.baseResult = null;
    }

    result.member = member;
    return result;
  }

  private DefCallResult typeCheckNamespace(Abstract.DefCallExpression expr, String next) {
    Name name = expr.getName();
    Abstract.Expression left = expr.getExpression();
    if (left == null) {
      return typeCheckName(expr, next);
    }

    DefCallResult result;
    if (left instanceof Abstract.DefCallExpression) {
      result = typeCheckNamespace((Abstract.DefCallExpression) left, name.name);
      if (result == null || result.baseResult != null && !(result.baseResult instanceof CheckTypeVisitor.OKResult)) {
        return result;
      }
    } else {
      CheckTypeVisitor.Result result1 = left.accept(myVisitor, null);
      if (result1 == null) {
        return null;
      }
      result = new DefCallResult(result1, null, null);
    }

    if (result.baseResult != null) {
      if (!(result.baseResult instanceof CheckTypeVisitor.OKResult)) {
        return result;
      }

      if (result.member == null) {
        CheckTypeVisitor.OKResult okResult = (CheckTypeVisitor.OKResult) result.baseResult;
        Expression type = okResult.type.normalize(NormalizeVisitor.Mode.WHNF, myVisitor.getLocalContext());

        if (type instanceof ClassCallExpression) {
          result.baseClassDefinition = ((ClassCallExpression) type).getDefinition();
          result.member = result.baseClassDefinition.getParentNamespace().getMember(result.baseClassDefinition.getName().name);
        } else {
          if (type instanceof UniverseExpression || type instanceof PiExpression) {
            List<Expression> arguments = new ArrayList<>();
            Expression function = okResult.expression.normalize(NormalizeVisitor.Mode.WHNF, myVisitor.getLocalContext()).getFunction(arguments);
            if (function instanceof DataCallExpression) {
              CheckTypeVisitor.OKResult conResult = typeCheckConstructor(((DataCallExpression) function).getDefinition(), arguments, name, expr);
              if (conResult == null) {
                return null;
              }
              conResult.equations = okResult.equations;
              return new DefCallResult(conResult, null, null);
            }
          }

          TypeCheckingError error = new TypeCheckingError("Expected an expression of a class type or a data type", expr, getNames(myVisitor.getLocalContext()));
          expr.setWellTyped(myVisitor.getLocalContext(), Error(okResult.expression, error));
          myVisitor.getErrorReporter().report(error);
          return null;
        }
      }
    }

    return nextResult(result, expr, next);
  }

  private CheckTypeVisitor.OKResult typeCheckConstructor(DataDefinition dataDefinition, List<Expression> arguments, Name conName, Abstract.Expression expr) {
    Collections.reverse(arguments);
    Constructor constructor = dataDefinition.getConstructor(conName.name);
    if (constructor == null) {
      TypeCheckingError error = new TypeCheckingError("Constructor '" + conName + "' is not defined in data type '" + dataDefinition.getName() + "'", expr, getNames(myVisitor.getLocalContext()));
      expr.setWellTyped(myVisitor.getLocalContext(), Error(null, error));
      myVisitor.getErrorReporter().report(error);
      return null;
    }

    if (constructor.getPatterns() != null) {
      Utils.PatternMatchResult matchResult = patternMatchAll(constructor.getPatterns(), arguments, myVisitor.getLocalContext());
      TypeCheckingError error = null;
      if (matchResult instanceof Utils.PatternMatchMaybeResult) {
        error = new TypeCheckingError("Constructor is not appropriate, failed to match data type parameters. " +
            "Expected " + ((Utils.PatternMatchMaybeResult) matchResult).maybePattern + ", got " + ((Utils.PatternMatchMaybeResult) matchResult).actualExpression.prettyPrint(getNames(myVisitor.getLocalContext())), expr, getNames(myVisitor.getLocalContext()));
      } else if (matchResult instanceof Utils.PatternMatchFailedResult) {
        error = new TypeCheckingError("Constructor is not appropriate, failed to match data type parameters. " +
            "Expected " + ((Utils.PatternMatchFailedResult) matchResult).failedPattern + ", got " + ((Utils.PatternMatchFailedResult) matchResult).actualExpression.prettyPrint(getNames(myVisitor.getLocalContext())), expr, getNames(myVisitor.getLocalContext()));
      } else if (matchResult instanceof Utils.PatternMatchOKResult) {
        arguments = ((Utils.PatternMatchOKResult) matchResult).expressions;
      }

      if (error != null) {
        expr.setWellTyped(myVisitor.getLocalContext(), Error(null, error));
        myVisitor.getErrorReporter().report(error);
        return null;
      }
    }

    CheckTypeVisitor.OKResult result = new CheckTypeVisitor.OKResult(ConCall(constructor, arguments), constructor.getBaseType(), null);
    fixConstructorParameters(constructor, result, true);
    return result;
  }
}
