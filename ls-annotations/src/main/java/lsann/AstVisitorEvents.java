package lsann;

public abstract class AstVisitorEvents {
    public void visitAnnotationDefinition(AstDefinition.ClassRep cls) {
    }
    public void visitClassWithAnnotation(AstDefinition.ClassRep cls) {
    }
    public void visitMethodWithAnnotation(AstDefinition.MethodRep cls) {
    }
    public void visitFieldWithAnnotation(AstDefinition.FieldRep rep) {
    }
    public void visitMethodParamWithAnnotation(AstDefinition.MethodParamRep cls) {
    }

}
