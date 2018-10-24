//@flow
import React from "react";
import { translate } from "react-i18next";
import PrimaryNavigationLink from "./PrimaryNavigationLink";

type Props = {
  t: string => string,
  repositoriesLink: string,
  usersLink: string,
  groupsLink: string,
  configLink: string,
  logoutLink: string
};

class PrimaryNavigation extends React.Component<Props> {
  render() {
    const { t, repositoriesLink, usersLink, groupsLink, configLink, logoutLink } = this.props;

    const links = [
      repositoriesLink ? (
        <PrimaryNavigationLink
          to="/repos"
          match="/(repo|repos)"
          label={t("primary-navigation.repositories")}
          key={"repositoriesLink"}
        />): null,
      usersLink ? (
        <PrimaryNavigationLink
          to="/users"
          match="/(user|users)"
          label={t("primary-navigation.users")}
          key={"usersLink"}
        />) : null,
      groupsLink ? (
        <PrimaryNavigationLink
          to="/groups"
          match="/(group|groups)"
          label={t("primary-navigation.groups")}
          key={"groupsLink"}
        />) : null,
      configLink ? (
        <PrimaryNavigationLink
          to="/config"
          label={t("primary-navigation.config")}
          key={"configLink"}
        />) : null,
      logoutLink ? (
        <PrimaryNavigationLink
          to="/logout"
          label={t("primary-navigation.logout")}
          key={"logoutLink"}
        />) : null
    ];

    return (
      <nav className="tabs is-boxed">
        <ul>
          {links}
        </ul>
      </nav>
    );
  }
}

export default translate("commons")(PrimaryNavigation);
