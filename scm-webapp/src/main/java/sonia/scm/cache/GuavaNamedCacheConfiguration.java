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

package sonia.scm.cache;

//~--- JDK imports ------------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@XmlRootElement(name = "cache")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class GuavaNamedCacheConfiguration extends GuavaCacheConfiguration {

  private static final long serialVersionUID = -624795324874828475L;
  private static final Logger LOG = LoggerFactory.getLogger(GuavaNamedCacheConfiguration.class);

  private String name;

  public String getName()
  {
    return name;
  }

  @XmlAttribute
  private void setName(String name) {
    this.name = name;
  }

  @Override
  void setConcurrencyLevel(Integer concurrencyLevel) {
    setIntegerValue("concurrencyLevel", concurrencyLevel, super::setConcurrencyLevel);
  }

  @Override
  void setCopyStrategy(CopyStrategy copyStrategy) {
    setValue("copyStrategy", copyStrategy, super::setCopyStrategy, propertyName -> of(System.getProperty(propertyName)).map(CopyStrategy::valueOf).orElse(null));
  }

  @Override
  void setExpireAfterAccess(Long expireAfterAccess) {
    setLongValue("expireAfterAccess", expireAfterAccess, super::setExpireAfterAccess);
  }

  @Override
  void setExpireAfterWrite(Long expireAfterWrite) {
    setLongValue("expireAfterWrite", expireAfterWrite, super::setExpireAfterWrite);
  }

  @Override
  void setInitialCapacity(Integer initialCapacity) {
    setIntegerValue("initialCapacity", initialCapacity, super::setInitialCapacity);
  }

  @Override
  void setMaximumSize(Long maximumSize) {
    setLongValue("maximumSize", maximumSize, super::setMaximumSize);
  }

  @Override
  void setMaximumWeight(Long maximumWeight) {
    setLongValue("maximumWeight", maximumWeight, super::setMaximumWeight);
  }

  @Override
  void setRecordStats(Boolean recordStats) {
    setBooleanValue("recordStats", recordStats, super::setRecordStats);
  }

  @Override
  void setSoftValues(Boolean softValues) {
    setBooleanValue("softValues", softValues, super::setSoftValues);
  }

  @Override
  void setWeakKeys(Boolean weakKeys) {
    setBooleanValue("weakKeys", weakKeys, super::setWeakKeys);
  }

  @Override
  void setWeakValues(Boolean weakValues) {
    setBooleanValue("weakValues", weakValues, super::setWeakValues);
  }

  private void setIntegerValue(String propertyName, Integer originalValue, Consumer<Integer> setter) {
    setValue(propertyName, originalValue, setter, Integer::getInteger);
  }

  private void setLongValue(String propertyName, Long originalValue, Consumer<Long> setter) {
    setValue(propertyName, originalValue, setter, Long::getLong);
  }

  private void setBooleanValue(String propertyName, Boolean originalValue, Consumer<Boolean> setter) {
    setValue(propertyName, originalValue, setter, Boolean::getBoolean);
  }

  private <T> void setValue(String propertyName, T originalValue, Consumer<T> setter, Function<String, T> systemPropertyReader) {
    setter.accept(originalValue);
    createPropertyName(propertyName)
      .map(systemPropertyReader)
      .ifPresent(value -> {
        logOverwrite(propertyName, originalValue, value);
        setter.accept(value);
      });
  }

  private void logOverwrite(String propertyName, Object originalValue, Object overwrittenValue) {
    LOG.debug("overwrite {} of cache '{}' with system property value {} (original value: {})", propertyName, name, overwrittenValue, originalValue);
  }

  private Optional<String> createPropertyName(String fieldName) {
    if (name == null) {
      LOG.warn("failed to overwrite cache configuration with system properties, because name has not been set yet");
      return empty();
    }
    if (name.startsWith("sonia.cache.")) {
      return of("scm.cache." + name.substring("sonia.cache.".length()) + "." + fieldName);
    }
    return empty();
  }
}
