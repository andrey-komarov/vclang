package com.jetbrains.jetpad.vclang.term.expr.visitor;

import com.jetbrains.jetpad.vclang.normalization.Untyped;
import com.jetbrains.jetpad.vclang.term.context.binding.Binding;
import com.jetbrains.jetpad.vclang.term.context.param.DependentLink;
import com.jetbrains.jetpad.vclang.term.definition.Constructor;
import com.jetbrains.jetpad.vclang.term.definition.Definition;
import com.jetbrains.jetpad.vclang.term.definition.FunctionDefinition;
import com.jetbrains.jetpad.vclang.term.expr.*;

import java.util.*;

public class EraseTypesVisitor extends BaseExpressionVisitor<Void, Untyped> {
  private Map<Binding, String> vars = new HashMap<>();
  private Map<String, FunctionDefinition> funs = new HashMap<>();

  public List<FunctionDefinition> getFunctions() {
    return new ArrayList<>(funs.values());
  }

  private void registerFunction(FunctionDefinition funDef) {
    funs.put(funDef.getResolvedName().getFullName(), funDef);
  }

  private String encode(Binding binding) {
    if (!vars.containsKey(binding)) {
      vars.put(binding, binding.getName() + vars.size());
    }
    return vars.get(binding);
  }

  private List<String> encodeMany(DependentLink link) {
    List<String> res = new ArrayList<>();
    for (; link.hasNext(); link = link.getNext()) {
      res.add(encode(link));
    }
    return res;
  }

  private int getConstructorTag(Constructor constructor) {
    List<Constructor> constructors = constructor.getDataType().getConstructors();
    int i = 0;
    for (Constructor constructor1 : constructors) {
      if (constructor1 == constructor) {
        return i;
      }
      i++;
    }
    throw new IllegalStateException();
  }
  
  @Override
  public Untyped visitDefCall(DefCallExpression expr, Void params) {
    List<ArgumentExpression> args = new ArrayList<>();
    expr.getFunctionArgs(args);
    Definition def = expr.getDefinition();
    if (def instanceof Constructor) {
      int tag = getConstructorTag((Constructor) def);
      List<Untyped> consParams = new ArrayList<>();
      for (ArgumentExpression arg : args) {
        consParams.add(arg.getExpression().accept(this, null));
      }
      return new Untyped.ConCall(tag, consParams);
    } else if (def instanceof FunctionDefinition) {
      registerFunction((FunctionDefinition) def);
      Untyped res = new Untyped.FunCall(def.getResolvedName().getFullName());
      for (ArgumentExpression arg : args) {
        res = new Untyped.App(res, arg.getExpression().accept(this, null));
      }
      return res;
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public Untyped visitApp(AppExpression expr, Void params) {
    List<Expression> args = new ArrayList<>();
    Expression fun = expr.getFunction(args);
    Untyped result = fun.accept(this, params);
    for (Expression arg : args) {
      result = new Untyped.App(result, arg.accept(this, params));
    }
    return result;
  }

  @Override
  public Untyped visitReference(ReferenceExpression expr, Void params) {
    return new Untyped.Var(encode(expr.getBinding()));
  }

  @Override
  public Untyped visitLam(LamExpression expr, Void params) {
    List<String> vars = encodeMany(expr.getParameters());
    Collections.reverse(vars);
    Untyped res = expr.getBody().accept(this, params);
    for (String var : vars) {
      res = new Untyped.Lam(var, res);
    }
    return res;
  }

  @Override
  public Untyped visitPi(PiExpression expr, Void params) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Untyped visitSigma(SigmaExpression expr, Void params) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Untyped visitUniverse(UniverseExpression expr, Void params) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Untyped visitError(ErrorExpression expr, Void params) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Untyped visitTuple(TupleExpression expr, Void params) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Untyped visitProj(ProjExpression expr, Void params) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Untyped visitNew(NewExpression expr, Void params) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Untyped visitLet(LetExpression letExpression, Void params) {
    throw new UnsupportedOperationException();
  }

}
