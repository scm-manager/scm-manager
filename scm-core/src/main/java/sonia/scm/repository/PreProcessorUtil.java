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

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
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
  public PreProcessorUtil(
          Set<ChangesetPreProcessor> changesetPreProcessorSet,
          Set<ChangesetPreProcessorFactory> changesetPreProcessorFactorySet,
          Set<FileObjectPreProcessor> fileObjectPreProcessorSet,
          Set<FileObjectPreProcessorFactory> fileObjectPreProcessorFactorySet,
          Set<BlameLinePreProcessor> blameLinePreProcessorSet,
          Set<BlameLinePreProcessorFactory> blameLinePreProcessorFactorySet)
  {
    this.changesetPreProcessorSet =
      Collections2.transform(changesetPreProcessorSet,
                             new Function<ChangesetPreProcessor,
                               ChangesetPreProcessorWrapper>()
    {
      @Override
      public ChangesetPreProcessorWrapper apply(ChangesetPreProcessor input)
      {
        return new ChangesetPreProcessorWrapper(input);
      }
    });
    this.changesetPreProcessorFactorySet =
      Collections2.transform(changesetPreProcessorFactorySet,
                             new Function<ChangesetPreProcessorFactory,
                               ChangesetPreProcessorFactoryWrapper>()
    {
      @Override
      public ChangesetPreProcessorFactoryWrapper apply(
              ChangesetPreProcessorFactory input)
      {
        return new ChangesetPreProcessorFactoryWrapper(input);
      }
    });
    this.fileObjectPreProcessorSet =
      Collections2.transform(fileObjectPreProcessorSet,
                             new Function<FileObjectPreProcessor,
                               FileObjectPreProcessorWrapper>()
    {
      @Override
      public FileObjectPreProcessorWrapper apply(FileObjectPreProcessor input)
      {
        return new FileObjectPreProcessorWrapper(input);
      }
    });
    this.fileObjectPreProcessorFactorySet =
      Collections2.transform(fileObjectPreProcessorFactorySet,
                             new Function<FileObjectPreProcessorFactory,
                               FileObjectPreProcessorFactoryWrapper>()
    {
      @Override
      public FileObjectPreProcessorFactoryWrapper apply(
              FileObjectPreProcessorFactory input)
      {
        return new FileObjectPreProcessorFactoryWrapper(input);
      }
    });
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
      new PreProcessorHandler<BlameLine>(blameLinePreProcessorFactorySet,
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
    if (logger.isTraceEnabled())
    {
      logger.trace("prepare blame result of repository {} for return",
                   repository.getName());
    }

    EscapeUtil.escape(blameResult);

    PreProcessorHandler<BlameLine> handler =
      new PreProcessorHandler<BlameLine>(blameLinePreProcessorFactorySet,
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
    if (logger.isTraceEnabled())
    {
      logger.trace("prepare changeset {} of repository {} for return",
                   changeset.getId(), repository.getName());
    }

    EscapeUtil.escape(changeset);

    PreProcessorHandler<Changeset> handler =
      new PreProcessorHandler<Changeset>(changesetPreProcessorFactorySet,
                              changesetPreProcessorSet, repository);

    handler.callPreProcessors(changeset);
    handler.callPreProcessorFactories(changeset);
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

    PreProcessorHandler<FileObject> handler =
      new PreProcessorHandler<FileObject>(fileObjectPreProcessorFactorySet,
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

    PreProcessorHandler<Changeset> handler =
      new PreProcessorHandler<Changeset>(changesetPreProcessorFactorySet,
                              changesetPreProcessorSet, repository);

    handler.callPreProcessors(result);
    handler.callPreProcessorFactories(result);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/06/16
   * @author         Enter your name here...
   */
  private static class ChangesetPreProcessorFactoryWrapper
          implements PreProcessorFactory<Changeset>
  {

    /**
     * Constructs ...
     *
     *
     * @param preProcessorFactory
     */
    public ChangesetPreProcessorFactoryWrapper(
            ChangesetPreProcessorFactory preProcessorFactory)
    {
      this.preProcessorFactory = preProcessorFactory;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param repository
     *
     * @return
     */
    @Override
    public PreProcessor<Changeset> createPreProcessor(Repository repository)
    {
      PreProcessor<Changeset> preProcessor = null;
      ChangesetPreProcessor changesetPreProcessor =
        preProcessorFactory.createPreProcessor(repository);

      if (changesetPreProcessor != null)
      {
        preProcessor = new ChangesetPreProcessorWrapper(changesetPreProcessor);
      }

      return preProcessor;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private ChangesetPreProcessorFactory preProcessorFactory;
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/06/16
   * @author         Enter your name here...
   */
  private static class ChangesetPreProcessorWrapper
          implements PreProcessor<Changeset>
  {

    /**
     * Constructs ...
     *
     *
     * @param preProcessor
     */
    public ChangesetPreProcessorWrapper(ChangesetPreProcessor preProcessor)
    {
      this.preProcessor = preProcessor;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param item
     */
    @Override
    public void process(Changeset item)
    {
      preProcessor.process(item);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private ChangesetPreProcessor preProcessor;
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/06/16
   * @author         Enter your name here...
   */
  private static class FileObjectPreProcessorFactoryWrapper
          implements PreProcessorFactory<FileObject>
  {

    /**
     * Constructs ...
     *
     *
     * @param preProcessorFactory
     */
    public FileObjectPreProcessorFactoryWrapper(
            FileObjectPreProcessorFactory preProcessorFactory)
    {
      this.preProcessorFactory = preProcessorFactory;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param repository
     *
     * @return
     */
    @Override
    public PreProcessor<FileObject> createPreProcessor(Repository repository)
    {
      PreProcessor<FileObject> preProcessor = null;
      FileObjectPreProcessor fileObjectPreProcessor =
        preProcessorFactory.createPreProcessor(repository);

      if (fileObjectPreProcessor != null)
      {
        preProcessor =
          new FileObjectPreProcessorWrapper(fileObjectPreProcessor);
      }

      return preProcessor;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private FileObjectPreProcessorFactory preProcessorFactory;
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/06/16
   * @author         Enter your name here...
   */
  private static class FileObjectPreProcessorWrapper
          implements PreProcessor<FileObject>
  {

    /**
     * Constructs ...
     *
     *
     * @param preProcessor
     */
    public FileObjectPreProcessorWrapper(FileObjectPreProcessor preProcessor)
    {
      this.preProcessor = preProcessor;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param item
     */
    @Override
    public void process(FileObject item)
    {
      preProcessor.process(item);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private FileObjectPreProcessor preProcessor;
  }


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
    private Collection<? extends PreProcessorFactory<T>> preProcessorFactorySet;

    /** Field description */
    private Collection<? extends PreProcessor<T>> preProcessorSet;

    /** Field description */
    private Repository repository;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Collection<BlameLinePreProcessorFactory> blameLinePreProcessorFactorySet;

  /** Field description */
  private Collection<BlameLinePreProcessor> blameLinePreProcessorSet;

  /** Field description */
  private Collection<ChangesetPreProcessorFactoryWrapper> changesetPreProcessorFactorySet;

  /** Field description */
  private Collection<ChangesetPreProcessorWrapper> changesetPreProcessorSet;

  /** Field description */
  private Collection<FileObjectPreProcessorFactoryWrapper> fileObjectPreProcessorFactorySet;

  /** Field description */
  private Collection<FileObjectPreProcessorWrapper> fileObjectPreProcessorSet;
}
