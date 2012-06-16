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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

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
   */
  @Inject
  public PreProcessorUtil(
          Set<ChangesetPreProcessor> changesetPreProcessorSet,
          Set<ChangesetPreProcessorFactory> changesetPreProcessorFactorySet,
          Set<FileObjectPreProcessor> fileObjectPreProcessorSet,
          Set<FileObjectPreProcessorFactory> fileObjectPreProcessorFactorySet)
  {
    this.changesetPreProcessorSet = changesetPreProcessorSet;
    this.changesetPreProcessorFactorySet = changesetPreProcessorFactorySet;
    this.fileObjectPreProcessorSet = fileObjectPreProcessorSet;
    this.fileObjectPreProcessorFactorySet = fileObjectPreProcessorFactorySet;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param changeset
   */
  public void prepareForReturn(Repository repository, Changeset changeset)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("prepare changeset {} of repository {} for return",
                   changeset.getId(), repository.getName());
    }

    EscapeUtil.escape(changeset);
    callPreProcessors(changesetPreProcessorSet, changeset);
    callPreProcessorFactories(changesetPreProcessorFactorySet, repository,
                              changeset);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param changeset
   * @param result
   */
  public void prepareForReturn(Repository repository, BrowserResult result)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("prepare browser result of repository {} for return",
                   repository.getName());
    }

    EscapeUtil.escape(result);
    callPreProcessors(fileObjectPreProcessorSet, result);
    callPreProcessorFactories(fileObjectPreProcessorFactorySet, repository,
                              result);
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
    if (logger.isTraceEnabled())
    {
      logger.trace("prepare changesets of repository {} for return",
                   repository.getName());
    }

    EscapeUtil.escape(result);
    callPreProcessors(changesetPreProcessorSet, result);
    callPreProcessorFactories(changesetPreProcessorFactorySet, repository,
                              result);
  }

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
  private <T> void callPreProcessorFactories(
          Set<? extends PreProcessorFactory<T>> preProcessorFactorySet,
          Repository repository, Iterable<T> items)
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
  private <T> void callPreProcessorFactories(
          Set<? extends PreProcessorFactory<T>> preProcessorFactorySet,
          Repository repository, T item)
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
  private <T> void callPreProcessors(
          Set<? extends PreProcessor<T>> preProcessorSet, Iterable<T> items)
  {
    if (Util.isNotEmpty(preProcessorSet))
    {
      for (T item : items)
      {
        callPreProcessors(preProcessorSet, item);
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
  private <T> void callPreProcessors(
          Set<? extends PreProcessor<T>> preProcessorSet, T item)
  {
    if (Util.isNotEmpty(preProcessorSet))
    {
      for (PreProcessor<T> preProcessor : preProcessorSet)
      {
        preProcessor.process(item);
      }
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<ChangesetPreProcessorFactory> changesetPreProcessorFactorySet;

  /** Field description */
  private Set<ChangesetPreProcessor> changesetPreProcessorSet;

  /** Field description */
  private Set<FileObjectPreProcessorFactory> fileObjectPreProcessorFactorySet;

  /** Field description */
  private Set<FileObjectPreProcessor> fileObjectPreProcessorSet;
}
