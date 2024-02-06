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


import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.Util;

import java.util.Collection;
import java.util.Set;

/**
 *
 * @since 1.17
 */
public class PreProcessorUtil
{

 
  private static final Logger logger =
    LoggerFactory.getLogger(PreProcessorUtil.class);

  private final Collection<BlameLinePreProcessorFactory> blameLinePreProcessorFactorySet;

  private final Collection<BlameLinePreProcessor> blameLinePreProcessorSet;

  private final Collection<ChangesetPreProcessorFactory> changesetPreProcessorFactorySet;

  private final Collection<ChangesetPreProcessor> changesetPreProcessorSet;

  private final Collection<ModificationsPreProcessorFactory> modificationsPreProcessorFactorySet;

  private final Collection<ModificationsPreProcessor> modificationsPreProcessorSet;

  private final Collection<FileObjectPreProcessorFactory> fileObjectPreProcessorFactorySet;

  private final Collection<FileObjectPreProcessor> fileObjectPreProcessorSet;

  @Inject
  public PreProcessorUtil(Set<ChangesetPreProcessor> changesetPreProcessorSet,
                          Set<ChangesetPreProcessorFactory> changesetPreProcessorFactorySet,
                          Set<FileObjectPreProcessor> fileObjectPreProcessorSet,
                          Set<FileObjectPreProcessorFactory> fileObjectPreProcessorFactorySet,
                          Set<BlameLinePreProcessor> blameLinePreProcessorSet,
                          Set<BlameLinePreProcessorFactory> blameLinePreProcessorFactorySet,
                          Set<ModificationsPreProcessorFactory> modificationsPreProcessorFactorySet,
                          Set<ModificationsPreProcessor> modificationsPreProcessorSet)
  {
    this.changesetPreProcessorSet = changesetPreProcessorSet;
    this.changesetPreProcessorFactorySet = changesetPreProcessorFactorySet;
    this.fileObjectPreProcessorSet = fileObjectPreProcessorSet;
    this.fileObjectPreProcessorFactorySet = fileObjectPreProcessorFactorySet;
    this.blameLinePreProcessorSet = blameLinePreProcessorSet;
    this.blameLinePreProcessorFactorySet = blameLinePreProcessorFactorySet;
    this.modificationsPreProcessorFactorySet = modificationsPreProcessorFactorySet;
    this.modificationsPreProcessorSet = modificationsPreProcessorSet;
  }



  public void prepareForReturn(Repository repository, BlameLine blameLine)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("prepare blame line {} of repository {} for return",
        blameLine.getLineNumber(), repository.getName());
    }

    handlePreProcess(repository,blameLine,blameLinePreProcessorFactorySet, blameLinePreProcessorSet);
  }


  public void prepareForReturn(Repository repository, BlameResult blameResult)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("prepare blame result of repository {} for return",
        repository.getName());
    }

    handlePreProcessForIterable(repository, blameResult.getBlameLines(),blameLinePreProcessorFactorySet, blameLinePreProcessorSet);
  }


  public void prepareForReturn(Repository repository, Changeset changeset)
  {
    logger.trace("prepare changeset {} of repository {} for return", changeset.getId(), repository);
    handlePreProcess(repository, changeset, changesetPreProcessorFactorySet, changesetPreProcessorSet);
  }

  public void prepareForReturn(Repository repository, Modifications modifications) {
    logger.trace("prepare modifications {} of repository {} for return", modifications, repository);
    handlePreProcess(repository, modifications, modificationsPreProcessorFactorySet, modificationsPreProcessorSet);
  }


  public void prepareForReturn(Repository repository, BrowserResult result)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("prepare browser result of repository {} for return", repository);
    }

    PreProcessorHandler<FileObject> handler = new PreProcessorHandler<>(fileObjectPreProcessorFactorySet, fileObjectPreProcessorSet, repository);
    handlePreProcessorForFileObject(handler, result.getFile());
  }

  private void handlePreProcessorForFileObject(PreProcessorHandler<FileObject> handler, FileObject fileObject) {
    if (fileObject.isDirectory()) {
      for (FileObject child : fileObject.getChildren()) {
        handlePreProcessorForFileObject(handler, child);
      }
    }
    handler.callPreProcessorFactories(fileObject);
    handler.callPreProcessors(fileObject);
  }


  public void prepareForReturn(Repository repository, ChangesetPagingResult result)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("prepare changesets of repository {} for return",
        repository.getName());
    }

    handlePreProcessForIterable(repository,result,changesetPreProcessorFactorySet, changesetPreProcessorSet);
  }

  private <T, F extends PreProcessorFactory<T>, P extends PreProcessor<T>> void handlePreProcess(Repository repository, T processedObject,
                                                                                                 Collection<F> factories,
                                                                                                 Collection<P> preProcessors) {
    PreProcessorHandler<T> handler = new PreProcessorHandler<T>(factories, preProcessors, repository);
    handler.callPreProcessors(processedObject);
    handler.callPreProcessorFactories(processedObject);
  }

  private <T, I extends Iterable<T>, F extends PreProcessorFactory<T>, P extends PreProcessor<T>> void handlePreProcessForIterable(Repository repository, I processedObjects,
                                                                                                 Collection<F> factories,
                                                                                                 Collection<P> preProcessors) {
    PreProcessorHandler<T> handler = new PreProcessorHandler<T>(factories, preProcessors, repository);
    handler.callPreProcessors(processedObjects);
    handler.callPreProcessorFactories(processedObjects);
  }

  private static class PreProcessorHandler<T>
  {
    private final Collection<? extends PreProcessorFactory<T>> preProcessorFactorySet;

    private final Collection<? extends PreProcessor<T>> preProcessorSet;

    private final Repository repository;

    public PreProcessorHandler(
      Collection<? extends PreProcessorFactory<T>> preProcessorFactorySet,
      Collection<? extends PreProcessor<T>> preProcessorSet,
      Repository repository)
    {
      this.preProcessorFactorySet = preProcessorFactorySet;
      this.preProcessorSet = preProcessorSet;
      this.repository = repository;
    }

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

  }

}
