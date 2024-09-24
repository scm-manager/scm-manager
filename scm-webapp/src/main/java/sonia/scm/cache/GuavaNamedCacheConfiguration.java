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

package sonia.scm.cache;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.WebappConfigProvider;

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
    setValue("copyStrategy", copyStrategy, super::setCopyStrategy, propertyName -> of(WebappConfigProvider.resolveAsString(propertyName).orElse("")).map(CopyStrategy::valueOf).orElse(null));
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
    setValue(propertyName, originalValue, setter, value -> WebappConfigProvider.resolveAsInteger(value).orElse(null));
  }

  private void setLongValue(String propertyName, Long originalValue, Consumer<Long> setter) {
    setValue(propertyName, originalValue, setter, value -> WebappConfigProvider.resolveAsLong(value).orElse(null));
  }

  private void setBooleanValue(String propertyName, Boolean originalValue, Consumer<Boolean> setter) {
    setValue(propertyName, originalValue, setter, value -> WebappConfigProvider.resolveAsBoolean(value).orElse(null));
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
