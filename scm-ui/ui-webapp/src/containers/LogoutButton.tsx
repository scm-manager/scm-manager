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

import React, { FC } from "react";
import { binder, ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { useTranslation } from "react-i18next";
import { Links } from "@scm-manager/ui-types";
import classNames from "classnames";
import HeaderButtonContent, { headerButtonContentClassName } from "../components/HeaderButtonContent";
import HeaderButton from "../components/HeaderButton";
import { Link } from "react-router-dom";

type Props = {
  className?: string;
  links: Links;
  burgerMode: boolean;
};

const LogoutButton: FC<Props> = ({ burgerMode, links, className }) => {
  const [t] = useTranslation("commons");

  const label = t("primary-navigation.logout");
  const content = <HeaderButtonContent burgerMode={burgerMode} label={label} icon="sign-out-alt" />;

  const extensionProps = {
    links,
    label,
    className: headerButtonContentClassName,
    content
  };

  if (links?.logout) {
    const shouldRenderExtension = binder.hasExtension<extensionPoints.PrimaryNavigationLogoutButton>(
      "primary-navigation.logout",
      extensionProps
    );

    return (
      <HeaderButton
        data-testid="primary-navigation-logout"
        className={classNames("is-flex-start", "navbar-item", className)}
      >
        {shouldRenderExtension ? (
          <ExtensionPoint<extensionPoints.PrimaryNavigationLogoutButton>
            name="primary-navigation.logout"
            props={extensionProps}
          />
        ) : (
          <Link to="/logout" className={headerButtonContentClassName}>
            {content}
          </Link>
        )}
      </HeaderButton>
    );
  }
  return null;
};

export default LogoutButton;
