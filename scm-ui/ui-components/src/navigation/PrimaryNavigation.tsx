/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React, { FC, ReactNode } from "react";
import PrimaryNavigationLink from "./PrimaryNavigationLink";
import { Links } from "@scm-manager/ui-types";
import { binder, ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { useTranslation } from "react-i18next";

type Props = {
  links: Links;
};

type Appender = (to: string, match: string, label: string, linkName: string) => void;

const PrimaryNavigation: FC<Props> = ({ links }) => {
  const [t] = useTranslation("commons");

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
      label: t("primary-navigation.first-menu")
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
          links
        }}
      />
    );

    return navItems;
  };

  return <>{createNavigationItems()}</>;
};

export default PrimaryNavigation;
