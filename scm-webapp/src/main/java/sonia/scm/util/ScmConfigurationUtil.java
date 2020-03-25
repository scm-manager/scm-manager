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
    
package sonia.scm.util;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ConfigurationException;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.CipherUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Sebastian Sdorra
 */
public final class ScmConfigurationUtil
{

  /** Field description */
  private static volatile ScmConfigurationUtil instance;

  /** the logger for ScmConfigurationUtil */
  private static final Logger logger =
    LoggerFactory.getLogger(ScmConfigurationUtil.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private ScmConfigurationUtil()
  {
    try
    {
      context = JAXBContext.newInstance(ScmConfiguration.class);
      file = new File(SCMContext.getContext().getBaseDirectory(),
        ScmConfiguration.PATH);
    }
    catch (JAXBException ex)
    {
      throw new ConfigurationException(ex);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public static ScmConfigurationUtil getInstance()
  {
    if (instance == null)
    {
      synchronized (ScmConfigurationUtil.class)
      {
        if (instance == null)
        {
          instance = new ScmConfigurationUtil();
        }
      }
    }

    return instance;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param configuration
   */
  public void load(ScmConfiguration configuration)
  {
    if (file.exists())
    {
      if (logger.isInfoEnabled())
      {
        logger.info("load ScmConfiguration from file {}", file);
      }

      try
      {
        Unmarshaller unmarshaller = context.createUnmarshaller();
        ScmConfiguration loadedConfig =
          (ScmConfiguration) unmarshaller.unmarshal(file);
        String password = loadedConfig.getProxyPassword();

        if (Util.isNotEmpty(password))
        {
          password = CipherUtil.getInstance().decode(password);
          loadedConfig.setProxyPassword(password);
        }

        configuration.load(loadedConfig);
      }
      catch (Exception ex)
      {
        throw new ConfigurationException("could not load config", ex);
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("could not find ScmConfiguration file at {}", file);
    }
  }

  /**
   * Method description
   *
   *
   * @param configuration
   */
  public void store(ScmConfiguration configuration)
  {
    try
    {
      if (logger.isInfoEnabled())
      {
        logger.info("store ScmConfiguration at {}", file);
      }

      if (!file.exists())
      {
        IOUtil.mkdirs(file.getParentFile());
      }

      ScmConfiguration config = new ScmConfiguration();

      config.load(configuration);

      String password = config.getProxyPassword();

      if (Util.isNotEmpty(password))
      {
        password = CipherUtil.getInstance().encode(password);
        config.setProxyPassword(password);
      }

      Marshaller marshaller = context.createMarshaller();

      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(config, file);
      configuration.fireChangeEvent();
    }
    catch (Exception ex)
    {
      throw new ConfigurationException("could not store config", ex);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private JAXBContext context;

  /** Field description */
  private File file;
}
