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
import { Trans, useTranslation } from "react-i18next";
import { Link, PluginCenterAuthenticationInfo } from "@scm-manager/ui-types";
import classNames from "classnames";
import styled from "styled-components";
import { Button } from "@scm-manager/ui-components";

type Props = {
  info: PluginCenterAuthenticationInfo;
};

const CloudoguPlatformBanner: FC<Props> = ({ info }) => {
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
          i18nKey="plugins.cloudoguPlatform.failed.message"
          values={{ subject: info.pluginCenterSubject }}
          components={[<a href="https://platform.cloudogu.com/">cloudogu platform</a>, <strong />]}
        />
      </p>
      <Button className="mt-5 has-text-weight-normal has-border-info" reducedMobile={true} link={link}>
        <Trans
          t={t}
          i18nKey="plugins.cloudoguPlatform.failed.button.label"
          components={[<span className="mx-1 has-text-info">cloudogu platform</span>]}
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
          i18nKey="plugins.cloudoguPlatform.login.button.label"
          components={[<span className="mx-1 has-text-info">cloudogu platform</span>]}
        />
      </Button>
      <p className="is-align-self-flex-start is-size-7">
        <Trans
          t={t}
          i18nKey="plugins.cloudoguPlatform.login.description"
          components={[
            <a href="https://platform.cloudogu.com/">cloudogu platform</a>,
            <a href="https://scm-manager.org/data-processing">Data Processing</a>
          ]}
        />
      </p>
    </Container>
  );
};

export default CloudoguPlatformBanner;
