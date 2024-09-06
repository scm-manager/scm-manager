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

package sonia.scm.group.cli;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.group.Group;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Mapper
interface GroupCommandBeanMapper {

  @Mapping(target = "membersList", ignore = true)
  GroupCommandBean map(Group group);

  @AfterMapping
  default void mapMembersList(@MappingTarget GroupCommandBean bean) {
    if (bean.getMembers() != null) {
      bean.setMembersList(String.join(", ", bean.getMembers()));
    }
  }

  default String mapTimestampToISODate(Long timestamp) {
    if (timestamp != null) {
      return DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(timestamp));
    }
    return null;
  }
}
