import React from "react";
import { Route, withRouter } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import Changesets from "./Changesets";

type Props = WithTranslation & {
  repository: Repository;
  selectedBranch: string;
  baseUrl: string;

  // Context props
  history: any; // TODO flow type
  match: any;
};

class ChangesetsRoot extends React.Component<Props> {
  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 1);
    }
    return url;
  };

  render() {
    const { repository, match, selectedBranch } = this.props;

    if (!repository) {
      return null;
    }

    const url = this.stripEndingSlash(match.url);

    return (
      <div className="panel">
        <Route
          path={`${url}/:page?`}
          component={() => <Changesets repository={repository} branch={selectedBranch} />}
        />
      </div>
    );
  }
}

export default withRouter(withTranslation("repos")(ChangesetsRoot));
