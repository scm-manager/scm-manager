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

type Props = {
  className?: string;
  links?: Links;
};

const LogoutButton: FC<Props> = ({ links, className }) => {
  const [t] = useTranslation("commons");
  const extensionProps = {
    links,
    label: t("primary-navigation.logout"),
  };

  if (binder.hasExtension("primary-navigation.logout", extensionProps)) {
    return <ExtensionPoint key="primary-navigation.logout" name="primary-navigation.logout" props={extensionProps} />;
  } else {
    return (
      <a
        data-testid="primary-navigation-logout"
        href={"scm/logout"}
        className={classNames("is-flex", "is-align-items-center", className)}
      >
        <Icon title={t("primary-navigation.logout")} name="sign-out-alt" color="white" className="is-size-4" />
      </a>
    );
  }
};

export default LogoutButton;
