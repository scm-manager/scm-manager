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

import org.apache.shiro.authc.credential.PasswordService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.user.User;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

public class UserDtoToUserMapperTest {

  @Mock
  private PasswordService passwordService;
  @InjectMocks
  private UserDtoToUserMapperImpl mapper;

  @Test
  public void shouldMapFields() {
    UserDto dto = createDefaultDto();
    User user = mapper.map(dto, "used password");
    assertEquals("abc" , user.getName());
    assertEquals("used password" , user.getPassword());
  }

  @Before
  public void init() {
    initMocks(this);
  }

  private UserDto createDefaultDto() {
    UserDto dto = new UserDto();
    dto.setName("abc");
    dto.setCreationDate(Instant.now());
    dto.setLastModified(null);
    return dto;
  }
}
