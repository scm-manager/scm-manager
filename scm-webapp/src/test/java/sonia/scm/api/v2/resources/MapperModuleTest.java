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

package sonia.scm.api.v2.resources;

import com.google.inject.binder.AnnotatedBindingBuilder;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MapperModuleTest {

  @Test
  public void shouldBindToClassesWithDefaultConstructorOnly() {
    AnnotatedBindingBuilder binding = mock(AnnotatedBindingBuilder.class);
    ArgumentCaptor<Class> captor = ArgumentCaptor.forClass(Class.class);
    when(binding.to(captor.capture())).thenReturn(null);
    new MapperModule() {
      @Override
      protected <T> AnnotatedBindingBuilder<T> bind(Class<T> clazz) {
        return binding;
      }
    }.configure();
    captor.getAllValues().forEach(this::verifyClassCanBeInstantiated);
  }

  private <T> T verifyClassCanBeInstantiated(Class<T> c) {
    try {
      return c.getConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
