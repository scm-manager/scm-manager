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

package sonia.scm.repository.spi;

import org.junit.jupiter.api.Test;
import sonia.scm.repository.Repository;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.repository.spi.IntegrateChangesFromWorkdirException.CODE_WITHOUT_ADDITIONAL_MESSAGES;
import static sonia.scm.repository.spi.IntegrateChangesFromWorkdirException.CODE_WITH_ADDITIONAL_MESSAGES;
import static sonia.scm.repository.spi.IntegrateChangesFromWorkdirException.forMessage;
import static sonia.scm.repository.spi.IntegrateChangesFromWorkdirException.withPattern;

class IntegrateChangesFromWorkdirExceptionTest {

  private static final Repository REPOSITORY = new Repository("1", "git", "hitchhiker", "hog");

  @Test
  void shouldExtractMessagesWithDefaultPrefix() {
    IntegrateChangesFromWorkdirException exception =
      forMessage(REPOSITORY, "prefix [SCM] line 1\nprefix [SCM] line 2\nirrelevant line\n");

    assertThat(exception.getAdditionalMessages())
      .extracting("message")
      .containsExactly("line 1", "line 2");
    assertThat(exception.getCode()).isEqualTo(CODE_WITH_ADDITIONAL_MESSAGES);
  }

  @Test
  void shouldExtractMessagesWithCustomPattern() {
    IntegrateChangesFromWorkdirException exception =
      withPattern(Pattern.compile("-custom- (.*)"))
      .forMessage(REPOSITORY, "to be ignored\n-custom- line\n");

    assertThat(exception.getAdditionalMessages())
      .extracting("message")
      .containsExactly("line");
    assertThat(exception.getCode()).isEqualTo(CODE_WITH_ADDITIONAL_MESSAGES);
  }

  @Test
  void shouldCreateSpecialMessageForMissingAdditionalMessages() {
    IntegrateChangesFromWorkdirException exception =
      forMessage(REPOSITORY, "There is no message");

    assertThat(exception.getAdditionalMessages()).isEmpty();
    assertThat(exception.getCode()).isEqualTo(CODE_WITHOUT_ADDITIONAL_MESSAGES);
  }
}
