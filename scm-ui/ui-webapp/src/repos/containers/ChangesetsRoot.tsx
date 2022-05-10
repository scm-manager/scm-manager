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
import { Route, useRouteMatch, useHistory } from "react-router-dom";
import { Repository, Branch } from "@scm-manager/ui-types";
import CodeActionBar from "../codeSection/components/CodeActionBar";
import { urls } from "@scm-manager/ui-components";
import Changesets from "./Changesets";

type Props = {
  repository: Repository;
  baseUrl: string;
  branches?: Branch[];
  selectedBranch?: string;
};

const ChangesetRoot: FC<Props> = ({ repository, baseUrl, branches, selectedBranch }) => {
  const match = useRouteMatch();
  const history = useHistory();
  if (!repository) {
    return null;
  }

  const url = urls.stripEndingSlash(urls.escapeUrlForRoute(match.url));
  const defaultBranch = branches?.find((b) => b.defaultBranch === true);

  const isBranchAvailable = () => {
    return branches?.filter((b) => b.name === selectedBranch).length === 0;
  };

  const evaluateSwitchViewLink = () => {
    if (selectedBranch) {
      return `${baseUrl}/sources/${encodeURIComponent(selectedBranch)}/`;
    }
    return `${baseUrl}/sources/`;
  };

  const onSelectBranch = (branch?: Branch) => {
    if (branch) {
      const url = `${baseUrl}/branch/${encodeURIComponent(branch.name)}/changesets/`;
      history.push(url);
    } else {
      history.push(`${baseUrl}/changesets/`);
    }
  };

  return (
    <>
      <CodeActionBar
        branches={branches}
        selectedBranch={!isBranchAvailable() ? selectedBranch : defaultBranch?.name}
        onSelectBranch={onSelectBranch}
        switchViewLink={evaluateSwitchViewLink()}
      />
      <Route path={`${url}/:page?`}>
        <Changesets repository={repository} branch={branches?.filter((b) => b.name === selectedBranch)[0]} />
      </Route>
    </>
  );
};

export default ChangesetRoot;
