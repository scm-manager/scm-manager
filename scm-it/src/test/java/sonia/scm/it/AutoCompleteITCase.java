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
