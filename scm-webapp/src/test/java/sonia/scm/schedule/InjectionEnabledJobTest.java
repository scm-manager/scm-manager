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
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

/**
 * Unit tests for {@link InjectionEnabledJob}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class InjectionEnabledJobTest {

  @Mock
  private Injector injector;
  
  @Mock
  private JobDataMap dataMap;
  
  @Mock
  private JobDetail detail;
  
  @Mock
  private JobExecutionContext jec;
  
  @Mock
  private Provider<Runnable> runnable;
  
  @Mock
  private AdministrationContext context;
  
  @Rule
  public ExpectedException expected = ExpectedException.none();
  
  /**
   * Tests {@link InjectionEnabledJob#execute(org.quartz.JobExecutionContext)} without context.
   * 
   * @throws JobExecutionException 
   */
  @Test
  public void testExecuteWithoutContext() throws JobExecutionException
  {
    expected.expect(NullPointerException.class);
    expected.expectMessage("execution context");
    new InjectionEnabledJob().execute(null);
  }
  
  /**
   * Tests {@link InjectionEnabledJob#execute(org.quartz.JobExecutionContext)} without job detail.
   * 
   * @throws JobExecutionException 
   */
  @Test
  public void testExecuteWithoutJobDetail() throws JobExecutionException
  {
    expected.expect(NullPointerException.class);
    expected.expectMessage("detail");
    new InjectionEnabledJob().execute(jec);
  }
  
  /**
   * Tests {@link InjectionEnabledJob#execute(org.quartz.JobExecutionContext)} without data map.
   * 
   * @throws JobExecutionException 
   */
  @Test
  public void testExecuteWithoutDataMap() throws JobExecutionException
  {
    when(jec.getJobDetail()).thenReturn(detail);
    expected.expect(NullPointerException.class);
    expected.expectMessage("data map");
    new InjectionEnabledJob().execute(jec);
  }

  /**
   * Tests {@link InjectionEnabledJob#execute(org.quartz.JobExecutionContext)} without injector.
   * 
   * @throws JobExecutionException 
   */
  @Test
  public void testExecuteWithoutInjector() throws JobExecutionException
  {
    when(jec.getJobDetail()).thenReturn(detail);
    when(detail.getJobDataMap()).thenReturn(dataMap);
    expected.expect(NullPointerException.class);
    expected.expectMessage("injector");
    new InjectionEnabledJob().execute(jec);
  }
  
  /**
   * Tests {@link InjectionEnabledJob#execute(org.quartz.JobExecutionContext)} without runnable.
   * 
   * @throws JobExecutionException 
   */
  @Test
  public void testExecuteWithoutRunnable() throws JobExecutionException
  {
    when(jec.getJobDetail()).thenReturn(detail);
    when(detail.getJobDataMap()).thenReturn(dataMap);
    when(dataMap.get(Injector.class.getName())).thenReturn(injector);
    expected.expect(JobExecutionException.class);
    expected.expectMessage("runnable");
    new InjectionEnabledJob().execute(jec);
  }

  /**
   * Tests {@link InjectionEnabledJob#execute(org.quartz.JobExecutionContext)}.
   * 
   * @throws JobExecutionException 
   */  
  @Test
  public void testExecute() throws JobExecutionException
  {
    when(jec.getJobDetail()).thenReturn(detail);
    when(detail.getJobDataMap()).thenReturn(dataMap);
    when(dataMap.get(Injector.class.getName())).thenReturn(injector);
    when(dataMap.get(Runnable.class.getName())).thenReturn(runnable);
    when(injector.getInstance(AdministrationContext.class)).thenReturn(context);
    new InjectionEnabledJob().execute(jec);
    verify(context).runAsAdmin(Mockito.any(PrivilegedAction.class));
  }

}