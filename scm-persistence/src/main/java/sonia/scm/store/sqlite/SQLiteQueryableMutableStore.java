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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.plugin.QueryableTypeDescriptor;
import sonia.scm.security.KeyGenerator;
import sonia.scm.store.Condition;
import sonia.scm.store.IdGenerator;
import sonia.scm.store.IdHandlerForStores;
import sonia.scm.store.IdHandlerForStoresForGeneratedId;
import sonia.scm.store.IdHandlerForStoresWithAutoIncrement;
import sonia.scm.store.QueryableMutableStore;
import sonia.scm.store.StoreException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

@Slf4j
class SQLiteQueryableMutableStore<T> extends SQLiteQueryableStore<T> implements QueryableMutableStore<T> {

  private final ObjectMapper objectMapper;

  private final Class<T> clazz;
  private final String[] parentIds;

  private final IdHandlerForStores<T> idHandlerForStores;

  public SQLiteQueryableMutableStore(ObjectMapper objectMapper,
                                     KeyGenerator keyGenerator,
                                     Connection connection,
                                     Class<T> clazz,
                                     QueryableTypeDescriptor queryableTypeDescriptor,
                                     String[] parentIds,
                                     ReadWriteLock lock) {
    super(objectMapper, connection, clazz, queryableTypeDescriptor, parentIds, lock);
    this.objectMapper = objectMapper;
    this.clazz = clazz;
    this.parentIds = parentIds;
    this.idHandlerForStores =
      queryableTypeDescriptor.getIdGenerator() == IdGenerator.AUTO_INCREMENT ?
        new IdHandlerForStoresWithAutoIncrement<>(clazz, this::doPut) :
        new IdHandlerForStoresForGeneratedId<>(clazz, keyGenerator, this::doPut);
  }

  @Override
  public String put(T item) {
    return idHandlerForStores.put(item);
  }

  @Override
  public void put(String id, T item) {
    idHandlerForStores.put(id, item);
  }

  private String doPut(String id, T item) {
    List<String> columnsToInsert = new ArrayList<>(Arrays.asList(parentIds));
    columnsToInsert.add(id);
    columnsToInsert.add(marshal(item));
    SQLInsertStatement sqlInsertStatement =
      new SQLInsertStatement(
        computeFromTable(),
        new SQLValue(columnsToInsert)
      );

    return executeWrite(
      sqlInsertStatement,
      statement -> {
        statement.executeUpdate();
        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
          String generatedKey = generatedKeys.getString(1);
          log.trace("Generated key for item with id {}: {}", id, generatedKey);
          return generatedKey;
        } else {
          return null;
        }
      }
    );
  }

  @Override
  public Map<String, T> getAll() {
    List<SQLField> columns = List.of(
      SQLField.PAYLOAD,
      new SQLField("ID")
    );

    SQLSelectStatement sqlStatementQuery =
      new SQLSelectStatement(
        columns,
        computeFromTable(),
        computeConditionsForAllValues()
      );

    return executeRead(
      sqlStatementQuery,
      statement -> {
        HashMap<String, T> result = new HashMap<>();
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
          result.put(resultSet.getString(2), objectMapper.readValue(resultSet.getString(1), clazz));
        }
        return Collections.unmodifiableMap(result);
      }
    );
  }

  @Override
  public void remove(String id) {
    SQLDeleteStatement sqlStatementQuery =
      new SQLDeleteStatement(
        computeFromTable(),
        computeConditionsFor(id)
      );

    executeWrite(
      sqlStatementQuery,
      statement -> {
        statement.executeUpdate();
        return null;
      }
    );
  }

  @Override
  public T get(String id) {
    SQLSelectStatement sqlStatementQuery =
      new SQLSelectStatement(
        List.of(SQLField.PAYLOAD),
        computeFromTable(),
        computeConditionsFor(id)
      );

    return executeRead(
      sqlStatementQuery,
      statement -> {
        ResultSet resultSet = statement.executeQuery();
        if (!resultSet.next()) {
          return null;
        }
        String json = resultSet.getString(1);
        if (json == null) {
          return null;
        }
        return objectMapper.readValue(json, clazz);
      }
    );
  }

  @Override
  public MutableQuery<T, ?> query(Condition<T>... conditions) {
    return new SQLiteMutableQuery(clazz, conditions);
  }

  private String marshal(T item) {
    try {
      return objectMapper.writeValueAsString(item);
    } catch (JsonProcessingException e) {
      throw new StoreException("Failed to marshal item as json", e);
    }
  }

  private List<SQLNodeWithValue> computeConditionsFor(String id) {
    List<SQLNodeWithValue> conditions = computeConditionsForAllValues();
    conditions.add(new SQLCondition("=", new SQLField("ID"), new SQLValue(id)));
    return conditions;
  }

  private class SQLiteMutableQuery extends SQLiteQuery<T, SQLiteMutableQuery> implements MutableQuery<T, SQLiteMutableQuery>, Cloneable {
    SQLiteMutableQuery(Class<T> type, Condition<T>[] conditions) {
      super(type, conditions);
    }

    @Override
    public void deleteAll() {
      List<SQLNodeWithValue> parentConditions = new ArrayList<>();
      evaluateParentConditions(parentConditions);

      SQLDeleteStatement sqlStatementQuery =
        new SQLDeleteStatement(
          computeFromTable(),
          computeCondition()
        );

      executeWrite(
        sqlStatementQuery,
        statement -> {
          statement.executeUpdate();
          return null;
        }
      );

      log.debug("All entries for {} have been deleted.", SQLiteQueryableMutableStore.this);
    }

    @Override
    public void retain(long n) {

      List<SQLField> columns = new ArrayList<>();
      addParentIdSQLFields(columns);

      List<SQLNodeWithValue> conditions = new ArrayList<>();
      List<SQLNodeWithValue> parentConditions = new ArrayList<>();

      evaluateParentConditions(parentConditions);
      conditions.addAll(parentConditions);
      conditions.addAll(this.computeCondition());

      SQLSelectStatement selectStatement = new SQLSelectStatement(
        columns,
        computeFromTable(),
        conditions,
        getOrderByString(),
        n,
        0L
      );

      SQLiteRetainStatement retainStatement = new SQLiteRetainStatement(
        computeFromTable(),
        columns,
        selectStatement,
        parentConditions
      );

      executeWrite(
        retainStatement,
        statement -> {
          statement.executeUpdate();
          return null;
        }
      );

      log.debug("All entries for {} have been deleted retaining the {} highest ones by ordering.", SQLiteQueryableMutableStore.this, n);
    }

    @Override
    public SQLiteMutableQuery clone() {
      return (SQLiteMutableQuery) super.clone();
    }
  }
}
