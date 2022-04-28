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
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { Icon } from "@scm-manager/ui-components";
import { MenuItemContainer } from "./ContentActionMenu";
import { extensionPoints } from "@scm-manager/ui-extensions";

const ModalMenuItem: FC<
  extensionPoints.ModalMenuProps & {
    active: boolean;
    onClick: (event: React.MouseEvent<HTMLDivElement, MouseEvent>) => void;
    setSelectedModal: (element: ReactElement | undefined) => void;
    extensionProps: extensionPoints.ContentActionExtensionProps;
  }
> = ({ modalElement, active, label, icon, props, extensionProps, setSelectedModal, ...rest }) => {
  const [t] = useTranslation("plugins");

  return (
    <MenuItemContainer
      className={classNames("is-clickable", "is-flex", "is-justify-content-space-between", {
        "has-background-info has-text-white": active,
      })}
      title={t(label)}
      {...props}
      {...rest}
      onClick={(event) => {
        setSelectedModal(
          React.createElement(modalElement, { ...extensionProps, close: () => setSelectedModal(undefined) })
        );
        rest.onClick(event);
      }}
    >
      <span className="pr-2">{t(label)}</span>
      <Icon name={icon} color="inherit" />
    </MenuItemContainer>
  );
};

export default ModalMenuItem;
