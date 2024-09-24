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
import { urls } from "@scm-manager/ui-components";
import { binder, ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { useTranslation } from "react-i18next";
import { Links } from "@scm-manager/ui-types";
import { Link, useLocation } from "react-router-dom";
import classNames from "classnames";
import HeaderButton from "../components/HeaderButton";
import HeaderButtonContent, { headerButtonContentClassName } from "../components/HeaderButtonContent";

type Props = {
  className?: string;
  links: Links;
  burgerMode: boolean;
};

const LoginButton: FC<Props> = ({ burgerMode, links, className }) => {
  const [t] = useTranslation("commons");
  const location = useLocation();

  const from = location.pathname;
  const loginPath = "/login";

  const label = t("primary-navigation.login");

  const to = `${loginPath}?from=${encodeURIComponent(from)}`;

  const content = <HeaderButtonContent burgerMode={burgerMode} label={label} icon="sign-in-alt" />;

  const extensionProps: extensionPoints.PrimaryNavigationLoginButtonProps = {
    links,
    label,
    loginUrl: urls.withContextPath(loginPath),
    from,
    to,
    className: headerButtonContentClassName,
    content
  };

  if (links?.login) {
    const shouldRenderExtension = binder.hasExtension<extensionPoints.PrimaryNavigationLoginButton>(
      "primary-navigation.login",
      extensionProps
    );
    return (
      <HeaderButton
        data-testid="primary-navigation-login"
        className={classNames("is-flex-start", "navbar-item", className)}
      >
        {shouldRenderExtension ? (
          <ExtensionPoint<extensionPoints.PrimaryNavigationLoginButton>
            name="primary-navigation.login"
            props={extensionProps}
          />
        ) : (
          <Link to={to} className={headerButtonContentClassName}>
            {content}
          </Link>
        )}
      </HeaderButton>
    );
  }

  return null;
};

export default LoginButton;
