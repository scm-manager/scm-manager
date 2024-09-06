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

package sonia.scm.store;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.Optional;
import java.util.Set;

/**
 * The fields of the {@link TypedStoreParameters} are used from the {@link ConfigurationStoreFactory},
 * {@link ConfigurationEntryStoreFactory} and {@link DataStoreFactory} to create a type safe store.
 *
 * @author Mohamed Karray
 * @since 2.0.0
 */
public interface TypedStoreParameters<T> extends StoreParameters {

  Class<T> getType();

  Optional<ClassLoader> getClassLoader();

  @SuppressWarnings("java:S1452") // we could not provide generic type, because we don't know it here
  Set<XmlAdapter<?,?>> getAdapters();

}
