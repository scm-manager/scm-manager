//@flow
import React from "react";
import { connect } from "react-redux";
import {
  Page,
  ErrorPage,
  Loading,
  Navigation,
  Section,
  NavLink
} from "@scm-manager/ui-components";
import { Route } from "react-router";
import { Details } from "./../components/table";
import {
  DeleteGroupNavLink,
  EditGroupNavLink,
  SetPermissionsNavLink
} from "./../components/navLinks";
import type { Group } from "@scm-manager/ui-types";
import type { History } from "history";
import {
  deleteGroup,
  fetchGroupByName,
  getGroupByName,
  isFetchGroupPending,
  getFetchGroupFailure,
  getDeleteGroupFailure,
  isDeleteGroupPending
} from "../modules/groups";

import { translate } from "react-i18next";
import EditGroup from "./EditGroup";
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
  deleteGroup: (group: Group, callback?: () => void) => void,
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

  deleteGroup = (group: Group) => {
    this.props.deleteGroup(group, this.groupDeleted);
  };

  groupDeleted = () => {
    this.props.history.push("/groups");
  };

  matchedUrl = () => {
    return this.stripEndingSlash(this.props.match.url);
  };

  render() {
    const { t, loading, error, group } = this.props;

    if (error) {
      return (
        <ErrorPage
          title={t("single-group.error-title")}
          subtitle={t("single-group.error-subtitle")}
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
              path={`${url}/edit`}
              exact
              component={() => <EditGroup group={group} />}
            />
            <ExtensionPoint
              name="group.route"
              props={extensionProps}
              renderAll={true}
            />
            <Route
              path={`${url}/permissions`}
              exact
              component={() => (
                <SetPermissions selectedPermissionsLink={group._links.permissions} />
              )}
            />
          </div>
          <div className="column">
            <Navigation>
              <Section label={t("single-group.navigation-label")}>
                <NavLink
                  to={`${url}`}
                  label={t("single-group.information-label")}
                />
                <SetPermissionsNavLink
                  group={group}
                  permissionsUrl={`${url}/permissions`}
                />
              </Section>
              <Section label={t("single-group.actions-label")}>
                <DeleteGroupNavLink
                  group={group}
                  deleteGroup={this.deleteGroup}
                />
                <EditGroupNavLink group={group} editUrl={`${url}/edit`} />
                <NavLink to="/groups" label={t("single-group.back-label")} />
                <ExtensionPoint
                  name="group.navigation"
                  props={extensionProps}
                  renderAll={true}
                />
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
  const loading =
    isFetchGroupPending(state, name) || isDeleteGroupPending(state, name);
  const error =
    getFetchGroupFailure(state, name) || getDeleteGroupFailure(state, name);
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
    },
    deleteGroup: (group: Group, callback?: () => void) => {
      dispatch(deleteGroup(group, callback));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("groups")(SingleGroup));
