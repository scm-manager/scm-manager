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
import { connect } from "react-redux";
import { Route, RouteComponentProps } from "react-router-dom";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { User } from "@scm-manager/ui-types";
import {
  ErrorPage,
  Loading,
  NavLink,
  Page,
  CustomQueryFlexWrappedColumns,
  PrimaryContentColumn,
  SecondaryNavigationColumn,
  SecondaryNavigation,
  SubNavigation,
  StateMenuContextProvider
} from "@scm-manager/ui-components";
import { Details } from "./../components/table";
import EditUser from "./EditUser";
import { fetchUserByName, getFetchUserFailure, getUserByName, isFetchUserPending } from "../modules/users";
import { EditUserNavLink, SetPasswordNavLink, SetPermissionsNavLink } from "./../components/navLinks";
import { WithTranslation, withTranslation } from "react-i18next";
import { getUsersLink } from "../../modules/indexResource";
import SetUserPassword from "../components/SetUserPassword";
import SetPermissions from "../../permissions/components/SetPermissions";

type Props = RouteComponentProps &
  WithTranslation & {
    name: string;
    user: User;
    loading: boolean;
    error: Error;
    usersLink: string;

    // dispatcher function
    fetchUserByName: (p1: string, p2: string) => void;
  };

class SingleUser extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchUserByName(this.props.usersLink, this.props.name);
  }

  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 2);
    }
    return url;
  };

  matchedUrl = () => {
    return this.stripEndingSlash(this.props.match.url);
  };

  render() {
    const { t, loading, error, user } = this.props;

    if (error) {
      return <ErrorPage title={t("singleUser.errorTitle")} subtitle={t("singleUser.errorSubtitle")} error={error} />;
    }

    if (!user || loading) {
      return <Loading />;
    }

    const url = this.matchedUrl();

    const extensionProps = {
      user,
      url
    };

    return (
      <StateMenuContextProvider>
        <Page title={user.displayName}>
          <CustomQueryFlexWrappedColumns>
            <PrimaryContentColumn>
              <Route path={url} exact component={() => <Details user={user} />} />
              <Route path={`${url}/settings/general`} component={() => <EditUser user={user} />} />
              <Route path={`${url}/settings/password`} component={() => <SetUserPassword user={user} />} />
              <Route
                path={`${url}/settings/permissions`}
                component={() => <SetPermissions selectedPermissionsLink={user._links.permissions} />}
              />
              <ExtensionPoint name="user.route" props={extensionProps} renderAll={true} />
            </PrimaryContentColumn>
            <SecondaryNavigationColumn>
              <SecondaryNavigation label={t("singleUser.menu.navigationLabel")}>
                <NavLink
                  to={`${url}`}
                  icon="fas fa-info-circle"
                  label={t("singleUser.menu.informationNavLink")}
                  title={t("singleUser.menu.informationNavLink")}
                  testId="user-information-link"
                />
                <SubNavigation
                  to={`${url}/settings/general`}
                  label={t("singleUser.menu.settingsNavLink")}
                  title={t("singleUser.menu.settingsNavLink")}
                  testId="user-settings-link"
                >
                  <EditUserNavLink user={user} editUrl={`${url}/settings/general`} />
                  <SetPasswordNavLink user={user} passwordUrl={`${url}/settings/password`} />
                  <SetPermissionsNavLink user={user} permissionsUrl={`${url}/settings/permissions`} />
                  <ExtensionPoint name="user.setting" props={extensionProps} renderAll={true} />
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
  const name = ownProps.match.params.name;
  const user = getUserByName(state, name);
  const loading = isFetchUserPending(state, name);
  const error = getFetchUserFailure(state, name);
  const usersLink = getUsersLink(state);
  return {
    usersLink,
    name,
    user,
    loading,
    error
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchUserByName: (link: string, name: string) => {
      dispatch(fetchUserByName(link, name));
    }
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(withTranslation("users")(SingleUser));
