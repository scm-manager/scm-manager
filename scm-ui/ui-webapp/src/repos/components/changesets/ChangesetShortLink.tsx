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

import React from "react";
import { Link } from "react-router-dom";
import { Changeset } from "@scm-manager/ui-types";
import { Replacement, changesetShortLinkRegex } from "@scm-manager/ui-components";

const ChangesetShortLink: (changeset: Changeset, value: string) => Replacement[] = (changeset, value) => {
  const regex = new RegExp(changesetShortLinkRegex, "g");

  const replacements: Replacement[] = [];

  let m = regex.exec(value);
  while (m) {
    const namespace = m[1];
    const name = m[2];
    const revision = m[3];
    const link = `/repo/${namespace}/${name}/code/changeset/${revision}`;
    replacements.push({
      textToReplace: m[0],
      replacement: <Link to={link}>{m[0]}</Link>
    });
    m = regex.exec(value);
  }

  return replacements;
};

export default ChangesetShortLink;
