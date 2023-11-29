/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
