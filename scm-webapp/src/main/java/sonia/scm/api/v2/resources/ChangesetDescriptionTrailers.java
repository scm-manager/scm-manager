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
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetTrailers;
import sonia.scm.repository.Repository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

@Extension
public class ChangesetDescriptionTrailers implements ChangesetTrailers {

  private static final List<String> types = ImmutableList.of("Co-authored-by", "Reviewed-by", "Signed-off-by", "Committed-by");

  @Inject
  public ChangesetDescriptionTrailers() {}

  @Override
  public List<TrailerPersonDto> getTrailers(Repository repository, Changeset changeset) {
    List<TrailerPersonDto> persons = new ArrayList<>();

    try (Scanner scanner = new Scanner(changeset.getDescription())) {
      scanner.useDelimiter(Pattern.compile("[\\n]"));
      while (scanner.hasNext()) {
        String line = scanner.next();

        for (String trailerType : types) {
          if (line.contains(trailerType)) {
            TrailerPersonDto personDto = createPersonDtoFromUser(line);
            personDto.setTrailerType(trailerType);
            persons.add(personDto);
          }
        }
      }
    }
    return persons;
  }

  private TrailerPersonDto createPersonDtoFromUser(String line) {
    TrailerPersonDto personDto = new TrailerPersonDto();

    String[] splittedTrailer = line.split("[:<>]");

    if (splittedTrailer.length > 1) {
      personDto.setName(splittedTrailer[1].trim());
      if (splittedTrailer.length > 2) {
        personDto.setMail(splittedTrailer[2]);
      }
    }

    return personDto;
  }
}
