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

package sonia.scm.repository.spi;


import org.junit.jupiter.api.Test;
import sonia.scm.repository.BlameLine;
import sonia.scm.repository.BlameResult;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class HgBlameCommandTest extends AbstractHgCommandTestBase
{

  @Test
  public void testGetBlameResult() throws IOException {
    BlameCommandRequest request = new BlameCommandRequest();

    request.setPath("a.txt");

    BlameResult result = createCommand().getBlameResult(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).isEqualTo(2);

    BlameLine line = result.getLine(0);

    checkFirstLine(line);
    line = result.getLine(1);

    assertThat(line.getLineNumber()).isEqualTo(2);
    assertThat(line.getRevision()).isEqualTo("2baab8e80280ef05a9aa76c49c76feca2872afb7");
    checkDate(line.getWhen());
    assertThat(line.getCode()).isEqualTo("line for blame");
    assertThat(line.getDescription()).isEqualTo("added new line for blame");
    assertThat(line.getAuthor().getName()).isEqualTo("Zaphod Beeblebrox");
    assertThat(line.getAuthor().getMail()).isEqualTo("zaphod.beeblebrox@hitchhiker.com");
  }

  @Test
  public void testGetBlameResultWithRevision() throws IOException {
    BlameCommandRequest request = new BlameCommandRequest();

    request.setPath("a.txt");
    request.setRevision("a9bacaf1b7fa0cebfca71fed4e59ed69a6319427");

    BlameResult result = createCommand().getBlameResult(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).isEqualTo(1);

    BlameLine line = result.getLine(0);

    checkFirstLine(line);
  }


  private void checkFirstLine(BlameLine line)
  {
    assertThat(line.getLineNumber()).isEqualTo(1);
    assertThat(line.getRevision()).isEqualTo("a9bacaf1b7fa0cebfca71fed4e59ed69a6319427");
    checkDate(line.getWhen());
    assertThat(line.getCode()).isEqualTo("a");
    assertThat(line.getDescription()).isEqualTo("added a and b files");
    assertThat(line.getAuthor().getName()).isEqualTo("Douglas Adams");
    assertThat(line.getAuthor().getMail()).isEqualTo("douglas.adams@hitchhiker.com");
  }


  private BlameCommand createCommand()
  {
    return new HgBlameCommand(cmdContext);
  }
}
