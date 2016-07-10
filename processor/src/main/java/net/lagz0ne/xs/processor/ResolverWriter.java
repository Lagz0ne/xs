package net.lagz0ne.xs.processor;

import com.squareup.javapoet.*;
import net.lagz0ne.xs.Resolver;

import javax.annotation.processing.ProcessingEnvironment;
import javax.inject.Inject;
import javax.lang.model.element.*;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResolverWriter {

    public enum InjectionType {
        FIELD, CONSTRUCTOR, NO_DEPENDENCIES
    }

    private static ParameterizedTypeName DEPS_DECLARE_TYPE = ParameterizedTypeName.get(
            ClassName.get(LinkedHashMap.class),
            TypeName.get(String.class),
            TypeName.get(Class.class)
    );

    private static ParameterizedTypeName DEPS_RETURN_TYPE = ParameterizedTypeName.get(
            ClassName.get(LinkedHashMap.class),
            TypeName.get(String.class),
            TypeName.get(Object.class)
    );

    private static InjectionType detectInjectionType(TypeElement element) {
        List<? extends Element> enclosed = element.getEnclosedElements();

        /**
         * Find any @Inject constructor
         */
        List<? extends Element> constructors = enclosed.stream().filter($element ->
                $element.getKind() == ElementKind.CONSTRUCTOR &&
                $element.getAnnotation(Inject.class) != null
        ).collect(Collectors.toList());

        if (constructors.size() > 1) {
            throw new RuntimeException("Didn't expect to have 2 @Inject constructors");
        }

        if (constructors.size() == 1) {
            if (findNoArgsConstructor(element).isPresent()) {
                return InjectionType.NO_DEPENDENCIES;
            } else {
                return InjectionType.CONSTRUCTOR;
            }
        }

        /**
         * Check if there's a public noArgs constructor
         */
        if (findNoArgsConstructor(element).isPresent()) {

            List<? extends Element> fields = getInjectingFields(element);

            if (fields.size() == 0) {
                return InjectionType.NO_DEPENDENCIES;
            }

            return InjectionType.FIELD;
        } else {
            throw new RuntimeException("Cannot process an instance without public no args constructor");
        }
    }

    private static List<? extends Element> getInjectingFields(TypeElement element) {
        List<? extends Element> enclosed = element.getEnclosedElements();
        return enclosed.stream().filter($element ->
                $element.getKind() == ElementKind.FIELD &&
                        $element.getAnnotation(Inject.class) != null
        ).collect(Collectors.toList());
    }

    private static Optional<Element> findNoArgsConstructor(TypeElement el) {
        for (Element subElement : el.getEnclosedElements()) {
            if (subElement.getKind() == ElementKind.CONSTRUCTOR &&
                    subElement.getModifiers().contains(Modifier.PUBLIC)) {
                TypeMirror mirror = subElement.asType();
                if (mirror.accept(noArgsVisitor, null)) return Optional.of(subElement);
            }
        }
        return Optional.empty();
    }

    private static final TypeVisitor<Boolean, Void> noArgsVisitor =
            new SimpleTypeVisitor6<Boolean, Void>() {
                public Boolean visitExecutable(ExecutableType t, Void v) {
                    return t.getParameterTypes().isEmpty();
                }
            };

    public static void write(TypeElement element, ProcessingEnvironment processingEnv) throws IOException {

        String className = element.getSimpleName().toString();
        PackageElement packageElement = (PackageElement) element.getEnclosingElement();
        String packageName = packageElement.getQualifiedName().toString();

        ClassName resolverClassName = ClassName.get(Resolver.class);
        TypeName currentClassName = TypeName.get(element.asType());

        ParameterizedTypeName genericResolver = ParameterizedTypeName.get(
                resolverClassName,
                currentClassName
        );

        InjectionType injectionType = detectInjectionType(element);

        TypeSpec resolver = TypeSpec.classBuilder(className + "Resolver")
                .addSuperinterface(genericResolver)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(buildDependencyList(element, processingEnv, injectionType))
                .addMethod(buildGetDependencies())
                .addMethod(buildGetConcreteClass(element))
                .addMethod(buildOnResolved(element, injectionType))
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, resolver)
                .indent("\t")
                .skipJavaLangImports(true)
                .build();

        javaFile.writeTo(processingEnv.getFiler());
    }

    /**
     * @Override public Class<AnnotatedService1> buildGetConcreteClass() {
     * return AnnotatedService1.class;
     * }
     */
    private static MethodSpec buildGetConcreteClass(TypeElement element) {
        ParameterizedTypeName returnType = ParameterizedTypeName.get(
                ClassName.get(Class.class),
                TypeName.get(element.asType())
        );

        return MethodSpec.methodBuilder("getConcreteClass")
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addAnnotation(Override.class)
                .addStatement("return $T.class", element.asType())
                .build();
    }

    /**
     * private static final LinkedHashMap<String, Class<?>> DEPENDENCIES = new LinkedHashMap<String, Class<?>>() {{
     * this.put("service2", AnnotatedService2.class);
     * }};
     */
    private static FieldSpec buildDependencyList(TypeElement element, ProcessingEnvironment processingEnv, InjectionType injectionType) {
        List<? extends Element> enclosed = element.getEnclosedElements();

        switch (injectionType) {
            case FIELD:
                return buildDependencyListForFields(ElementFilter.fieldsIn(enclosed), processingEnv);
            case CONSTRUCTOR:
                return buildDependencyListForConstructor(findNoArgsConstructor(element).get(), processingEnv);
            case NO_DEPENDENCIES:
                return buildDependencyListForNoDependency();
            default:
                return null;
        }
    }

    private static FieldSpec buildDependencyListForNoDependency() {
        return FieldSpec.builder(DEPS_DECLARE_TYPE, "DEPENDENCIES", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new LinkedHashMap<String, Class>()")
                .build();
    }

    private static FieldSpec buildDependencyListForConstructor(Element element, ProcessingEnvironment processingEnv) {
        return null;
    }

    /**
     * private static final LinkedHashMap<String, Class<?>> DEPENDENCIES = new LinkedHashMap<String, Class<?>>() {{
     * this.put("service2", AnnotatedService2.class);
     * }};
     */
    private static FieldSpec buildDependencyListForFields(List<VariableElement> fields, ProcessingEnvironment processingEnv) {
        CodeBlock.Builder depList = CodeBlock.builder().beginControlFlow("");
        fields.forEach($field ->
                depList.addStatement("this.put(\"$N\", $T.class)", $field.getSimpleName().toString(), $field.asType())
        );
        depList.endControlFlow();

        return FieldSpec.builder(DEPS_DECLARE_TYPE, "DEPENDENCIES", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(
                        CodeBlock.builder()
                                .beginControlFlow("new LinkedHashMap<String, Class>()")
                                .add(depList.build())
                                .endControlFlow()
                                .build()
                )
                .build();
    }

    /**
     * @Override public LinkedHashMap<String, Class<?>> dependencies() {
     * return DEPENDENCIES;
     * }
     */
    private static MethodSpec buildGetDependencies() {
        return MethodSpec.methodBuilder("dependencies")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(DEPS_DECLARE_TYPE)
                .addStatement("return DEPENDENCIES")
                .build();
    }

    /**
     * @Override public AnnotatedService1 onResolved(LinkedHashMap<String, Object> resolvedDependencies) throws Exception {
     * Constructor<AnnotatedService1> constructor = getConcreteClass().getConstructor(dependencies().values().toArray(new Class[]{}));
     * return constructor.newInstance(resolvedDependencies.values().toArray());
     * }
     */
    private static MethodSpec buildOnResolved(TypeElement element, InjectionType injectionType) {
        switch (injectionType) {
            case FIELD:
                return buildOnResolvedFromFields(element);
            case CONSTRUCTOR:
                return buildOnResolvedFromConstructor(element);
            case NO_DEPENDENCIES:
                return buildOnResolvedWithNoDependency(element);
            default:
                return null;
        }
    }

    private static MethodSpec buildOnResolvedFromFields(TypeElement element) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onResolved")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(element.asType()))
                .addException(Exception.class)
                .addParameter(DEPS_RETURN_TYPE, "resolvedDependencies")
                .addStatement("$T instance = new $T()", TypeName.get(element.asType()), TypeName.get(element.asType()));

        getInjectingFields(element).forEach(field ->
            builder.addStatement("instance.$N = get(resolvedDependencies, \"$N\")", field.getSimpleName().toString(), field.getSimpleName().toString())
        );

        builder.addStatement("return instance");

        return builder.build();
    }

    private static MethodSpec buildOnResolvedWithNoDependency(TypeElement element) {
        return MethodSpec.methodBuilder("onResolved")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(element.asType()))
                .addException(Exception.class)
                .addParameter(DEPS_RETURN_TYPE, "resolvedDependencies")
                .addStatement("return new $T()", TypeName.get(element.asType()))
                .build();
    }

    private static MethodSpec buildOnResolvedFromConstructor(TypeElement element) {
        return MethodSpec.methodBuilder("onResolved")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(element.asType()))
                .addException(Exception.class)
                .addParameter(DEPS_RETURN_TYPE, "resolvedDependencies")
                .addStatement("$T<$T> constructor = getConcreteClass().getConstructor(dependencies().values().toArray(new Class[]{}))", Constructor.class, TypeName.get(element.asType()))
                .addStatement("return constructor.newInstance(resolvedDependencies.values().toArray())")
                .build();
    }

}
