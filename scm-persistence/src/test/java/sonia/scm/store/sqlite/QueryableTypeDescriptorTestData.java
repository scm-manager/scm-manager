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

package sonia.scm.store.sqlite;

import sonia.scm.plugin.QueryableTypeDescriptor;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

public class QueryableTypeDescriptorTestData {
  static QueryableTypeDescriptor createDescriptor(String[] t) {
    return createDescriptor("com.cloudogu.space.to.be.Spaceship", t);
  }

  static QueryableTypeDescriptor createDescriptor(String clazz, String[] t) {
    QueryableTypeDescriptor descriptor = mock(QueryableTypeDescriptor.class);
    lenient().when(descriptor.getTypes()).thenReturn(t);
    lenient().when(descriptor.getClazz()).thenReturn(clazz);
    lenient().when(descriptor.getName()).thenReturn("");
    return descriptor;
  }
}
