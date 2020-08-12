/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
        const navigationItem = <PrimaryNavigationLink testId={label.replace(".", "-")} to={to} match={match} label={t(label)} key={linkName} />;
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

  appendLogin = (navigationItems: ReactNode[], append: Appender) => {
    const { t, links } = this.props;

    const props = {
      links,
      label: t("primary-navigation.login")
    };

    if (binder.hasExtension("primary-navigation.login", props)) {
      navigationItems.push(
        <ExtensionPoint key="primary-navigation.login" name="primary-navigation.login" props={props} />
      );
    } else {
      append("/login", "/login", "primary-navigation.login", "login");
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
    this.appendLogin(navigationItems, append);

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
