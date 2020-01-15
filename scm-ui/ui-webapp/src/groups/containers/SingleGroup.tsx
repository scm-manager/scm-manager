import React from "react";
import { connect } from "react-redux";
import { Route } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { History } from "history";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { Group } from "@scm-manager/ui-types";
import { ErrorPage, Loading, Navigation, NavLink, Page, Section, SubNavigation } from "@scm-manager/ui-components";
import { getGroupsLink } from "../../modules/indexResource";
import { fetchGroupByName, getFetchGroupFailure, getGroupByName, isFetchGroupPending } from "../modules/groups";
import { Details } from "./../components/table";
import { EditGroupNavLink, SetPermissionsNavLink } from "./../components/navLinks";
import EditGroup from "./EditGroup";
import SetPermissions from "../../permissions/components/SetPermissions";

type Props = WithTranslation & {
  name: string;
  group: Group;
  loading: boolean;
  error: Error;
  groupLink: string;

  // dispatcher functions
  fetchGroupByName: (p1: string, p2: string) => void;

  // context objects
  match: any;
  history: History;
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
      <Page title={group.name}>
        <div className="columns">
          <div className="column is-three-quarters">
            <Route path={url} exact component={() => <Details group={group} />} />
            <Route path={`${url}/settings/general`} exact component={() => <EditGroup group={group} />} />
            <Route
              path={`${url}/settings/permissions`}
              exact
              component={() => <SetPermissions selectedPermissionsLink={group._links.permissions} />}
            />
            <ExtensionPoint name="group.route" props={extensionProps} renderAll={true} />
          </div>
          <div className="column">
            <Navigation>
              <Section label={t("singleGroup.menu.navigationLabel")}>
                <NavLink to={`${url}`} icon="fas fa-info-circle" label={t("singleGroup.menu.informationNavLink")} />
                <ExtensionPoint name="group.navigation" props={extensionProps} renderAll={true} />
                <SubNavigation to={`${url}/settings/general`} label={t("singleGroup.menu.settingsNavLink")}>
                  <EditGroupNavLink group={group} editUrl={`${url}/settings/general`} />
                  <SetPermissionsNavLink group={group} permissionsUrl={`${url}/settings/permissions`} />
                  <ExtensionPoint name="group.setting" props={extensionProps} renderAll={true} />
                </SubNavigation>
              </Section>
            </Navigation>
          </div>
        </div>
      </Page>
    );
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
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

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchGroupByName: (link: string, name: string) => {
      dispatch(fetchGroupByName(link, name));
    }
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(withTranslation("groups")(SingleGroup));
