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



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "overrides")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassOverrides implements Iterable<ClassOverride>
{

  /** Field description */
  public static final String OVERRIDE_PATH = "META-INF/scm/override.xml";

  /**
   * the logger for ClassOverrides
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ClassOverrides.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   */
  public static ClassOverrides findOverrides()
  {
    ClassOverrides overrides = new ClassOverrides();

    try
    {
      Enumeration<URL> overridesEnm =
        getClassLoader().getResources(OVERRIDE_PATH);
      JAXBContext context = JAXBContext.newInstance(ClassOverrides.class);

      while (overridesEnm.hasMoreElements())
      {
        URL overrideUrl = overridesEnm.nextElement();

        if (logger.isInfoEnabled())
        {
          logger.info("load override from {}", overrideUrl.toExternalForm());
        }

        try
        {
          ClassOverrides co =
            (ClassOverrides) context.createUnmarshaller().unmarshal(
                overrideUrl);
        }
        catch (Exception ex)
        {
          logger.error("could not load ".concat(overrideUrl.toExternalForm()),
                       ex);
        }
      }
    }
    catch (IOException ex)
    {
      logger.error("could not load overrides", ex);
    }
    catch (JAXBException ex)
    {
      logger.error("could not create jaxb context for ClassOverrides", ex);
    }

    return overrides;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private static ClassLoader getClassLoader()
  {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    if (classLoader == null)
    {
      classLoader = ClassOverrides.class.getClassLoader();
    }

    return classLoader;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param overrides
   */
  public void append(ClassOverrides overrides)
  {
    getOverrides().addAll(overrides.getOverrides());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Iterator<ClassOverride> iterator()
  {
    return getOverrides().iterator();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param clazz
   * @param <T>
   *
   * @return
   */
  public <T> Class<T> getOverride(Class<T> clazz)
  {
    Class<T> implementation = null;

    for (ClassOverride co : getOverrides())
    {
      if (co.getBind().equals(clazz))
      {
        implementation = (Class<T>) co.getTo();
      }
    }

    return implementation;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<ClassOverride> getOverrides()
  {
    if (overrides == null)
    {
      overrides = new ArrayList<ClassOverride>();
    }

    return overrides;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param overrides
   */
  public void setOverrides(List<ClassOverride> overrides)
  {
    this.overrides = overrides;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "override")
  private List<ClassOverride> overrides;
}
