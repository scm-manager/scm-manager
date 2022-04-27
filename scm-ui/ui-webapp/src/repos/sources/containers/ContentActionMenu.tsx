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
import React, { FC, FunctionComponent, ReactElement, useState } from "react";
import { Button, Icon } from "@scm-manager/ui-components";
import styled from "styled-components";
import { Menu } from "@headlessui/react";
import classNames from "classnames";
import { useTranslation } from "react-i18next";

const MenuButton = styled(Menu.Button)`
  background: transparent;
  border: none;
  font-size: 1.5rem;
  height: 2.5rem;
  width: 50px;
  margin-bottom: 0.5rem;
`;

const FallbackButton = styled(Button)`
  height: 2.5rem;
  width: 50px;
  margin-bottom: 0.5rem;
  > i {
    padding: 0 !important;
  }
`;

const MenuItems = styled(Menu.Items)`
  padding: 0.5rem;
  position: absolute;
  z-index: 999;
  width: max-content;
  border: var(--scm-border);
  border-radius: 5px;
  background-color: var(--scm-light-color);
`;

const MenuItemContainer = styled.div`
  border-radius: 5px;
  padding: 0.5rem;
`;

const HR = styled.hr`
  margin: 0.25rem;
  background: var(--scm-border-color);
`;

type Props = {
  extensionProps: extensionPoints.ContentActionExtensionProps;
};

const ContentActionMenu: FC<Props> = ({ extensionProps }) => {
  const [t] = useTranslation("plugins");
  const [selectedComponent, setSelectedComponent] = useState<ReactElement | undefined>();
  const extensions = binder.getExtensions<extensionPoints.FileViewActionBarOverflowMenu>(
    "repos.sources.content.actionbar.menu",
    extensionProps
  );
  const categories = extensions.map(e => e.category);
  const filteredCategories = categories.filter((item, index) => categories?.indexOf(item) === index);

  const renderSelectedComponent = (component: FunctionComponent<extensionPoints.ContentActionExtensionProps>) => {
    setSelectedComponent(
      React.createElement(component, { ...extensionProps, unmountComponent: () => setSelectedComponent(undefined) })
    );
  };

  const renderMenu = () => (
    <Menu as="div" className="is-relative">
      {({ open }) => (
        <>
          <MenuButton>
            <Icon name="ellipsis-v" className="has-text-default" />
          </MenuButton>
          {open && (
            <div className="has-background-secondary-least">
              <MenuItems>
                {filteredCategories.map((category, index) => (
                  <div key={category}>
                    {extensions
                      .filter(extension => extension.category === category)
                      .map(extension => (
                        <Menu.Item as={React.Fragment} key={extension.label}>
                          {({ active }) => (
                            <MenuItemContainer
                              className={classNames("is-clickable", "is-flex", "is-justify-content-space-between", {
                                "has-background-info has-text-white": active
                              })}
                              onClick={() => renderSelectedComponent(extension.component)}
                              title={t(extension.label)}
                              {...extension.props}
                            >
                              <span className="pr-2">{t(extension.label)}</span>
                              <Icon name={extension.icon} color="inherit" />
                            </MenuItemContainer>
                          )}
                        </Menu.Item>
                      ))}
                    {filteredCategories.length > index + 1 ? <HR /> : null}
                  </div>
                ))}
              </MenuItems>
            </div>
          )}
        </>
      )}
    </Menu>
  );

  if (extensions.length <= 0) {
    return null;
  }

  return (
    <>
      {extensions.length === 1 ? (
        <FallbackButton
          title={t(extensions[0].label)}
          icon={extensions[0].icon}
          action={() => renderSelectedComponent(extensions[0].component)}
        />
      ) : (
        renderMenu()
      )}
      {selectedComponent || null}
    </>
  );
};

export default ContentActionMenu;
