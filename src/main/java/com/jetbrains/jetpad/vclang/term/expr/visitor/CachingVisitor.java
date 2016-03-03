package com.jetbrains.jetpad.vclang.term.expr.visitor;

import com.jetbrains.jetpad.vclang.term.expr.*;

import java.util.HashMap;

public class CachingVisitor<P, R> implements ExpressionVisitor<P, R> {
  private final ExpressionVisitor<P, R> myVisitor;
  int gets = 0;
  int puts = 0;
  private final HashMap<VisitRequest, R> cache = new HashMap<VisitRequest, R>() {
    @Override
    public R get(Object key) {
      gets++;
      return super.get(key);
    }

    @Override
    public R put(VisitRequest key, R value) {
      puts++;
      return super.put(key, value);
    }
  };

  private class VisitRequest {
    final P params;
    final Expression expr;

    public VisitRequest(P params, Expression expr) {
      this.params = params;
      this.expr = expr;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      VisitRequest that = (VisitRequest) o;

      if (params != null ? !params.equals(that.params) : that.params != null) return false;
      return expr != null ? expr.equals(that.expr) : that.expr == null;

    }

    @Override
    public int hashCode() {
      int result = params != null ? params.hashCode() : 0;
      result = 31 * result + (expr != null ? expr.hashCode() : 0);
      return result;
    }
  }

  public CachingVisitor(ExpressionVisitor<P, R> myVisitor) {
    this.myVisitor = myVisitor;
  }

  @Override
  public R visitApp(AppExpression expr, P params) {
    VisitRequest req = new VisitRequest(params, expr);
    R res = cache.get(req);
    if (res == null) {
      res = myVisitor.visitApp(expr, params);
      cache.put(req, res);
    }
    return res;
  }

  @Override
  public R visitFunCall(FunCallExpression expr, P params) {
    VisitRequest req = new VisitRequest(params, expr);
    R res = cache.get(req);
    if (res == null) {
      res = myVisitor.visitFunCall(expr, params);
      cache.put(req, res);
    }
    return res;
  }

  @Override
  public R visitConCall(ConCallExpression expr, P params) {
    VisitRequest req = new VisitRequest(params, expr);
    R res = cache.get(req);
    if (res == null) {
      res = myVisitor.visitConCall(expr, params);
      cache.put(req, res);
    }
    return res;
  }

  @Override
  public R visitDataCall(DataCallExpression expr, P params) {
    VisitRequest req = new VisitRequest(params, expr);
    R res = cache.get(req);
    if (res == null) {
      res = myVisitor.visitDataCall(expr, params);
      cache.put(req, res);
    }
    return res;
  }

  @Override
  public R visitFieldCall(FieldCallExpression expr, P params) {
    VisitRequest req = new VisitRequest(params, expr);
    R res = cache.get(req);
    if (res == null) {
      res = myVisitor.visitFieldCall(expr, params);
      cache.put(req, res);
    }
    return res;
  }

  @Override
  public R visitClassCall(ClassCallExpression expr, P params) {
    VisitRequest req = new VisitRequest(params, expr);
    R res = cache.get(req);
    if (res == null) {
      res = myVisitor.visitClassCall(expr, params);
      cache.put(req, res);
    }
    return res;
  }

  @Override
  public R visitReference(ReferenceExpression expr, P params) {
    VisitRequest req = new VisitRequest(params, expr);
    R res = cache.get(req);
    if (res == null) {
      res = myVisitor.visitReference(expr, params);
      cache.put(req, res);
    }
    return res;
  }

  @Override
  public R visitLam(LamExpression expr, P params) {
    VisitRequest req = new VisitRequest(params, expr);
    R res = cache.get(req);
    if (res == null) {
      res = myVisitor.visitLam(expr, params);
      cache.put(req, res);
    }
    return res;
  }

  @Override
  public R visitPi(PiExpression expr, P params) {
    VisitRequest req = new VisitRequest(params, expr);
    R res = cache.get(req);
    if (res == null) {
      res = myVisitor.visitPi(expr, params);
      cache.put(req, res);
    }
    return res;
  }

  @Override
  public R visitSigma(SigmaExpression expr, P params) {
    VisitRequest req = new VisitRequest(params, expr);
    R res = cache.get(req);
    if (res == null) {
      res = myVisitor.visitSigma(expr, params);
      cache.put(req, res);
    }
    return res;
  }

  @Override
  public R visitUniverse(UniverseExpression expr, P params) {
    VisitRequest req = new VisitRequest(params, expr);
    R res = cache.get(req);
    if (res == null) {
      res = myVisitor.visitUniverse(expr, params);
      cache.put(req, res);
    }
    return res;
  }

  @Override
  public R visitError(ErrorExpression expr, P params) {
    VisitRequest req = new VisitRequest(params, expr);
    R res = cache.get(req);
    if (res == null) {
      res = myVisitor.visitError(expr, params);
      cache.put(req, res);
    }
    return res;
  }

  @Override
  public R visitTuple(TupleExpression expr, P params) {
    VisitRequest req = new VisitRequest(params, expr);
    R res = cache.get(req);
    if (res == null) {
      res = myVisitor.visitTuple(expr, params);
      cache.put(req, res);
    }
    return res;
  }

  @Override
  public R visitProj(ProjExpression expr, P params) {
    VisitRequest req = new VisitRequest(params, expr);
    R res = cache.get(req);
    if (res == null) {
      res = myVisitor.visitProj(expr, params);
      cache.put(req, res);
    }
    return res;
  }

  @Override
  public R visitNew(NewExpression expr, P params) {
    VisitRequest req = new VisitRequest(params, expr);
    R res = cache.get(req);
    if (res == null) {
      res = myVisitor.visitNew(expr, params);
      cache.put(req, res);
    }
    return res;
  }

  @Override
  public R visitLet(LetExpression letExpression, P params) {
    VisitRequest req = new VisitRequest(params, letExpression);
    R res = cache.get(req);
    if (res == null) {
      res = myVisitor.visitLet(letExpression, params);
      cache.put(req, res);
    }
    return res;
  }
}
