/***
 * Copyright (c) 2015, Sebastian Sdorra
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
 * https://bitbucket.org/sdorra/scm-manager
 * 
 */

package sonia.scm.schedule;

import com.google.common.base.Throwables;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Initable;
import sonia.scm.SCMContextProvider;

import javax.inject.Inject;
import java.io.IOException;

/**
 * {@link Scheduler} which uses the quartz scheduler.
 * 
 * @author Sebastian Sdorra
 * @since 1.47
 * 
 * @see <a href="http://www.quartz-scheduler.org/">Quartz Job Scheduler</a>
 */
@Singleton
public class QuartzScheduler implements Scheduler, Initable {
  
  private static final Logger logger = LoggerFactory.getLogger(QuartzScheduler.class);
  
  private final Injector injector;
  private final org.quartz.Scheduler scheduler;

  /**
   * Creates a new quartz scheduler.
   * 
   * @param injector injector
   */
  @Inject
  public QuartzScheduler(Injector injector)
  {
    this.injector = injector;
    
    // get default scheduler
    try {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
    } catch (SchedulerException ex) {
        throw Throwables.propagate(ex);
    }
  }

  /**
   * Creates a new quartz scheduler. This constructor is only for testing.
   * 
   * @param injector injector
   * @param scheduler quartz scheduler
   */
  QuartzScheduler(Injector injector, org.quartz.Scheduler scheduler)
  {
    this.injector = injector;
    this.scheduler = scheduler;
  }
  
  @Override
  public void init(SCMContextProvider context)
  {
    try 
    {
      if (!scheduler.isStarted())
      {
        scheduler.start();
      }
    } 
    catch (SchedulerException ex)
    {
      logger.error("can not start scheduler", ex);
    }
  }

  @Override
  public void close() throws IOException
  {
    try
    {
      if (scheduler.isStarted()){
        scheduler.shutdown();
      }
    } 
    catch (SchedulerException ex)
    {
      logger.error("can not stop scheduler", ex);
    }
  }
  
  @Override
  public Task schedule(String expression, final Runnable runnable)
  {
    return schedule(expression, () -> runnable);
  }

  @Override
  public Task schedule(String expression, Class<? extends Runnable> runnable)
  {
    return schedule(expression, injector.getProvider(runnable));
  }

  private Task schedule(String expression, Provider<? extends Runnable> provider){
    // create data map with injection provider for InjectionEnabledJob
    JobDataMap map = new JobDataMap();
    map.put(Runnable.class.getName(), provider);
    map.put(Injector.class.getName(), injector);

    // create job detail for InjectionEnabledJob with the provider for the annotated class
    JobDetail detail = JobBuilder.newJob(InjectionEnabledJob.class)
            .usingJobData(map)
            .build();

    // create a trigger with the cron expression from the annotation
    Trigger trigger = TriggerBuilder.newTrigger()
            .forJob(detail)
            .withSchedule(CronScheduleBuilder.cronSchedule(expression))
            .build();

    try {
        scheduler.scheduleJob(detail, trigger);
    } catch (SchedulerException ex) {
        throw Throwables.propagate(ex);
    }
    
    return new QuartzTask(scheduler, trigger.getJobKey());
  }


}
