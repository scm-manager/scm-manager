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

import React, { FC } from "react";
import { urls } from "@scm-manager/ui-components";
import { binder, ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { useTranslation } from "react-i18next";
import { Links } from "@scm-manager/ui-types";
import { useLocation } from "react-router-dom";
import classNames from "classnames";
import HeaderButton from "../components/HeaderButton";
import { Link } from "react-router-dom";
import HeaderButtonContent, { headerButtonContentClassName } from "../components/HeaderButtonContent";
import { PrimaryNavigationLoginButtonExtension } from "@scm-manager/ui-extensions/src/extensionPoints";

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

  const extensionProps = {
    links,
    label,
    loginUrl: urls.withContextPath(loginPath),
    from,
    to,
    className: headerButtonContentClassName,
    content
  };

  if (links?.login) {
    const shouldRenderExtension = binder.hasExtension<PrimaryNavigationLoginButtonExtension>(
      "primary-navigation.login",
      extensionProps
    );
    return (
      <HeaderButton
        data-testid="primary-navigation-login"
        className={classNames("is-flex-start", "navbar-item", className)}
      >
        {shouldRenderExtension ? (
          <ExtensionPoint<PrimaryNavigationLoginButtonExtension>
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
