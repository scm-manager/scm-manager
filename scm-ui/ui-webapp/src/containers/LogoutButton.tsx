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
    const shouldRenderExtension = binder.hasExtension<extensionPoints.PrimaryNavigationLogoutButtonExtension>(
      "primary-navigation.logout",
      extensionProps
    );

    return (
      <HeaderButton
        data-testid="primary-navigation-logout"
        className={classNames("is-flex-start", "navbar-item", className)}
      >
        {shouldRenderExtension ? (
          <ExtensionPoint<extensionPoints.PrimaryNavigationLogoutButtonExtension>
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
