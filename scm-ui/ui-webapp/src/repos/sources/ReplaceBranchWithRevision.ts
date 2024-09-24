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

/**
 * Takes a given path to a source file and replaces the branch with a concrete revision.
 * This allows for the resulting url to act as a permalink.
 */
export default function replaceBranchWithRevision(path: string, revision: string) {
  const pathParts = path.split("/");
  pathParts[6] = revision; // The branch is at the 7th position in the url
  return pathParts.join("/");
}
