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
import React, { FC, ReactElement, useState } from "react";
import { Button, Icon } from "@scm-manager/ui-components";
import styled from "styled-components";
import { Menu } from "@headlessui/react";
import { useTranslation } from "react-i18next";
import ActionMenuItem from "./ActionMenuItem";
import LinkMenuItem from "./LinkMenuItem";
import ModalMenuItem from "./ModalMenuItem";
import { Link } from "react-router-dom";

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

const MenuItems = styled(Menu.Items)`
  padding: 0.5rem;
  position: absolute;
  z-index: 999;
  width: max-content;
  border: var(--scm-border);
  border-radius: 5px;
  background-color: var(--scm-secondary-background);
  box-shadow: 0 0.5em 1em -0.125em rgba(10, 10, 10, 0.1), 0 0px 0 1px rgba(10, 10, 10, 0.02);
`;

export const MenuItemContainer = styled.div`
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

const MenuItem: FC<
  extensionPoints.FileViewActionBarOverflowMenu["type"] &
    Props & {
      active: boolean;
      onClick: (event: React.MouseEvent<HTMLDivElement, MouseEvent>) => void;
      setSelectedModal: (element: ReactElement | undefined) => void;
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

const ContentActionMenu: FC<Props> = ({ extensionProps }) => {
  const [t] = useTranslation("plugins");
  const [selectedModal, setSelectedModal] = useState<ReactElement | undefined>();
  const extensions = binder.getExtensions<extensionPoints.FileViewActionBarOverflowMenu>(
    "repos.sources.content.actionbar.menu",
    extensionProps
  );
  const categories = extensions.reduce<Record<string, extensionPoints.FileViewActionBarOverflowMenu["type"][]>>(
    (result, extension) => {
      if (!(extension.category in result)) {
        result[extension.category] = [];
      }
      result[extension.category].push(extension);
      return result;
    },
    {}
  );

  const renderSingleButton = (extension: extensionPoints.FileViewActionBarOverflowMenu["type"]) => {
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

  const renderMenu = () => (
    <>
      <Menu as="div" className="is-relative">
        {({ open }) => (
          <>
            <MenuButton>
              <Icon name="ellipsis-v" className="has-text-default" />
            </MenuButton>
            {open && (
              <div className="has-background-secondary-least">
                <MenuItems>
                  {Object.entries(categories).map(([category, extensions], index) => (
                    <>
                      {extensions.map((extension) => (
                        <Menu.Item as={React.Fragment} key={extension.label}>
                          {({ active }) => {
                            return (
                              // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                              // @ts-ignore onClick prop required but gets provided implicit by the Menu.Item from headless ui
                              <MenuItem
                                extensionProps={extensionProps}
                                active={active}
                                setSelectedModal={setSelectedModal}
                                {...extension}
                              />
                            );
                          }}
                        </Menu.Item>
                      ))}
                      {Object.keys(categories).length > index + 1 ? <HR /> : null}
                    </>
                  ))}
                </MenuItems>
              </div>
            )}
          </>
        )}
      </Menu>
    </>
  );

  if (extensions.length <= 0) {
    return null;
  }

  return (
    <>
      {extensions.length === 1 ? renderSingleButton(extensions[0]) : renderMenu()} {selectedModal || null}
    </>
  );
};

export default ContentActionMenu;
