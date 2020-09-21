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
import { Route, withRouter, RouteComponentProps } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { Repository, Branch } from "@scm-manager/ui-types";
import Changesets from "./Changesets";
import { compose } from "redux";
import CodeActionBar from "../codeSection/components/CodeActionBar";
import { urls } from "@scm-manager/ui-components";

type Props = WithTranslation &
  RouteComponentProps & {
    repository: Repository;
    selectedBranch: string;
    baseUrl: string;
    branches: Branch[];
  };

class ChangesetsRoot extends React.Component<Props> {
  isBranchAvailable = () => {
    const { branches, selectedBranch } = this.props;
    return branches?.filter(b => b.name === selectedBranch).length === 0;
  };

  evaluateSwitchViewLink = () => {
    const { baseUrl, selectedBranch } = this.props;
    if (selectedBranch) {
      return `${baseUrl}/sources/${encodeURIComponent(selectedBranch)}/`;
    }
    return `${baseUrl}/sources/`;
  };

  onSelectBranch = (branch?: Branch) => {
    const { baseUrl, history } = this.props;
    if (branch) {
      const url = `${baseUrl}/branch/${encodeURIComponent(branch.name)}/changesets/`;
      history.push(url);
    } else {
      history.push(`${baseUrl}/changesets/`);
    }
  };

  render() {
    const { repository, branches, match, selectedBranch } = this.props;

    if (!repository) {
      return null;
    }

    const url = urls.stripEndingSlash(match.url);
    const defaultBranch = branches?.find(b => b.defaultBranch === true);

    return (
      <>
        <CodeActionBar
          branches={branches}
          selectedBranch={!this.isBranchAvailable() ? selectedBranch : defaultBranch?.name}
          onSelectBranch={this.onSelectBranch}
          switchViewLink={this.evaluateSwitchViewLink()}
        />
        <div className="panel">
          <Route
            path={`${url}/:page?`}
            component={() => (
              <Changesets repository={repository} branch={branches?.filter(b => b.name === selectedBranch)[0]} />
            )}
          />
        </div>
      </>
    );
  }
}

export default compose(withRouter, withTranslation("repos"))(ChangesetsRoot);
