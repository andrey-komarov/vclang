package com.jetbrains.jetpad.vclang.term.definition;

import com.jetbrains.jetpad.vclang.module.ModuleError;
import com.jetbrains.jetpad.vclang.term.Abstract;
import com.jetbrains.jetpad.vclang.term.definition.visitor.AbstractDefinitionVisitor;
import com.jetbrains.jetpad.vclang.term.expr.Expression;
import com.jetbrains.jetpad.vclang.term.expr.UniverseExpression;
import com.jetbrains.jetpad.vclang.term.expr.arg.TypeArgument;
import com.jetbrains.jetpad.vclang.term.expr.arg.Utils;

import java.util.List;

import static com.jetbrains.jetpad.vclang.term.expr.ExpressionFactory.Pi;

public class DataDefinition extends Definition implements Abstract.DataDefinition {
  private List<Constructor> myConstructors;
  private List<TypeArgument> myParameters;

  public DataDefinition(Utils.Name name, Definition parent, Precedence precedence, List<Constructor> constructors) {
    super(name, parent, precedence);
    myConstructors = constructors;
  }

  public DataDefinition(Utils.Name name, Definition parent, Precedence precedence, Universe universe, List<TypeArgument> parameters, List<Constructor> constructors) {
    super(name, parent, precedence);
    setUniverse(universe);
    hasErrors(false);
    myConstructors = constructors;
    myParameters = parameters;
  }

  @Override
  public List<TypeArgument> getParameters() {
    return myParameters;
  }

  public void setParameters(List<TypeArgument> arguments) {
    myParameters = arguments;
  }

  @Override
  public List<Constructor> getConstructors() {
    return myConstructors;
  }

  public void addConstructor(Constructor constructor, List<ModuleError> errors) {
    myConstructors.add(constructor);
    addStaticField(constructor, errors);
  }

  public void setConstructors(List<Constructor> constructors, List<ModuleError> errors) {
    myConstructors = constructors;
    getStaticFields().clear();
    for (Constructor constructor : constructors)
      addStaticField(constructor, errors);
  }

  @Override
  public Expression getType() {
    Expression resultType = new UniverseExpression(getUniverse());
    return myParameters.isEmpty() ? resultType : Pi(myParameters, resultType);
  }

  @Override
  public <P, R> R accept(AbstractDefinitionVisitor<? super P, ? extends R> visitor, P params) {
    return visitor.visitData(this, params);
  }
}
