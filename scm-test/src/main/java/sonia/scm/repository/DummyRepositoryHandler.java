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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Sets;
import jakarta.xml.bind.annotation.XmlRootElement;
import sonia.scm.AlreadyExistsException;
import sonia.scm.store.ConfigurationStoreFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * @author Sebastian Sdorra
 */
public class DummyRepositoryHandler
  extends AbstractSimpleRepositoryHandler<DummyRepositoryHandler.DummyRepositoryConfig> {

  public static final String TYPE_DISPLAYNAME = "Dummy";

  public static final String TYPE_NAME = "dummy";

  public static final RepositoryType TYPE = new RepositoryType(TYPE_NAME, TYPE_DISPLAYNAME, Sets.newHashSet());

  private final Set<String> existingRepoNames = new HashSet<>();

  public DummyRepositoryHandler(ConfigurationStoreFactory storeFactory, RepositoryLocationResolver repositoryLocationResolver) {
    super(storeFactory, repositoryLocationResolver, null);
  }

  @Override
  public RepositoryType getType() {
    return TYPE;
  }


  @Override
  protected void create(Repository repository, File directory) {
    String key = repository.getNamespace() + "/" + repository.getName();
    if (existingRepoNames.contains(key)) {
      throw new AlreadyExistsException(repository);
    } else {
      existingRepoNames.add(key);
    }
  }

  @Override
  protected DummyRepositoryConfig createInitialConfig() {
    return new DummyRepositoryConfig();
  }

  @Override
  protected Class<DummyRepositoryConfig> getConfigClass() {
    return DummyRepositoryConfig.class;
  }

  @XmlRootElement(name = "config")
  public static class DummyRepositoryConfig extends RepositoryConfig {
    @Override
    public String getId() {
      return TYPE_NAME;
    }
  }
}
