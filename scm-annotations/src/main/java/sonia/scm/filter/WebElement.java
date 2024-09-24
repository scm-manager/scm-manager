/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.filter;


import sonia.scm.plugin.PluginAnnotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to register servlets and filters. The annotation is automatically 
 * picked up by the plugin registration processor of SCM-Manager.
 *
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
   */
  public boolean regex() default false;

  /**
   * Returns an array of init params.
   */
  public WebInitParam[] initParams() default {};
}
