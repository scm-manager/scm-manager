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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This interface is used to query objects annotated with {@link QueryableType}. It will be created by the
 * {@link QueryableStoreFactory}.
 * <br/>
 * It is not meant to be instantiated by users of the API. Instead, use the query factory created by the annotation
 * processor for the annotated type.
 *
 * @param <T> The type of the objects to query.
 * @since 3.8.0
 */
public interface QueryableStore<T> extends AutoCloseable {

  /**
   * Creates a query for the objects of the type {@code T} with the given conditions. Conditions should be created by
   * either using the static methods of the {@link Conditions} class or by using the query fields of the type {@code T}
   * that will be created by the annotation processor in a separate class. If your annotated type is named
   * {@code MyType}, the query fields class will be named {@code MyTypeQueryFields}.
   * <br/>
   * If no conditions are given, all objects of the type {@code T} will be returned (limited by the ids of the
   * parent objects that had been specified when this instance of the store had been created by the factory). If more
   * than one condition is given, the conditions will be combined with a logical AND.
   *
   * @param conditions The conditions to filter the objects.
   * @return The query object to retrieve the result.
   */
  Query<T, T> query(Condition<T>... conditions);

  /**
   * Used to specify the order of the result of a query.
   */
  enum Order {
    /**
     * Ascending order.
     */
    ASC,
    /**
     * Descending order.
     */
    DESC
  }

  /**
   * The terminal interface for a query build by {@link #query(Condition[])}. It provides methods to retrieve the
   * result of the query in different forms.
   *
   * @param <T>        The type of the objects to query.
   * @param <T_RESULT> The type of the result objects (if a projection had been made, for example using
   *                   {@link #withIds()}).
   */
  interface Query<T, T_RESULT> {

    /**
     * Returns the first found object, if the query returns at least one result.
     * If the query returns no result, an empty optional will be returned.
     */
    Optional<T_RESULT> findFirst();

    /**
     * Returns the found object, if the query returns one exactly one result. When the query returns more than one
     * result, a {@link TooManyResultsException} will be thrown. If the query returns no result, an empty optional will be returned.
     */
    Optional<T_RESULT> findOne() throws TooManyResultsException;

    /**
     * Returns all objects that match the query. If the query returns no result, an empty list will be returned.
     */
    default List<T_RESULT> findAll() {
      return findAll(0, Integer.MAX_VALUE);
    }

    /**
     * Returns a subset of all objects that match the query. If the query returns no result or the {@code offset} and
     * {@code limit} are set in a way, that the result is exceeded, an empty list will be returned.
     *
     * @param offset The offset to start the result list.
     * @param limit  The maximum number of results to return.
     */
    List<T_RESULT> findAll(long offset, long limit);

    /**
     * Calls the given consumer for all objects that match the query.
     *
     * @param consumer The consumer that will be called for each single found object.
     */
    default void forEach(Consumer<T_RESULT> consumer) {
      forEach(consumer, 0, Integer.MAX_VALUE);
    }

    /**
     * Calls the given consumer for a subset of all objects that match the query.
     *
     * @param consumer The consumer that will be called for each single found object.
     * @param offset The offset to start feeding results to the consumer.
     * @param limit  The maximum number of results.
     */
    void forEach(Consumer<T_RESULT> consumer, long offset, long limit);

    /**
     * Returns the found objects in combination with the parent ids they belong to. This is useful if you are using a
     * queryable store that is not scoped to specific parent objects, and you therefore want to know to which parent
     * objects each of the found objects belong to.
     *
     * @return The query object to continue building the query.
     */
    Query<T, Result<T_RESULT>> withIds();

    /**
     * Orders the result by the given field in the given order. If the order is not set, the order of the result is not
     * specified. Orders can be chained, so you can call this method multiple times to order by multiple fields.
     *
     * @param field The field to order by.
     * @param order The order to use (either ascending or descending).
     * @return The query object to continue building the query.
     */
    Query<T, T_RESULT> orderBy(QueryField<T, ?> field, Order order);

    /**
     * Returns the count of all objects that match the query.
     */
    long count();

