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

package sonia.scm.update;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class MapBasedPropertyReaderInstance implements V1PropertyReader.Instance {
  private final Stream<Map.Entry<String, V1Properties>> all;

  public MapBasedPropertyReaderInstance(Map<String, V1Properties> all) {
    this(all.entrySet().stream());
  }

  private MapBasedPropertyReaderInstance(Stream<Map.Entry<String, V1Properties>> all) {
    this.all = all;
  }

  @Override
  public void forEachEntry(BiConsumer<String, V1Properties> propertiesForNameConsumer) {
    all.forEach(e -> call(e.getKey(), e.getValue(), propertiesForNameConsumer));
  }

  @Override
  public V1PropertyReader.Instance havingAnyOf(String... keys) {
    return new MapBasedPropertyReaderInstance(all.filter(e -> e.getValue().hasAny(keys)));
  }

  @Override
  public V1PropertyReader.Instance havingAllOf(String... keys) {
    return new MapBasedPropertyReaderInstance(all.filter(e -> e.getValue().hasAll(keys)));
  }

  private void call(String repositoryId, V1Properties properties, BiConsumer<String, V1Properties> propertiesForNameConsumer) {
    propertiesForNameConsumer.accept(repositoryId, properties);
  }
}
