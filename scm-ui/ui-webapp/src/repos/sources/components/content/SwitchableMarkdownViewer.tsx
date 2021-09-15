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
import React, { FC, useState } from "react";
import styled from "styled-components";
import MarkdownViewer from "./MarkdownViewer";
import SourcecodeViewer from "./SourcecodeViewer";
import { File } from "@scm-manager/ui-types";
import { Button } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

const ToggleButton = styled(Button)`
  max-width: 1rem;
  position: absolute;
  top: 0;
  right: 0.25rem;
  z-index: 30;
`;

type Props = {
  file: File;
  basePath: string;
};

const SwitchableMarkdownViewer: FC<Props> = ({ file, basePath }) => {
  const { t } = useTranslation("repos");
  const [renderMarkdown, setRenderMarkdown] = useState(true);

  const toggleMarkdown = () => {
    setRenderMarkdown(!renderMarkdown);
  };

  return (
    <div className="is-relative">
      <ToggleButton
        color={renderMarkdown ? "link" : ""}
        action={toggleMarkdown}
        title={
          renderMarkdown
            ? t("sources.content.toggleButton.showSources")
            : t("sources.content.toggleButton.showMarkdown")
        }
      >
        <i className="fab fa-markdown" />
      </ToggleButton>
      {renderMarkdown ? (
        <MarkdownViewer file={file} basePath={basePath} />
      ) : (
        <SourcecodeViewer file={file} language={"MARKDOWN"} />
      )}
    </div>
  );
};

export default SwitchableMarkdownViewer;
