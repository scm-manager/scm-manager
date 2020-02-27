import React from "react";
import { connect } from "react-redux";
import { Route, RouteComponentProps } from "react-router-dom";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { User } from "@scm-manager/ui-types";
import {
  ErrorPage,
  isMenuCollapsed,
  Loading,
  MenuContext,
  Navigation,
  NavLink,
  Page,
  Section,
  SubNavigation
} from "@scm-manager/ui-components";
import { Details } from "./../components/table";
import EditUser from "./EditUser";
import { fetchUserByName, getFetchUserFailure, getUserByName, isFetchUserPending } from "../modules/users";
import { EditUserNavLink, SetPasswordNavLink, SetPermissionsNavLink } from "./../components/navLinks";
import { WithTranslation, withTranslation } from "react-i18next";
import { getUsersLink } from "../../modules/indexResource";
import SetUserPassword from "../components/SetUserPassword";
import SetPermissions from "../../permissions/components/SetPermissions";
import { storeMenuCollapsed } from "@scm-manager/ui-components/src";

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

type State = {
  menuCollapsed: boolean;
  setMenuCollapsed: (collapsed: boolean) => void;
};

class SingleUser extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      menuCollapsed: isMenuCollapsed(),
      setMenuCollapsed: (collapsed: boolean) => this.setState({ menuCollapsed: collapsed })
    };
  }

  componentDidMount() {
    this.props.fetchUserByName(this.props.usersLink, this.props.name);
  }

  componentDidUpdate() {
    if (this.state.menuCollapsed && this.isCollapseForbidden()) {
      this.setState({ menuCollapsed: false });
    }
  }

  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 2);
    }
    return url;
  };

  isCollapseForbidden = () => {
    return this.props.location.pathname.includes("/settings/");
  };

  onCollapseUserMenu = (collapsed: boolean) => {
    this.setState({ menuCollapsed: collapsed }, () => storeMenuCollapsed(collapsed));
  };

  matchedUrl = () => {
    return this.stripEndingSlash(this.props.match.url);
  };

  render() {
    const { t, loading, error, user } = this.props;
    const { menuCollapsed } = this.state;

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
      <MenuContext.Provider value={this.state}>
        <Page title={user.displayName}>
          <div className="columns">
            <div className="column">
              <Route path={url} exact component={() => <Details user={user} />} />
              <Route path={`${url}/settings/general`} component={() => <EditUser user={user} />} />
              <Route path={`${url}/settings/password`} component={() => <SetUserPassword user={user} />} />
              <Route
                path={`${url}/settings/permissions`}
                component={() => <SetPermissions selectedPermissionsLink={user._links.permissions} />}
              />
              <ExtensionPoint name="user.route" props={extensionProps} renderAll={true} />
            </div>
            <div className={menuCollapsed ? "column is-1" : "column is-3"}>
              <Navigation>
                <Section
                  label={t("singleUser.menu.navigationLabel")}
                  onCollapse={this.isCollapseForbidden() ? undefined : () => this.onCollapseUserMenu(!menuCollapsed)}
                  collapsed={menuCollapsed}
                >
                  <NavLink
                    to={`${url}`}
                    icon="fas fa-info-circle"
                    label={t("singleUser.menu.informationNavLink")}
                    title={t("singleUser.menu.informationNavLink")}
                  />
                  <SubNavigation
                    to={`${url}/settings/general`}
                    label={t("singleUser.menu.settingsNavLink")}
                    title={t("singleUser.menu.settingsNavLink")}
                  >
                    <EditUserNavLink user={user} editUrl={`${url}/settings/general`} />
                    <SetPasswordNavLink user={user} passwordUrl={`${url}/settings/password`} />
                    <SetPermissionsNavLink user={user} permissionsUrl={`${url}/settings/permissions`} />
                    <ExtensionPoint name="user.setting" props={extensionProps} renderAll={true} />
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
