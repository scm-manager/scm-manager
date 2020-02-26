import React from "react";
import { connect } from "react-redux";
import { Route, RouteComponentProps } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { Group } from "@scm-manager/ui-types";
import {
  ErrorPage,
  Loading,
  Navigation,
  NavLink,
  Page,
  Section,
  SubNavigation,
  isMenuCollapsed,
  MenuContext
} from "@scm-manager/ui-components";
import { getGroupsLink } from "../../modules/indexResource";
import { fetchGroupByName, getFetchGroupFailure, getGroupByName, isFetchGroupPending } from "../modules/groups";
import { Details } from "./../components/table";
import { EditGroupNavLink, SetPermissionsNavLink } from "./../components/navLinks";
import EditGroup from "./EditGroup";
import SetPermissions from "../../permissions/components/SetPermissions";
import { storeMenuCollapsed } from "@scm-manager/ui-components/src";

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

type State = {
  menuCollapsed: boolean;
  setMenuCollapsed: (collapsed: boolean) => void;
};

class SingleGroup extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      menuCollapsed: isMenuCollapsed(),
      setMenuCollapsed: (collapsed: boolean) => this.setState({ menuCollapsed: collapsed })
    };
  }

  componentDidMount() {
    this.props.fetchGroupByName(this.props.groupLink, this.props.name);
  }

  componentDidUpdate() {
    if (this.state.menuCollapsed && this.isCollapseForbidden()) {
      this.setState({ menuCollapsed: false });
    }
  }

  isCollapseForbidden = () => {
    return this.props.location.pathname.includes("/settings/");
  };

  onCollapseGroupMenu = (collapsed: boolean) => {
    this.setState({ menuCollapsed: collapsed }, () => storeMenuCollapsed(collapsed));
  };

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
    const { menuCollapsed } = this.state;

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
      <MenuContext.Provider value={this.state}>
        <Page title={group.name}>
          <div className="columns">
            <div className="column">
              <Route path={url} exact component={() => <Details group={group} />} />
              <Route path={`${url}/settings/general`} exact component={() => <EditGroup group={group} />} />
              <Route
                path={`${url}/settings/permissions`}
                exact
                component={() => <SetPermissions selectedPermissionsLink={group._links.permissions} />}
              />
              <ExtensionPoint name="group.route" props={extensionProps} renderAll={true} />
            </div>
            <div className={menuCollapsed ? "column is-1" : "column is-3"}>
              <Navigation>
                <Section
                  label={t("singleGroup.menu.navigationLabel")}
                  onCollapse={this.isCollapseForbidden() ? undefined : () => this.onCollapseGroupMenu(!menuCollapsed)}
                  collapsed={menuCollapsed}
                >
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
                </Section>
              </Navigation>
            </div>
          </div>
        </Page>
      </MenuContext.Provider>
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