    /**
     * Returns the minimum value of the given field that match the query.
     */
    <A> A min(AggregatableQueryField<T, A> field);

    /**
     * Returns the maximum value of the given field that match the query.
     */
    <A> A max(AggregatableQueryField<T, A> field);

    /**
     * Returns the sum of the given field that match the query.
     */
    <A> A sum(AggregatableNumberQueryField<T, A> field);

    /**
     * Returns the average value of the given field that match the query.
     */
    <A> Double average(AggregatableNumberQueryField<T, A> field);
  }

  /**
   * The result of a query that was built by {@link QueryableStore.Query#withIds()}. It contains the parent ids of the
   * found objects in addition to the objects and their ids themselves.
   *
   * @param <T> The type of the queried objects.
   */
  interface Result<T> {
    /**
     * Returns the parent ids of the found objects. The parent ids are ordered in the same way as their types are
     * specified in the @{@link QueryableType} annotation for the queried type.
     */
    Optional<String> getParentId(Class<?> clazz);

    /**
     * Returns the id of the found object.
     */
    String getId();

    /**
     * Returns the found object itself.
     */
    T getEntity();
  }

  /**
   * Instances of this class will be created by the annotation processor for each class annotated with
   * {@link QueryableType}. It provides query fields for the annotated class to build queries with.
   * <br/>
   * This is not meant to be extended or instantiated by users of the API!
   *
   * @param <T> The type of the objects this field is used for.
   * @param <F> The type of the field.
   */
  @SuppressWarnings("unused")
  interface QueryField<T, F> {
    String getName();

    boolean isIdField();
  }

  @SuppressWarnings("unused")
  abstract class BaseQueryField<T, F> implements QueryField<T, F> {
    private final String name;

    BaseQueryField(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public boolean isIdField() {
      return false;
    }

    /**
     * Creates a condition that checks if the field is null.
     *
     * @return The condition to use in a query.
     */
    public Condition<T> isNull() {
      return new LeafCondition<>(this, Operator.NULL, null);
    }
  }

  /**
   * Query fields implementing this can compute aggregates like minimum or maximum.
   */
  interface AggregatableQueryField<T, A> extends QueryField<T, A> {
    Class<A> getFieldType();
  }

  /**
   * Query fields implementing this can compute aggregates like sum or average.
   */
  interface AggregatableNumberQueryField<T, A> extends AggregatableQueryField<T, A> {
  }

  /**
   * This class is used to create conditions for queries. Instances of this class will be created by the annotation
   * processor for each {@link String} field of a class annotated with {@link QueryableType}.
   * <br/>
   * This is not meant to be instantiated by users of the API!
   *
   * @param <T> The type of the objects this condition is used for.
   */
  class StringQueryField<T> extends BaseQueryField<T, String> implements AggregatableQueryField<T, String> {

    public StringQueryField(String name) {
      super(name);
    }

    /**
     * Creates a condition that checks if the field is equal to the given value.
     *
     * @param value The value to compare the field with.
     * @return The condition to use in a query.
     */
    public LeafCondition<T, String> eq(String value) {
      return new LeafCondition<>(this, Operator.EQ, value);
    }

    /**
     * Creates a condition that checks if the field contains the given value as a substring.
     *
     * @param value The value to check for.
     * @return The condition to use in a query.
     */
    public Condition<T> contains(String value) {
      return new LeafCondition<>(this, Operator.CONTAINS, value);
    }


    /**
     * Creates a condition that checks if the field is equal to any of the given values.
     *
     * @param values The values to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> in(String... values) {
      return new LeafCondition<>(this, Operator.IN, values);
    }

    /**
     * Creates a condition that checks if the field is equal to any of the given values.
     *
     * @param values The values to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> in(Collection<String> values) {
      return in(values.toArray(new String[0]));
    }

    @Override
    public Class<String> getFieldType() {
      return String.class;
    }
  }

  /**
   * This class is used to create conditions for queries. Instances of this class will be created by the annotation
   * processor for a class annotated with {@link QueryableType}.
   * <br/>
   * This is not meant to be instantiated by users of the API!
   *
   * @param <T> The type of the objects this condition is used for.
   */
  class IdQueryField<T> extends StringQueryField<T> {
    public IdQueryField(Class<?> clazz) {
      super(clazz.getName());
    }

