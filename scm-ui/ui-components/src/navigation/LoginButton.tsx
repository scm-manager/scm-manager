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
import { devices, Icon } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { Link, useLocation } from "react-router-dom";
import styled from "styled-components";

type Props = {
  className?: string;
  burgerMode: boolean;
};

const StyledLoginButton = styled(Link)`
  @media screen and (max-width: ${devices.desktop.width - 1}px) {
    border-top: 1px solid white;
    margin-top: 1rem;
    padding-top: 1rem;
    padding-bottom: 1rem;
  }

  @media screen and (min-width: ${devices.desktop.width}px) {
    margin-left: 2rem;
  }
`;

const LoginButton: FC<Props> = ({ burgerMode, className }) => {
  const [t] = useTranslation("commons");
  const location = useLocation();

  const from = location.pathname;
  const loginPath = "/login";
  const to = `${loginPath}?from=${encodeURIComponent(from)}`;

  return (
    <StyledLoginButton
      data-testid="primary-navigation-login"
      to={to}
      className={classNames("is-align-items-center", "navbar-item", className)}
    >
      <Icon
        title={t("primary-navigation.login")}
        name="sign-in-alt"
        color="white"
        className={burgerMode ? "is-size-5" : "is-size-4"}
      />
      {" " + t("primary-navigation.login")}
    </StyledLoginButton>
  );
};

export default LoginButton;
