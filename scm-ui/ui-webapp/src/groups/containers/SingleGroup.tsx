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
import { WithTranslation, withTranslation } from "react-i18next";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { Group } from "@scm-manager/ui-types";
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
import { getGroupsLink, mustGetGroupsLink } from "../../modules/indexResource";
import { fetchGroupByName, getFetchGroupFailure, getGroupByName, isFetchGroupPending } from "../modules/groups";
import { Details } from "./../components/table";
import { EditGroupNavLink, SetPermissionsNavLink } from "./../components/navLinks";
import EditGroup from "./EditGroup";
import SetPermissions from "../../permissions/components/SetPermissions";
import { urls } from "@scm-manager/ui-components";

type Props = RouteComponentProps &
  WithTranslation & {
    name: string;
    group: Group;
    loading: boolean;
    error: Error;
    groupLink: string;

    // dispatcher functions
    fetchGroupByName: (p1: string, p2: string) => void;
  };

class SingleGroup extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchGroupByName(this.props.groupLink, this.props.name);
  }

  matchedUrl = () => {
    return urls.stripEndingSlash(this.props.match.url);
  };

  render() {
    const { t, loading, error, group } = this.props;

    if (error) {
      return <ErrorPage title={t("singleGroup.errorTitle")} subtitle={t("singleGroup.errorSubtitle")} error={error} />;
    }

    if (!group || loading) {
      return <Loading />;
    }

    const url = this.matchedUrl();

    const extensionProps = {
      group,
      url
    };

    return (
      <StateMenuContextProvider>
        <Page title={group.name}>
          <CustomQueryFlexWrappedColumns>
            <PrimaryContentColumn>
              <Route path={url} exact component={() => <Details group={group} />} />
              <Route path={`${url}/settings/general`} exact component={() => <EditGroup group={group} />} />
              <Route
                path={`${url}/settings/permissions`}
                exact
                component={() => <SetPermissions selectedPermissionsLink={group._links.permissions} />}
              />
              <ExtensionPoint name="group.route" props={extensionProps} renderAll={true} />
            </PrimaryContentColumn>
            <SecondaryNavigationColumn>
              <SecondaryNavigation label={t("singleGroup.menu.navigationLabel")}>
                <NavLink
                  to={`${url}`}
                  icon="fas fa-info-circle"
                  label={t("singleGroup.menu.informationNavLink")}
                  title={t("singleGroup.menu.informationNavLink")}
                />
                <ExtensionPoint name="group.navigation" props={extensionProps} renderAll={true} />
                <SubNavigation
                  to={`${url}/settings/general`}
                  label={t("singleGroup.menu.settingsNavLink")}
                  title={t("singleGroup.menu.settingsNavLink")}
                >
                  <EditGroupNavLink group={group} editUrl={`${url}/settings/general`} />
                  <SetPermissionsNavLink group={group} permissionsUrl={`${url}/settings/permissions`} />
                  <ExtensionPoint name="group.setting" props={extensionProps} renderAll={true} />
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
  const group = getGroupByName(state, name);
  const loading = isFetchGroupPending(state, name);
  const error = getFetchGroupFailure(state, name);
  const groupLink = mustGetGroupsLink(state);

  return {
    name,
    group,
    loading,
    error,
    groupLink
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchGroupByName: (link: string, name: string) => {
      dispatch(fetchGroupByName(link, name));
    }
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(withTranslation("groups")(SingleGroup));
