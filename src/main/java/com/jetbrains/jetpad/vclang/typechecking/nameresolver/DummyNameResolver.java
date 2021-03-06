package com.jetbrains.jetpad.vclang.typechecking.nameresolver;

import com.jetbrains.jetpad.vclang.naming.Namespace;
import com.jetbrains.jetpad.vclang.naming.NamespaceMember;

public class DummyNameResolver implements NameResolver {
  private DummyNameResolver() {
  }

  private final static DummyNameResolver INSTANCE = new DummyNameResolver();

  public static DummyNameResolver getInstance() {
    return INSTANCE;
  }

  @Override
  public NamespaceMember locateName(String name) {
    return null;
  }

  @Override
  public NamespaceMember getMember(Namespace parent, String name) {
    return parent.getMember(name);
  }
}
