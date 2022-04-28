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
import ActionMenuItem from "./ActionMenuItem";
import LinkMenuItem from "./LinkMenuItem";
import ModalMenuItem from "./ModalMenuItem";
import { extensionPoints } from "@scm-manager/ui-extensions";

const MenuItem: FC<
  extensionPoints.FileViewActionBarOverflowMenu["type"] & {
    active: boolean;
    onClick: (event: React.MouseEvent<HTMLDivElement, MouseEvent>) => void;
    setSelectedModal: (element: ReactElement | undefined) => void;
    extensionProps: extensionPoints.ContentActionExtensionProps;
  }
> = ({ extensionProps, label, icon, props, category, active, onClick, setSelectedModal, ...rest }) => {
  if ("action" in rest) {
    return (
      <ActionMenuItem
        label={label}
        icon={icon}
        category={category}
        extensionProps={extensionProps}
        active={active}
        onClick={onClick}
        {...rest}
      />
    );
  }
  if ("link" in rest) {
    return (
      <LinkMenuItem
        category={category}
        label={label}
        icon={icon}
        active={active}
        extensionProps={extensionProps}
        {...rest}
      />
    );
  }
  if ("modalElement" in rest) {
    return (
      <ModalMenuItem
        category={category}
        label={label}
        icon={icon}
        extensionProps={extensionProps}
        active={active}
        onClick={onClick}
        setSelectedModal={setSelectedModal}
        {...rest}
      />
    );
  }
  return null;
};

export default MenuItem;
