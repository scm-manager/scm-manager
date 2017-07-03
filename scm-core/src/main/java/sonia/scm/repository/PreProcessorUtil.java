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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.17
 */
public class PreProcessorUtil
{

  /**
   * the logger for PreProcessorUtil
   */
  private static final Logger logger =
    LoggerFactory.getLogger(PreProcessorUtil.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param changesetPreProcessorSet
   * @param changesetPreProcessorFactorySet
   * @param fileObjectPreProcessorSet
   * @param fileObjectPreProcessorFactorySet
   * @param blameLinePreProcessorSet
   * @param blameLinePreProcessorFactorySet
   */
  @Inject
  public PreProcessorUtil(Set<ChangesetPreProcessor> changesetPreProcessorSet,
    Set<ChangesetPreProcessorFactory> changesetPreProcessorFactorySet,
    Set<FileObjectPreProcessor> fileObjectPreProcessorSet,
    Set<FileObjectPreProcessorFactory> fileObjectPreProcessorFactorySet,
    Set<BlameLinePreProcessor> blameLinePreProcessorSet,
    Set<BlameLinePreProcessorFactory> blameLinePreProcessorFactorySet)
  {
    this.changesetPreProcessorSet = changesetPreProcessorSet;
    this.changesetPreProcessorFactorySet = changesetPreProcessorFactorySet;
    this.fileObjectPreProcessorSet = fileObjectPreProcessorSet;
    this.fileObjectPreProcessorFactorySet = fileObjectPreProcessorFactorySet;
    this.blameLinePreProcessorSet = blameLinePreProcessorSet;
    this.blameLinePreProcessorFactorySet = blameLinePreProcessorFactorySet;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param blameLine
   */
  public void prepareForReturn(Repository repository, BlameLine blameLine)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("prepare blame line {} of repository {} for return",
        blameLine.getLineNumber(), repository.getName());
    }

    EscapeUtil.escape(blameLine);

    PreProcessorHandler<BlameLine> handler =
      new PreProcessorHandler<>(blameLinePreProcessorFactorySet,
                                blameLinePreProcessorSet, repository);

    handler.callPreProcessors(blameLine);
    handler.callPreProcessorFactories(blameLine);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param blameResult
   */
  public void prepareForReturn(Repository repository, BlameResult blameResult)
  {
    prepareForReturn(repository, blameResult, true);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param blameResult
   * @param escape
   *
   * @since 1.35
   */
  public void prepareForReturn(Repository repository, BlameResult blameResult,
    boolean escape)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("prepare blame result of repository {} for return",
        repository.getName());
    }

    if (escape)
    {
      EscapeUtil.escape(blameResult);
    }

    PreProcessorHandler<BlameLine> handler =
      new PreProcessorHandler<>(blameLinePreProcessorFactorySet,
                                blameLinePreProcessorSet, repository);

    handler.callPreProcessors(blameResult.getBlameLines());
    handler.callPreProcessorFactories(blameResult.getBlameLines());
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param changeset
   */
  public void prepareForReturn(Repository repository, Changeset changeset)
  {
    prepareForReturn(repository, changeset, true);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param changeset
   * @param escape
   *
   * @since 1.35
   */
  public void prepareForReturn(Repository repository, Changeset changeset,
    boolean escape)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("prepare changeset {} of repository {} for return",
        changeset.getId(), repository.getName());
    }

    if (escape)
    {
      EscapeUtil.escape(changeset);
    }

    PreProcessorHandler<Changeset> handler =
      new PreProcessorHandler<>(changesetPreProcessorFactorySet,
                                changesetPreProcessorSet, repository);

    handler.callPreProcessors(changeset);
    handler.callPreProcessorFactories(changeset);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param result
   */
  public void prepareForReturn(Repository repository, BrowserResult result)
  {
    prepareForReturn(repository, result, true);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param result
   * @param escape
   *
   * @since 1.35
   */
  public void prepareForReturn(Repository repository, BrowserResult result,
    boolean escape)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("prepare browser result of repository {} for return",
        repository.getName());
    }

    if (escape)
    {
      EscapeUtil.escape(result);
    }

    PreProcessorHandler<FileObject> handler =
      new PreProcessorHandler<>(fileObjectPreProcessorFactorySet,
                                fileObjectPreProcessorSet, repository);

    handler.callPreProcessors(result);
    handler.callPreProcessorFactories(result);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param result
   * @param escape
   *
   * @since 1.35
   */
  public void prepareForReturn(Repository repository,
    ChangesetPagingResult result, boolean escape)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("prepare changesets of repository {} for return",
        repository.getName());
    }

