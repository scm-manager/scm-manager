//@flow
import React from "react";
import { connect } from "react-redux";
import {
  Page,
  ErrorPage,
  Loading,
  Navigation,
  SubNavigation,
  Section,
  NavLink
} from "@scm-manager/ui-components";
import { Route } from "react-router";
import { Details } from "./../components/table";
import {
  GeneralGroupNavLink,
  SetPermissionsNavLink
} from "./../components/navLinks";
import type { Group } from "@scm-manager/ui-types";
import type { History } from "history";
import {
  fetchGroupByName,
  getGroupByName,
  isFetchGroupPending,
  getFetchGroupFailure
} from "../modules/groups";

import { translate } from "react-i18next";
import GeneralGroup from "./GeneralGroup";
import { getGroupsLink } from "../../modules/indexResource";
import SetPermissions from "../../permissions/components/SetPermissions";
import {ExtensionPoint} from "@scm-manager/ui-extensions";

type Props = {
  name: string,
  group: Group,
  loading: boolean,
  error: Error,
  groupLink: string,

  // dispatcher functions
  fetchGroupByName: (string, string) => void,

  // context objects
  t: string => string,
  match: any,
  history: History
};

class SingleGroup extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchGroupByName(this.props.groupLink, this.props.name);
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
    const { t, loading, error, group } = this.props;

    if (error) {
      return (
        <ErrorPage
          title={t("singleGroup.errorTitle")}
          subtitle={t("singleGroup.errorSubtitle")}
          error={error}
        />
      );
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
      <Page title={group.name}>
        <div className="columns">
          <div className="column is-three-quarters">
            <Route
              path={url}
              exact
              component={() => <Details group={group} />}
            />
            <Route
              path={`${url}/settings/general`}
              exact
              component={() => <GeneralGroup group={group} />}
            />
            <Route
              path={`${url}/settings/permissions`}
              exact
              component={() => (
                <SetPermissions selectedPermissionsLink={group._links.permissions} />
              )}
            />
            <ExtensionPoint
              name="group.route"
              props={extensionProps}
              renderAll={true}
            />
          </div>
          <div className="column">
            <Navigation>
              <Section label={t("singleGroup.menu.navigationLabel")}>
                <NavLink
                  to={`${url}`}
                  label={t("singleGroup.menu.informationNavLink")}
                />
                <ExtensionPoint
                  name="group.navigation"
                  props={extensionProps}
                  renderAll={true}
                />
                <SubNavigation
                  to={`${url}/settings/general`}
                  label={t("singleGroup.menu.settingsNavLink")}
                >
                  <GeneralGroupNavLink
                    group={group}
                    editUrl={`${url}/settings/general`}
                  />
                  <SetPermissionsNavLink
                    group={group}
                    permissionsUrl={`${url}/settings/permissions`}
                  />
                  <ExtensionPoint name="group.subnavigation" props={extensionProps} renderAll={true} />
                </SubNavigation>
              </Section>
            </Navigation>
          </div>
        </div>
      </Page>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const name = ownProps.match.params.name;
  const group = getGroupByName(state, name);
  const loading = isFetchGroupPending(state, name);
  const error = getFetchGroupFailure(state, name);
  const groupLink = getGroupsLink(state);

  return {
    name,
    group,
    loading,
    error,
    groupLink
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchGroupByName: (link: string, name: string) => {
      dispatch(fetchGroupByName(link, name));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("groups")(SingleGroup));
