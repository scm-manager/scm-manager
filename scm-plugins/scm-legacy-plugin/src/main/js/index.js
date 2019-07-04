// @flow

import React from "react";
import { withRouter } from "react-router-dom";
import { binder } from "@scm-manager/ui-extensions";
import { ProtectedRoute, apiClient } from "@scm-manager/ui-components";
import DummyComponent from "./DummyComponent";

type Props = {
  authenticated?: boolean,

  //context objects
  history: History
};

class LegacyRepositoryRedirect extends React.Component<Props> {
  constructor(props: Props) {
    super(props);
  }

  redirectLegacyRepository() {
    const { history } = this.props;
    if (location.href && location.href.includes("#diffPanel;")) {
      let splittedUrl = location.href.split(";");
      let repoId = splittedUrl[1];
      let changeSetId = splittedUrl[2];

      apiClient.get("/legacy/repository/" + repoId)
        .then(response => response.json())
        .then(payload => history.push("/repo/" + payload.namespace + "/" + payload.name + "/changesets/" + changeSetId)
        );
    }
  }

  render() {
    const { authenticated } = this.props;

    return (
      <>
      {
        authenticated?
        this.redirectLegacyRepository():
        <ProtectedRoute
          path="/index.html"
          component={DummyComponent}
          authenticated={authenticated}
        />
      }
      </>
    );
  }
}

binder.bind("legacyRepository.redirect", withRouter(LegacyRepositoryRedirect));

