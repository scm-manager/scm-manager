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

import com.google.inject.Injector;
import com.google.inject.Provider;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 * Unit tests for {@link QuartzScheduler}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class QuartzSchedulerTest {

  @Mock
  private Injector injector;
  
  @Mock
  private org.quartz.Scheduler quartzScheduler;
  
  private QuartzScheduler scheduler;
  
  @Before
  public void setUp()
  {
    scheduler = new QuartzScheduler(injector, quartzScheduler);
  }
  
  /**
   * Tests {@link QuartzScheduler#schedule(java.lang.String, java.lang.Runnable)}.
   * 
   * @throws SchedulerException 
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testSchedule() throws SchedulerException
  { 
    DummyRunnable dr = new DummyRunnable();
    Task task = scheduler.schedule("42 2 * * * ?", dr);
    assertNotNull(task);
    
    ArgumentCaptor<JobDetail> detailCaptor = ArgumentCaptor.forClass(JobDetail.class);
    ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
    verify(quartzScheduler).scheduleJob(detailCaptor.capture(), triggerCaptor.capture());
    
    Trigger trigger = triggerCaptor.getValue();
    assertThat(trigger, is(instanceOf(CronTrigger.class)));
    CronTrigger cron = (CronTrigger) trigger;
    assertEquals("42 2 * * * ?", cron.getCronExpression());
    
    JobDetail detail = detailCaptor.getValue();
    assertEquals(InjectionEnabledJob.class, detail.getJobClass());
    Provider<Runnable> runnable = (Provider<Runnable>) detail.getJobDataMap().get(Runnable.class.getName());
    assertNotNull(runnable);
    assertSame(dr, runnable.get());
    assertEquals(injector, detail.getJobDataMap().get(Injector.class.getName()));
  }

  /**
   * Tests {@link QuartzScheduler#schedule(java.lang.String, java.lang.Class)}.
   * 
   * @throws SchedulerException 
   */
  @Test
  public void testScheduleWithClass() throws SchedulerException
  { 
    scheduler.schedule("42 * * * * ?", DummyRunnable.class);
    
    verify(injector).getProvider(DummyRunnable.class);
    
    ArgumentCaptor<JobDetail> detailCaptor = ArgumentCaptor.forClass(JobDetail.class);
    ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
    verify(quartzScheduler).scheduleJob(detailCaptor.capture(), triggerCaptor.capture());
    
    Trigger trigger = triggerCaptor.getValue();
    assertThat(trigger, is(instanceOf(CronTrigger.class)));
    CronTrigger cron = (CronTrigger) trigger;
    assertEquals("42 * * * * ?", cron.getCronExpression());
    
    JobDetail detail = detailCaptor.getValue();
    assertEquals(InjectionEnabledJob.class, detail.getJobClass());
    assertEquals(injector, detail.getJobDataMap().get(Injector.class.getName()));
  }

  /**
   * Tests {@link QuartzScheduler#init(sonia.scm.SCMContextProvider)}.
   * 
   * @throws SchedulerException 
   */
  @Test
  public void testInit() throws SchedulerException
  {
    when(quartzScheduler.isStarted()).thenReturn(Boolean.FALSE);
    scheduler.init(null);
    verify(quartzScheduler).start();
  }
  
  /**
   * Tests {@link QuartzScheduler#init(sonia.scm.SCMContextProvider)} when the underlying scheduler is already started.
   * 
   * @throws SchedulerException 
   */  
  @Test
  public void testInitAlreadyRunning() throws SchedulerException
  {
    when(quartzScheduler.isStarted()).thenReturn(Boolean.TRUE);
    scheduler.init(null);
    verify(quartzScheduler, never()).start();
  }
  
  /**
   * Tests {@link QuartzScheduler#init(sonia.scm.SCMContextProvider)} when the underlying scheduler throws an exception.
   * 
   * @throws SchedulerException 
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testInitException() throws SchedulerException
  {
    when(quartzScheduler.isStarted()).thenThrow(SchedulerException.class);
    scheduler.init(null);
    verify(quartzScheduler, never()).start();
  }
  
  /**
   * Tests {@link QuartzScheduler#close()}.
   * 
   * @throws IOException
   * @throws SchedulerException 
   */
  @Test
  public void testClose() throws IOException, SchedulerException
  {
    when(quartzScheduler.isStarted()).thenReturn(Boolean.TRUE);
    scheduler.close();
    verify(quartzScheduler).shutdown();
  }
  
  /**
   * Tests {@link QuartzScheduler#close()} when the underlying scheduler is not running.
   * 
   * @throws IOException
   * @throws SchedulerException 
   */
  @Test
  public void testCloseNotRunning() throws IOException, SchedulerException
  {
    when(quartzScheduler.isStarted()).thenReturn(Boolean.FALSE);
    scheduler.close();
    verify(quartzScheduler, never()).shutdown();
  }
  
  /**
   * Tests {@link QuartzScheduler#close()} when the underlying scheduler throws an exception.
   * 
   * @throws IOException
   * @throws SchedulerException 
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testCloseException() throws IOException, SchedulerException
  {
    when(quartzScheduler.isStarted()).thenThrow(SchedulerException.class);
    scheduler.close();
    verify(quartzScheduler, never()).shutdown();
  }

  
  public static class DummyRunnable implements Runnable {

    @Override
    public void run()
    {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
  }
}