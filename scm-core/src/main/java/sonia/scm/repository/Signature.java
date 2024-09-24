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

import lombok.Value;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

/**
 * Signature is the output of a signature verification.
 *
 * @since 2.4.0
 */
@Value
public class Signature implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String keyId;
  private final String type;
  private final SignatureStatus status;
  private final String owner;
  private final Set<Person> contacts;

  public Optional<String> getOwner() {
    return Optional.ofNullable(owner);
  }

}
