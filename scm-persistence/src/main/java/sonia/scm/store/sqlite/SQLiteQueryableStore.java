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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.plugin.QueryableTypeDescriptor;
import sonia.scm.store.Condition;
import sonia.scm.store.Conditions;
import sonia.scm.store.LeafCondition;
import sonia.scm.store.LogicalCondition;
import sonia.scm.store.QueryableMaintenanceStore;
import sonia.scm.store.QueryableStore;
import sonia.scm.store.StoreException;
import sonia.scm.store.StoreReadOnlyException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static sonia.scm.store.sqlite.SQLiteIdentifiers.computeColumnIdentifier;
import static sonia.scm.store.sqlite.SQLiteIdentifiers.computeTableName;

@Slf4j
class SQLiteQueryableStore<T> implements QueryableStore<T>, QueryableMaintenanceStore<T> {

  public static final String TEMPORARY_UPDATE_TABLE_NAME = "update_tmp";
  private final ObjectMapper objectMapper;
  private final Connection connection;

  private final Class<T> clazz;
  private final QueryableTypeDescriptor queryableTypeDescriptor;
  private final String[] parentIds;

  private final ReadWriteLock lock;
  private final boolean readOnly;

  public SQLiteQueryableStore(ObjectMapper objectMapper,
                              Connection connection,
                              Class<T> clazz,
                              QueryableTypeDescriptor queryableTypeDescriptor,
                              String[] parentIds,
                              ReadWriteLock lock,
                              boolean readOnly) {
    this.objectMapper = objectMapper;
    this.connection = connection;
    this.clazz = clazz;
    this.parentIds = parentIds;
    this.queryableTypeDescriptor = queryableTypeDescriptor;
    this.lock = lock;
    this.readOnly = readOnly;
  }

  @Override
  public Query<T, T, ?> query(Condition<T>... conditions) {
    return new SQLiteQuery<>(clazz, conditions);
  }

