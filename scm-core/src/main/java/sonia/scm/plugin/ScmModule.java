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

package sonia.scm.plugin;

import com.google.common.collect.ImmutableSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

/**
 * @author Sebastian Sdorra
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
  private Set<ClassElement> cliCommands;

  @XmlElement(name = "mapper")
  private Set<ClassElement> mappers;

  @XmlElement(name = "subscriber")
  private Set<SubscriberElement> subscribers;

  @XmlElement(name = "web-element")
  private Set<WebElementDescriptor> webElements;

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

  public Iterable<ClassElement> getCliCommands() {
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

  private <T> Iterable<T> nonNull(Iterable<T> iterable) {
    if (iterable == null) {
      iterable = ImmutableSet.of();
    }

    return iterable;
  }
}
