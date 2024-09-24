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

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.GpgSignature;
import org.eclipse.jgit.lib.GpgSigner;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.GPG;
import sonia.scm.security.PublicKey;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitChangesetConverterTest {

  private static Git git;

  @BeforeAll
  static void setUpRepository(@TempDir Path repositoryPath) throws GitAPIException {
    // we use the same repository for all tests to speed things up
    git = Git.init().setDirectory(repositoryPath.toFile()).call();
  }

  @AfterAll
  static void closeRepository() {
    git.close();
  }

  @Test
  void shouldConvertChangeset() throws GitAPIException, IOException {
    long now = System.currentTimeMillis() - 1000L;
    Changeset changeset = commit(
      "Tricia McMillan", "trillian@hitchhiker.com", "Added awesome markdown file"
    );
    assertThat(changeset.getId()).isNotEmpty();
    assertThat(changeset.getDate()).isGreaterThanOrEqualTo(now);
    assertThat(changeset.getDescription()).isEqualTo("Added awesome markdown file");

    Person author = changeset.getAuthor();
    assertThat(author.getName()).isEqualTo("Tricia McMillan");
    assertThat(author.getMail()).isEqualTo("trillian@hitchhiker.com");
  }

  private Changeset commit(String name, String mail, String message) throws GitAPIException, IOException {
    addRandomFileToRepository();

    RevCommit commit = git.commit()
      .setAuthor(name, mail)
      .setMessage(message)
      .call();

    GitChangesetConverterFactory converterFactory = GitTestHelper.createConverterFactory();
    return converterFactory.create(git.getRepository()).createChangeset(commit);
  }

  private void addRandomFileToRepository() throws IOException, GitAPIException {
    File directory = git.getRepository().getWorkTree();
    String name = UUID.randomUUID().toString();
    File file = new File(directory, name + ".md");
    Files.write(file.toPath(), ("# Greetings\n\nFrom " + name).getBytes(StandardCharsets.UTF_8));
    git.add().addFilepattern(name + ".md").call();
  }

  @Nested
  class SignatureTests {

    @Mock
    private GPG gpg;
    @Mock
    private PublicKey publicKey;

    private PGPKeyPair keyPair;
    private GpgSigner defaultSigner;

    @BeforeEach
    void setUpTestingSignerAndCaptureDefault() throws Exception {
      defaultSigner = GpgSigner.getDefault();
      // we use the same keypair for all tests to speed things up a little bit
      if (keyPair == null) {
        keyPair = createKeyPair();
        GpgSigner.setDefault(new TestingGpgSigner(keyPair));
      }
    }

    @AfterEach
    void restoreDefaultSigner() {
      GpgSigner.setDefault(defaultSigner);
    }

    @Test
    void shouldReturnUnknownSignature() throws Exception {
      String identity = "0xAWESOMExBOB";
      when(gpg.findPublicKeyId(any())).thenReturn(identity);

      Signature signature = addSignedCommitAndReturnSignature(identity);
      assertThat(signature).isEqualTo(new Signature(identity, "gpg", SignatureStatus.NOT_FOUND, null, Collections.emptySet()));
    }

    @Test
    void shouldReturnKnownButInvalidSignature() throws Exception {
      String identity = "0xAWESOMExBOB";
      String owner = "BobTheSigner";
      setPublicKey(identity, owner, false);

      Signature signature = addSignedCommitAndReturnSignature(identity);
      assertThat(signature).isEqualTo(new Signature(identity, "gpg", SignatureStatus.INVALID, owner, Collections.emptySet()));
    }

    @Test
    void shouldReturnValidSignature() throws Exception {
      String identity = "0xAWESOMExBOB";
      String owner = "BobTheSigner";
      setPublicKey(identity, owner, true);

      Signature signature = addSignedCommitAndReturnSignature(identity);
      assertThat(signature).isEqualTo(new Signature(identity, "gpg", SignatureStatus.VERIFIED, owner, Collections.emptySet()));
    }

    @Test
    void shouldPassDataAndSignatureForVerification() throws Exception {
      setPublicKey("0x42", "Me", true);
      addSignedCommitAndReturnSignature("0x42");

      ArgumentCaptor<byte[]> dataCaptor = ArgumentCaptor.forClass(byte[].class);
      ArgumentCaptor<byte[]> signatureCaptor = ArgumentCaptor.forClass(byte[].class);
      verify(publicKey).verify(dataCaptor.capture(), signatureCaptor.capture());

      String data = new String(dataCaptor.getValue());
      assertThat(data).contains("author Bob The Signer <sign@bob.de>");

      String signature = new String(signatureCaptor.getValue());
      assertThat(signature).contains("BEGIN PGP SIGNATURE", "END PGP SIGNATURE");
    }

    @Test
    void shouldNotReturnSignatureForNonSignedCommit() throws GitAPIException, IOException {
      Changeset changeset = commit("Bob", "unsigned@bob.de", "not signed");
      assertThat(changeset.getSignatures()).isEmpty();
    }

    private void setPublicKey(String id, String owner, boolean valid) {
      when(gpg.findPublicKeyId(any())).thenReturn(id);
      when(gpg.findPublicKey(id)).thenReturn(Optional.of(publicKey));

      when(publicKey.getOwner()).thenReturn(Optional.of(owner));
      when(publicKey.verify(any(byte[].class), any(byte[].class))).thenReturn(valid);
    }

    private Signature addSignedCommitAndReturnSignature(String keyIdentity) throws IOException, GitAPIException {
      RevCommit commit = addSignedCommit(keyIdentity);
      GitChangesetConverterFactory factory = new GitChangesetConverterFactory(gpg);
      GitChangesetConverter converter = factory.create(git.getRepository());

      List<Signature> signatures = converter.createChangeset(commit).getSignatures();
      assertThat(signatures).isNotEmpty().hasSize(1);

      return signatures.get(0);
    }

    private RevCommit addSignedCommit(String keyIdentity) throws IOException, GitAPIException {
      addRandomFileToRepository();
      return git.commit()
        .setAuthor("Bob The Signer", "sign@bob.de")
        .setMessage("Signed from Bob")
        .setSign(true)
        .setSigningKey(keyIdentity)
        .call();
    }


  }

  private PGPKeyPair createKeyPair() throws PGPException, NoSuchProviderException, NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
    // we use a small key size to speedup test, a much larger size should be used for production
    keyPairGenerator.initialize(512);
    KeyPair pair = keyPairGenerator.generateKeyPair();
    return new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, pair, new Date());
  }

  private static class TestingGpgSigner extends GpgSigner {

    private final PGPKeyPair keyPair;

    TestingGpgSigner(PGPKeyPair keyPair) {
      this.keyPair = keyPair;
    }

    @Override
    public boolean canLocateSigningKey(String gpgSigningKey, PersonIdent committer, CredentialsProvider credentialsProvider) {
      return true;
    }

    @Override
    public void sign(CommitBuilder commit, String gpgSigningKey,
                     PersonIdent committer, CredentialsProvider credentialsProvider) {
      try {
        if (keyPair == null) {
          throw new JGitInternalException(JGitText.get().unableToSignCommitNoSecretKey);
        }

        PGPPrivateKey privateKey = keyPair.getPrivateKey();

        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
          new JcaPGPContentSignerBuilder(
            keyPair.getPublicKey().getAlgorithm(),
            HashAlgorithmTags.SHA256).setProvider(BouncyCastleProvider.PROVIDER_NAME)
        );
        signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privateKey);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (BCPGOutputStream out = new BCPGOutputStream(new ArmoredOutputStream(buffer))) {
          signatureGenerator.update(commit.build());
          signatureGenerator.generate().encode(out);
        }
        commit.setGpgSignature(new GpgSignature(buffer.toByteArray()));
      } catch (PGPException | IOException e) {
        throw new JGitInternalException(e.getMessage(), e);
      }
    }

  }

  // register bouncy castle provider on load
  static {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }
}


