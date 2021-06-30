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

import React, { FC, useRef, useState } from "react";
import { Button, Icon, Modal } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { ApiKeyWithToken } from "@scm-manager/ui-types";

type Props = {
  addedKey: ApiKeyWithToken;
  close: () => void;
};

const KeyArea = styled.textarea`
  white-space: nowrap;
  overflow: auto;
  font-family: "Courier New", Monaco, Menlo, "Ubuntu Mono", "source-code-pro", monospace;
  height: 3rem;
`;

const NoLeftMargin = styled.div`
  margin-left: -1rem;
`;

const ApiKeyCreatedModal: FC<Props> = ({ addedKey, close }) => {
  const [t] = useTranslation("users");
  const [copied, setCopied] = useState(false);
  const keyRef = useRef<HTMLTextAreaElement>(null);

  const copy = () => {
    keyRef.current!.select();
    document.execCommand("copy");
    setCopied(true);
  };

  const newPassphraseModalContent = (
    <div className={"media-content"}>
      <p>{t("apiKey.modal.text1")}</p>
      <p>
        <b>{t("apiKey.modal.text2")}</b>
      </p>
      <hr />
      <div className={"columns"}>
        <div className={"column is-11"}>
          <KeyArea wrap={"soft"} ref={keyRef} className={"input"} value={addedKey.token} />
        </div>
        <NoLeftMargin className={"column is-1"}>
          <Icon
            className={"is-hidden-mobile fa-2x"}
            name={copied ? "clipboard-check" : "clipboard"}
            title={t("apiKey.modal.clipboard")}
            onClick={copy}
          />
        </NoLeftMargin>
      </div>
    </div>
  );

  return (
    <Modal
      body={newPassphraseModalContent}
      closeFunction={close}
      title={t("apiKey.modal.title")}
      footer={<Button label={t("apiKey.modal.close")} action={close} />}
      active={true}
    />
  );
};

export default ApiKeyCreatedModal;
