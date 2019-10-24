import React, { ReactNode } from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import PrimaryNavigationLink from "./PrimaryNavigationLink";
import { Links } from "@scm-manager/ui-types";
import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";

type Props = WithTranslation & {
  links: Links;
};

type Appender = (to: string, match: string, label: string, linkName: string) => void;

class PrimaryNavigation extends React.Component<Props> {
  createNavigationAppender = (navigationItems: ReactNode[]): Appender => {
    const { t, links } = this.props;

    return (to: string, match: string, label: string, linkName: string) => {
      const link = links[linkName];
      if (link) {
        const navigationItem = <PrimaryNavigationLink to={to} match={match} label={t(label)} key={linkName} />;
        navigationItems.push(navigationItem);
      }
    };
  };

  appendLogout = (navigationItems: ReactNode[], append: Appender) => {
    const { t, links } = this.props;

    const props = {
      links,
      label: t("primary-navigation.logout")
    };

    if (binder.hasExtension("primary-navigation.logout", props)) {
      navigationItems.push(
        <ExtensionPoint key="primary-navigation.logout" name="primary-navigation.logout" props={props} />
      );
    } else {
      append("/logout", "/logout", "primary-navigation.logout", "logout");
    }
  };

  createNavigationItems = () => {
    const navigationItems: ReactNode[] = [];
    const { t, links } = this.props;

    const props = {
      links,
      label: t("primary-navigation.first-menu")
    };

    const append = this.createNavigationAppender(navigationItems);
    if (binder.hasExtension("primary-navigation.first-menu", props)) {
      navigationItems.push(
        <ExtensionPoint key="primary-navigation.first-menu" name="primary-navigation.first-menu" props={props} />
      );
    }
    append("/repos/", "/(repo|repos)", "primary-navigation.repositories", "repositories");
    append("/users/", "/(user|users)", "primary-navigation.users", "users");
    append("/groups/", "/(group|groups)", "primary-navigation.groups", "groups");
    append("/admin", "/admin", "primary-navigation.admin", "config");

    navigationItems.push(
      <ExtensionPoint
        key="primary-navigation"
        name="primary-navigation"
        renderAll={true}
        props={{
          links: this.props.links
        }}
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

export default withTranslation("commons")(PrimaryNavigation);
