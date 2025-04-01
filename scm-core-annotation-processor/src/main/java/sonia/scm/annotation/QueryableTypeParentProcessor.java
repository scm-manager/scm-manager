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

import sonia.scm.store.QueryableType;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class QueryableTypeParentProcessor {
  @SuppressWarnings("unchecked")
  public List<String> getQueryableTypeValues(TypeElement typeElement) {
    return new AnnotationProcessor().findAnnotation(typeElement, QueryableType.class)
      .map(annotationMirror -> {
        Optional<? extends AnnotationValue> value = new AnnotationHelper().findAnnotationValue(annotationMirror, "value");
        if (value.isEmpty()) {
          return new ArrayList<String>();
        }
        List<AnnotationValue> parentClassTypes = (List<AnnotationValue>) value.orElseThrow().getValue();
        return parentClassTypes.stream()
          .map(AnnotationValue::getValue)
          .map(Object::toString)
          .toList();
      })
      .orElseGet(List::of);
  }
}
