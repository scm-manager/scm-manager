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
import { LoginButton, urls } from "@scm-manager/ui-components";
import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";
import { useTranslation } from "react-i18next";
import { Links } from "@scm-manager/ui-types";
import { useLocation } from "react-router-dom";

type Props = {
  className?: string;
  links?: Links;
  burgerMode: boolean;
};

const LoginButtonExtensionWrapper: FC<Props> = ({ burgerMode, links, className }) => {
  const [t] = useTranslation("commons");
  const location = useLocation();

  const from = location.pathname;
  const loginPath = "/login";

  const extensionProps = {
    links,
    label: t("primary-navigation.login"),
    loginUrl: urls.withContextPath(loginPath),
    from,
    burgerMode,
  };

  if (links?.login) {
    if (binder.hasExtension("primary-navigation.login", extensionProps)) {
      return <ExtensionPoint key="primary-navigation.login" name="primary-navigation.login" props={extensionProps} />;
    } else {
      return <LoginButton burgerMode={burgerMode} className={className} />;
    }
  }
  return null;
};

export default LoginButtonExtensionWrapper;
