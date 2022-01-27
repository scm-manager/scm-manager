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
import React from "react";
import styled from "styled-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
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
        <ExtensionPoint
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