    public IdQueryField() {
      super(null);
    }

    @Override
    public boolean isIdField() {
      return true;
    }
  }

  /**
   * This class is used to create conditions for queries. Instances of this class will be created by the annotation
   * processor for each number field (either {@link Integer}, {@link Long}, {@code int}, or {@code long}) of a class
   * annotated with {@link QueryableType}.
   * <br/>
   * This is not meant to be instantiated by users of the API!
   *
   * @param <T> The type of the objects this condition is used for.
   * @param <N> The type of the number field.
   */
  abstract class NumberQueryField<T, N extends Number> extends BaseQueryField<T, N> {

    NumberQueryField(String name) {
      super(name);
    }

    /**
     * Creates a condition that checks if the field is equal to the given value.
     *
     * @param value The value to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> eq(N value) {
      return new LeafCondition<>(this, Operator.EQ, value);
    }

    /**
     * Creates a condition that checks if the field is greater than the given value.
     *
     * @param value The value to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> greater(N value) {
      return new LeafCondition<>(this, Operator.GREATER, value);
    }

    /**
     * Creates a condition that checks if the field is less than the given value.
     *
     * @param value The value to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> less(N value) {
      return new LeafCondition<>(this, Operator.LESS, value);
    }

    /**
     * Creates a condition that checks if the field is greater than or equal to the given value.
     *
     * @param value The value to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> greaterOrEquals(N value) {
      return new LeafCondition<>(this, Operator.GREATER_OR_EQUAL, value);
    }

    /**
     * Creates a condition that checks if the field is less than or equal to the given value.
     *
     * @param value The value to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> lessOrEquals(N value) {
      return new LeafCondition<>(this, Operator.LESS_OR_EQUAL, value);
    }

    /**
     * Creates a condition that checks if the fields is inclusively between the from and to values.
     *
     * @param from The lower limit to compare the value with.
     * @param to   The upper limit to compare the value with.
     * @return The condition to use in a query.
     */
    public Condition<T> between(N from, N to) {
      return Conditions.and(lessOrEquals(to), greaterOrEquals(from));
    }

    /**
     * Creates a condition that checks if the field is equal to any of the given values.
     *
     * @param values The values to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> in(N... values) {
      return new LeafCondition<>(this, Operator.IN, values);
    }

    /**
     * Creates a condition that checks if the field is equal to any of the given values.
     *
     * @param values The values to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> in(Collection<N> values) {
      return new LeafCondition<>(this, Operator.IN, values.toArray(new Object[0]));
    }
  }

  class IntegerQueryField<T> extends NumberQueryField<T, Integer> implements AggregatableNumberQueryField<T, Integer> {
    public IntegerQueryField(String name) {
      super(name);
    }

    @Override
    public Class<Integer> getFieldType() {
      return Integer.class;
    }
  }

  class LongQueryField<T> extends NumberQueryField<T, Long> implements AggregatableNumberQueryField<T, Long> {
    public LongQueryField(String name) {
      super(name);
    }

    @Override
    public Class<Long> getFieldType() {
      return Long.class;
    }
  }

  class FloatQueryField<T> extends NumberQueryField<T, Float> implements AggregatableNumberQueryField<T, Float> {
    public FloatQueryField(String name) {
      super(name);
    }

    @Override
    public Class<Float> getFieldType() {
      return Float.class;
    }
  }

  class DoubleQueryField<T> extends NumberQueryField<T, Double> implements AggregatableNumberQueryField<T, Double> {
    public DoubleQueryField(String name) {
      super(name);
    }

    @Override
    public Class<Double> getFieldType() {
      return Double.class;
    }
  }

  /**
   * This class is used to create conditions for queries. Instances of this class will be created by the annotation
   * processor for each date field of a class annotated with {@link QueryableType}.
   * <br/>
   * This is not meant to be instantiated by users of the API!
   *
   * @param <T> The type of the objects this condition is used for.
   */
  class InstantQueryField<T> extends BaseQueryField<T, Instant> {
    public InstantQueryField(String name) {
      super(name);
    }

