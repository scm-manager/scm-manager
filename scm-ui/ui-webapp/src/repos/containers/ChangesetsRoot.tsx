import React from "react";
import { Route, withRouter, RouteComponentProps } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { Repository, Branch } from "@scm-manager/ui-types";
import Changesets from "./Changesets";
import { compose } from "redux";
import CodeActionBar from "../codeSection/components/CodeActionBar";

type Props = WithTranslation &
  RouteComponentProps & {
    repository: Repository;
    selectedBranch: string;
    baseUrl: string;
    branches: Branch[];
  };

class ChangesetsRoot extends React.Component<Props> {
  componentDidMount() {
    const { branches, baseUrl } = this.props;
    if (branches?.length > 0 && this.isSelectedBranchNotABranch()) {
      const defaultBranch = branches?.filter(b => b.defaultBranch === true)[0];
      this.props.history.push(`${baseUrl}/branch/${encodeURIComponent(defaultBranch.name)}/changesets/`);
    }
  }

  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 1);
    }
    return url;
  };

  isSelectedBranchNotABranch = () => {
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
      let url = `${baseUrl}/branch/${encodeURIComponent(branch.name)}/changesets/`;
      history.push(url);
    }
  };

  render() {
    const { repository, branches, match, selectedBranch } = this.props;

    if (!repository) {
      return null;
    }

    const url = this.stripEndingSlash(match.url);

    return (
      <>
        <CodeActionBar
          branches={branches}
          selectedBranch={!this.isSelectedBranchNotABranch() ? selectedBranch : undefined}
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
