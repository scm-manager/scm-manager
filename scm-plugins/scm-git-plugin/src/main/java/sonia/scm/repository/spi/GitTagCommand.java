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

import com.google.common.base.Strings;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.RawParseUtils;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Signature;
import sonia.scm.repository.SignatureStatus;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.TagDeleteRequest;
import sonia.scm.repository.api.TagCreateRequest;
import sonia.scm.security.GPG;
import sonia.scm.security.PublicKey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class GitTagCommand extends AbstractGitCommand implements TagCommand {
  private final GPG gpg;

  GitTagCommand(GitContext context, GPG gpg) {
    super(context);
    this.gpg = gpg;
  }

  @Override
  public Tag create(TagCreateRequest request) {
    try (Git git = new Git(context.open())) {
      Tag tag;
      String revision = request.getRevision();

      RevObject revObject = null;
      Long tagTime = null;

      if (!Strings.isNullOrEmpty(revision)) {
        ObjectId id = git.getRepository().resolve(revision);

        try (RevWalk walk = new RevWalk(git.getRepository())) {
          revObject = walk.parseAny(id);
          tagTime = GitUtil.getTagTime(walk, id);
        }
      }

      Ref ref;

      if (revObject != null) {
        ref =
          git.tag()
            .setObjectId(revObject)
            .setTagger(new PersonIdent("SCM-Manager", "noreply@scm-manager.org"))
            .setName(request.getName())
            .call();
      } else {
        throw new InternalRepositoryException(repository, "could not create tag because revision does not exist");
      }

      ObjectId objectId;
      if (ref.isPeeled()) {
        objectId = ref.getPeeledObjectId();
      } else {
        objectId = ref.getObjectId();
      }
      tag = new Tag(request.getName(), objectId.toString(), tagTime);

      try (RevWalk walk = new RevWalk(git.getRepository())) {
        revObject = walk.parseTag(objectId);
        tag.addSignature(getTagSignature((RevTag) revObject));
      }

      return tag;
    } catch (IOException | GitAPIException ex) {
      throw new InternalRepositoryException(repository, "could not create tag " + request.getName(), ex);
    }
  }

  @Override
  public void delete(TagDeleteRequest request) {
    try (Git git = new Git(context.open())) {
      git.tagDelete().setTags(request.getName()).call();
    } catch (GitAPIException | IOException e) {
      throw new InternalRepositoryException(repository, "could not delete tag", e);
    }
  }

  private static final byte[] GPG_HEADER = {'g', 'p', 'g', 's', 'i', 'g'};

  private Signature getTagSignature(RevTag tag) {
    byte[] raw = tag.getFullMessage().getBytes();

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
    if (!publicKeyById.isPresent()) {
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
}
