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
import React, { ComponentType, FC, ReactElement, useState } from "react";
import { Button, Icon } from "@scm-manager/ui-components";
import styled from "styled-components";
import { Menu } from "@headlessui/react";
import classNames from "classnames";
import { useTranslation } from "react-i18next";
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

const MenuItemLinkContainer = styled(Link)`
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

const ActionMenuItem: FC<extensionPoints.ActionMenuProps & Props & { active: boolean }> = ({
  action,
  active,
  label,
  icon,
  props,
  extensionProps,
  ...rest
}) => {
  const [t] = useTranslation("plugins");

  return (
    <MenuItemContainer
      className={classNames("is-clickable", "is-flex", "is-justify-content-space-between", {
        "has-background-info has-text-white": active
      })}
      title={t(label)}
      {...props}
      {...rest}
      onClick={event => {
        rest.onClick(event);
        action(extensionProps);
      }}
    >
      <span className="pr-2">{t(label)}</span>
      <Icon name={icon} color="inherit" />
    </MenuItemContainer>
  );
};

const LinkMenuItem: FC<extensionPoints.LinkMenuProps & Props & { active: boolean }> = ({
  link,
  active,
  label,
  icon,
  props,
  extensionProps,
  ...rest
}) => {
  const [t] = useTranslation("plugins");

  return (
    <MenuItemLinkContainer
      className={classNames("is-clickable", "is-flex", "is-justify-content-space-between", {
        "has-background-info has-text-white": active
      })}
      to={link(extensionProps)}
      title={t(label)}
      {...props}
      {...rest}
    >
      <span className="pr-2">{t(label)}</span>
      <Icon name={icon} color="inherit" />
    </MenuItemLinkContainer>
  );
};

const ModalMenuItem: FC<extensionPoints.ModalMenuProps & Props & { active: boolean }> = ({
  modalElement,
  active,
  label,
  icon,
  props,
  extensionProps,
  ...rest
}) => {
  const [t] = useTranslation("plugins");
  const [showModal, setShowModal] = useState(false);

  return (
    <MenuItemContainer
      className={classNames("is-clickable", "is-flex", "is-justify-content-space-between", {
        "has-background-info has-text-white": active
      })}
      title={t(label)}
      {...props}
      {...rest}
      onClick={event => {
        //TODO fix modal for keyboard actions
        rest.onClick(event);
        setShowModal(true);
      }}
    >
      <span className="pr-2">{t(label)}</span>
      <Icon name={icon} color="inherit" />
      {showModal ? React.createElement(modalElement, { ...extensionProps, close: () => setShowModal(false) }) : null}
    </MenuItemContainer>
  );
};

const MenuItem: FC<extensionPoints.FileViewActionBarOverflowMenu["type"] & Props & { active: boolean }> = ({
  extensionProps,
  label,
  icon,
  props,
  category,
  active,
  ...rest
}) => {
  console.log("rest", rest);
  if ("action" in rest) {
    return (
      <ActionMenuItem
        label={label}
        icon={icon}
        category={category}
        extensionProps={extensionProps}
        active={active}
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
        {...rest}
      />
    );
  }
  return null;
};

const ContentActionMenu: FC<Props> = ({ extensionProps }) => {
  const [t] = useTranslation("plugins");
  const [selectedComponent, setSelectedComponent] = useState<ReactElement | undefined>();
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

  const renderSelectedComponent = (component: ComponentType<extensionPoints.ContentActionExtensionProps>) => {
    setSelectedComponent(React.createElement(component, { ...extensionProps }));
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
                {Object.entries(categories).map(([category, extensions], index) => (
                  <>
                    {extensions.map(extension => (
                      <Menu.Item as={React.Fragment} key={extension.label}>
                        {({ active }) => <MenuItem extensionProps={extensionProps} active={active} {...extension} />}
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
