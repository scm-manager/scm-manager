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

import React, { FC, ReactElement } from "react";
import { Button, Icon } from "@scm-manager/ui-components";
import styled from "styled-components";
import { Link } from "react-router-dom";
import { extensionPoints } from "@scm-manager/ui-extensions";
import { useTranslation } from "react-i18next";

const FallbackButton = styled(Button)`
  height: 2.5rem;
  width: 50px;
  margin-bottom: 0.5rem;
  > i {
    padding: 0 !important;
  }
  &:hover {
    color: var(--scm-link-color);
  }
`;

const FallbackLink = styled(Link)`
  width: 50px;
  &:hover {
    color: var(--scm-link-color);
  }
`;

const FallbackMenuButton: FC<{
  extension: extensionPoints.FileViewActionBarOverflowMenu["type"];
  extensionProps: extensionPoints.ContentActionExtensionProps;
  setSelectedModal: (element: ReactElement | undefined) => void;
}> = ({ extension, extensionProps, setSelectedModal }) => {
  const [t] = useTranslation("plugins");
  if ("action" in extension) {
    return (
      <FallbackButton
        icon={extension.icon}
        title={t(extension.label)}
        action={() => extension.action(extensionProps)}
      />
    );
  }
  if ("link" in extension) {
    return (
      <FallbackLink to={extension.link(extensionProps)} className="button" title={t(extension.label)}>
        <Icon name={extension.icon} color="inherit" />
      </FallbackLink>
    );
  }
  if ("modalElement" in extension) {
    return (
      <FallbackButton
        icon={extension.icon}
        title={t(extension.label)}
        action={() =>
          setSelectedModal(
            React.createElement(extension.modalElement, {
              ...extensionProps,
              close: () => setSelectedModal(undefined),
            })
          )
        }
      />
    );
  }
  return null;
};
export default FallbackMenuButton;