    /**
     * Creates a condition that checks if the field is equal to the given value. The given instant will be truncated to
     * milliseconds.
     *
     * @param value The value to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> eq(Instant value) {
      return new LeafCondition<>(this, Operator.EQ, value.truncatedTo(ChronoUnit.MILLIS));
    }

    /**
     * Creates a condition that checks if the field is after the given value.
     *
     * @param value The value to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> after(Instant value) {
      return new LeafCondition<>(this, Operator.GREATER, value);
    }

    /**
     * Creates a condition that checks if the field is before the given value.
     *
     * @param value The value to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> before(Instant value) {
      return new LeafCondition<>(this, Operator.LESS, value);
    }

    /**
     * Creates a condition that checks if the field is between the given values.
     *
     * @param from The lower bound of the range to compare the field with.
     * @param to   The upper bound of the range to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> between(Instant from, Instant to) {
      return Conditions.and(after(from), before(to));
    }

    /**
     * Creates a condition that checks if the field is equal to the given value.
     *
     * @param value The value to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> eq(Date value) {
      return eq(value.toInstant());
    }

    /**
     * Creates a condition that checks if the field is after the given value.
     *
     * @param value The value to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> after(Date value) {
      return after(value.toInstant());
    }

    /**
     * Creates a condition that checks if the field is before the given value.
     *
     * @param value The value to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> before(Date value) {
      return before(value.toInstant());
    }

    /**
     * Creates a condition that checks if the field is between the given values.
     *
     * @param from The lower bound of the range to compare the field with.
     * @param to   The upper bound of the range to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> between(Date from, Date to) {
      return between(from.toInstant(), to.toInstant());
    }
  }

  /**
   * This class is used to create conditions for queries. Instances of this class will be created by the annotation
   * processor for each boolean field of a class annotated with {@link QueryableType}.
   * <br/>
   * This is not meant to be instantiated by users of the API!
   *
   * @param <T> The type of the objects this condition is used for.
   */
  class BooleanQueryField<T> extends BaseQueryField<T, Boolean> {

    public BooleanQueryField(String name) {
      super(name);
    }

    /**
     * Creates a condition that checks if the field is equal to the given value.
     *
     * @param b The value to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> eq(Boolean b) {
      return new LeafCondition<>(this, Operator.EQ, b);
    }

    /**
     * Creates a condition that checks if the field is true.
     *
     * @return The condition to use in a query.
     */
    public Condition<T> isTrue() {
      return eq(Boolean.TRUE);
    }

    /**
     * Creates a condition that checks if the field is false.
     *
     * @return The condition to use in a query.
     */
    public Condition<T> isFalse() {
      return eq(Boolean.FALSE);
    }
  }

  /**
   * This class is used to create conditions for queries. Instances of this class will be created by the annotation
   * processor for each enum field of a class annotated with {@link QueryableType}.
   * <br/>
   * This is not meant to be instantiated by users of the API!
   *
   * @param <T> The type of the objects this condition is used for.
   * @param <E> The type of the enum field.
   */
  class EnumQueryField<T, E extends Enum<E>> extends BaseQueryField<T, Enum<E>> {
    public EnumQueryField(String name) {
      super(name);
    }

    /**
     * Creates a condition that checks if the field is equal to the given value.
     *
     * @param value The value to compare the field with.
     * @return The condition to use in a query.
     */
    public LeafCondition<T, String> eq(E value) {
      return new LeafCondition<>(this, Operator.EQ, value.name());
    }

    /**
     * Creates a condition that checks if the field is equal to any of the given values.
     *
     * @param values The values to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> in(E... values) {
      return new LeafCondition<>(this, Operator.IN, Arrays.stream(values).map(Enum::name).toArray());
    }

    /**
     * Creates a condition that checks if the field is equal to any of the given values.
     *
     * @param values The values to compare the field with.
     * @return The condition to use in a query.
     */
    public Condition<T> in(Collection<E> values) {
      return new LeafCondition<>(this, Operator.IN, values.stream().map(Enum::name).toArray());
    }
  }

