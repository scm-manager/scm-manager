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

    const _repositoriesLink = repositoriesLink ? (
      <PrimaryNavigationLink
      to="/repos"
      match="/(repo|repos)"
      label={t("primary-navigation.repositories")}
    />): null;

    const _usersLink = usersLink ? (
      <PrimaryNavigationLink
      to="/users"
      match="/(user|users)"
      label={t("primary-navigation.users")}
    />) : null;

    const _groupsLink = groupsLink ? (
      <PrimaryNavigationLink
      to="/groups"
      match="/(group|groups)"
      label={t("primary-navigation.groups")}
    />) : null;

    const _configLink = configLink ? (
      <PrimaryNavigationLink
      to="/config"
      label={t("primary-navigation.config")}
    />) : null;

    const _logoutLink = logoutLink ? (
      <PrimaryNavigationLink
      to="/logout"
      label={t("primary-navigation.logout")}
    />) : null;

    return (
      <nav className="tabs is-boxed">
        <ul>
          {_repositoriesLink}
          {_usersLink}
          {_groupsLink}
          {_configLink}
          {_logoutLink}
        </ul>
      </nav>
    );
  }
}

export default translate("commons")(PrimaryNavigation);
