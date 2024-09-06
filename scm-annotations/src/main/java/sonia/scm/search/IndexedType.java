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

package sonia.scm.search;

import com.google.common.annotations.Beta;
import sonia.scm.plugin.PluginAnnotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark an field object should be indexed.
 *
 * @since 2.21.0
 */
@Beta
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@PluginAnnotation("indexed-type")
public @interface IndexedType {
  /**
   * Returns the name of the indexed object.
   * Default is the simple name of the indexed class, with the first char is lowercase.
   *
   * @return name of the index object or an empty string which indicates that the default should be used.
   */
  String value() default "";

  /**
   * Returns the required permission for searching this type.
   * Default means that every user is able to search that type of data.
   * Note: Every entry in the index has its own permission.
   *
   * @return required permission for searching this type.
   */
  String permission() default "";

  /**
   * If this is <code>true</code>, objects of this type will be available to be searched for in
   * the scope of a single repository or a namespace. This implies, that the id for this type
   * has to have a repository set that can be queried. This implicitly enables the search in
   * the scope of a namespace, too (so implicitly sets {@link #namespaceScoped()}
   * <code>true</code>).
   *
   * @return <code>true</code>, if this object shall be available to be searched for in the
   * scope of a repository.
   *
   * @since 2.38.0
   */
  boolean repositoryScoped() default false;

  /**
   * If this is <code>true</code>, objects of this type will be available to be searched for in
   * the scope of a single namespace. This implies, that the id for this type has a repository
   * set that can be queried. If {@link #repositoryScoped()} is set to <code>true</code>, this
   * will be assumed to be <code>true</code>, too, so this does not have to be set explicitly
   * in this case.
   *
   * @return <code>true</code>, if this object shall be available to be searched for in the
   * scope of a namespace.
   *
   * @since 2.38.0
   */
  boolean namespaceScoped() default false;
}
