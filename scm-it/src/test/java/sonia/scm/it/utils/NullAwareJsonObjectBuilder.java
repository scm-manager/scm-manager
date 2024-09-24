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

package sonia.scm.it.utils;

import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NullAwareJsonObjectBuilder implements JsonObjectBuilder {
  public static JsonObjectBuilder wrap(JsonObjectBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("Can't wrap nothing.");
    }
    return new NullAwareJsonObjectBuilder(builder);
  }

  private final JsonObjectBuilder builder;

  private NullAwareJsonObjectBuilder(JsonObjectBuilder builder) {
    this.builder = builder;
  }

  public JsonObjectBuilder add(String name, JsonValue value) {
    return builder.add(name, (value == null) ? JsonValue.NULL : value);
  }

  @Override
  public JsonObjectBuilder add(String name, String value) {
    if (value != null){
      return builder.add(name, value );
    }else{
      return builder.addNull(name);
    }
  }

  @Override
  public JsonObjectBuilder add(String name, BigInteger value) {
    if (value != null){
      return builder.add(name, value );
    }else{
      return builder.addNull(name);
    }
  }

  @Override
  public JsonObjectBuilder add(String name, BigDecimal value) {
    if (value != null){
      return builder.add(name, value );
    }else{
      return builder.addNull(name);
    }
  }

  @Override
  public JsonObjectBuilder add(String s, int i) {
    return builder.add(s, i);
  }

  @Override
  public JsonObjectBuilder add(String s, long l) {
    return builder.add(s, l);
  }

  @Override
  public JsonObjectBuilder add(String s, double v) {
    return builder.add(s, v);
  }

  @Override
  public JsonObjectBuilder add(String s, boolean b) {
    return builder.add(s, b);
  }

  @Override
  public JsonObjectBuilder addNull(String s) {
    return builder.addNull(s);
  }

  @Override
  public JsonObjectBuilder add(String s, JsonObjectBuilder jsonObjectBuilder) {
    return builder.add(s, jsonObjectBuilder);
  }

  @Override
  public JsonObjectBuilder add(String s, JsonArrayBuilder jsonArrayBuilder) {
    return builder.add(s, jsonArrayBuilder);
  }

  @Override
  public JsonObject build() {
    return builder.build();
  }

}
