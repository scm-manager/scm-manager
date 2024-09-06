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

package sonia.scm.it;

import org.junit.Before;
import org.junit.Test;
import sonia.scm.it.utils.ScmRequests;
import sonia.scm.it.utils.TestData;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class AutoCompleteITCase {


  public static final String CREATED_USER_PREFIX = "user_";
  public static final String CREATED_GROUP_PREFIX = "group_";

  @Before
  public void init() {
    TestData.cleanup();
  }

  @Test
  public void adminShouldAutoComplete() {
    shouldAutocomplete(TestData.USER_SCM_ADMIN, TestData.USER_SCM_ADMIN);
  }

  @Test
  public void userShouldAutoComplete() {
    String username = "nonAdmin";
    String password = "pass";
    TestData.createUser(username, password, false, "xml", "email@e.de");
    shouldAutocomplete(username, password);
  }

  public void shouldAutocomplete(String username, String password) {
    createUsers();
    createGroups();
    ScmRequests.start()
      .requestIndexResource(username, password)
      .assertStatusCode(200)
      .requestAutoCompleteGroups("group*")
      .assertStatusCode(200)
      .assertAutoCompleteResults(assertAutoCompleteResult(CREATED_GROUP_PREFIX))
      .returnToPrevious()
      .requestAutoCompleteUsers("user*")
      .assertStatusCode(200)
      .assertAutoCompleteResults(assertAutoCompleteResult(CREATED_USER_PREFIX));
  }

  @SuppressWarnings("unchecked")
  private Consumer<List<Map>> assertAutoCompleteResult(String id) {
    return autoCompleteDtos -> {
      IntStream.range(0, 5).forEach(i -> {
        assertThat(autoCompleteDtos).as("return maximum 5 entries").hasSize(5);
        assertThat(autoCompleteDtos.get(i)).containsEntry("id", id + (i + 1));
        assertThat(autoCompleteDtos.get(i)).containsEntry("displayName", id + (i + 1));
      });
    };
  }

  private void createUsers() {
    IntStream.range(0, 6).forEach(i -> TestData.createUser(CREATED_USER_PREFIX + (i + 1), "pass", false, "xml", CREATED_USER_PREFIX + (i + 1) + "@scm-manager.org"));
  }

  private void createGroups() {
    IntStream.range(0, 6).forEach(i -> TestData.createGroup(CREATED_GROUP_PREFIX + (i + 1), CREATED_GROUP_PREFIX + (i + 1)));
  }

}
