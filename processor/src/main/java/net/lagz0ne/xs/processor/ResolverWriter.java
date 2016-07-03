package net.lagz0ne.xs.processor;

import com.squareup.javapoet.*;
import net.lagz0ne.xs.Resolver;

import javax.annotation.processing.ProcessingEnvironment;
import javax.inject.Inject;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ResolverWriter {

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

        TypeSpec helloWorld = TypeSpec.classBuilder(className + "Resolver")
                .addSuperinterface(genericResolver)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(buildDependencyList(element, processingEnv))
                .addMethod(buildGetDependencies())
                .addMethod(buildGetConcreteClass(element))
                .addMethod(buildOnResolved(element))
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, helloWorld)
                .indent("\t")
                .skipJavaLangImports(true)
                .build();

        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Writing random thing");
        javaFile.writeTo(processingEnv.getFiler());
    }

    /**
     *  @Override public Class<AnnotatedService1> buildGetConcreteClass() {
            return AnnotatedService1.class;
        }
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
            this.put("service2", AnnotatedService2.class);
       }};
     */
    private static FieldSpec buildDependencyList(TypeElement element, ProcessingEnvironment processingEnv) {
        List<? extends Element> enclosed = element.getEnclosedElements();

        List<? extends Element> constructors = enclosed.stream().filter($element ->
            $element.getKind() == ElementKind.CONSTRUCTOR &&
            $element.getAnnotation(Inject.class) != null
        ).collect(Collectors.toList());

        if (constructors.size() > 1) {
            throw new RuntimeException("Didn't expect to have 2 @Inject constructors");
        }

        if (constructors.isEmpty()) {
            List<VariableElement> variableElements = ElementFilter.fieldsIn(enclosed);
            return buildDependencyListForFields(variableElements, processingEnv);
        } else {
            return buildDependencyListForConstructor(constructors.get(0), processingEnv);
        }

    }

    private static FieldSpec buildDependencyListForConstructor(Element element, ProcessingEnvironment processingEnv) {
        return null;
    }

    /**
     *
     * private static final LinkedHashMap<String, Class<?>> DEPENDENCIES = new LinkedHashMap<String, Class<?>>() {{
            this.put("service2", AnnotatedService2.class);
        }};
     */
    private static FieldSpec buildDependencyListForFields(List<VariableElement> fields, ProcessingEnvironment processingEnv) {
        if (fields.isEmpty()) {
            return FieldSpec.builder(DEPS_DECLARE_TYPE, "DEPENDENCIES", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("new LinkedHashMap<String, Class>()")
                    .build();
        }

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
            return DEPENDENCIES;
        }
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
     *    @Override public AnnotatedService1 onResolved(LinkedHashMap<String, Object> resolvedDependencies) throws Exception {
            Constructor<AnnotatedService1> constructor = getConcreteClass().getConstructor(dependencies().values().toArray(new Class[]{}));
            return constructor.newInstance(resolvedDependencies.values().toArray());
          }
     */
    private static MethodSpec buildOnResolved(TypeElement element) {
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
