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


package sonia.scm.event;

//~--- JDK imports ------------------------------------------------------------

import com.google.common.eventbus.Subscribe;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import sonia.scm.EagerSingleton;

/**
 * The subscriber annotation could be use to register listener classes to the 
 * {@link ScmEventBus}. A registered listener object can use the 
 * {@link Subscribe} annotation for methods to receive events, 
 * for more informations have a look at {@link ScmEventBus}. The annotated object 
 * is only bind to the {@link ScmEventBus}, if the the object is loaded from the
 * injection system of SCM-Manager. If your object is not loaded by the 
 * injection system you have to register your object by yourself with the 
 * {@link ScmEventBus#register(Object, boolean)} method. If you would like to 
 * register object which is a "listener only" object you have to use the 
 * {@link EagerSingleton} scope for your object. E.g.:
 * 
 * <pre><code>
 *   @Extension
 *   @Subscriber
 *   @EagerSingleton
 *   public class MyListener {
 * 
 *     @Subscribe
 *     public void receiveEvent(Event event){
 *       // do something with the event
 *     }
 * 
 *   }
 * </code></pre>
 *
 * @author Sebastian Sdorra
 * @since 1.23
 * 
 * @apiviz.landmark
 */
@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscriber
{
  boolean async() default true;
}
