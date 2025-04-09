/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.annotation;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

class QueryFieldClassCreator {

  private static final String SIZE_SUFFIX = "SIZE";
  private static final String STORE_PACKAGE_NAME = "sonia.scm.store";
  private static final String QUERYABLE_STORE_CLASS_NAME = "QueryableStore";
  private static final String ID_QUERY_FIELD_CLASS_NAME = "IdQueryField";

  private static final FieldInitializer SIMPLE_INITIALIZER = (fieldBuilder, element, fieldClass, fieldName) -> fieldBuilder
    .initializer(
      "new $T<>($S)",
      ClassName.get(STORE_PACKAGE_NAME, QUERYABLE_STORE_CLASS_NAME).nestedClass(fieldClass),
      fieldName
    );

  private final ProcessingEnvironment processingEnv;

  QueryFieldClassCreator(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
  }

  void createQueryFieldClass(Element element, String packageName, TypeElement typeElement) throws IOException {
    TypeSpec.Builder builder =
      TypeSpec
        .classBuilder(element.getSimpleName() + "QueryFields")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addJavadoc("Generated query fields for type {@link $T}.\nTo create a queryable store for this, use an injected instance of the {@link $TStoreFactory}.\n", TypeName.get(typeElement.asType()), TypeName.get(typeElement.asType()));

    createPrivateConstructor(builder);
    processParents(typeElement, builder);
    processId(typeElement, builder);
    processFields(typeElement, builder);

    JavaFile.builder(packageName, builder.build())
      .build()
      .writeTo(processingEnv.getFiler());
  }

