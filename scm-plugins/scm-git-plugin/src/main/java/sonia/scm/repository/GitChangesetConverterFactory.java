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

package sonia.scm.repository;

import jakarta.inject.Inject;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import sonia.scm.security.GPG;
import sonia.scm.security.PublicKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class GitChangesetConverterFactory {

  private final GPG gpg;

  @Inject
  public GitChangesetConverterFactory(GPG gpg) {
    this.gpg = gpg;
  }

  public GitChangesetConverter create(Repository repository) {
    return builder(repository).create();
  }

  public GitChangesetConverter create(Repository repository, RevWalk revWalk) {
    return builder(repository).withRevWalk(revWalk).create();
  }

  public Builder builder(Repository repository) {
    return new Builder(gpg, repository);
  }

  public static class Builder {

    private final GPG gpg;
    private final Repository repository;
    private RevWalk revWalk;
    private final List<PublicKey> additionalPublicKeys = new ArrayList<>();

    private Builder(GPG gpg, Repository repository) {
      this.gpg = gpg;
      this.repository = repository;
    }

    public Builder withRevWalk(RevWalk revWalk) {
      this.revWalk = revWalk;
      return this;
    }

    public Builder withAdditionalPublicKeys(PublicKey... publicKeys) {
      additionalPublicKeys.addAll(Arrays.asList(publicKeys));
      return this;
    }

    public Builder withAdditionalPublicKeys(Collection<PublicKey> publicKeys) {
      additionalPublicKeys.addAll(publicKeys);
      return this;
    }

    public GitChangesetConverter create() {
      return new GitChangesetConverter(
        new GPGSignatureResolver(gpg, additionalPublicKeys),
        repository,
        revWalk != null ? revWalk : new RevWalk(repository)
      );
    }

  }

}
