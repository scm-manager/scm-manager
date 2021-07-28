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
import { Icon } from "@scm-manager/ui-components";
import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";
import { useTranslation } from "react-i18next";
import { Links } from "@scm-manager/ui-types";
import classNames from "classnames";
import { Link } from "react-router-dom";
import { StyledHeaderButton } from "./LoginButton";

type Props = {
  className?: string;
  links?: Links;
  burgerMode: boolean;
};

const DefaultLogoutLink: FC<{ burgerMode: boolean }> = ({ burgerMode }) => {
  const [t] = useTranslation("commons");

  return (
    <Link to={"/logout"} className="is-flex is-align-items-center is-justify-content-flex-start">
      <Icon
        title={t("primary-navigation.logout")}
        name="sign-out-alt"
        color="white"
        className={burgerMode ? "is-size-5" : "is-size-4"}
      />
      <span className="has-text-white">{" " + t("primary-navigation.logout")}</span>
    </Link>
  );
};

const LogoutButton: FC<Props> = ({ burgerMode, links, className }) => {
  const [t] = useTranslation("commons");

  const shouldRenderExtension = () => {
    return binder.hasExtension("primary-navigation.logout", extensionProps);
  };

  const extensionProps = {
    links,
    label: t("primary-navigation.logout"),
  };

  if (links?.logout) {
    return (
      <StyledHeaderButton
        data-testid="primary-navigation-logout"
        className={classNames("is-flex-start", "navbar-item", className)}
      >
        {shouldRenderExtension() ? (
          <ExtensionPoint key="primary-navigation.logout" name="primary-navigation.logout" props={extensionProps} />
        ) : (
          <DefaultLogoutLink burgerMode={burgerMode} />
        )}
      </StyledHeaderButton>
    );
  }
  return null;
};

export default LogoutButton;
