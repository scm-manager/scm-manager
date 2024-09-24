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
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { Repository } from "@scm-manager/ui-types";
import { Image } from "@scm-manager/ui-components";
import styled from "styled-components";

const Avatar = styled.div`
  border-radius: 5px;
`;

type Props = {
  repository: Repository;
  size?: 16 | 24 | 32 | 48 | 64 | 96 | 128;
};

const renderExtensionPoint = (repository: Repository, size: Props["size"]) => {
  return (
    <ExtensionPoint<extensionPoints.PrimaryRepositoryAvatar>
      name="repos.repository-avatar.primary"
      props={{
        repository,
        size: size || 64
      }}
    >
      <ExtensionPoint<extensionPoints.RepositoryAvatar>
        name="repos.repository-avatar"
        props={{
          repository,
        }}
      >
        <Image src="/images/blib.jpg" alt="Logo" />
      </ExtensionPoint>
    </ExtensionPoint>
  );
};

const RepositoryAvatar: FC<Props> = ({ repository, size = 64 }) => {
  return <Avatar className={`image is-${size}x${size}`}>{renderExtensionPoint(repository, size)}</Avatar>;
};

export default RepositoryAvatar;
