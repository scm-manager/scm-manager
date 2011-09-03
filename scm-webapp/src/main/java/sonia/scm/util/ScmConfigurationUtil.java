/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
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
public class ScmConfigurationUtil
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
  public ScmConfigurationUtil()
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

        if (loadedConfig != null)
        {
          configuration.load(loadedConfig);
        }
      }
      catch (Exception ex)
      {
        throw new ConfigurationException("could not load config", ex);
      }
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("could not find ScmConfiuration file at {}", file);
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
