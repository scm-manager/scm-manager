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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import java.util.Map;
import java.util.Optional;

class AnnotationHelper {

  public Optional<? extends AnnotationValue> findAnnotationValue(AnnotationMirror annotationMirror, String name) {
    return annotationMirror.getElementValues()
      .entrySet()
      .stream()
      .filter(entry -> entry.getKey().getSimpleName().toString().equals(name))
      .map(Map.Entry::getValue)
      .findFirst();
  }
}
