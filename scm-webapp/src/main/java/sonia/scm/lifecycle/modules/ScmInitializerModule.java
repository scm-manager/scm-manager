/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
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
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */


package sonia.scm.lifecycle.modules;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Initable;
import sonia.scm.SCMContext;

/**
 * Initializes all instances which are implementing the {@link Initable} interface.
 *
 * @author Sebastian Sdorra
 */
public class ScmInitializerModule extends AbstractModule {

  private static final Logger LOG = LoggerFactory.getLogger(ScmInitializerModule.class);

  @Override
  protected void configure() {
    bindListener(MoreMatchers.isSubtypeOf(Initable.class), new TypeListener() {
      @Override
      public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        encounter.register((InjectionListener<I>) i -> {
          LOG.trace("initialize initable {}", i.getClass());

          Initable initable = (Initable) i;

          initable.init(SCMContext.getContext());
        });
      }
    });
  }

}