  /**
   * This class is used to create conditions for queries. Instances of this class will be created by the annotation
   * processor for each collection field of a class annotated with {@link QueryableType}. Note that this can only be
   * used for collections of base types like {@link String}, number types, enums or booleans.
   * <br/>
   * This is not meant to be instantiated by users of the API!
   *
   * @param <T> The type of the objects this condition is used for.
   */
  class CollectionQueryField<T> extends BaseQueryField<T, Object> {
    public CollectionQueryField(String name) {
      super(name);
    }

    /**
     * Creates a condition that checks if the field contains the given value.
     *
     * @param value The value to check for.
     * @return The condition to use in a query.
     */
    public Condition<T> contains(Object value) {
      return new LeafCondition<>(this, Operator.EQ, value);
    }
  }

  /**
   * This class is used to create conditions for queries, based on the size of a collection.
   * Instances of this class will be created by the annotation processor for each collection
   * field of a class annotated with {@link QueryableType}. Note that this can only be used
   * for collections of base types like {@link String}, number types, enums or booleans.
   * <br/>
   * This is not meant to be instantiated by users of the API!
   *
   * @param <T> The type of the objects this condition is used for.
   */
  class CollectionSizeQueryField<T> extends NumberQueryField<T, Long> implements AggregatableNumberQueryField<T, Long> {
    public CollectionSizeQueryField(String name) {
      super(name);
    }

    /**
     * Creates a condition that checks if the collection field is empty.
     *
     * @return The condition to use in a query.
     */
    public Condition<T> isEmpty() {
      return eq(0L);
    }

    @Override
    public Class<Long> getFieldType() {
      return Long.class;
    }
  }

  /**
   * This class is used to create conditions for queries. Instances of this class will be created by the annotation
   * processor for each map field of a class annotated with {@link QueryableType}. Note that this can only be used for
   * maps with base types like {@link String}, number types, enums or booleans as keys.
   * <br/>
   * This is not meant to be instantiated by users of the API!
   *
   * @param <T> The type of the objects this condition is used for.
   */
  class MapQueryField<T> extends BaseQueryField<T, Object> {
    public MapQueryField(String name) {
      super(name);
    }

    /**
     * Creates a condition that checks if the field contains the given key.
     *
     * @param key The key to check for.
     * @return The condition to use in a query.
     */
    public Condition<T> containsKey(Object key) {
      return new LeafCondition<>(this, Operator.KEY, key);
    }

    /**
     * Creates a condition that checks if the field contains the given value.
     *
     * @param value The value to check for.
     * @return The condition to use in a query.
     */
    public Condition<T> containsValue(Object value) {
      return new LeafCondition<>(this, Operator.VALUE, value);
    }
  }

  /**
   * This class is used to create conditions for queries, based on the size of a map.
   * Instances of this class will be created by the annotation processor for each map
   * field of a class annotated with {@link QueryableType}. Note that this can only be used
   * for collections of base types like {@link String}, number types, enums or booleans.
   * <br/>
   * This is not meant to be instantiated by users of the API!
   *
   * @param <T> The type of the objects this condition is used for.
   */
  class MapSizeQueryField<T> extends NumberQueryField<T, Long> implements AggregatableNumberQueryField<T, Long> {
    public MapSizeQueryField(String name) {
      super(name);
    }

    /**
     * Creates a condition that checks if the map field is empty.
     *
     * @return The condition to use in a query.
     */
    public Condition<T> isEmpty() {
      return eq(0L);
    }

    @Override
    public Class<Long> getFieldType() {
      return Long.class;
    }
  }

  /**
   * An exception occurring, if the client queried for one result with {@link Query#findOne()}, but the query returned multiple results.
   */
  class TooManyResultsException extends RuntimeException {
    public TooManyResultsException() {
      super("Found more than one result");
    }
  }
}
