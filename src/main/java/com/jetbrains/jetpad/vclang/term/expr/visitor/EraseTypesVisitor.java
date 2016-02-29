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
  public AST visitDefCall(DefCallExpression expr, Void params) {
    List<ArgumentExpression> args = new ArrayList<>();
    expr.getFunctionArgs(args);
    Definition def = expr.getDefinition();
    if (def instanceof Constructor) {
      int tag = getConstructorTag((Constructor) def);
      List<AST> consParams = new ArrayList<>();
      for (ArgumentExpression arg : args) {
        consParams.add(arg.getExpression().accept(this, null));
      }
      return new ConCall(tag, consParams);
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

  public static class App implements AST {
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

  public static class ConCall implements AST {
    public final int tag;
    public final List<AST> params;

    public ConCall(int tag, List<AST> params) {
      this.tag = tag;
      this.params = params;
    }

    @Override
    public String toString() {
      return "data(" + tag + ", " + params + ")";
    }
  }

  public static class Var implements AST {
    public final String name;

    public Var(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public static class Lam implements AST {
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

  public static class Data {
    public final String name;
    public final List<Con> constructors;

    public Data(String name, List<Con> constructors) {
      this.name = name;
      this.constructors = constructors;
    }

    public static Data decode(DataDefinition data) {
      List<Con> constructors = new ArrayList<>();
      for (Constructor constructor : data.getConstructors()) {
        int size = 0;
        for (DependentLink link = constructor.getParameters(); link.hasNext(); link = link.getNext()) {
          size++;
        }
        Con con = new Con(constructor.getResolvedName().getFullName(), size);
        constructors.add(con);
      }
      return new Data(data.getResolvedName().getFullName(), constructors);
    }

    @Override
    public String toString() {
      return "Data{" +
              "name='" + name + '\'' +
              ", constructors=" + constructors +
              '}';
    }
  }

  public static class Con {
    public final String name;
    public final int nArgs;

    public Con(String name, int nArgs) {
      this.name = name;
      this.nArgs = nArgs;
    }

    @Override
    public String toString() {
      return name + "<" + nArgs + ">";
    }
  }
}
