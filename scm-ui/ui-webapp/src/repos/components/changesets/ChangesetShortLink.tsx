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
      replacement: <Link to={link}>{m[0]}</Link>,
    });
    m = regex.exec(value);
  }

  return replacements;
};

export default ChangesetShortLink;
