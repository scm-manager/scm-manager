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
