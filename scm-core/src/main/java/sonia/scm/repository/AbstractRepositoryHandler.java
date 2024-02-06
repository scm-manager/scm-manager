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
    
package sonia.scm.repository;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.FeatureNotSupportedException;
import sonia.scm.SCMContextProvider;
import sonia.scm.event.ScmEventBus;

import java.io.File;
import java.io.IOException;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

public abstract class AbstractRepositoryHandler<C extends RepositoryConfig>
  implements RepositoryHandler
{

  
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractRepositoryHandler.class);

  protected File baseDirectory;

  protected C config;

  protected ConfigurationStore<C> store;
 
  protected AbstractRepositoryHandler(ConfigurationStoreFactory storeFactory) {
    this.store = storeFactory
      .withType(getConfigClass())
      .withName(getType().getName())
      .build();
  }


  
  protected abstract Class<C> getConfigClass();


  @Override
  public void close() throws IOException
  {

    // do nothing
  }


  @Override
  public void init(SCMContextProvider context)
  {
    baseDirectory = context.getBaseDirectory();
    loadConfig();
  }

   public void loadConfig()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("load config from store");
    }

    config = store.get();
  }

   public void storeConfig()
  {
    if (config != null)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("store config");
      }

      store.set(config);
    }
  }


  
  public C getConfig()
  {
    return config;
  }


  @Override
  public ImportHandler getImportHandler()
  {
    throw new FeatureNotSupportedException("import");
  }

  /**
   * Returns true if the plugin is configured and enabled.
   */
  @Override
  public boolean isConfigured()
  {
    return (config != null) && config.isValid() &&!config.isDisabled();
  }



  public void setConfig(C config)
  {
    this.config = config;
    fireConfigChanged();
  }


   private void fireConfigChanged()
  {
    ScmEventBus.getInstance().post(
      new RepositoryHandlerConfigChangedEvent<C>(config));
  }

}
