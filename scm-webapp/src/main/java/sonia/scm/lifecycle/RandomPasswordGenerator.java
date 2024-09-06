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

package sonia.scm.lifecycle;

import org.apache.commons.lang.RandomStringUtils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

final class RandomPasswordGenerator {

  String createRandomPassword() {
    try {
      SecureRandom random = SecureRandom.getInstanceStrong();
      return RandomStringUtils.random(20, 0, 0, true, true, null, random);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Every Java distribution is required to support a strong secure random generator; this should not have happened", e);
    }
  }
}
