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

package sonia.scm.repository.api;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.MirrorCommand;
import sonia.scm.repository.spi.MirrorCommandRequest;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * @since 2.19.0
 */
public final class MirrorCommandBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(MirrorCommandBuilder.class);

  private final MirrorCommand mirrorCommand;
  private final Repository targetRepository;

  private String sourceUrl;
  private List<Credential> credentials = emptyList();

  MirrorCommandBuilder(MirrorCommand mirrorCommand, Repository targetRepository) {
    this.mirrorCommand = mirrorCommand;
    this.targetRepository = targetRepository;
  }

  public MirrorCommandBuilder setCredentials(Credential credential, Credential... furtherCredentials) {
    this.credentials = new ArrayList<>();
    credentials.add(credential);
    credentials.addAll(asList(furtherCredentials));
    return this;
  }

  public MirrorCommandBuilder setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
    return this;
  }

  public MirrorCommandResult initialCall() {
    LOG.info("Creating mirror for {} in repository {}", sourceUrl, targetRepository);
    MirrorCommandRequest mirrorCommandRequest = createRequest();
    return mirrorCommand.mirror(mirrorCommandRequest);
  }

  public MirrorCommandResult update() {
    LOG.debug("Updating mirror for {} in repository {}", sourceUrl, targetRepository);
    MirrorCommandRequest mirrorCommandRequest = createRequest();
    return mirrorCommand.update(mirrorCommandRequest);
  }

  private MirrorCommandRequest createRequest() {
    MirrorCommandRequest mirrorCommandRequest = new MirrorCommandRequest();
    mirrorCommandRequest.setSourceUrl(sourceUrl);
    mirrorCommandRequest.setCredentials(credentials);
    Preconditions.checkArgument(mirrorCommandRequest.isValid(), "source url has to be specified");
    return mirrorCommandRequest;
  }
}
