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
import { withRouter, RouteComponentProps } from "react-router-dom";
import { binder, extensionPoints } from "@scm-manager/ui-extensions";
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
      error: undefined,
    };
  }

  handleError = (error: Error) => {
    this.setState({
      error,
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
        .then((response) => response.json())
        .then((payload) =>
          history.push("/repo/" + payload.namespace + "/" + payload.name + "/changeset/" + changeSetId)
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
          <ProtectedRoute path="/index.html" component={DummyComponent} authenticated={authenticated} />
        )}
      </>
    );
  }
}

binder.bind<extensionPoints.MainRouteExtension>("main.route", withRouter(LegacyRepositoryRedirect));
