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
