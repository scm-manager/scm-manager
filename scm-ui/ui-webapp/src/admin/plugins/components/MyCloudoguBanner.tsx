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
import { Trans, useTranslation } from "react-i18next";
import { Link, PluginCenterAuthenticationInfo } from "@scm-manager/ui-types";
import classNames from "classnames";
import styled from "styled-components";
import { Button } from "@scm-manager/ui-components";

type Props = {
  info: PluginCenterAuthenticationInfo;
};

const MyCloudoguBanner: FC<Props> = ({ info }) => {
  const loginLink = (info._links.login as Link)?.href;
  if (loginLink) {
    return <Unauthenticated info={info} link={loginLink} />;
  }

  if (info.failed) {
    const reconnectLink = (info._links.reconnect as Link)?.href;
    if (reconnectLink) {
      return <FailedAuthentication info={info} link={reconnectLink} />;
    }
  }

  return null;
};

type PropsWithLink = Props & {
  link: string;
};

const FailedAuthentication: FC<PropsWithLink> = ({ info, link }) => {
  const [t] = useTranslation("admin");
  return (
    <Container className="has-border-danger">
      <p className="is-align-self-flex-start">
        <Trans
          t={t}
          i18nKey="plugins.myCloudogu.failed.message"
          values={{ subject: info.pluginCenterSubject }}
          components={[<a href="https://my.cloudogu.com/">myCloudogu</a>, <strong />]}
        />
      </p>
      <Button className="mt-5 has-text-weight-normal has-border-info" reducedMobile={true} link={link}>
        <Trans
          t={t}
          i18nKey="plugins.myCloudogu.failed.button.label"
          components={[<span className="mx-1 has-text-info">myCloudogu</span>]}
        />
      </Button>
    </Container>
  );
};

type ContainerProps = {
  className?: string;
};

const Container: FC<ContainerProps> = ({ className, children }) => (
  <DivWithSolidBorder
    className={classNames(
      "has-rounded-border is-flex is-flex-direction-column is-align-items-center p-5 mb-4",
      className
    )}
  >
    {children}
  </DivWithSolidBorder>
);

const DivWithSolidBorder = styled.div`
  border: 2px solid;
`;

const Unauthenticated: FC<PropsWithLink> = ({ link, info }) => {
  const [t] = useTranslation("admin");
  return (
    <Container className="has-border-success">
      <Button className="mb-5 has-text-weight-normal has-border-info" reducedMobile={true} link={link}>
        <Trans
          t={t}
          i18nKey="plugins.myCloudogu.login.button.label"
          components={[<span className="mx-1 has-text-info">myCloudogu</span>]}
        />
      </Button>
      <p className="is-align-self-flex-start is-size-7">
        <Trans
          t={t}
          i18nKey="plugins.myCloudogu.login.description"
          components={[
            <a href="https://my.cloudogu.com/">myCloudogu</a>,
            <a href="https://scm-manager.org/data-processing">Data Processing</a>
          ]}
        />
      </p>
    </Container>
  );
};

export default MyCloudoguBanner;
