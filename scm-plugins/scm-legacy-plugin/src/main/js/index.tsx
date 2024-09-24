/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React from "react";
import { withRouter, RouteComponentProps } from "react-router-dom";
import { binder } from "@scm-manager/ui-extensions";
import { apiClient, ErrorBoundary, ErrorNotification, ProtectedRoute } from "@scm-manager/ui-components";
import DummyComponent from "./DummyComponent";
import { Links, Link } from "@scm-manager/ui-types";

type Props = RouteComponentProps & {
  authenticated?: boolean;
  links: Links;
};

type State = {
  error?: Error;
};

class LegacyRepositoryRedirect extends React.Component<Props, State> {
  constructor(props: Props, state: State) {
    super(props, state);
    this.state = {
      error: undefined
    };
  }

  handleError = (error: Error) => {
    this.setState({
      error
    });
  };

  redirectLegacyRepository() {
    const { history, links } = this.props;
    // eslint-disable-next-line no-restricted-globals
    if (location.href && location.href.includes("#diffPanel;")) {
      // eslint-disable-next-line no-restricted-globals
      const splittedUrl = location.href.split(";");
      const repoId = splittedUrl[1];
      const changeSetId = splittedUrl[2];

      const namespaceAndNameLink = links.nameAndNamespace as Link;

      apiClient
        .get(namespaceAndNameLink.href.replace("{id}", repoId))
        .then(response => response.json())
        .then(payload => history.push("/repo/" + payload.namespace + "/" + payload.name + "/changeset/" + changeSetId))
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
          <ProtectedRoute path="/index.html" component={DummyComponent} authenticated={authenticated} />
        )}
      </>
    );
  }
}

binder.bind("main.route", withRouter(LegacyRepositoryRedirect));
