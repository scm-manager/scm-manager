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
  const [t] = useTranslation("repos");
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
