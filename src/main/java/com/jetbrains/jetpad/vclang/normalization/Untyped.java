package com.jetbrains.jetpad.vclang.normalization;

import java.util.List;

public interface Untyped {
  class App implements Untyped {
    public final Untyped fun;
    public final Untyped arg;

    public App(Untyped fun, Untyped arg) {
      this.fun = fun;
      this.arg = arg;
    }

    @Override
    public String toString() {
      return "(" + fun + " " + arg + ")";
    }
  }

  class ConCall implements Untyped {
    public final int tag;
    public final List<Untyped> params;

    public ConCall(int tag, List<Untyped> params) {
      this.tag = tag;
      this.params = params;
    }

    @Override
    public String toString() {
      return "data(" + tag + ", " + params + ")";
    }
  }

  class Var implements Untyped {
    public final String name;

    public Var(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  class Lam implements Untyped {
    public final String var;
    public final Untyped body;

    public Lam(String var, Untyped body) {
      this.var = var;
      this.body = body;
    }

    @Override
    public String toString() {
      return "(\\" + var + " . " + body + ")";
    }
  }

  class FunCall implements Untyped {
    public final String fun;

    public FunCall(String fun) {
      this.fun = fun;
    }

    @Override
    public String toString() {
      return "(" + fun + ")";
    }
  }
}
