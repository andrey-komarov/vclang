package com.jetbrains.jetpad.vclang.term.expr.visitor;

import com.jetbrains.jetpad.vclang.term.context.binding.Binding;
import com.jetbrains.jetpad.vclang.term.context.param.DependentLink;
import com.jetbrains.jetpad.vclang.term.definition.Constructor;
import com.jetbrains.jetpad.vclang.term.definition.DataDefinition;
import com.jetbrains.jetpad.vclang.term.definition.Definition;
import com.jetbrains.jetpad.vclang.term.expr.*;

import java.util.*;

public class EraseTypesVisitor extends BaseExpressionVisitor<Void, EraseTypesVisitor.AST> {
  private Map<Binding, String> vars = new HashMap<>();
  private Set<DataDefinition> data = new HashSet<>();

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

  private void registerData(DataDefinition dataDefinition) {
    data.add(dataDefinition);
  }

  @Override
  public AST visitDefCall(DefCallExpression expr, Void params) {
    List<ArgumentExpression> args = new ArrayList<>();
    expr.getFunctionArgs(args);
    Definition def = expr.getDefinition();
    if (def instanceof Constructor) {
      registerData(((Constructor) def).getDataType());
      AST res = new Var(def.getName());
      for (ArgumentExpression arg : args) {
        res = new App(res, arg.getExpression().accept(this, null));
      }
      return res;
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public AST visitApp(AppExpression expr, Void params) {
    List<Expression> args = new ArrayList<>();
    Expression fun = expr.getFunction(args);
    AST result = fun.accept(this, params);
    for (Expression arg : args) {
      result = new App(result, arg.accept(this, params));
    }
    return result;
  }

  @Override
  public AST visitReference(ReferenceExpression expr, Void params) {
    return new Var(encode(expr.getBinding()));
  }

  @Override
  public AST visitLam(LamExpression expr, Void params) {
    List<String> vars = encodeMany(expr.getParameters());
    Collections.reverse(vars);
    AST res = expr.getBody().accept(this, params);
    for (String var : vars) {
      res = new Lam(var, res);
    }
    return res;
  }

  @Override
  public AST visitPi(PiExpression expr, Void params) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AST visitSigma(SigmaExpression expr, Void params) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AST visitUniverse(UniverseExpression expr, Void params) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AST visitError(ErrorExpression expr, Void params) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AST visitTuple(TupleExpression expr, Void params) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AST visitProj(ProjExpression expr, Void params) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AST visitNew(NewExpression expr, Void params) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AST visitLet(LetExpression letExpression, Void params) {
    throw new UnsupportedOperationException();
  }

  public interface AST {}

  public class App implements AST {
    public final AST fun;
    public final AST arg;

    public App(AST fun, AST arg) {
      this.fun = fun;
      this.arg = arg;
    }

    @Override
    public String toString() {
      return "(" + fun + " " + arg + ")";
    }
  }

  public class Var implements AST {
    public final String name;

    public Var(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public class Lam implements AST {
    public final String var;
    public final AST body;

    public Lam(String var, AST body) {
      this.var = var;
      this.body = body;
    }

    @Override
    public String toString() {
      return "(\\" + var + " . " + body + ")";
    }
  }
}
