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
package sonia.scm.store;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.util.IOUtil;

import java.io.File;

/**
 * JAXB implementation of {@link JAXBConfigurationStoreFactory}.
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class JAXBConfigurationStoreFactory implements ConfigurationStoreFactory {

  /**
   * the logger for JAXBConfigurationStoreFactory
   */
  private static final Logger LOG = LoggerFactory.getLogger(JAXBConfigurationStoreFactory.class);

  private final File configDirectory;

  /**
   * Constructs a new instance.
   *
   * @param context scm context
   */
  @Inject
  public JAXBConfigurationStoreFactory(SCMContextProvider context) {
    configDirectory = new File(context.getBaseDirectory(), StoreConstants.CONFIGDIRECTORY_NAME);
    IOUtil.mkdirs(configDirectory);
  }

  @Override
  public <T> JAXBConfigurationStore<T> getStore(Class<T> type, String name) {
    if (configDirectory == null) {
      throw new IllegalStateException("store factory is not initialized");
    }

    File configFile = new File(configDirectory, name.concat(StoreConstants.FILE_EXTENSION));

    if (LOG.isDebugEnabled()) {
      LOG.debug("create store for {} at {}", type.getName(),
        configFile.getPath());
    }

    return new JAXBConfigurationStore<>(type, configFile);
  }

}