  private void createPrivateConstructor(TypeSpec.Builder builder) {
    builder.addMethod(
      MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PRIVATE)
        .build());
  }

  private void processParents(TypeElement typeElement, TypeSpec.Builder builder) {
    new QueryableTypeParentProcessor().getQueryableTypeValues(typeElement)
      .forEach(queryableType -> {
        String parentClassPackage = queryableType.substring(0, queryableType.lastIndexOf("."));
        String parentClassName = queryableType.substring(queryableType.lastIndexOf(".") + 1);
        builder.addField(
          FieldSpec
            .builder(
              ParameterizedTypeName.get(
                ClassName.get(STORE_PACKAGE_NAME, QUERYABLE_STORE_CLASS_NAME)
                  .nestedClass(ID_QUERY_FIELD_CLASS_NAME),
                TypeName.get(typeElement.asType())),
              parentClassName.toUpperCase(Locale.ENGLISH) + "_ID"
            )
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer(
              "new $T<>($T.class)",
              ClassName.get(STORE_PACKAGE_NAME, QUERYABLE_STORE_CLASS_NAME).nestedClass(ID_QUERY_FIELD_CLASS_NAME),
              ClassName.get(parentClassPackage, parentClassName))
            .build());
      });
  }

  private void processId(TypeElement typeElement, TypeSpec.Builder builder) {
    builder.addField(
      FieldSpec
        .builder(
          ParameterizedTypeName.get(
            ClassName.get(STORE_PACKAGE_NAME, QUERYABLE_STORE_CLASS_NAME)
              .nestedClass(ID_QUERY_FIELD_CLASS_NAME),
            TypeName.get(typeElement.asType())),
          "INTERNAL_ID"
        )
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .initializer(
          "new $T<>()",
          ClassName.get(STORE_PACKAGE_NAME, QUERYABLE_STORE_CLASS_NAME).nestedClass(ID_QUERY_FIELD_CLASS_NAME))
        .build());
  }

  private void processFields(TypeElement typeElement, TypeSpec.Builder builder) {
    processFields(typeElement, typeElement, builder);
  }

  @SuppressWarnings("UnstableApiUsage")
  private void processFields(TypeElement typeElement, TypeElement superTypeElement, TypeSpec.Builder builder) {
    processingEnv
      .getElementUtils()
      .getAllMembers(typeElement)
      .stream()
      .filter(member -> member.getKind() == ElementKind.FIELD)
      .filter(member -> !member.getModifiers().contains(Modifier.STATIC))
      .filter(member -> !member.getModifiers().contains(Modifier.TRANSIENT))
      .flatMap(field ->
        createFieldSpec(
          superTypeElement,
          MoreElements.asVariable(field))
      )
      .forEach(builder::addField);
    TypeElement superclass = (TypeElement) processingEnv.getTypeUtils().asElement(typeElement.getSuperclass());
    if (superclass != null && !superclass.getQualifiedName().toString().equals(Object.class.getCanonicalName())) {
      processFields(superclass, typeElement, builder);
    }
  }

  private Stream<FieldSpec> createFieldSpec(TypeElement element, VariableElement field) {
    TypeMirror effectiveFieldType = determineFieldType(field);
    return createFieldHandler(effectiveFieldType).stream()
      .map(queryFieldHandler -> {
        String fieldName = field.getSimpleName().toString();
        String fieldClass = queryFieldHandler.getClazz();
        TypeName[] furtherGenerics = queryFieldHandler.getGenerics();
        TypeName[] generics = new TypeName[furtherGenerics.length + 1];
        generics[0] = TypeName.get(element.asType());
        System.arraycopy(furtherGenerics, 0, generics, 1, furtherGenerics.length);
        FieldSpec.Builder fieldBuilder = FieldSpec
          .builder(
            ParameterizedTypeName.get(
              ClassName
                .get(STORE_PACKAGE_NAME, QUERYABLE_STORE_CLASS_NAME)
                .nestedClass(fieldClass),
              generics),
            determineFieldNameWithSuffix(fieldName, queryFieldHandler).toUpperCase(Locale.ENGLISH)
          )
          .addJavadoc("Generated query field to create conditions for field {@link $L#$L} of type {@link $L}.\n", TypeName.get(element.asType()), fieldName, effectiveFieldType)
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        queryFieldHandler.getInitializer().initialize(fieldBuilder, element, fieldClass, fieldName);
        return fieldBuilder.build();
      });
  }

  private TypeMirror determineFieldType(VariableElement field) {
    return new AnnotationProcessor().findAnnotation(field, XmlJavaTypeAdapter.class)
      .map(this::determineTypeFromAdapter)
      .orElseGet(field::asType);
  }

  private TypeMirror determineTypeFromAdapter(AnnotationMirror annotationMirror) {
    AnnotationValue value = new AnnotationHelper().findAnnotationValue(annotationMirror, "value").orElseThrow();
    TypeMirror adapterType = (TypeMirror) value.getValue();
    TypeMirror xmlAdapterType = processingEnv.getTypeUtils()
      .directSupertypes(adapterType)
      .stream()
      .filter(typeMirror -> processingEnv.getTypeUtils()
        .isAssignable(
          processingEnv.getTypeUtils().erasure(typeMirror),
          processingEnv.getElementUtils().getTypeElement(XmlAdapter.class.getCanonicalName()).asType()
        ))
      .findFirst()
      .orElseThrow(RuntimeException::new);
    DeclaredType declaredType = MoreTypes.asDeclared(xmlAdapterType);
    return declaredType.getTypeArguments().get(0);
  }

  private Collection<QueryFieldHandler> createFieldHandler(TypeMirror fieldType) {
    TypeMirror collectionType = processingEnv.getElementUtils().getTypeElement(Collection.class.getCanonicalName()).asType();
    TypeMirror erasure = processingEnv.getTypeUtils().erasure(fieldType);
    if (processingEnv.getTypeUtils().isAssignable(erasure, collectionType)) {
      return List.of(
        new QueryFieldHandler(
          "CollectionQueryField",
          new TypeName[]{},
          SIMPLE_INITIALIZER
        ),
        new QueryFieldHandler(
          "CollectionSizeQueryField",
          new TypeName[]{},
          SIMPLE_INITIALIZER,
          SIZE_SUFFIX
        )
      );
    }
    TypeMirror mapType = processingEnv.getElementUtils().getTypeElement(Map.class.getCanonicalName()).asType();
    if (processingEnv.getTypeUtils().isAssignable(erasure, mapType)) {
      return List.of(
        new QueryFieldHandler(
          "MapQueryField",
          new TypeName[]{},
          SIMPLE_INITIALIZER
        ),
        new QueryFieldHandler(
          "MapSizeQueryField",
          new TypeName[]{},
          SIMPLE_INITIALIZER,
          SIZE_SUFFIX
        )
      );
    }
    Element fieldAsElement = processingEnv.getTypeUtils().asElement(fieldType);
    if (fieldAsElement != null && fieldAsElement.getKind() == ElementKind.ENUM) {
      return List.of(new QueryFieldHandler(
        "EnumQueryField",
        new TypeName[]{TypeName.get(fieldAsElement.asType())},
        (fieldBuilder, element, fieldClass, fieldName) -> fieldBuilder
          .initializer(
            "new $T<>($S)",
            ClassName.get(STORE_PACKAGE_NAME, QUERYABLE_STORE_CLASS_NAME).nestedClass(fieldClass),
            fieldName
          )
      ));
    }
    return switch (fieldType.toString()) {
      case "java.lang.String" -> List.of(
        new QueryFieldHandler(
          "StringQueryField",
          new TypeName[]{},
          SIMPLE_INITIALIZER));
      case "boolean", "java.lang.Boolean" -> List.of(
        new QueryFieldHandler(
          "BooleanQueryField",
          new TypeName[]{},
          SIMPLE_INITIALIZER));
      case "int", "java.lang.Integer" -> List.of(
        new NumberQueryFieldHandler(
          "Integer"));
      case "long", "java.lang.Long" -> List.of(
        new NumberQueryFieldHandler(
          "Long"));
      case "float", "java.lang.Float" -> List.of(
        new NumberQueryFieldHandler(
          "Float"));
      case "double", "java.lang.Double" -> List.of(
        new NumberQueryFieldHandler(
          "Double"));
      case "java.util.Date", "java.time.Instant" -> List.of(
        new QueryFieldHandler(
          "InstantQueryField",
          new TypeName[]{},
          SIMPLE_INITIALIZER));
      default -> List.of();
    };
  }

  private String determineFieldNameWithSuffix(String fieldName, QueryFieldHandler fieldHandler) {
    return fieldHandler.getSuffix()
      .map(suffix -> String.format("%s_%s", fieldName, suffix))
      .orElse(fieldName);
  }
}
