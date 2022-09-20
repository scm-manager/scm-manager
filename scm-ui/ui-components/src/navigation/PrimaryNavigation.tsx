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
import React, { FC, ReactNode } from "react";
import PrimaryNavigationLink from "./PrimaryNavigationLink";
import { Links } from "@scm-manager/ui-types";
import { binder, ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { useTranslation } from "react-i18next";
import useShortcut from "@scm-manager/ui-webapp/src/shortcuts/useShortcut";
import { useHistory } from "react-router-dom";

type Props = {
  links: Links;
};

type Appender = (to: string, match: string, label: string, linkName: string) => void;

const PrimaryNavigation: FC<Props> = ({ links }) => {
  const [t] = useTranslation("commons");

  const history = useHistory();
  useShortcut("option+r", () => {
    history.push("/repos/");
  });
  useShortcut("option+u", () => {
    history.push("/users/");
  });
  useShortcut("option+g", () => {
    history.push("/groups/");
  });
  useShortcut("option+a", () => {
    history.push("/admin/");
  });

  const createNavigationAppender = (navItems: ReactNode[]): Appender => {
    return (to: string, match: string, label: string, linkName: string) => {
      const link = links[linkName];
      if (link) {
        const navigationItem = (
          <PrimaryNavigationLink
            testId={label.replace(".", "-")}
            to={to}
            match={match}
            label={t(label)}
            key={linkName}
            className="navbar-item"
          />
        );
        navItems.push(navigationItem);
      }
    };
  };

  const createNavigationItems = () => {
    const navItems: ReactNode[] = [];

    const extensionProps = {
      links,
      label: t("primary-navigation.first-menu"),
    };

    const append = createNavigationAppender(navItems);
    if (
      binder.hasExtension<extensionPoints.PrimaryNavigationFirstMenu>("primary-navigation.first-menu", extensionProps)
    ) {
      navItems.push(
        <ExtensionPoint<extensionPoints.PrimaryNavigationFirstMenu>
          key="primary-navigation.first-menu"
          name="primary-navigation.first-menu"
          props={extensionProps}
        />
      );
    }
    append("/repos/", "/(repo|repos)", "primary-navigation.repositories", "repositories");
    append("/users/", "/(user|users)", "primary-navigation.users", "users");
    append("/groups/", "/(group|groups)", "primary-navigation.groups", "groups");
    append("/admin", "/admin", "primary-navigation.admin", "config");

    navItems.push(
      <ExtensionPoint<extensionPoints.PrimaryNavigation>
        key="primary-navigation"
        name="primary-navigation"
        renderAll={true}
        props={{
          links,
        }}
      />
    );

    return navItems;
  };

  return <>{createNavigationItems()}</>;
};

export default PrimaryNavigation;
