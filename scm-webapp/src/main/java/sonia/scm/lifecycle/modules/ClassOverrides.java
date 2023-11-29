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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Module;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.ClassLoaders;
import sonia.scm.util.Util;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

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
   *
   * @param classLoader
   * @return
   *
   */
  public static ClassOverrides findOverrides(ClassLoader classLoader)
  {
    final ClassOverrides overrides = new ClassOverrides();

    try
    {
      final Enumeration<URL> overridesEnm =
        classLoader.getResources(OVERRIDE_PATH);
      final JAXBContext context = JAXBContext.newInstance(ClassOverrides.class);

      ClassLoaders.executeInContext(classLoader, new Runnable()
      {

        @Override
        public void run()
        {
          while (overridesEnm.hasMoreElements())
          {
            URL overrideUrl = overridesEnm.nextElement();

            if (logger.isInfoEnabled())
            {
              logger.info("load override from {}",
                overrideUrl.toExternalForm());
            }

            try
            {
              ClassOverrides co =
                (ClassOverrides) context.createUnmarshaller().unmarshal(
                  overrideUrl);

              overrides.append(co);
            }
            catch (JAXBException ex)
            {
              logger.error(
                "could not load ".concat(overrideUrl.toExternalForm()), ex);
            }
          }
        }
      });

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

  /**
   * Method description
   *
   *
   * @param overrides
   */
  public void append(ClassOverrides overrides)
  {
    AssertUtil.assertIsNotNull(overrides);

    for (ClassOverride co : overrides)
    {
      if (co.isValid())
      {
        getOverrides().add(co);
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("could not append ClassOverride, because it is not valid");
      }
    }

    getModuleClasses().addAll(overrides.getModuleClasses());
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
   * @return
   */
  public List<Class<? extends Module>> getModuleClasses()
  {
    if (moduleClasses == null)
    {
      moduleClasses = Lists.newArrayList();
    }

    return moduleClasses;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  public List<? extends Module> getModules()
  {
    List<? extends Module> modules;

    if (Util.isNotEmpty(moduleClasses))
    {
      modules = Lists.transform(moduleClasses,
        new Function<Class<? extends Module>, Module>()
      {
        @Override
        public Module apply(Class<? extends Module> moduleClass)
        {
          Module module = null;

          try
          {
            module = moduleClass.newInstance();
          }
          catch (IllegalAccessException | InstantiationException ex)
          {
            logger.error(
              "could not create module instance of ".concat(
                moduleClass.getName()), ex);
          }

          return module;
        }
      });
    }
    else
    {
      modules = Collections.EMPTY_LIST;
    }

    return modules;
  }

  /**
   * Method description
   *
   *
   * @param clazz
   * @param <T>
   *
   * @return
   */
  @SuppressWarnings("unchecked")
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
      overrides = Lists.newArrayList();
    }

    return overrides;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param moduleClasses
   */
  public void setModuleClasses(List<Class<? extends Module>> moduleClasses)
  {
    this.moduleClasses = moduleClasses;
  }

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
  @XmlElement(name = "module")
  private List<Class<? extends Module>> moduleClasses;

  /** Field description */
  @XmlElement(name = "override")
  private List<ClassOverride> overrides;
}
