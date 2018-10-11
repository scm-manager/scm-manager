// @flow

import React from "react";
import type { Repository } from "@scm-manager/ui-types";
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
    const url = this.matchedUrl();

    return (
      <BranchChooser
        repository={this.props.repository}
        label={"Branches"}
        branchSelected={this.branchSelected}
      >
        <Changesets repository={this.props.repository} />
        {/*<RouteDelegate repository={this.props.repository} url={url} />*/}
      </BranchChooser>
    );
  }
}

function RouteDelegate(props) {
  return (
    <Route
      path={`${props.url}/:branch/changesets/:page?`}
      component={() => <Changesets repository={props.repository} {...props} />}
    />
  );
}

export default withRouter(BranchRoot);
