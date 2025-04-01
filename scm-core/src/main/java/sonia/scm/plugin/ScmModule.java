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

package sonia.scm.plugin;

import com.google.common.collect.ImmutableSet;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import sonia.scm.config.ConfigElement;

import java.util.Set;

/**
 * @since 2.0.0
 */
@XmlRootElement(name = "module")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScmModule {

  @XmlElement(name = "indexed-type")
  private Set<ClassElement> indexedTypes;

  @XmlElement(name = "event")
  private Set<ClassElement> events;

  @XmlElement(name = "extension-point")
  private Set<ExtensionPointElement> extensionPoints;

  @XmlElement(name = "extension")
  private Set<ClassElement> extensions;

  @XmlElement(name = "rest-provider")
  private Set<ClassElement> restProviders;

  @XmlElement(name = "rest-resource")
  private Set<ClassElement> restResources;

  @XmlElement(name = "cli-command")
  private Set<NamedClassElement> cliCommands;

  @XmlElement(name = "mapper")
  private Set<ClassElement> mappers;

  @XmlElement(name = "subscriber")
  private Set<SubscriberElement> subscribers;

  @XmlElement(name = "config-value")
  private Set<ConfigElement> configElements;

  @XmlElement(name = "web-element")
  private Set<WebElementDescriptor> webElements;

  @XmlElement(name = "queryable-type")
  private Set<QueryableTypeDescriptor> queryableTypes;

  public Iterable<ClassElement> getEvents() {
    return nonNull(events);
  }

  public Iterable<ExtensionPointElement> getExtensionPoints() {
    return nonNull(extensionPoints);
  }

  public Iterable<ClassElement> getExtensions() {
    return nonNull(extensions);
  }

  public Iterable<ClassElement> getRestProviders() {
    return nonNull(restProviders);
  }

  public Iterable<ClassElement> getRestResources() {
    return nonNull(restResources);
  }

  public Iterable<NamedClassElement> getCliCommands() {
    return nonNull(cliCommands);
  }

  public Iterable<ClassElement> getMappers() {
    return nonNull(mappers);
  }

  public Iterable<SubscriberElement> getSubscribers() {
    return nonNull(subscribers);
  }

  public Iterable<WebElementDescriptor> getWebElements() {
    return nonNull(webElements);
  }

  public Iterable<ClassElement> getIndexedTypes() {
    return nonNull(indexedTypes);
  }

  /**
   * @since 3.0.0
   */
  public Iterable<ConfigElement> getConfigElements() {
    return nonNull(configElements);
  }

  /**
   * @since 3.7.0
   */
  public Iterable<QueryableTypeDescriptor> getQueryableTypes() {
    return nonNull(queryableTypes);
  }

  private <T> Iterable<T> nonNull(Iterable<T> iterable) {
    if (iterable == null) {
      iterable = ImmutableSet.of();
    }

    return iterable;
  }
}
