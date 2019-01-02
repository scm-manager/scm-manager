//@flow
import React from "react";
import { translate } from "react-i18next";
import PrimaryNavigationLink from "./PrimaryNavigationLink";
import type { Links } from "@scm-manager/ui-types";
import { binder } from "@scm-manager/ui-extensions";

type Props = {
  t: string => string,
  links: Links,
};

class PrimaryNavigation extends React.Component<Props> {

  createNavigationAppender = (navigationItems) => {
    const { t, links } = this.props;

    return (to: string, match: string, label: string, linkName: string) => {
      const link = links[linkName];
      if (link) {
        const navigationItem = (
          <PrimaryNavigationLink
            to={to}
            match={match}
            label={t(label)}
            key={linkName}
          />)
        ;
        navigationItems.push(navigationItem);
      }
    };
  };

  createLogoutFromExtension = () => {
    const { t, links } = this.props;

    const props = {
      links,
      label: t("primary-navigation.logout")
    };

    return binder.getExtension("primary-navigation.logout", props);
  };

  createNavigationItems = () => {
    const navigationItems = [];

    const append = this.createNavigationAppender(navigationItems);
    append("/repos", "/(repo|repos)", "primary-navigation.repositories", "repositories");
    append("/users", "/(user|users)", "primary-navigation.users", "users");
    append("/groups", "/(group|groups)", "primary-navigation.groups", "groups");
    append("/config", "/config", "primary-navigation.config", "config");

    if (binder.hasExtension("primary-navigation.logout")) {
      const extension = this.createLogoutFromExtension();
      navigationItems.push(extension);
    } else {
      append("/logout", "/logout", "primary-navigation.logout", "logout");
    }

    return navigationItems;
  };

  render() {
    const navigationItems = this.createNavigationItems();

    return (
      <nav className="tabs is-boxed">
        <ul>
          {navigationItems}
        </ul>
      </nav>
    );
  }
}

export default translate("commons")(PrimaryNavigation);
