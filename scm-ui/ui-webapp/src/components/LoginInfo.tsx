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
