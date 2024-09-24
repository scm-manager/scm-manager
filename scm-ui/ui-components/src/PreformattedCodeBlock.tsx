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
import Button from "./buttons/Button";
import copyToClipboard from "./CopyToClipboard";

type Props = {
  children: string;
};

const TopRightButton = styled(Button)`
  position: absolute;
  display: none;
  height: inherit;
  top: 1.25em;
  right: 1.5em;
`;

const Container = styled.div`
  &:hover > ${TopRightButton} {
    display: inline-block;
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
      <TopRightButton className="is-small" title={t("syntaxHighlighting.copyButton")} action={copy}>
        <i className={copied ? "fa fa-clipboard-check" : "fa fa-clipboard"} />
      </TopRightButton>
    </Container>
  );
};

export default PreformattedCodeBlock;
