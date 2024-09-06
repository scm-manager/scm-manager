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

package sonia.scm.lifecycle.modules;

import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import sonia.scm.config.ConfigBinding;
import sonia.scm.config.ConfigElement;
import sonia.scm.config.ConfigValue;
import sonia.scm.plugin.PluginLoader;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Objects;

public class ConfigModule extends AbstractModule {

  private final PluginLoader pluginLoader;

  public ConfigModule(PluginLoader pluginLoader) {
    this.pluginLoader = pluginLoader;
  }

  @Override
  protected void configure() {
    Iterable<ConfigBinding> configBindings = pluginLoader.getExtensionProcessor().getConfigBindings();
    for (ConfigBinding binding : configBindings) {
      ConfigElement configElement = binding.getConfigElement();
      Object value = binding.getValue();
      if (value != null) {
        bind((Class) value.getClass()).annotatedWith(
          new ConfigValueImpl(configElement.getKey(), configElement.getDefaultValue(), configElement.getDescription())
        ).toInstance(value);
      }
    }
  }


  public static final class ConfigValueImpl implements ConfigValue, Serializable {

    private final String key;
    private final String defaultValue;
    private final String description;

    public ConfigValueImpl(String key, String defaultValue, String description) {
      this.key = key;
      this.defaultValue = Strings.nullToEmpty(defaultValue);
      this.description = Strings.nullToEmpty(description);
    }

    @Override
    public String key() {
      return key;
    }

    @Override
    public String defaultValue() {
      return defaultValue;
    }

    @Override
    public String description() {
      return description;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return ConfigValue.class;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o instanceof ConfigValue) {
        ConfigValue that = (ConfigValue) o;
        return Objects.equals(key, that.key())
          && Objects.equals(defaultValue, that.defaultValue())
          && Objects.equals(description, that.description());
      }
      return false;
    }

    /**
     * https://docs.oracle.com/javase%2F7%2Fdocs%2Fapi%2F%2F/java/lang/annotation/Annotation.html
     */
    @Override
    public int hashCode() {
      return ((127 * "key".hashCode()) ^ key.hashCode())
        + ((127 * "defaultValue".hashCode()) ^ defaultValue.hashCode())
        + ((127 * "description".hashCode()) ^ description.hashCode());
    }
  }
}
