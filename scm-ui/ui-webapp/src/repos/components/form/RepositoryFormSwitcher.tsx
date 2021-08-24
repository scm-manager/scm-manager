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
import styled from "styled-components";
import { Button, ButtonAddons, Icon, Level, urls } from "@scm-manager/ui-components";
import { useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";

const SmallButton = styled(Button)`
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 600;
`;

const TopLevel = styled(Level)`
  margin-top: 1.5rem;
  margin-bottom: -1.5rem;
  height: 0;
  position: absolute;
  right: 0;
  @media (max-width: 785px) {
    margin-top: 4.5rem;
  }
`;

type RepositoryForm = {
  path: string;
  icon: string;
  label: string;
};

const RepositoryFormButton: FC<RepositoryForm> = ({ path, icon, label }) => {
  const location = useLocation();
  const href = urls.concat("/repos/create", path);
  const isSelected = href === location.pathname;
  const [t] = useTranslation(["repos", "plugins"]);

  return (
    <SmallButton color={isSelected ? "link is-selected" : undefined} link={!isSelected ? href : undefined}>
      <Icon className="pr-2" name={icon} color={isSelected ? "white" : "default"} />
      <p className="is-hidden-mobile is-hidden-tablet-only">{t(`plugins:${label}`, label)}</p>
    </SmallButton>
  );
};

type Props = {
  forms: RepositoryForm[];
};

const RepositoryFormSwitcher: FC<Props> = ({ forms }) => (
  <TopLevel
    right={
      <ButtonAddons>
        {(forms || []).map((form) => (
          <RepositoryFormButton key={form.path} {...form} />
        ))}
      </ButtonAddons>
    }
  />
);

export default RepositoryFormSwitcher;
