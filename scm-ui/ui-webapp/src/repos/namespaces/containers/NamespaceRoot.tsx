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
import { WithTranslation, withTranslation } from "react-i18next";
import { connect } from "react-redux";
import { Redirect, Route, RouteComponentProps, Switch } from "react-router-dom";
import { fetchNamespaceByName, getNamespace, isFetchNamespacePending } from "../../modules/repos";
import { getNamespacesLink } from "../../../modules/indexResource";
import { Namespace } from "@scm-manager/ui-types";
import {
  CustomQueryFlexWrappedColumns,
  ErrorPage,
  Loading,
  Page, PrimaryContentColumn,
  SecondaryNavigation,
  SecondaryNavigationColumn,
  StateMenuContextProvider,
  SubNavigation
} from "@scm-manager/ui-components";
import Permissions from "../../permissions/containers/Permissions";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import PermissionsNavLink from "./PermissionsNavLink";

type Props = RouteComponentProps &
  WithTranslation & {
    loading: boolean;
    namespaceName: string;
    namespacesLink: string;
    namespace: Namespace;

    // dispatch functions
    fetchNamespace: (link: string, namespace: string) => void;
  };

class NamespaceRoot extends React.Component<Props> {
  componentDidMount() {
    const { namespacesLink, namespaceName, fetchNamespace } = this.props;
    fetchNamespace(namespacesLink, namespaceName);
  }

  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 1);
    }
    return url;
  };

  matchedUrl = () => {
    return this.stripEndingSlash(this.props.match.url);
  };

  render() {
    const { loading, error, namespaceName, namespace, t } = this.props;
    const url = this.matchedUrl();

    const extensionProps = {
      namespace,
      url
    };

    if (error) {
      return (
        <ErrorPage title={t("namespaceRoot.errorTitle")} subtitle={t("namespaceRoot.errorSubtitle")} error={error} />
      );
    }

    if (!namespace || loading) {
      return <Loading />;
    }

    return (
      <StateMenuContextProvider>
        <Page title={namespaceName}>
          <CustomQueryFlexWrappedColumns>
            <PrimaryContentColumn>
              <Switch>
                <Redirect exact from={`${url}/settings`} to={`${url}/settings/permissions`} />
                <Route
                  path={`${url}/settings/permissions`}
                  render={() => {
                    return <Permissions namespace={namespaceName} />;
                  }}
                />
              </Switch>
            </PrimaryContentColumn>
            <SecondaryNavigationColumn>
              <SecondaryNavigation label={t("namespaceRoot.menu.navigationLabel")}>
                <ExtensionPoint name="namespace.navigation.topLevel" props={extensionProps} renderAll={true} />
                <ExtensionPoint name="namespace.route" props={extensionProps} renderAll={true} />
                <SubNavigation
                  to={`${url}/settings`}
                  label={t("namespaceRoot.menu.settingsNavLink")}
                  title={t("namespaceRoot.menu.settingsNavLink")}
                >
                  <PermissionsNavLink permissionUrl={`${url}/settings/permissions`} namespace={namespace} />
                  <ExtensionPoint name="namespace.setting" props={extensionProps} renderAll={true} />
                </SubNavigation>
              </SecondaryNavigation>
            </SecondaryNavigationColumn>
          </CustomQueryFlexWrappedColumns>
        </Page>
      </StateMenuContextProvider>
    );
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const { namespaceName } = ownProps.match.params;
  const namespacesLink = getNamespacesLink(state);
  const namespace = getNamespace(state, namespaceName);
  const loading = isFetchNamespacePending(state);
  return { namespaceName, namespacesLink, loading, namespace };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchNamespace: (link: string, namespaceName: string) => {
      dispatch(fetchNamespaceByName(link, namespaceName));
    }
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(withTranslation("namespaces")(NamespaceRoot));
