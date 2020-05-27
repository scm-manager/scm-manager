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

package sonia.scm.api.v2.resources;

import com.google.common.collect.ImmutableList;
import sonia.scm.repository.ChangesetTrailerTypes;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.UserDisplayManager;

import javax.inject.Inject;
import java.util.*;
import java.util.regex.Pattern;

public class ChangesetTrailerExtractor {

  private final UserDisplayManager userDisplayManager;
  private final ChangesetTrailerTypes changesetTrailerTypes;

  @Inject
  public ChangesetTrailerExtractor(UserDisplayManager userDisplayManager, ChangesetTrailerTypes changesetTrailerTypes) {
    this.userDisplayManager = userDisplayManager;
    this.changesetTrailerTypes = changesetTrailerTypes;
  }

  Map<String, PersonDto> extractTrailersFromCommitMessage(String commitMessage) {

    HashMap<String, PersonDto> persons = new HashMap<>();

    try (Scanner scanner = new Scanner(commitMessage)) {
      scanner.useDelimiter(Pattern.compile("[\\n;]"));
      while (scanner.hasNext()) {
        String line = scanner.next();

        for (String trailerType : changesetTrailerTypes.getTrailerTypes()) {
          if (line.contains(trailerType)) {
            String mail = line.split("<|>")[1];
            persons.put(trailerType, createPersonDtoFromUser(mail));
          }
        }

/*        if (line.contains("Co-authored-by")) {
          persons.put("Co-authored-by", createPersonDtoFromUser(mail));
        }
        if (line.contains("Reviewed-by")) {
          persons.put("Reviewed-by", createPersonDtoFromUser(mail));
        }*/

      }
    }


    return persons;
  }

  private PersonDto createPersonDtoFromUser(String mail) {
    DisplayUser displayUser = userDisplayManager.autocomplete(mail).iterator().next();
    PersonDto personDto = new PersonDto();
    personDto.setName(displayUser.getDisplayName());
    personDto.setMail(displayUser.getMail());
    return personDto;
  }

  static class TrailerTypes implements ChangesetTrailerTypes {

    @Override
    public List<String> getTrailerTypes() {
      return ImmutableList.of("Co-authored-by", "Reviewed-by", "Signed-off-by", "Committed-by");
    }
  }
}
