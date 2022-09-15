package org.babyfish.jimmer.apt.meta;

import com.squareup.javapoet.ClassName;
import org.babyfish.jimmer.meta.ModelException;

import javax.lang.model.element.*;
import javax.validation.Constraint;
import java.util.*;

public class ValidationMessages {

    public static final String CONSTRAINT_FULL_NAME = Constraint.class.getName();

    private ValidationMessages() {}

    public static Map<ClassName, String> parseMessageMap(Element element) {
        Map<ClassName, String> map = new LinkedHashMap<>();
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (hasConstraint(annotationMirror.getAnnotationType().asElement())) {
                TypeElement typeElement = (TypeElement) annotationMirror.getAnnotationType().asElement();
                ClassName className = ClassName.get(
                        ((PackageElement)typeElement.getEnclosingElement()).getQualifiedName().toString(),
                        typeElement.getSimpleName().toString()
                );
                if (map.containsKey(className)) {
                    throw new ModelException("Duplicate annotation \"" + className + "\" for " + element);
                }
                String message = "";
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e :
                        annotationMirror.getElementValues().entrySet()) {
                    if (e.getKey().getSimpleName().toString().equals("message")) {
                        Object msg = e.getValue().getValue();
                        if (msg instanceof String) {
                            message = (String) msg;
                        }
                        break;
                    }
                }
                map.put(className, message);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    private static boolean hasConstraint(Element element) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            TypeElement annoElement = (TypeElement) mirror.getAnnotationType().asElement();
            if (annoElement.getQualifiedName().toString().equals(CONSTRAINT_FULL_NAME)) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e :
                    mirror.getElementValues().entrySet()) {
                    if (e.getKey().getSimpleName().toString().equals("validatedBy")) {
                        return !((Collection<?>)e.getValue().getValue()).isEmpty();
                    }
                }
                break;
            }
        }
        return false;
    }
}