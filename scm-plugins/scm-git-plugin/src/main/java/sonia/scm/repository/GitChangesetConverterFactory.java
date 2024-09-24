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
