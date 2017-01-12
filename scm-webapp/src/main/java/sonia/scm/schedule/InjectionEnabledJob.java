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

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

/**
 * InjectionEnabledJob allows the execution of quartz jobs and enable injection on them.
 * 
 * @author Sebastian Sdorra <sebastian.sdorra@triology.de>
 * @since 1.47
 */
public class InjectionEnabledJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(InjectionEnabledJob.class);
  
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        Preconditions.checkNotNull(jec, "execution context is null");
      
        JobDetail detail = jec.getJobDetail();
        Preconditions.checkNotNull(detail, "job detail not provided");
        
        JobDataMap dataMap = detail.getJobDataMap();
        Preconditions.checkNotNull(dataMap, "job detail does not contain data map");
        
        Injector injector = (Injector) dataMap.get(Injector.class.getName());
        Preconditions.checkNotNull(injector, "data map does not contain injector");
           
        final Provider<Runnable> runnableProvider = (Provider<Runnable>) dataMap.get(Runnable.class.getName());
        if (runnableProvider == null) {
            throw new JobExecutionException("could not find runnable provider");
        }

        AdministrationContext ctx = injector.getInstance(AdministrationContext.class);
        ctx.runAsAdmin(new PrivilegedAction()
        {
          @Override
          public void run()
          {
            logger.trace("create runnable from provider");
            Runnable runnable = runnableProvider.get();
            logger.debug("execute injection enabled job {}", runnable.getClass());
            runnable.run();
          }
        });
    }


}
