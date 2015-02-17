package main.java.com.jetbrains.term.visitor;

import main.java.com.jetbrains.term.definition.Definition;
import main.java.com.jetbrains.term.definition.FunctionDefinition;
import main.java.com.jetbrains.term.expr.*;
import main.java.com.jetbrains.term.typechecking.TypeCheckingException;
import main.java.com.jetbrains.term.typechecking.TypeInferenceException;
import main.java.com.jetbrains.term.typechecking.TypeMismatchException;

import java.util.List;

public class InferTypeVisitor implements ExpressionVisitor<Expression> {
    private final List<Definition> context;

    public InferTypeVisitor(List<Definition> context) {
        this.context = context;
    }

    @Override
    public Expression visitApp(AppExpression expr) {
        if (expr.getFunction() instanceof NelimExpression) {
            Expression type = expr.getArgument().inferType(context);
            return new PiExpression(new PiExpression(new NatExpression(), new PiExpression(type, type)), new PiExpression(new NatExpression(), type));
        }

        Expression functionType = expr.getFunction().inferType(context).normalize();
        if (functionType instanceof PiExpression) {
            PiExpression arrType = (PiExpression)functionType;
            expr.getArgument().checkType(context, arrType.getLeft());
            return arrType.getRight();
        } else {
            throw new TypeMismatchException(new PiExpression(new VarExpression("_"), new VarExpression("_")), functionType, expr.getFunction());
        }
    }

    @Override
    public Expression visitDefCall(DefCallExpression expr) {
        return expr.getDefinition().getType();
    }

    @Override
    public Expression visitIndex(IndexExpression expr) {
        assert expr.getIndex() < context.size();
        return context.get(context.size() - 1 - expr.getIndex()).getType();
    }

    @Override
    public Expression visitLam(LamExpression expr) {
        throw new TypeInferenceException(expr);
    }

    @Override
    public Expression visitNat(NatExpression expr) {
        return new UniverseExpression(0);
    }

    @Override
    public Expression visitNelim(NelimExpression expr) {
        throw new TypeInferenceException(expr);
    }

    @Override
    public Expression visitPi(PiExpression expr) {
        Expression leftType = expr.getLeft().inferType(context).normalize();
        context.add(new FunctionDefinition(expr.getVariable(), leftType, new VarExpression(expr.getVariable())));
        Expression rightType = expr.getRight().inferType(context).normalize();
        context.remove(context.size() - 1);
        boolean leftOK = leftType instanceof UniverseExpression;
        boolean rightOK = rightType instanceof UniverseExpression;
        if (leftOK && rightOK) {
            return new UniverseExpression(Integer.max(((UniverseExpression) leftType).getLevel(), ((UniverseExpression) rightType).getLevel()));
        } else {
            throw new TypeMismatchException(new UniverseExpression(), leftOK ? rightType : leftType, leftOK ? expr.getRight() : expr.getLeft());
        }
    }

    @Override
    public Expression visitSuc(SucExpression expr) {
        return new PiExpression(new NatExpression(), new NatExpression());
    }

    @Override
    public Expression visitUniverse(UniverseExpression expr) {
        return expr;
    }

    @Override
    public Expression visitVar(VarExpression expr) {
        throw new TypeCheckingException(expr);
    }

    @Override
    public Expression visitZero(ZeroExpression expr) {
        return new NatExpression();
    }
}
