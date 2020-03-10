import React from "react";
import { Route, RouteComponentProps, withRouter } from "react-router-dom";
import { getMe } from "../modules/auth";
import { compose } from "redux";
import { connect } from "react-redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { Me } from "@scm-manager/ui-types";
import {
  ErrorPage,
  isMenuCollapsed,
  MenuContext,
  Navigation,
  NavLink,
  Page,
  Section,
  SubNavigation
} from "@scm-manager/ui-components";
import ChangeUserPassword from "./ChangeUserPassword";
import ProfileInfo from "./ProfileInfo";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { storeMenuCollapsed } from "@scm-manager/ui-components/src";

type Props = RouteComponentProps &
  WithTranslation & {
    me: Me;

    // Context props
    match: any;
  };

type State = {
  menuCollapsed: boolean;
};

class Profile extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      menuCollapsed: isMenuCollapsed()
    };
  }

  onCollapseProfileMenu = (collapsed: boolean) => {
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
    const url = this.matchedUrl();

    const { me, t } = this.props;
    const { menuCollapsed } = this.state;

    if (!me) {
      return (
        <ErrorPage
          title={t("profile.error-title")}
          subtitle={t("profile.error-subtitle")}
          error={{
            name: t("profile.error"),
            message: t("profile.error-message")
          }}
        />
      );
    }

    const extensionProps = {
      me,
      url
    };

    return (
      <MenuContext.Provider
        value={{ menuCollapsed, setMenuCollapsed: (collapsed: boolean) => this.setState({ menuCollapsed: collapsed }) }}
      >
        <Page title={me.displayName}>
          <div className="columns">
            <div className="column">
              <Route path={url} exact render={() => <ProfileInfo me={me} />} />
              <Route path={`${url}/settings/password`} render={() => <ChangeUserPassword me={me} />} />
              <ExtensionPoint name="profile.route" props={extensionProps} renderAll={true} />
            </div>
            <div className={menuCollapsed ? "column is-1" : "column is-3"}>
              <Section
                label={t("profile.navigationLabel")}
                onCollapse={() => this.onCollapseProfileMenu(!menuCollapsed)}
                collapsed={menuCollapsed}
              >
                <NavLink
                  to={`${url}`}
                  icon="fas fa-info-circle"
                  label={t("profile.informationNavLink")}
                  title={t("profile.informationNavLink")}
                />
                <SubNavigation
                  to={`${url}/settings/password`}
                  label={t("profile.settingsNavLink")}
                  title={t("profile.settingsNavLink")}
                >
                  <NavLink to={`${url}/settings/password`} label={t("profile.changePasswordNavLink")} />
                  <ExtensionPoint name="profile.setting" props={extensionProps} renderAll={true} />
                </SubNavigation>
              </Section>
            </div>
          </div>
        </Page>
      </MenuContext.Provider>
    );
  }
}

const mapStateToProps = (state: any) => {
  return {
    me: getMe(state)
  };
};

export default compose(withTranslation("commons"), connect(mapStateToProps), withRouter)(Profile);
