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

import { Redirect, Route, Switch } from "react-router-dom";
import { Links, Me } from "@scm-manager/ui-types";

import Overview from "../repos/containers/Overview";
import Users from "../users/containers/Users";
import Login from "../containers/Login";
import Logout from "../containers/Logout";

import { ErrorBoundary, ProtectedRoute } from "@scm-manager/ui-components";
import { binder, ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";

import CreateUser from "../users/containers/CreateUser";
import SingleUser from "../users/containers/SingleUser";
import RepositoryRoot from "../repos/containers/RepositoryRoot";

import Groups from "../groups/containers/Groups";
import SingleGroup from "../groups/containers/SingleGroup";
import CreateGroup from "../groups/containers/CreateGroup";

import Admin from "../admin/containers/Admin";

import Profile from "./Profile";
import NamespaceRoot from "../repos/namespaces/containers/NamespaceRoot";
import ImportLog from "../repos/importlog/ImportLog";
import CreateRepositoryRoot from "../repos/containers/CreateRepositoryRoot";
import Search from "../search/Search";
import Syntax from "../search/Syntax";
import ExternalError from "./ExternalError";

type Props = {
  me: Me;
  authenticated?: boolean;
  links: Links;
};

const Main: FC<Props> = props => {
  const { authenticated, me } = props;
  const redirectUrlFactory = binder.getExtension<extensionPoints.MainRedirect>("main.redirect", props);
  let url = "/";
  if (authenticated) {
    url = "/repos/";
  }
  if (redirectUrlFactory) {
    url = redirectUrlFactory(props);
  }
  if (!me) {
    url = "/login";
  }
  return (
    <ErrorBoundary>
      <div className="main">
        <Switch>
          <Redirect exact from="/" to={url} />
          <Route exact path="/login" component={Login} />
          <Route path="/logout" component={Logout} />
          <Route path="/error/:code" component={ExternalError} />
          <Redirect exact strict from="/repos" to="/repos/" />
          <ProtectedRoute exact path="/repos/" component={Overview} authenticated={authenticated} />
          <ProtectedRoute path="/repos/create" component={CreateRepositoryRoot} authenticated={authenticated} />
          <Redirect exact strict from="/repos/import" to="/repos/create/import" />
          <ProtectedRoute exact path="/repos/:namespace" component={Overview} authenticated={authenticated} />
          <ProtectedRoute exact path="/repos/:namespace/:page" component={Overview} authenticated={authenticated} />
          <ProtectedRoute path="/repo/:namespace/:name" component={RepositoryRoot} authenticated={authenticated} />
          <ProtectedRoute path="/namespace/:namespaceName" component={NamespaceRoot} authenticated={authenticated} />
          <Redirect exact strict from="/users" to="/users/" />
          <ProtectedRoute exact path="/users/" component={Users} authenticated={authenticated} />
          <ProtectedRoute path="/users/create" component={CreateUser} authenticated={authenticated} />
          <ProtectedRoute exact path="/users/:page" component={Users} authenticated={authenticated} />
          <ProtectedRoute path="/user/:name" component={SingleUser} authenticated={authenticated} />
          <Redirect exact strict from="/groups" to="/groups/" />
          <ProtectedRoute exact path="/groups/" component={Groups} authenticated={authenticated} />
          <ProtectedRoute path="/group/:name" component={SingleGroup} authenticated={authenticated} />
          <ProtectedRoute path="/groups/create" component={CreateGroup} authenticated={authenticated} />
          <ProtectedRoute exact path="/groups/:page" component={Groups} authenticated={authenticated} />
          <ProtectedRoute path="/admin" component={Admin} authenticated={authenticated} />
          <ProtectedRoute path="/me" component={Profile} authenticated={authenticated} />
          <ProtectedRoute path="/importlog/:logId" component={ImportLog} authenticated={authenticated} />
          <Redirect exact strict from="/search/:type" to="/search/:type/" />
          <ProtectedRoute path="/search/:type/:page" component={Search} authenticated={authenticated} />
          <ProtectedRoute path="/search/:type/" component={Search} authenticated={authenticated} />
          <ProtectedRoute path="/help/search-syntax/" component={Syntax} authenticated={authenticated} />
          <ExtensionPoint<extensionPoints.MainRoute> name="main.route" renderAll={true} props={props} />
        </Switch>
      </div>
    </ErrorBoundary>
  );
};

export default Main;
