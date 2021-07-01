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
import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";
import { useTranslation } from "react-i18next";
import { Links } from "@scm-manager/ui-types";
import classNames from "classnames";
import { useHistory } from "react-router-dom";
import styled from "styled-components";

type Props = {
  className?: string;
  links?: Links;
  burgerMode: boolean;
};

const StyledLogoutButton = styled.div`
  @media screen and (max-width: ${devices.desktop.width}px) {
    border-top: 1px solid white;
    margin-top: 1rem;
    padding-top: 1rem;
    padding-bottom: 1rem;
  }

  @media screen and (min-width: ${devices.desktop.width}px) {
    border-left: 3px solid white;
    margin-left: 2rem;
  }
`;

const LogoutButton: FC<Props> = ({ burgerMode, links, className }) => {
  const [t] = useTranslation("commons");
  const history = useHistory();

  const extensionProps = {
    links,
    label: t("primary-navigation.logout"),
  };

  if (binder.hasExtension("primary-navigation.logout", extensionProps)) {
    return <ExtensionPoint key="primary-navigation.logout" name="primary-navigation.logout" props={extensionProps} />;
  } else {
    return (
      <StyledLogoutButton
        data-testid="primary-navigation-logout"
        onClick={() => history.push({ pathname: "/logout" })}
        className={classNames("is-align-items-center", "navbar-item", className)}
      >
        <Icon
          title={t("primary-navigation.logout")}
          name="sign-out-alt"
          color="white"
          className={burgerMode ? "is-size-5" : "is-size-4"}
        />
        {" " + t("primary-navigation.logout")}
      </StyledLogoutButton>
    );
  }
};

export default LogoutButton;
