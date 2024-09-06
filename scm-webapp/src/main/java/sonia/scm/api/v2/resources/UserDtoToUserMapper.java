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

package sonia.scm.api.v2.resources;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.user.User;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class UserDtoToUserMapper extends BaseDtoMapper {

  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "properties", ignore = true)
  public abstract User map(UserDto userDto, @Context String usedPassword);


  /**
   * depends on the use case the right password will be mapped.
   * The given Password in the context parameter will be set.
   * The mapper consumer have the control of what password should be set.
   * </p>
   * eg. for update user action the password will be set to the original password
   * for create user and change password actions the password is the user input
   *
   * @param usedPassword the password to be set
   * @param user         the target
   */
  @AfterMapping
  void overridePassword(@MappingTarget User user, @Context String usedPassword) {
    user.setPassword(usedPassword);
  }

}
