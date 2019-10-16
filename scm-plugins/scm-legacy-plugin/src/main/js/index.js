// @flow
import React from "react";
import {withRouter} from "react-router-dom";
import {binder} from "@scm-manager/ui-extensions";
import {apiClient, ErrorBoundary, ErrorNotification, ProtectedRoute} from "@scm-manager/ui-components";
import DummyComponent from "./DummyComponent";
import type {Links} from "@scm-manager/ui-types";

type Props = {
  authenticated?: boolean,
  links: Links,

  //context objects
  history: History
};

type State = {
  error?: Error
};

class LegacyRepositoryRedirect extends React.Component<Props, State> {
  constructor(props: Props, state: State) {
    super(props, state);
    this.state = { error: undefined };
  }

  handleError = (error: Error) => {
    this.setState({
      error
    });
  };

  redirectLegacyRepository() {
    const { history, links } = this.props;
    if (location.href && location.href.includes("#diffPanel;")) {
      let splittedUrl = location.href.split(";");
      let repoId = splittedUrl[1];
      let changeSetId = splittedUrl[2];

      apiClient
        .get(links.nameAndNamespace.href.replace("{id}", repoId))
        .then(response => response.json())
        .then(payload =>
          history.push(
            "/repo/" +
              payload.namespace +
              "/" +
              payload.name +
              "/changeset/" +
              changeSetId
          )
        )
        .catch(this.handleError);
    }
  }

  render() {
    const { authenticated } = this.props;
    const { error } = this.state;

    if (error) {
      return (
        <section className="section">
          <div className="container">
            <ErrorBoundary>
              <ErrorNotification error={error} />
            </ErrorBoundary>
          </div>
        </section>
      );
    }

    return (
      <>
        {authenticated ? (
          this.redirectLegacyRepository()
        ) : (
          <ProtectedRoute
            path="/index.html"
            component={DummyComponent}
            authenticated={authenticated}
          />
        )}
      </>
    );
  }
}

binder.bind("main.route", withRouter(LegacyRepositoryRedirect));
