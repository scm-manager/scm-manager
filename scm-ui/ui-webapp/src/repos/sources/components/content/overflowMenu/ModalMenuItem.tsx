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
import { Icon } from "@scm-manager/ui-components";
import { extensionPoints } from "@scm-manager/ui-extensions";
import { Menu } from "@scm-manager/ui-overlays";

const ModalMenuItem: FC<
  extensionPoints.ModalMenuProps & {
    setSelectedModal: (element: ReactElement | undefined) => void;
    extensionProps: extensionPoints.ContentActionExtensionProps;
    setLoading?: (isLoading: boolean) => void;
  }
> = ({ modalElement, label, icon, props, extensionProps, setSelectedModal, setLoading }) => {
  const [t] = useTranslation("plugins");

  return (
    <Menu.Button
      onSelect={() =>
        setSelectedModal(
          React.createElement(modalElement, { ...extensionProps, close: () => setSelectedModal(undefined), setLoading })
        )
      }
      {...props}
    >
      <Icon name={icon} className="pr-5 has-text-inherit" />
      <span>{t(label)}</span>
    </Menu.Button>
  );
};

export default ModalMenuItem;
