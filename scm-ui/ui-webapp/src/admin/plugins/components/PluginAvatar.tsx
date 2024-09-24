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

import React from "react";
import styled from "styled-components";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { Plugin } from "@scm-manager/ui-types";
import { Image } from "@scm-manager/ui-components";

type Props = {
  plugin: Plugin;
};

const BoundingBox = styled.p`
  img {
    object-fit: contain;
    height: 64px;
    width: 64px;
  }
`;

export default class PluginAvatar extends React.Component<Props> {
  render() {
    const { plugin } = this.props;
    return (
      <BoundingBox className="image is-64x64">
        <ExtensionPoint<extensionPoints.PluginAvatar>
          name="plugins.plugin-avatar"
          props={{
            plugin
          }}
        >
          <Image src={plugin.avatarUrl ? plugin.avatarUrl : "/images/blibTransparentBG.png"} alt="Logo" />
        </ExtensionPoint>
      </BoundingBox>
    );
  }
}
