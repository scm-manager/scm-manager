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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.RawParseUtils;
import sonia.scm.security.PublicKey;
import sonia.scm.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GitChangesetConverter implements Closeable {

  private final GPGSignatureResolver gpg;
  private final Multimap<ObjectId, String> tags;
  private final TreeWalk treeWalk;

  GitChangesetConverter(GPGSignatureResolver gpg, org.eclipse.jgit.lib.Repository repository, RevWalk revWalk) {
    this.gpg = gpg;
    this.tags = GitUtil.createTagMap(repository, revWalk);
    this.treeWalk = new TreeWalk(repository);
  }

  public Changeset createChangeset(RevCommit commit, String... branches) {
    return createChangeset(commit, Arrays.asList(branches));
  }

  public Changeset createChangeset(RevCommit commit, List<String> branches) {
    String id = commit.getId().name();
    List<String> parentList = null;
    RevCommit[] parents = commit.getParents();

    if (Util.isNotEmpty(parents)) {
      parentList = new ArrayList<>();

      for (RevCommit parent : parents) {
        parentList.add(parent.getId().name());
      }
    }

    PersonIdent authorIdent = commit.getAuthorIdent();
    PersonIdent committerIdent = commit.getCommitterIdent();
    Person author = createPersonFor(authorIdent);
    String message = commit.getFullMessage();

    if (message != null) {
      message = message.trim();
    }

    Changeset changeset = new Changeset(id, authorIdent.getWhenAsInstant().toEpochMilli(), author, message);
    if (!committerIdent.equals(authorIdent)) {
      changeset.addContributor(new Contributor(Contributor.COMMITTED_BY, createPersonFor(committerIdent), committerIdent.getWhenAsInstant()));
    }

    if (parentList != null) {
      changeset.setParents(parentList);
    }

    Collection<String> tagCollection = tags.get(commit.getId());

    if (Util.isNotEmpty(tagCollection)) {
      // create a copy of the tag collection to reduce memory on caching
      changeset.getTags().addAll(Lists.newArrayList(tagCollection));
    }

    changeset.setBranches(new ArrayList<>(branches));

    Signature signature = createSignature(commit);
    if (signature != null) {
      changeset.addSignature(signature);
    }

    return changeset;
  }

  private static final byte[] GPG_HEADER = {'g', 'p', 'g', 's', 'i', 'g'};

  private Signature createSignature(RevCommit commit) {
    byte[] raw = commit.getRawBuffer();

    int start = RawParseUtils.headerStart(GPG_HEADER, raw, 0);
    if (start < 0) {
      return null;
    }

    int end = RawParseUtils.headerEnd(raw, start);
    byte[] signature = Arrays.copyOfRange(raw, start, end);

    String publicKeyId = gpg.findPublicKeyId(signature);
    if (Strings.isNullOrEmpty(publicKeyId)) {
      // key not found
      return new Signature(publicKeyId, "gpg", SignatureStatus.NOT_FOUND, null, Collections.emptySet());
    }

    Optional<PublicKey> publicKeyById = gpg.findPublicKey(publicKeyId);
    if (publicKeyById.isEmpty()) {
      // key not found
      return new Signature(publicKeyId, "gpg", SignatureStatus.NOT_FOUND, null, Collections.emptySet());
    }

    PublicKey publicKey = publicKeyById.get();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      byte[] headerPrefix = Arrays.copyOfRange(raw, 0, start - GPG_HEADER.length - 1);
      baos.write(headerPrefix);

      byte[] headerSuffix = Arrays.copyOfRange(raw, end + 1, raw.length);
      baos.write(headerSuffix);
    } catch (IOException ex) {
      // this will never happen, because we are writing into memory
      throw new IllegalStateException("failed to write into memory", ex);
    }

    boolean verified = publicKey.verify(baos.toByteArray(), signature);
    return new Signature(
      publicKeyId,
      "gpg",
      verified ? SignatureStatus.VERIFIED : SignatureStatus.INVALID,
      publicKey.getOwner().orElse(null),
      publicKey.getContacts()
    );
  }

  public Person createPersonFor(PersonIdent personIndent) {
    return new Person(personIndent.getName(), personIndent.getEmailAddress());
  }

  @Override
  public void close() {
    GitUtil.release(treeWalk);
  }

}
