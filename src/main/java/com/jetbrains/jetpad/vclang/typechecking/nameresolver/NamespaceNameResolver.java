package com.jetbrains.jetpad.vclang.typechecking.nameresolver;

import com.jetbrains.jetpad.vclang.naming.Namespace;
import com.jetbrains.jetpad.vclang.naming.NamespaceMember;

public class NamespaceNameResolver implements NameResolver {
  private final Namespace myNamespace;

  public NamespaceNameResolver(Namespace namespace) {
    myNamespace = namespace;
  }

  protected Namespace getNamespace() {
    return myNamespace;
  }

  @Override
  public NamespaceMember locateName(String name) {
    return myNamespace.getMember(name);
  }

  @Override
  public NamespaceMember getMember(Namespace parent, String name) {
    return null;
  }
}
