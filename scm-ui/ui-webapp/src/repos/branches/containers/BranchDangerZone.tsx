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

import React, { FC } from "react";
import { Branch, Repository } from "@scm-manager/ui-types";
import { DangerZone, Subtitle } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import DeleteBranch from "./DeleteBranch";

type Props = {
  repository: Repository;
  branch: Branch;
};

const BranchDangerZone: FC<Props> = ({ repository, branch }) => {
  const [t] = useTranslation("repos");

  const dangerZone = [];

  if (branch?._links?.delete) {
    dangerZone.push(<DeleteBranch repository={repository} branch={branch} key={dangerZone.length} />);
  }

  if (dangerZone.length === 0) {
    return null;
  }

  return (
    <>
      <hr />
      <Subtitle subtitle={t("branch.dangerZone")} />
      <DangerZone className="px-4 py-5">{dangerZone}</DangerZone>
    </>
  );
};

export default BranchDangerZone;
