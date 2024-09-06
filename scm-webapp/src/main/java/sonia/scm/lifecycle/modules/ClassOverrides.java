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


@XmlRootElement(name = "overrides")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassOverrides implements Iterable<ClassOverride>
{

  public static final String OVERRIDE_PATH = "META-INF/scm/override.xml";

 
  private static final Logger logger =
    LoggerFactory.getLogger(ClassOverrides.class);

  @XmlElement(name = "module")
  private List<Class<? extends Module>> moduleClasses;

  @XmlElement(name = "override")
  private List<ClassOverride> overrides;

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


  @Override
  public Iterator<ClassOverride> iterator()
  {
    return getOverrides().iterator();
  }



  public List<Class<? extends Module>> getModuleClasses()
  {
    if (moduleClasses == null)
    {
      moduleClasses = Lists.newArrayList();
    }

    return moduleClasses;
  }


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


  public List<ClassOverride> getOverrides()
  {
    if (overrides == null)
    {
      overrides = Lists.newArrayList();
    }

    return overrides;
  }



  public void setModuleClasses(List<Class<? extends Module>> moduleClasses)
  {
    this.moduleClasses = moduleClasses;
  }


  public void setOverrides(List<ClassOverride> overrides)
  {
    this.overrides = overrides;
  }

}
