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

import { binder, extensionPoints } from "@scm-manager/ui-extensions";
import React, { FC } from "react";
import { ContentActionExtensionProps } from "./Content";
import { Button, Icon } from "@scm-manager/ui-components";
import styled from "styled-components";
import { Menu } from "@headlessui/react";
import classNames from "classnames";

const MenuButton = styled(Menu.Button)`
  background: transparent;
  border: none;
  font-size: 1.5rem;
  height: 2.5rem;
  margin-bottom: 0.5rem;
`;

const MenuItems = styled(Menu.Items)`
  padding: 0.5rem;
  position: absolute;
  z-index: 999;
  width: max-content;
  background: var(--scm-white-color);
  border: var(--scm-border);
  border-radius: 5px;
`;

const MenuItemContainer = styled.div`
  border-radius: 5px;
  padding: 0.25rem;
`;

const Category = styled.span`
  font-weight: bold;
  padding: 0.5rem 0;
`;

const HR = styled.hr`
  margin: 0.25rem;
  background: var(--scm-border-color);
`;

type Props = {
  extensionProps: ContentActionExtensionProps;
};

const ContentActionMenu: FC<Props> = ({ extensionProps }) => {
  const extensions = binder.getExtensions<extensionPoints.FileViewActionBarOverflowMenu>(
    "repos.sources.content.actionbar.menu",
    extensionProps
  );

  if (extensions.length <= 0) {
    return null;
  }

  if (extensions.length === 1) {
    const extension = extensions[0];
    return <Button title={extension.label} action={() => extension.action} icon={extension.icon} />;
  }

  const categories = extensions.map(e => e.category);
  const filteredCategories = categories.filter((item, index) => categories?.indexOf(item) === index);

  const renderMenu = () => (
    <>
      <Menu as="div" className="is-relative">
        {({ open }) => (
          <>
            <MenuButton>
              <Icon name="ellipsis-v" color="inherit" />
            </MenuButton>
            {open && (
              <MenuItems>
                {filteredCategories.map((category, index) => (
                  <>
                    <Category className="is-unselectable">{category}</Category>
                    {extensions
                      .filter(extension => extension.category === category)
                      .map(extension => (
                        <Menu.Item as={React.Fragment}>
                          {({ active }) => (
                            <MenuItemContainer
                              className={classNames("is-clickable", {
                                "has-background-info has-text-white": active
                              })}
                              key={extension.label}
                              onClick={() => extension.action(extensionProps)}
                            >
                              {extension.label}
                              <Icon name={extension.icon} color="inherit" />
                            </MenuItemContainer>
                          )}
                        </Menu.Item>
                      ))}
                    {filteredCategories.length > index + 1 ? <HR /> : null}
                  </>
                ))}
              </MenuItems>
            )}
          </>
        )}
      </Menu>
    </>
  );

  return <div>{renderMenu()}</div>;
};

export default ContentActionMenu;
