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
import InfoBox from "./InfoBox";
import LoginForm from "./LoginForm";
import { Image, Loading } from "@scm-manager/ui-components";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import styled from "styled-components";
import { useLoginInfo } from "@scm-manager/ui-api";
import { useTranslation } from "react-i18next";

const TopMarginBox = styled.div`
  margin-top: 5rem;
`;

const AvatarWrapper = styled.figure`
  display: flex;
  justify-content: center;
  margin: -70px auto 20px;
  width: 128px;
  height: 128px;
  background: var(--scm-white-color);
  border: 1px solid lightgray;
  border-radius: 50%;
`;

const AvatarImage = styled(Image)`
  width: 75%;
  margin-left: 0.25rem;
  padding: 5px;
`;

type Props = {
  /**
   * @deprecated Unused because the component now uses {@link useLoginInfo} internally.
   */
  loginInfoLink?: string;
  loading?: boolean;
  error?: Error | null;
  loginHandler: (username: string, password: string) => void;
};

const LoginInfo: FC<Props> = (props) => {
  const { isLoading: isLoadingLoginInfo, data: info } = useLoginInfo();
  const [t] = useTranslation("commons");

  if (isLoadingLoginInfo) {
    return <Loading />;
  }

  let infoPanel;
  if (info) {
    infoPanel = (
      <div className="column is-7 is-offset-1 p-0">
        <InfoBox item={info.feature} type="feature" />
        <InfoBox item={info.plugin} type="plugin" />
      </div>
    );
  }

  return (
    <>
      <div className="column is-4 box has-text-centered has-background-secondary-less">
        <h3 className="title">{t("login.title")}</h3>
        <p className="subtitle">{t("login.subtitle")}</p>
        <TopMarginBox className="box">
          <AvatarWrapper>
            <AvatarImage src="/images/blibSmallLightBackground.svg" alt={t("login.logo-alt")} />
          </AvatarWrapper>
          <ExtensionPoint<extensionPoints.LoginForm> name="login.form" props={{}}>
            <LoginForm {...props} />
          </ExtensionPoint>
        </TopMarginBox>
      </div>
      {infoPanel}
    </>
  );
};

export default LoginInfo;
