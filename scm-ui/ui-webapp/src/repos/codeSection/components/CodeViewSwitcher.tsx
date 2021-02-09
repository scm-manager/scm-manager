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
import { Button, ButtonAddons } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

const SmallButton = styled(Button)`
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 600;
`;

const ButtonAddonsMarginRight = styled(ButtonAddons)`
  margin-right: -0.2em;
`;

type Props = {
  currentUrl: string;
  switchViewLink: string;
};

const CodeViewSwitcher: FC<Props> = ({ currentUrl, switchViewLink }) => {
  const { t } = useTranslation("repos");

  let location = "";
  if (currentUrl.includes("/code/branch") || currentUrl.includes("/code/changesets")) {
    location = "changesets";
  } else if (currentUrl.includes("/code/sources")) {
    location = "sources";
  }

  return (
    <ButtonAddonsMarginRight>
      <SmallButton
        label={t("code.commits")}
        icon="fa fa-exchange-alt"
        color={location === "changesets" ? "link is-selected" : undefined}
        link={location === "sources" ? switchViewLink : undefined}
      />
      <SmallButton
        label={t("code.sources")}
        icon="fa fa-code"
        color={location === "sources" ? "link is-selected" : undefined}
        link={location === "changesets" ? switchViewLink : undefined}
      />
    </ButtonAddonsMarginRight>
  );
};

export default CodeViewSwitcher;
