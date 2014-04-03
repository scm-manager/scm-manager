/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@XmlRootElement(name = "module")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScmModule
{

  /** Field description */
  private static final Unwrapper unwrapper = new Unwrapper();

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<Class<?>> getEvents()
  {
    return unwrap(events);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<ExtensionPointElement> getExtensionPoints()
  {
    return nonNull(extensionPoints);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<Class<?>> getExtensions()
  {
    return unwrap(extensions);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<Class<?>> getRestProviders()
  {
    return unwrap(restProviders);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<Class<?>> getRestResources()
  {
    return unwrap(restResources);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<SubscriberElement> getSubscribers()
  {
    return nonNull(subscribers);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param iterable
   * @param <T>
   *
   * @return
   */
  private <T> Iterable<T> nonNull(Iterable<T> iterable)
  {
    if (iterable == null)
    {
      iterable = ImmutableSet.of();
    }

    return iterable;
  }

  /**
   * Method description
   *
   *
   * @param iterable
   *
   * @return
   */
  private Iterable<Class<?>> unwrap(Iterable<ClassElement> iterable)
  {
    Iterable<Class<?>> unwrapped;

    if (iterable != null)
    {
      unwrapped = Iterables.transform(iterable, unwrapper);
    }
    else
    {
      unwrapped = ImmutableSet.of();
    }

    return unwrapped;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/03/28
   * @author         Enter your name here...    
   */
  private static class Unwrapper implements Function<ClassElement, Class<?>>
  {

    /**
     * Method description
     *
     *
     * @param classElement
     *
     * @return
     */
    @Override
    public Class<?> apply(ClassElement classElement)
    {
      return classElement.getClazz();
    }
  }


  ;

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "event")
  private Set<ClassElement> events;

  /** Field description */
  @XmlElement(name = "extension-point")
  private Set<ExtensionPointElement> extensionPoints;

  /** Field description */
  @XmlElement(name = "extension")
  private Set<ClassElement> extensions;

  /** Field description */
  @XmlElement(name = "rest-provider")
  private Set<ClassElement> restProviders;

  /** Field description */
  @XmlElement(name = "rest-resource")
  private Set<ClassElement> restResources;

  /** Field description */
  @XmlElement(name = "subscriber")
  private Set<SubscriberElement> subscribers;
}
