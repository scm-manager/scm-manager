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

import * as React from "react";
import { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { Icon, IconButton } from "@scm-manager/ui-core";
import copyToClipboard from "./CopyToClipboard";

type Props = {
  children: string;
};

// Opaque background, so the code below the transparent icon button stays covered
const TopRightCorner = styled.div`
  position: absolute;
  display: none;
  top: 1.25em;
  right: 1.5em;
  border-radius: 6px;
  background-color: var(--scm-secondary-background);
`;

const Container = styled.div`
  &:hover > ${TopRightCorner} {
    display: flex;
  }
`;

const PreformattedCodeBlock: FC<Props> = ({ children }) => {
  const [t] = useTranslation("commons");
  const [copied, setCopied] = useState(false);

  const copy = () => copyToClipboard(children).then(() => setCopied(true));

  return (
    <Container className="is-relative">
      <pre>
        <code>{children}</code>
      </pre>
      <TopRightCorner>
        <IconButton
          title={t("syntaxHighlighting.copyButton")}
          aria-label={t("syntaxHighlighting.copyButton")}
          onClick={copy}
        >
          <Icon>{copied ? "clipboard-check" : "clipboard"}</Icon>
        </IconButton>
      </TopRightCorner>
    </Container>
  );
};

export default PreformattedCodeBlock;
