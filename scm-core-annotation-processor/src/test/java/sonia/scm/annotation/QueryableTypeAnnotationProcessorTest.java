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

import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.tools.JavaFileObject;
import java.util.List;

@SuppressWarnings("java:S115") // we do not heed enum naming conventions for better readability in the test
class QueryableTypeAnnotationProcessorTest {

  enum FieldScenario {
    A("empty class"),
    B("string query field"),
    C("boolean query fields"),
    D("number query fields"),
    E("enum query field"),
    F("collection query field"),
    G("map query field"),
    H("unknown field"),
    I("instant field mapped to string"),
    K("parent id field"),
    L("unmapped instant field"),
    M("unmapped java util date field"),
    N("static field"),
    O("transient field"),
    BSub("fields from super class");

    private final String description;

    FieldScenario(String description) {
      this.description = description;
    }

    @Override
    public String toString() {
      return description;
    }
  }

  @ParameterizedTest(name = "should test field scenario for {0}")
  @EnumSource(FieldScenario.class)
  void shouldTest(FieldScenario scenario) {
    JavaFileObject someObject = JavaFileObjects.forResource(String.format("sonia/scm/testing/%s.java", scenario.name()));
    Truth.assert_()
      .about(JavaSourcesSubjectFactory.javaSources())
      .that(List.of(someObject))
      .processedWith(new QueryableTypeAnnotationProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(JavaFileObjects.forResource(String.format("sonia/scm/testing/%sQueryFields.java", scenario.name())));
  }

  enum FactoryScenario {
    A("class without parent"),
    OneParent("class with one parent"),
    TwoParents("class with two parents"),
    ThreeParents("class with three parents"),
    OneNonModelObjectParent("class with one model object parent and one non model object parent");

    private final String description;

    FactoryScenario(String description) {
      this.description = description;
    }

    @Override
    public String toString() {
      return description;
    }
  }

  @ParameterizedTest(name = "should test factory scenario for {0}")
  @EnumSource(FactoryScenario.class)
  void shouldTest(FactoryScenario scenario) {
    JavaFileObject someObject = JavaFileObjects.forResource(String.format("sonia/scm/testing/%s.java", scenario.name()));
    Truth.assert_()
      .about(JavaSourcesSubjectFactory.javaSources())
      .that(List.of(someObject))
      .processedWith(new QueryableTypeAnnotationProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(JavaFileObjects.forResource(String.format("sonia/scm/testing/%sStoreFactory.java", scenario.name())));
  }

  @Test
  void shouldHandleInnerClasses() {
    JavaFileObject someObject = JavaFileObjects.forResource("sonia/scm/testing/InnerA.java");
    Truth.assert_()
      .about(JavaSourcesSubjectFactory.javaSources())
      .that(List.of(someObject))
      .processedWith(new QueryableTypeAnnotationProcessor())
      .failsToCompile();
  }
}
