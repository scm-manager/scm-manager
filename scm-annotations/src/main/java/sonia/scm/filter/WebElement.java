/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
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



package sonia.scm.filter;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.plugin.PluginAnnotation;

//~--- JDK imports ------------------------------------------------------------

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to register servlets and filters. The annotation is automatically 
 * picked up by the plugin registration processor of SCM-Manager.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Documented
@Target(ElementType.TYPE)
@PluginAnnotation("web-element")
@Retention(RetentionPolicy.RUNTIME)
public @interface WebElement
{
  /**
   * Returns filter or servlet path pattern. The path can be specified as glob 
   * or as regex.
   * 
   * @return mapping path
   */
  public String value();

  /**
   * Returns additional filter or servlet path patterns. The path can be 
   * specified as glob or as regex.
   * 
   * @return mapping paths
   */
  public String[] morePatterns() default {};

  /**
   * Returns {@code true} if the path patterns are specified as regex patterns.
   * Default is {@code false}.
   * 
   * @return {@code true} if the path patterns are specified as regex patterns
   */
  public boolean regex() default false;

  /**
   * Returns an array of init params.
   * 
   * @return array of init params
   */
  public WebInitParam[] initParams() default {};
}
