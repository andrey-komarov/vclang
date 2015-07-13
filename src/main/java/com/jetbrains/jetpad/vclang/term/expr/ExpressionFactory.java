package com.jetbrains.jetpad.vclang.term.expr;

import com.jetbrains.jetpad.vclang.term.Abstract;
import com.jetbrains.jetpad.vclang.term.Prelude;
import com.jetbrains.jetpad.vclang.term.definition.*;
import com.jetbrains.jetpad.vclang.term.error.TypeCheckingError;
import com.jetbrains.jetpad.vclang.term.expr.arg.Argument;
import com.jetbrains.jetpad.vclang.term.expr.arg.NameArgument;
import com.jetbrains.jetpad.vclang.term.expr.arg.TelescopeArgument;
import com.jetbrains.jetpad.vclang.term.expr.arg.TypeArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ExpressionFactory {
  public static Expression Apps(Expression expr, Expression... exprs) {
    for (Expression expr1 : exprs) {
      expr = new AppExpression(expr, new ArgumentExpression(expr1, true, false));
    }
    return expr;
  }

  public static Expression Apps(Expression expr, ArgumentExpression... exprs) {
    for (ArgumentExpression expr1 : exprs) {
      expr = new AppExpression(expr, expr1);
    }
    return expr;
  }

  public static Expression Apps(Expression expr, Expression arg, boolean explicit, boolean hidden) {
    return new AppExpression(expr, new ArgumentExpression(arg, explicit, hidden));
  }

  public static DefCallExpression DefCall(Definition definition) {
    return new DefCallExpression(definition);
  }

  public static ClassExtExpression ClassExt(ClassDefinition baseClass, Map<FunctionDefinition, OverriddenDefinition> definitions, Universe universe) {
    return new ClassExtExpression(baseClass, definitions, universe);
  }

  public static NewExpression New(Expression expression) {
    return new NewExpression(expression);
  }

  public static IndexExpression Index(int i) {
    return new IndexExpression(i);
  }

  public static LamExpression Lam(List<Argument> arguments, Expression body) {
    return new LamExpression(arguments, body);
  }

  public static LamExpression Lam(String var, Expression body) {
    List<Argument> arguments = new ArrayList<>(1);
    arguments.add(new NameArgument(true, var));
    return Lam(arguments, body);
  }

  public static List<String> vars(String... vars) {
    return Arrays.asList(vars);
  }

  public static List<TypeArgument> args(TypeArgument... args) {
    return Arrays.asList(args);
  }

  public static List<TelescopeArgument> teleArgs(TelescopeArgument... args) {
    return Arrays.asList(args);
  }

  public static List<NameArgument> nameArgs(NameArgument... args) {
    return Arrays.asList(args);
  }

  public static List<Argument> lamArgs(Argument... args) {
    return Arrays.asList(args);
  }

  public static NameArgument Name(boolean explicit, String name) {
    return new NameArgument(explicit, name);
  }

  public static NameArgument Name(String name) {
    return new NameArgument(true, name);
  }

  public static TypeArgument TypeArg(boolean explicit, Expression type) {
    return new TypeArgument(explicit, type);
  }

  public static TypeArgument TypeArg(Expression type) {
    return new TypeArgument(true, type);
  }

  public static TelescopeArgument Tele(boolean explicit, List<String> names, Expression type) {
    return new TelescopeArgument(explicit, names, type);
  }

  public static TelescopeArgument Tele(List<String> names, Expression type) {
    return new TelescopeArgument(true, names, type);
  }

  public static PiExpression Pi(List<TypeArgument> arguments, Expression codomain) {
    return new PiExpression(arguments, codomain);
  }

  public static PiExpression Pi(boolean explicit, String var, Expression domain, Expression codomain) {
    List<TypeArgument> arguments = new ArrayList<>(1);
    List<String> vars = new ArrayList<>(1);
    vars.add(var);
    arguments.add(new TelescopeArgument(explicit, vars, domain));
    return new PiExpression(arguments, codomain);
  }

  public static PiExpression Pi(String var, Expression domain, Expression codomain) {
    return Pi(true, var, domain, codomain);
  }

  public static PiExpression Pi(Expression domain, Expression codomain) {
    return new PiExpression(domain, codomain);
  }

  public static SigmaExpression Sigma(List<TypeArgument> arguments) {
    return new SigmaExpression(arguments);
  }

  public static TupleExpression Tuple(List<Expression> fields, SigmaExpression type) {
    return new TupleExpression(fields, type);
  }

  public static TupleExpression Tuple(SigmaExpression type, Expression... fields) {
    return new TupleExpression(Arrays.asList(fields), type);
  }

  public static FieldAccExpression FieldAcc(Expression expr, Definition field) {
    return new FieldAccExpression(expr, field);
  }

  public static ProjExpression Proj(Expression expr, int field) {
    return new ProjExpression(expr, field);
  }

  public static DefCallExpression Nat() {
    return DefCall(Prelude.NAT);
  }

  public static DefCallExpression Zero() {
    return DefCall(Prelude.ZERO);
  }

  public static DefCallExpression Suc() {
    return DefCall(Prelude.SUC);
  }

  public static Expression Suc(Expression expr) {
    return Apps(Suc(), expr);
  }

  public static UniverseExpression Universe() {
    return new UniverseExpression(new Universe.Type());
  }

  public static UniverseExpression Universe(int level) {
    return new UniverseExpression(new Universe.Type(level));
  }

  public static UniverseExpression Universe(int level, int truncated) {
    return new UniverseExpression(new Universe.Type(level, truncated));
  }

  public static ErrorExpression Error(Expression expr, TypeCheckingError error) {
    return new ErrorExpression(expr, error);
  }

  public static Expression BinOp(Expression left, Definition binOp, Expression right) {
    return Apps(DefCall(binOp), left, right);
  }

  public static ElimExpression Elim(Abstract.ElimExpression.ElimType elimType, IndexExpression expression, List<Clause> clauses, Clause otherwise) {
    return new ElimExpression(elimType, expression, clauses, otherwise);
  }
}
