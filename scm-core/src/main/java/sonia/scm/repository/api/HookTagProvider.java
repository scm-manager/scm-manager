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

package sonia.scm.repository.api;

import java.util.List;
import sonia.scm.repository.Tag;

/**
 * The HookTagProvider returns information about tags during the
 * current hook.
 * 
 * @since 1.50
 */
public interface HookTagProvider {
    
  /**
   * Return all tags which are delivered during the current hook.
   * 
   * @return all tags of current hook
   */
  public List<Tag> getCreatedTags();
  
  /**
   * Return all tags which are deleted during the current hook.
   * 
   * @return all deleted tags of current hook
   */
  public List<Tag> getDeletedTags();
}
