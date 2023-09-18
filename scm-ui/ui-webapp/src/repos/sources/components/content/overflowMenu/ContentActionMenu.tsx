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
import React, { FC, ReactElement, useCallback, useState } from "react";
import styled from "styled-components";
import { Menu } from "@scm-manager/ui-overlays";
import FallbackMenuButton from "./FallbackMenuButton";
import MenuItem from "./MenuItem";
import { Button, Icon } from "@scm-manager/ui-buttons";
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
