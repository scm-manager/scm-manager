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

package sonia.scm.security.gpg;

import org.apache.shiro.SecurityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.pgpainless.PGPainless;
import org.pgpainless.key.generation.type.rsa.RsaLength;
import sonia.scm.repository.Person;
import sonia.scm.user.User;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

final class GPGKeyPairGenerator {

  static {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  private GPGKeyPairGenerator() {}

  static PGPSecretKeyRing generateKeyPair() throws PGPException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    final User user = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
    final Person person = new Person(user.getDisplayName(), user.getMail());

    return PGPainless.generateKeyRing().simpleRsaKeyRing(person.toString(),  RsaLength._4096);
  }
}
