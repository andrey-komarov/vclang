package com.jetbrains.jetpad.vclang.term.expr.visitor;

import com.jetbrains.jetpad.vclang.term.context.param.DependentLink;
import com.jetbrains.jetpad.vclang.term.expr.*;

import java.util.ArrayList;
import java.util.List;

public class HashCodeVisitor extends BaseExpressionVisitor<Void, Integer> {

  @Override
  public Integer visitDefCall(DefCallExpression expr, Void params) {
    int hash = 1;
    hash = hash * 31 + expr.getType().hashCode();
    return hash;
  }

  @Override
  public Integer visitApp(AppExpression expr, Void params) {
    List<ArgumentExpression> args = new ArrayList<>();
    Expression fun = expr.getFunctionArgs(args);
    int hash = 2;
    hash = hash * 31 + fun.hashCode();
    for (ArgumentExpression arg : args) {
      hash = hash * 31 + arg.getExpression().hashCode();
    }
    return hash;
  }

  @Override
  public Integer visitReference(ReferenceExpression expr, Void params) {
    return 42;
  }

  @Override
  public Integer visitLam(LamExpression expr, Void params) {
    List<DependentLink> lamParams = new ArrayList<>();
    Expression body = expr.getLamParameters(lamParams);
    int hash = 4;
    hash = hash * 31 + lamParams.size();
    hash = hash * 31 + body.hashCode();
    return hash;
  }

  @Override
  public Integer visitPi(PiExpression expr, Void params) {
    int hash = 5;
    hash = hash * 31 + expr.getCodomain().hashCode();
    return hash;
  }

  @Override
  public Integer visitSigma(SigmaExpression expr, Void params) {
    int hash = 6;
    for (DependentLink link = expr.getParameters(); link.hasNext(); link = link.getNext()) {
      hash = hash * 31 + link.getType().hashCode();
    }
    return hash;
  }

  @Override
  public Integer visitUniverse(UniverseExpression expr, Void params) {
    int hash = 7;
    return hash;
  }

  @Override
  public Integer visitError(ErrorExpression expr, Void params) {
    int hash = 8;
    return hash;
  }

  @Override
  public Integer visitTuple(TupleExpression expr, Void params) {
    int hash = 9;
    hash = hash * 31 + expr.getType().hashCode();
    for (Expression field : expr.getFields()) {
      hash = hash * 31 + field.hashCode();
    }
    return hash;
  }

  @Override
  public Integer visitProj(ProjExpression expr, Void params) {
    int hash = 10;
    hash = hash * 31 + expr.getType().hashCode();
    hash = hash * 31 + expr.getExpression().hashCode();
    hash = hash * 31 + expr.getField();
    return hash;
  }

  @Override
  public Integer visitNew(NewExpression expr, Void params) {
    int hash = 11;
    hash = hash * 31 + expr.getType().hashCode();
    hash = hash * 31 + expr.getExpression().hashCode();
    return hash;
  }

  @Override
  public Integer visitLet(LetExpression letExpression, Void params) {
    int hash = 12;
    hash = hash * 31 + letExpression.getType().hashCode();
    hash = hash * 31 + letExpression.getExpression().hashCode();
    return hash;
  }
}
