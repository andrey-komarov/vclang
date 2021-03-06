package com.jetbrains.jetpad.vclang.typechecking.implicitargs;

import com.jetbrains.jetpad.vclang.term.*;
import com.jetbrains.jetpad.vclang.term.context.binding.FunctionInferenceBinding;
import com.jetbrains.jetpad.vclang.term.context.binding.IgnoreBinding;
import com.jetbrains.jetpad.vclang.term.context.binding.InferenceBinding;
import com.jetbrains.jetpad.vclang.term.context.param.DependentLink;
import com.jetbrains.jetpad.vclang.term.definition.Constructor;
import com.jetbrains.jetpad.vclang.term.expr.*;
import com.jetbrains.jetpad.vclang.term.expr.visitor.CheckTypeVisitor;
import com.jetbrains.jetpad.vclang.term.expr.visitor.CompareVisitor;
import com.jetbrains.jetpad.vclang.term.expr.visitor.NormalizeVisitor;
import com.jetbrains.jetpad.vclang.typechecking.error.TypeCheckingError;
import com.jetbrains.jetpad.vclang.typechecking.error.TypeMismatchError;
import com.jetbrains.jetpad.vclang.typechecking.implicitargs.equations.DummyEquations;
import com.jetbrains.jetpad.vclang.typechecking.implicitargs.equations.Equations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.jetbrains.jetpad.vclang.term.expr.ExpressionFactory.*;

public class StdImplicitArgsInference extends BaseImplicitArgsInference {
  public StdImplicitArgsInference(CheckTypeVisitor visitor) {
    super(visitor);
  }

  protected boolean fixImplicitArgs(CheckTypeVisitor.Result result, List<DependentLink> parameters, Abstract.Expression expr) {
    if (parameters.isEmpty()) {
      return true;
    }

    Substitution substitution = new Substitution();
    for (int i = 0; i < parameters.size(); i++) {
      DependentLink parameter = parameters.get(i);
      InferenceBinding inferenceBinding = new FunctionInferenceBinding(parameter.getName(), parameter.getType(), i + 1, expr);
      result.addUnsolvedVariable(inferenceBinding);
      Expression binding = Reference(inferenceBinding);
      result.expression = Apps(result.expression, binding, false, true);
      substitution.add(parameter, binding);
    }
    result.type = result.type.subst(substitution);
    return true;
  }

  protected CheckTypeVisitor.Result inferArg(CheckTypeVisitor.Result result, Abstract.Expression arg, boolean isExplicit, Abstract.Expression fun) {
    if (result == null) {
      return null;
    }

    if (isExplicit) {
      List<DependentLink> params = new ArrayList<>();
      result.type = result.type.getPiParameters(params, true, true);
      if (!fixImplicitArgs(result, params, fun)) {
        return null;
      }
    } else {
      result.type = result.type.normalize(NormalizeVisitor.Mode.WHNF);
    }

    if (!(result.type instanceof PiExpression)) {
      TypeCheckingError error = new TypeMismatchError(new StringPrettyPrintable("A pi type"), result.type, fun);
      fun.setWellTyped(myVisitor.getContext(), new ErrorExpression(result.expression, error));
      myVisitor.getErrorReporter().report(error);
      return null;
    }

    PiExpression actualType = (PiExpression) result.type;
    CheckTypeVisitor.Result argResult = myVisitor.typeCheck(arg, actualType.getParameters().getType());
    if (argResult == null) {
      return null;
    }

    if (actualType.getParameters().isExplicit() != isExplicit) {
      TypeCheckingError error = new TypeCheckingError("Expected an " + (actualType.getParameters().isExplicit() ? "explicit" : "implicit") + " argument", arg);
      arg.setWellTyped(myVisitor.getContext(), new ErrorExpression(argResult.expression, error));
      myVisitor.getErrorReporter().report(error);
      return null;
    }

    result.expression = new AppExpression(result.expression, new ArgumentExpression(argResult.expression, isExplicit, false));
    result.type = actualType.applyExpressions(Collections.singletonList(argResult.expression));
    result.add(argResult);
    result.update();
    return result;
  }

