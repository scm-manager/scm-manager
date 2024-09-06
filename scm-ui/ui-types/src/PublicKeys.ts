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

import { HalRepresentation, HalRepresentationWithEmbedded } from "./hal";

export type PublicKeysCollection = HalRepresentationWithEmbedded<{
  keys: PublicKey[];
}>;

export type PublicKeyBase = {
  displayName: string;
  raw: string;
};

export type PublicKey = HalRepresentation & PublicKeyBase & {
  id: string;
  created?: string;
};

export type PublicKeyCreation = PublicKeyBase;
