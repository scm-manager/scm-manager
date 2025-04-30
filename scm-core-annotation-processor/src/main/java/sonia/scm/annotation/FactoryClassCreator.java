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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import jakarta.inject.Inject;
import sonia.scm.ModelObject;
import sonia.scm.store.QueryableStoreFactory;

import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;

class FactoryClassCreator {

  private static final String STORE_PACKAGE_NAME = "sonia.scm.store";
  private static final String QUERYABLE_MUTABLE_STORE_CLASS_NAME = "QueryableMutableStore";
  private static final String QUERYABLE_STORE_CLASS_NAME = "QueryableStore";

  private final ProcessingEnvironment processingEnv;

  FactoryClassCreator(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
  }

  void createFactoryClass(Element element, String packageName, TypeElement dataClassTypeElement) throws IOException {
    TypeName typeNameOfDataClass = TypeName.get(dataClassTypeElement.asType());
    TypeSpec.Builder builder =
      TypeSpec
        .classBuilder(element.getSimpleName() + "StoreFactory")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Generated.class).addMember("value", "$S", "sonia.scm.annotation.QueryableTypeAnnotationProcessor").build())
        .addJavadoc("Generated queryable store factory for type {@link $T}.\nTo create conditions in queries, use the static fields in the class {@link $TQueryFields}.\n", typeNameOfDataClass, typeNameOfDataClass)
        .addOriginatingElement(dataClassTypeElement);

    createStoreFactoryField(builder);
    createConstructor(builder);

    List<ParentSpec> parents = determineParentSpecs(dataClassTypeElement);
    if (parents.isEmpty()) {
      createGetterForDataTypeWithoutParent(builder, typeNameOfDataClass);
    } else {
      createOverallGetterForDataTypeWithParents(builder, typeNameOfDataClass);
      createMutableGetterForDataTypeWithParents(typeNameOfDataClass, parents, builder);
      createPartialGetterForDataTypeWithParents(builder, typeNameOfDataClass, parents);
    }

    JavaFile.builder(packageName, builder.build())
      .build()
      .writeTo(processingEnv.getFiler());
  }

  private void createMutableGetterForDataTypeWithParents(TypeName typeNameOfDataClass, List<ParentSpec> parents, TypeSpec.Builder builder) {
    builder.addMethod(
      createGetMutableMethodSpec(
        typeNameOfDataClass,
        "Returns a store to modify elements of the type {@link $T}.\nTo do so, an id has to be specified for each parent type.\n",
        parents,
        ParentSpec::buildParameterNameWithIdSuffix,
        ParentSpec::appendParentAsIdStringArgument
      )
    );

    if (isEveryParentModelObject(parents)) {
      builder.addMethod(
        createGetMutableMethodSpec(
          typeNameOfDataClass,
          "Returns a store to modify elements of the type {@link $T}.\nTo do so, an instance of each parent type has to be specified.\n",
          parents,
          ParentSpec::buildIdGetterWithParameterName,
          ParentSpec::appendParentAsObjectArgument
        )
      );
    }
  }

  private MethodSpec createGetMutableMethodSpec(TypeName typeNameOfDataClass,
                                                String javaDoc,
                                                List<ParentSpec> parents,
                                                Function<ParentSpec, String> parentIdProcessor,
                                                BiConsumer<MethodSpec.Builder, ParentSpec> parentArgumentProcessor) {
    MethodSpec.Builder getMutableBuilder = MethodSpec
      .methodBuilder("getMutable")
      .addModifiers(Modifier.PUBLIC)
      .addJavadoc(javaDoc, typeNameOfDataClass)
      .returns(ParameterizedTypeName.get(ClassName.get(STORE_PACKAGE_NAME, QUERYABLE_MUTABLE_STORE_CLASS_NAME), typeNameOfDataClass))
      .addStatement(
        "return storeFactory.getMutable($T.class, $L)",
        typeNameOfDataClass,
        parents.stream().map(parentIdProcessor).reduce((s1, s2) -> s1 + ", " + s2).orElseThrow()
      );

    parents.forEach(
      parent -> parentArgumentProcessor.accept(getMutableBuilder, parent)
    );

    return getMutableBuilder.build();
  }

  private boolean isEveryParentModelObject(List<ParentSpec> parents) {
    return parents.stream().allMatch(ParentSpec::isModelObject);
  }

  private void createPartialGetterForDataTypeWithParents(TypeSpec.Builder builder, TypeName typeNameOfDataClass, List<ParentSpec> parents) {
    for (int i = 0; i < parents.size(); i++) {
      String javaDocParentDescriptor;
      if (i == 0) {
        javaDocParentDescriptor = "only to the first parent";
      } else if (i < parents.size() - 1) {
        javaDocParentDescriptor = "only to the first " + (i + 1) + "parents";
      } else {
        javaDocParentDescriptor = "to all parents";
      }

      int currentParentLimit = i + 1;
      builder.addMethod(
        createPartialGetterMethodSpec(
          typeNameOfDataClass,
          "Returns a store to query elements of the type {@link $T} limited " + javaDocParentDescriptor + " specified by their ids.\n",
          parents,
          currentParentLimit,
          ParentSpec::buildParameterNameWithIdSuffix,
          ParentSpec::appendParentAsIdStringArgument
        )
      );

      if (isEveryParentModelObject(parents)) {
        builder.addMethod(
          createPartialGetterMethodSpec(
            typeNameOfDataClass,
            "Returns a store to query elements of the type {@link $T} limited " + javaDocParentDescriptor + " specified as instances of the parent type.\n",
            parents,
            currentParentLimit,
            ParentSpec::buildIdGetterWithParameterName,
            ParentSpec::appendParentAsObjectArgument
          )
        );
      }
    }
  }

  private MethodSpec createPartialGetterMethodSpec(TypeName typeNameOfDataClass,
                                                   String javaDoc,
                                                   List<ParentSpec> parents,
                                                   int parentLimit,
                                                   Function<ParentSpec, String> parentIdProcessor,
                                                   BiConsumer<MethodSpec.Builder, ParentSpec> parentArgumentProcessor) {
    MethodSpec.Builder getBuilder = MethodSpec
      .methodBuilder(parentLimit == parents.size() ? "get" : "getOverlapping")
      .addModifiers(Modifier.PUBLIC)
      .returns(ParameterizedTypeName.get(ClassName.get(STORE_PACKAGE_NAME, QUERYABLE_STORE_CLASS_NAME), typeNameOfDataClass))
      .addJavadoc(javaDoc, typeNameOfDataClass)
      .addStatement(
        "return storeFactory.getReadOnly($T.class, $L)",
        typeNameOfDataClass,
        parents.stream()
          .limit(parentLimit)
          .map(parentIdProcessor)
          .reduce((s1, s2) -> s1 + ", " + s2)
          .orElseThrow()
      );

    parents.stream()
      .limit(parentLimit)
      .forEach(parent -> parentArgumentProcessor.accept(getBuilder, parent));

    return getBuilder.build();
  }

  private void createOverallGetterForDataTypeWithParents(TypeSpec.Builder builder, TypeName typeNameOfDataClass) {
    builder.addMethod(
      MethodSpec.methodBuilder("getOverall")
        .addModifiers(Modifier.PUBLIC)
        .returns(ParameterizedTypeName.get(ClassName.get(STORE_PACKAGE_NAME, QUERYABLE_STORE_CLASS_NAME), typeNameOfDataClass))
        .addStatement("return storeFactory.getReadOnly($T.class)", typeNameOfDataClass)
        .addJavadoc("Returns a store to overall query elements of the type {@link $T} independent of any parent.\n", typeNameOfDataClass)
        .build());
  }

  private void createGetterForDataTypeWithoutParent(TypeSpec.Builder builder, TypeName typeNameOfDataClass) {
    builder.addMethod(
      MethodSpec.methodBuilder("get")
        .addModifiers(Modifier.PUBLIC)
        .returns(ParameterizedTypeName.get(ClassName.get(STORE_PACKAGE_NAME, QUERYABLE_STORE_CLASS_NAME), typeNameOfDataClass))
        .addStatement("return storeFactory.getReadOnly($T.class)", typeNameOfDataClass)
        .addJavadoc("Returns a store to query elements of the type {@link $T}.\n", typeNameOfDataClass)
        .build());
    builder.addMethod(
      MethodSpec.methodBuilder("getMutable")
        .addModifiers(Modifier.PUBLIC)
        .returns(ParameterizedTypeName.get(ClassName.get(STORE_PACKAGE_NAME, QUERYABLE_MUTABLE_STORE_CLASS_NAME), typeNameOfDataClass))
        .addStatement("return storeFactory.getMutable($T.class)", typeNameOfDataClass)
        .addJavadoc("Returns a store to modify elements of the type {@link $T}.\n", typeNameOfDataClass)
        .build());
  }

  private List<ParentSpec> determineParentSpecs(TypeElement typeElement) {
    return new QueryableTypeParentProcessor().getQueryableTypeValues(typeElement)
      .stream()
      .map(queryableType -> {
        String parentClassPackage = queryableType.substring(0, queryableType.lastIndexOf("."));
        String parentClassName = queryableType.substring(queryableType.lastIndexOf(".") + 1);
        String parameterName = lowercaseFirstLetter(parentClassName);
        return new ParentSpec(parentClassPackage, parentClassName, parameterName, isParentModelObject(queryableType));
      })
      .toList();
  }

  private boolean isParentModelObject(String parentType) {
    try {
      Class<?> parentClass = Class.forName(parentType);
      return Arrays.stream(parentClass.getInterfaces()).anyMatch(parentInterface -> parentInterface.getName().equals(ModelObject.class.getName()));
    } catch (ClassNotFoundException e) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, String.format("Failed to find class of parent '%s'. Unable to determine whether this is a ModelObject or not. Will not generate factory methods for parent objects, only for ids.", parentType));
      return false;
    }
  }

  private String lowercaseFirstLetter(String parentClassName) {
    return parentClassName.substring(0, 1).toLowerCase(Locale.ENGLISH) + parentClassName.substring(1);
  }

  private void createConstructor(TypeSpec.Builder builder) {
    builder.addMethod(
      MethodSpec
        .constructorBuilder()
        .addParameter(QueryableStoreFactory.class, "storeFactory")
        .addStatement("this.storeFactory = storeFactory")
        .addAnnotation(Inject.class)
        .addJavadoc("Instances should not be created manually, but injected by dependency injection using {@link $T}.\n", Inject.class)
        .build());
  }

  private void createStoreFactoryField(TypeSpec.Builder builder) {
    builder.addField(QueryableStoreFactory.class, "storeFactory", Modifier.PRIVATE, Modifier.FINAL);
  }

  private record ParentSpec(String classPackage, String className, String parameterName, boolean isModelObject) {
    String buildParameterNameWithIdSuffix() {
      return parameterName + "Id";
    }

    String buildIdGetterWithParameterName() {
      return parameterName + ".getId()";
    }

    static void appendParentAsIdStringArgument(MethodSpec.Builder builder, ParentSpec parent) {
      builder.addParameter(String.class, parent.buildParameterNameWithIdSuffix());
    }

    static void appendParentAsObjectArgument(MethodSpec.Builder builder, ParentSpec parent) {
      builder.addParameter(ClassName.get(parent.classPackage(), parent.className()), parent.parameterName());
    }
  }
}
