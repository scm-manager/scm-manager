// @flow

import React from "react";
import type { Repository, Branch } from "@scm-manager/ui-types";
import BranchChooser from "./BranchChooser";
import { Route, withRouter } from "react-router-dom";
import Changesets from "./Changesets";

type Props = {
  repository: Repository,
  history: History,
  match: any
};

class BranchRoot extends React.Component<Props> {
  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 2);
    }
    return url;
  };

  matchedUrl = () => {
    return this.stripEndingSlash(this.props.match.url);
  };

  branchSelected = (branchName: string) => {
    const url = this.matchedUrl();
    if (branchName === "") {
      this.props.history.push(`${url}/changesets/`);
    } else {
      this.props.history.push(
        `${url}/${encodeURIComponent(branchName)}/changesets/`
      );
    }
  };

  render() {
    const { repository } = this.props;
    const url = this.matchedUrl();
    if (!repository) {
      return null;
    }
    return (
      <BranchChooser
        repository={this.props.repository}
        label={"Branches"}
        branchSelected={this.branchSelected}
      >
        <RouteDelegate repository={this.props.repository} url={url} />
      </BranchChooser>
    );
  }
}

type RDProps = {
  repository: Repository,
  branch: Branch,
  url: string
};

class RouteDelegate extends React.Component<RDProps> {
  shouldComponentUpdate(nextProps: RDProps, nextState: any) {
    return (
      nextProps.repository !== this.props.repository ||
      nextProps.branch !== this.props.branch ||
      nextProps.url !== this.props.url
    );
  }

  render() {
    const { url, repository, branch } = this.props;
    return (
      <Route
        exact
        path={`${url}/:branch/changesets/:page?`}
        component={() => <Changesets repository={repository} branch={branch} />}
      />
    );
  }
}

export default withRouter(BranchRoot);
