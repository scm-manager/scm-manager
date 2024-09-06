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

import sonia.scm.security.NotPublicKeyException;
import sonia.scm.security.PublicKey;
import sonia.scm.security.PublicKeyParser;

import java.util.Collections;

import static sonia.scm.ContextEntry.ContextBuilder.noContext;

public class DefaultPublicKeyParser implements PublicKeyParser {
  @Override
  public PublicKey parse(String raw) {
    if (!raw.contains("PUBLIC KEY")) {
      throw new NotPublicKeyException(noContext(), "The provided key is not a public key");
    }

    Keys keys = Keys.resolve(raw);
    String master = keys.getMaster();

    return new DefaultPublicKey(master, null, raw, Collections.emptySet());
  }
}
