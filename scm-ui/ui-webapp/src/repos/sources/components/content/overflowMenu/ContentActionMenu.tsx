/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import { binder, extensionPoints } from "@scm-manager/ui-extensions";
import React, { FC, ReactElement, useCallback, useState } from "react";
import styled from "styled-components";
import { Menu } from "@scm-manager/ui-overlays";
import FallbackMenuButton from "./FallbackMenuButton";
import MenuItem from "./MenuItem";
import { Button, Icon } from "@scm-manager/ui-core";
import { useTranslation } from "react-i18next";
import { SmallLoadingSpinner } from "@scm-manager/ui-components";

const HR = styled.hr`
  margin: 0.25rem;
  background: var(--scm-border-color);
`;

const StyledLoadingButton = styled(Button)`
  padding-left: 1rem;
  padding-right: 1rem;
`;

type Props = {
  extensionProps: extensionPoints.ContentActionExtensionProps;
};

const ContentActionMenu: FC<Props> = ({ extensionProps }) => {
  const [t] = useTranslation("repos");
  const [selectedModal, setSelectedModal] = useState<ReactElement | undefined>();
  const extensions = binder.getExtensions<extensionPoints.FileViewActionBarOverflowMenu>(
    "repos.sources.content.actionbar.menu",
    extensionProps
  );

  const [loadingExtension, setLoadingExtensions] = useState<Record<string, boolean>>({});

  const setLoading = useCallback((isLoading: boolean, extension: string) => {
    setLoadingExtensions((prevState) => ({ ...prevState, [extension]: isLoading }));
  }, []);

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

  if (extensions.length <= 0) {
    return null;
  }

  return (
    <>
      {Object.values(loadingExtension).some((isLoading) => isLoading) ? (
        <StyledLoadingButton disabled>
          <SmallLoadingSpinner />
        </StyledLoadingButton>
      ) : extensions.length === 1 ? (
        <FallbackMenuButton
          extension={extensions[0]}
          extensionProps={extensionProps}
          setSelectedModal={setSelectedModal}
        />
      ) : (
        <Menu
          trigger={
            <Menu.Trigger
              className="has-background-transparent has-hover-color-blue px-2"
              aria-label={t("sources.content.actionMenuTrigger")}
            >
              <Icon>ellipsis-v</Icon>
            </Menu.Trigger>
          }
        >
          {Object.entries(categories).map(([_category, extensionSet], index) => (
            <>
              {extensionSet.map((extension) => (
                <MenuItem
                  key={extension.label}
                  extensionProps={extensionProps}
                  setSelectedModal={setSelectedModal}
                  setLoading={(isLoading: boolean) => setLoading(isLoading, extension.label)}
                  {...extension}
                />
              ))}
              {Object.keys(categories).length > index + 1 ? <HR /> : null}
            </>
          ))}
        </Menu>
      )}
      {selectedModal || null}
    </>
  );
};

export default ContentActionMenu;