    if (escape)
    {
      EscapeUtil.escape(result);
    }

    PreProcessorHandler<Changeset> handler =
      new PreProcessorHandler<>(changesetPreProcessorFactorySet,
                                changesetPreProcessorSet, repository);

    handler.callPreProcessors(result);
    handler.callPreProcessorFactories(result);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param result
   */
  public void prepareForReturn(Repository repository,
    ChangesetPagingResult result)
  {
    prepareForReturn(repository, result, true);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @param <T>
   *
   * @version        Enter version here..., 12/06/16
   * @author         Enter your name here...
   */
  private static class PreProcessorHandler<T>
  {

    /**
     * Constructs ...
     *
     *
     * @param preProcessorFactorySet
     * @param preProcessorSet
     * @param repository
     */
    public PreProcessorHandler(
      Collection<? extends PreProcessorFactory<T>> preProcessorFactorySet,
      Collection<? extends PreProcessor<T>> preProcessorSet,
      Repository repository)
    {
      this.preProcessorFactorySet = preProcessorFactorySet;
      this.preProcessorSet = preProcessorSet;
      this.repository = repository;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     *
     *
     * @param preProcessorFactorySet
     * @param repository
     * @param changesets
     * @param items
     * @param <T>
     */
    public void callPreProcessorFactories(Iterable<T> items)
    {
      if (Util.isNotEmpty(preProcessorFactorySet))
      {
        for (PreProcessorFactory<T> factory : preProcessorFactorySet)
        {
          PreProcessor<T> preProcessor = factory.createPreProcessor(repository);

          if (preProcessor != null)
          {
            for (T item : items)
            {
              preProcessor.process(item);
            }
          }
        }
      }
    }

    /**
     * Method description
     *
     *
     *
     * @param preProcessorFactorySet
     * @param repository
     * @param item
     * @param <T>
     */
    public void callPreProcessorFactories(T item)
    {
      if (Util.isNotEmpty(preProcessorFactorySet))
      {
        for (PreProcessorFactory<T> factory : preProcessorFactorySet)
        {
          PreProcessor<T> cpp = factory.createPreProcessor(repository);

          if (cpp != null)
          {
            cpp.process(item);
          }
        }
      }
    }

    /**
     * Method description
     *
     *
     * @param changesets
     *
     * @param preProcessorSet
     * @param items
     * @param <T>
     */
    public void callPreProcessors(Iterable<T> items)
    {
      if (Util.isNotEmpty(preProcessorSet))
      {
        for (T item : items)
        {
          callPreProcessors(item);
        }
      }
    }

    /**
     * Method description
     *
     *
     * @param c
     *
     * @param preProcessorSet
     * @param item
     * @param <T>
     */
    public void callPreProcessors(T item)
    {
      if (Util.isNotEmpty(preProcessorSet))
      {
        for (PreProcessor<T> preProcessor : preProcessorSet)
        {
          preProcessor.process(item);
        }
      }
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final Collection<? extends PreProcessorFactory<T>> preProcessorFactorySet;

    /** Field description */
    private final Collection<? extends PreProcessor<T>> preProcessorSet;

    /** Field description */
    private final Repository repository;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Collection<BlameLinePreProcessorFactory> blameLinePreProcessorFactorySet;

  /** Field description */
  private final Collection<BlameLinePreProcessor> blameLinePreProcessorSet;

  /** Field description */
  private final Collection<ChangesetPreProcessorFactory> changesetPreProcessorFactorySet;

  /** Field description */
  private final Collection<ChangesetPreProcessor> changesetPreProcessorSet;

  /** Field description */
  private final Collection<FileObjectPreProcessorFactory> fileObjectPreProcessorFactorySet;

  /** Field description */
  private final Collection<FileObjectPreProcessor> fileObjectPreProcessorSet;
}
