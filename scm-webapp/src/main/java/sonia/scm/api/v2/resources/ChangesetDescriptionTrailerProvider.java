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

import com.google.common.collect.ImmutableSet;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPreProcessor;
import sonia.scm.repository.ChangesetPreProcessorFactory;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.Trailer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Extension
public class ChangesetDescriptionTrailerProvider implements ChangesetPreProcessorFactory {

  private static final Collection<String> SUPPORTED_TRAILER_TYPES = ImmutableSet.of("Co-authored-by", "Reviewed-by", "Signed-off-by", "Committed-by");
  private static final Pattern PERSON_PATTERN = Pattern.compile("^\\W*(.*)\\W+<(.*)>\\W*$");

  @Override
  public ChangesetPreProcessor createPreProcessor(Repository repository) {
    return new TrailerChangesetPreProcessor();
  }

  private static class TrailerChangesetPreProcessor implements ChangesetPreProcessor {

    private final List<Trailer> trailers = new ArrayList<>();
    private final StringBuilder newDescription = new StringBuilder();

    @Override
    public void process(Changeset changeset) {

      try (Scanner scanner = new Scanner(changeset.getDescription())) {
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();

          String[] typeAndUser = line.split(":\\W");
          if (typeAndUser.length == 2) {
            String type = typeAndUser[0];
            String person = typeAndUser[1];
            if (!SUPPORTED_TRAILER_TYPES.contains(type) || !createTrailer(type, person)) {
              appendLine(scanner, line);
            }
          } else {
            appendLine(scanner, line);
          }
        }
      }
      changeset.addTrailers(trailers);
      changeset.setDescription(newDescription.toString());
    }

    public void appendLine(Scanner scanner, String line) {
      newDescription.append(line);
      if (scanner.hasNextLine()) {
        newDescription.append('\n');
      }
    }

    private boolean createTrailer(String type, String person) {
      Matcher matcher = PERSON_PATTERN.matcher(person.trim());
      if (matcher.matches()) {
        MatchResult matchResult = matcher.toMatchResult();
        trailers.add(new Trailer(type, new Person(matchResult.group(1), matchResult.group(2))));
        return true;
      } else {
        return false;
      }
    }
  }
}
