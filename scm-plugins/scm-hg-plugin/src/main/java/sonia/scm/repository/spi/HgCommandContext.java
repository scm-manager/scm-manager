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

package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.aragost.javahg.Repository;

import com.google.common.base.Strings;

import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.web.HgUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgCommandContext implements Closeable, Supplier<sonia.scm.repository.Repository>
{

  /** Field description */
  private static final String PROPERTY_ENCODING = "hg.encoding";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param hookManager
   * @param handler
   * @param repository
   * @param directory
   */
  public HgCommandContext(HgHookManager hookManager,
                          HgRepositoryHandler handler, sonia.scm.repository.Repository repository,
                          File directory)
  {
    this(hookManager, handler, repository, directory,
      handler.getHgContext().isPending());
  }

  /**
   * Constructs ...
   *
   *
   * @param hookManager
   * @param handler
   * @param repository
   * @param directory
   * @param pending
   */
  public HgCommandContext(HgHookManager hookManager,
                          HgRepositoryHandler handler, sonia.scm.repository.Repository repository,
                          File directory, boolean pending)
  {
    this.hookManager = hookManager;
    this.handler = handler;
    this.directory = directory;
    this.scmRepository = repository;
    this.encoding = repository.getProperty(PROPERTY_ENCODING);
    this.pending = pending;

    if (Strings.isNullOrEmpty(encoding))
    {
      encoding = handler.getConfig().getEncoding();
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    if (repository != null)
    {
      repository.close();
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Repository open()
  {
    if (repository == null)
    {
      repository = HgUtil.open(handler, hookManager, directory, encoding, pending);
    }

    return repository;
  }

  public Repository openWithSpecialEnvironment(BiConsumer<sonia.scm.repository.Repository, Map<String, String>> prepareEnvironment)
  {
    return HgUtil.open(handler, directory, encoding,
      pending, environment -> prepareEnvironment.accept(scmRepository, environment));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public HgConfig getConfig()
  {
    return handler.getConfig();
  }

  public sonia.scm.repository.Repository getScmRepository() {
    return scmRepository;
  }

  @Override
  public sonia.scm.repository.Repository get() {
    return getScmRepository();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File directory;

  /** Field description */
  private String encoding;

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private HgHookManager hookManager;

  /** Field description */
  private boolean pending;

  /** Field description */
  private Repository repository;

  private final sonia.scm.repository.Repository scmRepository;
}
