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
import org.kohsuke.MetaInfServices;
import sonia.scm.store.QueryableType;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@SupportedAnnotationTypes("sonia.scm.store.QueryableType")
@MetaInfServices(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class QueryableTypeAnnotationProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
    if (roundEnvironment.processingOver()) {
      return false;
    }
    // Only process elements that actually have the annotation
    Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(QueryableType.class);
    if (elements.isEmpty()) {
      return false;
    }

    for (TypeElement annotation : annotations) {
      log("Found annotation: " + annotation.getQualifiedName());
      roundEnvironment.getElementsAnnotatedWith(annotation).forEach(element -> {
        log("Found annotated element: " + element.getSimpleName());
        tryToCreateQueryFieldClass(element);
        tryToCreateFactoryClass(element);
      });
    }
    return true;
  }

  @Override
  public Set<String> getSupportedOptions() {
    return Collections.emptySet();
  }

  @SuppressWarnings("UnstableApiUsage")
  private void tryToCreateQueryFieldClass(Element element) {
    TypeElement typeElement = MoreElements.asType(element);
    getPackageName(typeElement)
      .ifPresent(packageName -> {
        try {
          new QueryFieldClassCreator(processingEnv).createQueryFieldClass(element, packageName, typeElement);
        } catch (IOException e) {
          error("Failed to create query field class for type " + typeElement + ": " + e.getMessage());
        }
      });
  }

  @SuppressWarnings("UnstableApiUsage")
  private void tryToCreateFactoryClass(Element element) {
    TypeElement typeElement = MoreElements.asType(element);
    getPackageName(typeElement)
      .ifPresent(packageName -> {
        try {
          new FactoryClassCreator(processingEnv).createFactoryClass(element, packageName, typeElement);
        } catch (IOException e) {
          error("Failed to create factory class for type " + typeElement + ": " + e.getMessage());
        }
      });
  }

  @SuppressWarnings("UnstableApiUsage")
  private Optional<String> getPackageName(TypeElement typeElement) {
    Element enclosingElement = typeElement.getEnclosingElement();
    try {
      return of(MoreElements.asPackage(enclosingElement).getQualifiedName().toString());
    } catch (IllegalArgumentException e) {
      error("Could not determine package name for " + typeElement + ". QueryableType annotation does not support inner classes. Exception: " + e.getMessage());
      return empty();
    }
  }

  private void log(String message) {
    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
  }

  private void error(String message) {
    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
  }
}
