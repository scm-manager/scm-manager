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

import com.google.common.collect.Streams;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This store should be used only in update steps or other maintenance tasks like deleting all entries for a deleted
 * parent entity.
 *
 * @param <T> The entity type of the store.
 */
public interface QueryableMaintenanceStore<T> {

  Collection<Row<T>> readAll() throws SerializationException;

  <U> Collection<Row<U>> readAllAs(Class<U> type) throws SerializationException;

  Collection<RawRow> readRaw();

  @SuppressWarnings("rawtypes")
  default void writeAll(Iterable<Row> rows) throws SerializationException {
    writeAll(Streams.stream(rows));
  }

  @SuppressWarnings("rawtypes")
  void writeAll(Stream<Row> rows) throws SerializationException;

  default void writeRaw(Iterable<RawRow> rows) {
    writeRaw(Streams.stream(rows));
  }

  void writeRaw(Stream<RawRow> rows);

  @Data
  @XmlAccessorType(XmlAccessType.FIELD)
  @NoArgsConstructor
  @AllArgsConstructor
  class Row<U> {
    private String[] parentIds;
    private String id;
    private U value;
  }

  @Data
  @XmlAccessorType(XmlAccessType.FIELD)
  @NoArgsConstructor
  @AllArgsConstructor
  class RawRow {
    private String[] parentIds;
    private String id;
    private String value;
  }

  /**
   * Deletes all entries from the store. If the store has been created limited to a concrete parent
   * or a subset of parents, only the entries for this parent(s) will be deleted.
   */
  void clear();

  /**
   * Returns an iterator to iterate over all entries in the store. If the store has been created limited to a concrete parent
   * or a subset of parents, only the entries for this parent(s) will be returned.
   * The iterated values offer additional methods to update or delete entries.
   * <br>
   * The iterator must be closed after usage. Otherwise, updates may not be persisted.
   */
  MaintenanceIterator<T> iterateAll();

  /**
   * Iterator for existing entries in the store.
   */
  interface MaintenanceIterator<T> extends Iterator<MaintenanceStoreEntry<T>>, AutoCloseable {
  }

  /**
   * Maintenance helper for a concrete entry in the store.
   */
  interface MaintenanceStoreEntry<T> {

    /**
     * The id of the entry.
     */
    String getId();

    /**
     * Returns the id of the parent for the given class.
     */
    Optional<String> getParentId(Class<?> clazz);

    /**
     * Returns the entity as the specified type of the store.
     *
     * @throws SerializationException if the entry cannot be deserialized to the type of the store.
     */
    T get();

    /**
     * Returns the entry as the given type, not as the type that has been specified for the store.
     * This can be used whenever the type of the store has been changed in a way that no longer is compatible with the
     * stored data. In this case, the entry can be deserialized to a different type that is only used during the
     * migration.
     *
     * @param <U> The type of the entry.
     * @throws SerializationException if the entry cannot be deserialized to the given type.
     */
    <U> U getAs(Class<U> type);

    /**
     * Update the store entry with the given object.
     *
     * @throws SerializationException if the object cannot be serialized.
     */
    void update(Object object);
  }

  class SerializationException extends RuntimeException {
    public SerializationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