  @Override
  public void clear() {
    List<SQLNodeWithValue> parentConditions = new ArrayList<>();
    evaluateParentConditions(parentConditions);

    SQLDeleteStatement sqlStatementQuery =
      new SQLDeleteStatement(
        computeFromTable(),
        parentConditions
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
  public Collection<RawRow> readRaw() {
    return readAllAs(RawRow::new);
  }

  @Override
  public Collection<Row<T>> readAll() {
    return readAllAs(clazz);
  }

  @Override
  public <U> Collection<Row<U>> readAllAs(Class<U> type) {
    return readAllAs((parentIds, id, json) -> new Row<>(parentIds, id, objectMapper.readValue(json, type)));
  }

  private <R> Collection<R> readAllAs(RowBuilder<R> rowBuilder) {
    List<SQLNodeWithValue> parentConditions = new ArrayList<>();
    evaluateParentConditions(parentConditions);
    List<SQLNode> fields = new ArrayList<>();
    addParentIdSQLFields(fields);
    int parentIdsLength = fields.size() - 1; // addParentIdSQLFields has already added the ID field
    fields.add(new SQLField("PAYLOAD"));
    SQLSelectStatement sqlSelectQuery =
      new SQLSelectStatement(
        fields,
        computeFromTable(),
        parentConditions
      );
    return executeRead(
      sqlSelectQuery,
      statement -> {
        List<R> result = new ArrayList<>();
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
          String[] allParentIds = new String[parentIdsLength];
          for (int i = 0; i < parentIdsLength; i++) {
            allParentIds[i] = resultSet.getString(i + 1);
          }
          String id = resultSet.getString(parentIdsLength + 1);
          String json = resultSet.getString(parentIdsLength + 2);
          result.add(rowBuilder.build(allParentIds, id, json));
        }
        return Collections.unmodifiableList(result);
      }
    );
  }

  @Override
  @SuppressWarnings("rawtypes")
  public void writeAll(Stream<Row> rows) {
    writeRaw(rows.map(row -> new RawRow(row.getParentIds(), row.getId(), serialize(row.getValue()))));
  }

  @Override
  public void writeRaw(Stream<RawRow> rows) {
    transactional(
      () -> {
        rows.forEach(row -> {
          List<String> columnsToInsert = new ArrayList<>(Arrays.asList(row.getParentIds()));
          // overwrite parentIds from the export with the parentIds of the current store:
          for (int i = 0; i < parentIds.length; i++) {
            columnsToInsert.set(i, parentIds[i]);
          }
          columnsToInsert.add(row.getId());
          columnsToInsert.add(row.getValue());
          SQLInsertStatement sqlInsertStatement =
            new SQLInsertStatement(
              computeFromTable(),
              new SQLValue(columnsToInsert)
            );

          executeWrite(
            sqlInsertStatement,
            statement -> {
              statement.executeUpdate();
              return null;
            }
          );
        });
        return true;
      }
    );
  }

  @Override
  public MaintenanceIterator<T> iterateAll() {
    List<SQLNode> columns = new LinkedList<>();
    columns.add(new SQLField("payload"));
    addParentIdSQLFields(columns);

    return new TemporaryTableMaintenanceIterator(columns);
  }

  public void transactional(BooleanSupplier callback) {
    log.debug("start transactional operation");
    Lock writeLock = lock.writeLock();
    writeLock.lock();
    try {
      getConnection().setAutoCommit(false);
      boolean commit = callback.getAsBoolean();
      if (commit) {
        log.debug("commit operation");
        getConnection().commit();
      } else {
        log.debug("rollback operation");
        getConnection().rollback();
      }
      log.debug("operation finished");
    } catch (SQLException e) {
      throw new StoreException("failed to disable auto-commit", e);
    } finally {
      writeLock.unlock();
    }
  }

  List<SQLNodeWithValue> computeConditionsForAllValues() {
    List<SQLNodeWithValue> conditions = new ArrayList<>();
    evaluateParentConditions(conditions);
    return conditions;
  }

  SQLTable computeFromTable() {
    return new SQLTable(computeTableName(queryableTypeDescriptor));
  }

  <R> R executeRead(SQLNodeWithValue sqlStatement, StatementCallback<R> callback) {
    String sql = sqlStatement.toSQL();
    log.debug("executing 'read' SQL: {}", sql);
    return executeWithLock(sqlStatement, callback, lock.readLock(), sql);
  }

  <R> R executeWrite(SQLNodeWithValue sqlStatement, StatementCallback<R> callback) {
    assertNotReadOnly();
    String sql = sqlStatement.toSQL();
    log.debug("executing 'write' SQL: {}", sql);
    return executeWithLock(sqlStatement, callback, lock.writeLock(), sql);
  }

  private void assertNotReadOnly() {
    if (readOnly) {
      throw new StoreReadOnlyException(clazz.getName());
    }
  }

  private <R> R executeWithLock(SQLNodeWithValue sqlStatement, StatementCallback<R> callback, Lock writeLock, String sql) {
    writeLock.lock();
    try (PreparedStatement statement = connection.prepareStatement(sql, RETURN_GENERATED_KEYS)) {
      sqlStatement.apply(statement, 1);
      return callback.apply(statement);
    } catch (SQLException | JsonProcessingException e) {
      throw new StoreException("An exception occurred while executing a query on SQLite database: " + sql, e);
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public void close() {
    try {
      log.debug("closing connection");
      connection.close();
    } catch (SQLException e) {
      throw new StoreException("failed to close connection", e);
    }
  }

  Connection getConnection() {
    return connection;
  }

  void evaluateParentConditions(List<SQLNodeWithValue> conditions) {
    for (int i = 0; i < parentIds.length; i++) {
      SQLCondition condition = new SQLCondition("=", new SQLField(computeColumnIdentifier(queryableTypeDescriptor.getTypes()[i])), new SQLValue(parentIds[i]));
      conditions.add(condition);
    }
  }

  void addParentIdSQLFields(List<SQLNode> fields) {
    for (int i = 0; i < queryableTypeDescriptor.getTypes().length; i++) {
      fields.add(new SQLField(computeColumnIdentifier(queryableTypeDescriptor.getTypes()[i])));
    }
    fields.add(new SQLField("ID"));
  }

  private String serialize(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new SerializationException("failed to serialize object to json", e);
    }
  }

  @Override
  public String toString() {
    return format("Store for class %s with parent ids %s", this.clazz.getName(), Arrays.toString(this.parentIds));
  }


  interface StatementCallback<R> {
    R apply(PreparedStatement statement) throws SQLException, JsonProcessingException;
  }

  private interface RowBuilder<R> {
    R build(String[] parentIds, String id, String json) throws JsonProcessingException;
  }

  record OrderBy<T>(QueryField<T, ?> field, OrderOptions options) {
    @Override
    public String toString() {
      if (options == null) {
        return field.getName();
      } else {
        return field.getName() + " " + options;
      }
    }
  }

  /**
   * @param <T_RESULT> "Result" &ndash; result type
   * @param <SELF>     "Self" &ndash; instance type of this query
   */
  @Setter(AccessLevel.PACKAGE)
  @Getter(AccessLevel.PROTECTED)
  class SQLiteQuery<T_RESULT, SELF extends SQLiteQuery<T_RESULT, SELF>> implements Query<T, T_RESULT, SELF>, Cloneable {

    private final Class<T_RESULT> resultType;
    private final Class<T> entityType;
    private final Condition<T> condition;
    private final QueryField<T, ?>[] projection;
    private List<OrderBy<T>> orderBy;
    private boolean distinct = false;

    SQLiteQuery(Class<T_RESULT> resultType, Condition<T>[] conditions) {
      this(resultType, resultType, conjunct(conditions), Collections.emptyList(), null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private SQLiteQuery(Class<T_RESULT> resultType, Class entityType, Condition<T> condition, List<OrderBy<T>> orderBy, QueryField<T, ?>[] projection) {
      this.resultType = resultType;
      this.entityType = entityType;
      this.condition = condition;
      this.orderBy = orderBy;
      this.projection = projection;
    }

    private static <T> Condition<T> conjunct(Condition<T>[] conditions) {
      if (conditions.length == 0) {
        return null;
      } else if (conditions.length == 1) {
        return conditions[0];
      } else {
        return Conditions.and(conditions);
      }
    }

    @Override
    public Optional<T_RESULT> findFirst() {
      return findAll(0, 1).stream().findFirst();
    }

    @Override
    public Optional<T_RESULT> findOne() {
      List<T_RESULT> all = findAll(0, 2);
      if (all.size() > 1) {
        throw new TooManyResultsException();
      } else if (all.size() == 1) {
        return Optional.of(all.get(0));
      } else {
        return Optional.empty();
      }
    }

    @Override
    public void forEach(Consumer<T_RESULT> consumer, long offset, long limit) {
      String orderByString = getOrderByString();

      SQLSelectStatement sqlSelectQuery =
        new SQLSelectStatement(
          computeFields(),
          computeFromTable(),
          computeCondition(),
          orderByString,
          limit,
          offset,
          distinct
        );

      executeRead(
        sqlSelectQuery,
        statement -> {
          try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
              consumer.accept(extractResult(resultSet));
            }
          }
          return null;
        }
      );
    }

    @Override
    public Query<T, T_RESULT, ?> distinct() {
      this.distinct = true;
      return this;
    }

    @Override
    public Query<T, Object[], ?> project(QueryField<T, ?>... fields) {
      return new SQLiteQuery<>(Object[].class, resultType, condition, orderBy, fields);
    }

    String getOrderByString() {
      StringBuilder orderByBuilder = new StringBuilder();
      if (orderBy != null && !orderBy.isEmpty()) {
        toOrderBySQL(orderByBuilder);
      }
      return orderByBuilder.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Query<T, Result<T_RESULT>, ?> withIds() {
      return new SQLiteQuery<>((Class<Result<T_RESULT>>) (Class<?>) Result.class, resultType, condition, orderBy, null);
    }

    @Override
    public long count() {
      SQLSelectStatement sqlStatementQuery =
        new SQLSelectStatement(
          List.of(new SQLCountField(computeFields(), distinct)),
          computeFromTable(),
          computeCondition()
        );

      return executeRead(
        sqlStatementQuery,
        statement -> {
          ResultSet resultSet = statement.executeQuery();
          if (resultSet.next()) {
            return resultSet.getLong(1);
          }
          throw new IllegalStateException("failed to read count for type " + queryableTypeDescriptor);
        }
      );
    }

    @Override
    public <A> A min(AggregatableQueryField<T, A> field) {
      return aggregate(field, "MIN", field.getFieldType());
    }

    @Override
    public <A> A max(AggregatableQueryField<T, A> field) {
      return aggregate(field, "MAX", field.getFieldType());
    }

    @Override
    public <A> A sum(AggregatableNumberQueryField<T, A> field) {
      return aggregate(field, "SUM", field.getFieldType());
    }

    @Override
    public <A> Double average(AggregatableNumberQueryField<T, A> field) {
      return aggregate(field, "AVG", Double.class);
    }

    private <A> A aggregate(AggregatableQueryField<T, ?> field, String aggregate, Class<A> resultType) {
      SQLSelectStatement sqlStatementQuery =
        new SQLSelectStatement(
          List.of(new SQLAggregate(aggregate, field)),
          computeFromTable(),
          computeCondition()
        );

      return executeRead(
        sqlStatementQuery,
        statement -> {
          ResultSet resultSet = statement.executeQuery();
          if (resultSet.next()) {
            if (resultSet.getObject(1) == null) {
              return null;
            }
            return resultSet.getObject(1, resultType);
          }
          throw new IllegalStateException("failed to read count for type " + queryableTypeDescriptor);
        }
      );
    }

    @Override
    public SELF orderBy(QueryField<T, ?> field, OrderOptions options) {
      List<OrderBy<T>> extendedOrderBy = new ArrayList<>(this.orderBy);
      extendedOrderBy.add(new OrderBy<>(field, options));
      SELF newOrderBy = (SELF) this.clone();
      newOrderBy.setOrderBy(extendedOrderBy);
      return newOrderBy;
    }

    private List<SQLNode> computeFields() {
      if (projection != null && projection.length > 0) {
        return computeProjectedFields();
      }
      return computeDefaultFields();
    }

    private List<SQLNode> computeDefaultFields() {
      List<SQLNode> fields = new ArrayList<>();
      fields.add(SQLField.PAYLOAD);
      if (resultType.isAssignableFrom(Result.class)) {
        addParentIdSQLFields(fields);
      }
      return fields;
    }

    private List<SQLNode> computeProjectedFields() {
      return Arrays.stream(projection)
        .map(SQLFieldHelper::computeSQLField)
        .map(SQLField::new)
        .map(field -> (SQLNode) field)
        .toList();
    }

    List<SQLNodeWithValue> computeCondition() {
      List<SQLNodeWithValue> conditions = new ArrayList<>();

      evaluateParentConditions(conditions);

      if (condition != null) {
        if (condition instanceof LeafCondition<T, ?> leafCondition) {
          conditions.add(SQLConditionMapper.mapToSQLCondition(leafCondition));
        }
        if (condition instanceof LogicalCondition<T> logicalCondition) {
          conditions.add(SQLConditionMapper.mapToSQLLogicalCondition(logicalCondition));
        }
        log.debug("Unsupported condition type: {}", condition.getClass().getName());
      }

      return conditions;
    }

    private void toOrderBySQL(StringBuilder orderByBuilder) {
      Iterator<OrderBy<T>> it = orderBy.iterator();
      while (it.hasNext()) {
        OrderBy<T> order = it.next();
        if (order.options.isNumerical()) {
          orderByBuilder.append("CAST(");
        }
        if (order.field instanceof IdQueryField) {
          orderByBuilder.append("ID ");
        } else {
          orderByBuilder.append("json_extract(payload, '$.").append(order.field.getName()).append("') ");
        }
        if (order.options.isNumerical()) {
          orderByBuilder.append("AS REAL) ");
        }
        orderByBuilder.append(order.options.getOrder().name());
        if (it.hasNext()) {
          orderByBuilder.append(", ");
        }
      }
    }

    @SuppressWarnings("unchecked")
    private T_RESULT extractResult(ResultSet resultSet) throws JsonProcessingException, SQLException {
      if (projection != null && projection.length > 0) {
        Object[] values = new Object[projection.length];
        for (int i = 0; i < projection.length; i++) {
          QueryField<T, ?> field = projection[i];
          if (field instanceof QueryableStore.CollectionSizeQueryField<?>) {
            values[i] = resultSet.getInt(i + 1);
          } else if (field instanceof QueryableStore.MapSizeQueryField<?>) {
            values[i] = resultSet.getInt(i + 1);
          } else if (field.isIdField()) {
            values[i] = resultSet.getString(i + 1);
          } else {
            values[i] = resultSet.getObject(i + 1);
          }
        }
        return (T_RESULT) values;
      }
      T entity = objectMapper.readValue(resultSet.getString(1), entityType);
      if (resultType.isAssignableFrom(Result.class)) {
        Map<String, String> parentIdMapping = new HashMap<>(queryableTypeDescriptor.getTypes().length);
        for (int i = 0; i < queryableTypeDescriptor.getTypes().length; i++) {
          parentIdMapping.put(computeColumnIdentifier(queryableTypeDescriptor.getTypes()[i]), resultSet.getString(i + 2));
        }
        String id = resultSet.getString(queryableTypeDescriptor.getTypes().length + 2);
        return (T_RESULT) new Result<T>() {
          @Override
          public Optional<String> getParentId(Class<?> clazz) {
            String parentClassName = computeColumnIdentifier(clazz.getName());
            return Optional.ofNullable(parentIdMapping.get(parentClassName));
          }

          @Override
          public String getId() {
            return id;
          }

          @Override
          public T getEntity() {
            return entity;
          }
        };
      } else {
        return (T_RESULT) entity;
      }
    }

    /* We explicitly suppress this warning since it's based on a generic whose information is lost during runtime,
    which can be conveniently circumvented with clone().
     */
    @SuppressWarnings("java:S2975")
    @Override
    public SQLiteQuery<T_RESULT, SELF> clone() {
      try {
        // Keep in mind that this clone shares the mutable entities with its origin.
        return (SQLiteQuery<T_RESULT, SELF>) super.clone();
      } catch (CloneNotSupportedException e) {
        throw new AssertionError();
      }
    }
  }

  private class TemporaryTableMaintenanceIterator implements MaintenanceIterator<T> {
    private final PreparedStatement iterateStatement;
    private final List<SQLNode> columns;
    private final ResultSet resultSet;
    private Boolean hasNext;

    public TemporaryTableMaintenanceIterator(List<SQLNode> columns) {
      this.columns = columns;
      this.hasNext = null;
      SQLSelectStatement iterateQuery =
        new SQLSelectStatement(
          columns,
          computeFromTable(),
          computeConditionsForAllValues()
        );
      String sql = iterateQuery.toSQL();
      log.debug("iterating SQL: {}", sql);
      try {
        iterateStatement = connection.prepareStatement(sql);
        iterateQuery.apply(iterateStatement, 1);
        resultSet = iterateStatement.executeQuery();
      } catch (SQLException e) {
        throw new StoreException("Failed to iterate: " + sql, e);
      }

      createTemporaryTable();
    }

    private void createTemporaryTable() {
      dropTemporaryTable();
      StringBuilder tmpTableStatement = new StringBuilder("create table if not exists ").append(TEMPORARY_UPDATE_TABLE_NAME).append(" (");
      for (int i = 0; i < queryableTypeDescriptor.getTypes().length; i++) {
        tmpTableStatement.append(computeColumnIdentifier(queryableTypeDescriptor.getTypes()[i])).append(" TEXT NOT NULL, ");
      }
      tmpTableStatement.append("ID TEXT NOT NULL, payload JSONB)");
      try (Statement statement = connection.createStatement()) {
        String createTableSql = tmpTableStatement.toString();
        log.debug("creating table: {}", createTableSql);
        statement.execute(createTableSql);
      } catch (SQLException e) {
        throw new StoreException("Failed to create temporary table: " + tmpTableStatement, e);
      }
    }

    private void dropTemporaryTable() {
      String sql = "DROP TABLE IF EXISTS " + TEMPORARY_UPDATE_TABLE_NAME;
      try (Statement statement = connection.createStatement()) {
        log.trace("dropping table: {}", sql);
        statement.executeUpdate(sql);
      } catch (SQLException e) {
        throw new StoreException("Failed to drop temporary table: " + sql, e);
      }
    }

    @Override
    public boolean hasNext() {
      if (hasNext != null) {
        return hasNext;
      }
      try {
        hasNext = resultSet.next();
        return hasNext;
      } catch (SQLException e) {
        throw new StoreException("Failed to get next row from result set", e);
      }
    }

    @Override
    public MaintenanceStoreEntry<T> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      hasNext = null;
      return new InnerStoreEntry();
    }

    @Override
    public void remove() {
      List<SQLNodeWithValue> parentConditions = new ArrayList<>();
      try {
        for (int i = 0; i < queryableTypeDescriptor.getTypes().length; i++) {
          String columnName = computeColumnIdentifier(queryableTypeDescriptor.getTypes()[i]);
          parentConditions.add(new SQLCondition("=", new SQLField(columnName), new SQLValue(resultSet.getString(columnName))));
        }
        parentConditions.add(new SQLCondition("=", new SQLField("ID"), new SQLValue(resultSet.getString("ID"))));
      } catch (SQLException e) {
        throw new StoreException("Failed to delete item from table", e);
      }
      SQLDeleteStatement sqlStatementQuery =
        new SQLDeleteStatement(
          computeFromTable(),
          parentConditions
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
    public void close() throws Exception {
      iterateStatement.close();

      SQLSelectStatement tmpIterateQuery =
        new SQLSelectStatement(
          columns,
          new SQLTable(TEMPORARY_UPDATE_TABLE_NAME),
          computeConditionsForAllValues()
        );
      String sql = tmpIterateQuery.toSQL();
      log.debug("iterating temporary table: {}", sql);
      iterateStatement.close();
      try (PreparedStatement tmpIterateStatement = connection.prepareStatement(sql)) {
        tmpIterateQuery.apply(tmpIterateStatement, 1);
        ResultSet tmpResultSet = tmpIterateStatement.executeQuery();
        while (tmpResultSet.next()) {
          Collection<String> allParentIds = computeAllParentIds(tmpResultSet);
          writeJsonInTable(
            computeFromTable(),
            allParentIds,
            tmpResultSet.getString(queryableTypeDescriptor.getTypes().length + 2),
            tmpResultSet.getString(1)
          );
        }
      } catch (SQLException e) {
        throw new StoreException("Failed to transfer entries from temporary table", e);
      }
      dropTemporaryTable();
    }

    private List<String> computeAllParentIds(ResultSet tmpResultSet) throws SQLException {
      List<String> allParentIds = new ArrayList<>();
      for (int columnNr = 0; columnNr < queryableTypeDescriptor.getTypes().length; ++columnNr) {
        allParentIds.add(tmpResultSet.getString(columnNr + 2));
      }
      return allParentIds;
    }

    private void writeJsonInTable(SQLTable table, Collection<String> allParentIds, String id, String json) {
      List<String> columnsToInsert = new ArrayList<>(allParentIds);
      columnsToInsert.add(id);
      columnsToInsert.add(json);
      SQLInsertStatement sqlInsertStatement =
        new SQLInsertStatement(
          table,
          new SQLValue(columnsToInsert)
        );

      executeWrite(
        sqlInsertStatement,
        statement -> {
          statement.executeUpdate();
          return null;
        }
      );
    }

    private class InnerStoreEntry implements MaintenanceStoreEntry<T> {

      private final Map<String, String> parentIds = new LinkedHashMap<>();
      private final String id;
      private final String json;

      InnerStoreEntry() {
        try {
          json = resultSet.getString(1);
          for (int i = 0; i < queryableTypeDescriptor.getTypes().length; i++) {
            parentIds.put(computeColumnIdentifier(queryableTypeDescriptor.getTypes()[i]), resultSet.getString(i + 2));
          }
          id = resultSet.getString(queryableTypeDescriptor.getTypes().length + 2);
        } catch (SQLException e) {
          throw new StoreException("Failed to read next entry for maintenance", e);
        }
      }

      @Override
      public String getId() {
        return id;
      }

      @Override
      public Optional<String> getParentId(Class<?> clazz) {
        String parentClassName = computeColumnIdentifier(clazz.getName());
        return Optional.ofNullable(parentIds.get(parentClassName));
      }

      @Override
      public T get() {
        return getAs(clazz);
      }

      @Override
      public <U> U getAs(Class<U> type) {
        try {
          return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
          throw new SerializationException("failed to read object from json", e);
        }
      }

      void updateJson(String json) {
        SQLTable table = new SQLTable(TEMPORARY_UPDATE_TABLE_NAME);
        writeJsonInTable(table, parentIds.values(), id, json);
      }

      @Override
      public void update(Object object) {
        updateJson(serialize(object));
      }
    }
  }
}
