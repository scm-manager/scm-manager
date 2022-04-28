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
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { Icon } from "@scm-manager/ui-components";
import styled from "styled-components";
import { Link } from "react-router-dom";
import { extensionPoints } from "@scm-manager/ui-extensions";

const MenuItemLinkContainer = styled(Link)<{ active: boolean }>`
  border-radius: 5px;
  padding: 0.5rem;
  color: ${(props) => (props.active ? "var(--scm-white-color)" : "inherit")};
  :hover {
    color: var(--scm-white-color);
  }
`;

const LinkMenuItem: FC<
  extensionPoints.LinkMenuProps & { active: boolean; extensionProps: extensionPoints.ContentActionExtensionProps }
> = ({ link, active, label, icon, props, extensionProps, ...rest }) => {
  const [t] = useTranslation("plugins");

  return (
    <MenuItemLinkContainer
      className={classNames("is-clickable", "is-flex", "is-justify-content-space-between", {
        "has-background-info": active,
      })}
      to={link(extensionProps)}
      title={t(label)}
      active={active}
      {...props}
      {...rest}
    >
      <span className="pr-2">{t(label)}</span>
      <Icon name={icon} color="inherit" />
    </MenuItemLinkContainer>
  );
};

export default LinkMenuItem;
