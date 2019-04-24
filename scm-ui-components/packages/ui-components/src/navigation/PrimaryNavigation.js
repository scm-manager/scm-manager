//@flow
import React from "react";
import { translate } from "react-i18next";
import PrimaryNavigationLink from "./PrimaryNavigationLink";
import type { Links } from "@scm-manager/ui-types";
import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";

type Props = {
  t: string => string,
  links: Links
};

class PrimaryNavigation extends React.Component<Props> {
  createNavigationAppender = navigationItems => {
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
          />
        );
        navigationItems.push(navigationItem);
      }
    };
  };

  appendLogout = (navigationItems, append) => {
    const { t, links } = this.props;

    const props = {
      links,
      label: t("primary-navigation.logout")
    };

    if (binder.hasExtension("primary-navigation.logout", props)) {
      navigationItems.push(
        <ExtensionPoint name="primary-navigation.logout" props={props} />
      );
    } else {
      append("/logout", "/logout", "primary-navigation.logout", "logout");
    }
  };

  createNavigationItems = () => {
    const navigationItems = [];
    const { t, links } = this.props;

    const props = {
      links,
      label: t("primary-navigation.first-menu")
    };

    const append = this.createNavigationAppender(navigationItems);
    if (binder.hasExtension("primary-navigation.first-menu", props)) {
      navigationItems.push(
        <ExtensionPoint name="primary-navigation.first-menu" props={props} />
      );
    }
    append(
      "/repos/",
      "/(repo|repos)",
      "primary-navigation.repositories",
      "repositories"
    );
    append("/users/", "/(user|users)", "primary-navigation.users", "users");
    append(
      "/groups/",
      "/(group|groups)",
      "primary-navigation.groups",
      "groups"
    );
    append("/config", "/config", "primary-navigation.config", "config");

    navigationItems.push(
      <ExtensionPoint
        name="primary-navigation"
        renderAll={true}
        props={{ links: this.props.links }}
      />
    );

    this.appendLogout(navigationItems, append);

    return navigationItems;
  };

  render() {
    const navigationItems = this.createNavigationItems();

    return (
      <nav className="tabs is-boxed">
        <ul>{navigationItems}</ul>
      </nav>
    );
  }
}

export default translate("commons")(PrimaryNavigation);