  protected CheckTypeVisitor.Result inferArg(Abstract.Expression fun, Abstract.Expression arg, boolean isExplicit, Expression expectedType) {
    CheckTypeVisitor.Result result;
    if (fun instanceof Abstract.AppExpression) {
      Abstract.ArgumentExpression argument = ((Abstract.AppExpression) fun).getArgument();
      result = inferArg(((Abstract.AppExpression) fun).getFunction(), argument.getExpression(), argument.isExplicit(), expectedType);
    } else {
      if (fun instanceof Abstract.DefCallExpression && isExplicit) {
        if (expectedType != null) {
          result = myVisitor.getTypeCheckingDefCall().typeCheckDefCall((Abstract.DefCallExpression) fun);
          if (result != null && result.expression instanceof ConCallExpression) {
            List<Expression> args = new ArrayList<>();
            Expression expectedTypeNorm = expectedType.normalize(NormalizeVisitor.Mode.WHNF).getFunction(args);
            if (expectedTypeNorm instanceof DataCallExpression) {
              Collections.reverse(args);
              ConCallExpression conCall = (ConCallExpression) result.expression;
              args = conCall.getDefinition().matchDataTypeArguments(args);
              if (!conCall.getDataTypeArguments().isEmpty()) {
                args = args.subList(conCall.getDataTypeArguments().size(), args.size());
              }
              if (!args.isEmpty()) {
                for (Expression arg1 : args) {
                  result.expression = Apps(result.expression, arg1, false, true);
                }
                result.type = result.type.applyExpressions(args);
              }
              CheckTypeVisitor.Result result1 = inferArg(result, arg, true, fun);
              if (result1 != null && Prelude.isPathCon(conCall.getDefinition())) {
                Expression argExpr = ((AppExpression) result1.expression).getArgument().getExpression();
                result1.type = Apps(((AppExpression) ((AppExpression) result1.type).getFunction()).getFunction(), Apps(argExpr, ConCall(Prelude.LEFT)), Apps(argExpr, ConCall(Prelude.RIGHT)));
                if (!myVisitor.compare(result1, expectedType, Equations.CMP.EQ, fun)) {
                  return null;
                }
              }
              return result1;
            }
          }
        } else {
          result = myVisitor.typeCheck(fun, null);
        }

        if (result != null && result.expression instanceof ConCallExpression && Prelude.isPathCon(((ConCallExpression) result.expression).getDefinition())) {
          Expression interval = DataCall(Prelude.INTERVAL);
          CheckTypeVisitor.Result argResult = myVisitor.typeCheck(arg, Pi(interval, Reference(new IgnoreBinding(null, Universe()))));
          if (argResult == null) return null;
          Expression type = argResult.type.normalize(NormalizeVisitor.Mode.WHNF);
          if (type instanceof PiExpression) {
            PiExpression piType = (PiExpression) type;
            DependentLink params = piType.getParameters();
            Expression domType = params.getType().normalize(NormalizeVisitor.Mode.WHNF);

            if (argResult.getEquations() instanceof DummyEquations) {
              argResult.setEquations(myVisitor.getImplicitArgsInference().newEquations());
            }
            if (CompareVisitor.compare(argResult.getEquations(), Equations.CMP.EQ, interval, domType, arg)) {
              Expression lamExpr;
              if (params.getNext().hasNext()) {
                DependentLink lamParam = param("i", interval);
                lamExpr = Lam(lamParam, Pi(params.getNext(), piType.getCodomain()).subst(params, Reference(lamParam)));
              } else {
                lamExpr = Lam(params, piType.getCodomain());
              }
              Expression expr1 = Apps(argResult.expression, ConCall(Prelude.LEFT));
              Expression expr2 = Apps(argResult.expression, ConCall(Prelude.RIGHT));
              Constructor pathCon = ((ConCallExpression) result.expression).getDefinition();
              argResult.expression = Apps(ConCall(pathCon, lamExpr, expr1, expr2), argResult.expression);
              argResult.type = Apps(DataCall(pathCon.getDataType()), lamExpr, expr1, expr2);
              return argResult;
            }
          }

          TypeCheckingError error = new TypeCheckingError("Expected an expression of a type of the form I -> _", arg);
          arg.setWellTyped(myVisitor.getContext(), new ErrorExpression(result.expression, error));
          myVisitor.getErrorReporter().report(error);
          return null;
        }
      } else {
        result = myVisitor.typeCheck(fun, null);
      }
    }

    if (result == null) {
      myVisitor.typeCheck(arg, null);
      return null;
    }
    return inferArg(result, arg, isExplicit, fun);
  }

  @Override
  public CheckTypeVisitor.Result infer(Abstract.AppExpression expr, Expression expectedType) {
    Abstract.ArgumentExpression arg = expr.getArgument();
    return inferArg(expr.getFunction(), arg.getExpression(), arg.isExplicit(), expectedType);
  }

  @Override
  public CheckTypeVisitor.Result infer(Abstract.BinOpExpression expr, Expression expectedType) {
    Concrete.Position position = expr instanceof Concrete.Expression ? ((Concrete.Expression) expr).getPosition() : ConcreteExpressionFactory.POSITION;
    return inferArg(inferArg(new Concrete.DefCallExpression(position, expr.getResolvedBinOp()), expr.getLeft(), true, null), expr.getRight(), true, expr);
  }

  @Override
  public CheckTypeVisitor.Result inferTail(CheckTypeVisitor.Result result, Expression expectedType, Abstract.Expression expr) {
    List<DependentLink> actualParams = new ArrayList<>();
    Expression actualType = result.type.getPiParameters(actualParams, true, true);
    List<DependentLink> expectedParams = new ArrayList<>(actualParams.size());
    Expression expectedType1 = expectedType.getPiParameters(expectedParams, true, true);
    if (expectedParams.size() > actualParams.size()) {
      TypeCheckingError error = new TypeMismatchError(expectedType.normalize(NormalizeVisitor.Mode.HUMAN_NF), result.type.normalize(NormalizeVisitor.Mode.HUMAN_NF), expr);
      expr.setWellTyped(myVisitor.getContext(), new ErrorExpression(result.expression, error));
      myVisitor.getErrorReporter().report(error);
      return null;
    }

    if (expectedParams.size() != actualParams.size()) {
      int argsNumber = actualParams.size() - expectedParams.size();
      result.type = actualType.fromPiParameters(actualParams.subList(argsNumber, actualParams.size()));
      if (!fixImplicitArgs(result, actualParams.subList(0, argsNumber), expr)) {
        return null;
      }
      expectedType = expectedType1.fromPiParameters(expectedParams);
    }

    result = myVisitor.checkResult(expectedType, result, expr);
    if (result != null) {
      result.update();
    }
    return result;
  }
}
