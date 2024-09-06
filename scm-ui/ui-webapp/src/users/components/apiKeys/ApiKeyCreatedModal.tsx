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
  overflow-x: auto;
  overflow-y: hidden;
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
    keyRef.current?.select();
    document.execCommand("copy");
    setCopied(true);
  };

  const newPassphraseModalContent = (
    <div className="media-content">
      <p>{t("apiKey.modal.text1")}</p>
      <p>
        <b>{t("apiKey.modal.text2")}</b>
      </p>
      <hr />
      <div className="columns">
        <div className="column is-11">
          <KeyArea
            wrap={"soft"}
            ref={keyRef}
            className="input"
            value={addedKey.token}
            aria-label={t("apiKey.modal.alt")}
          />
        </div>
        <NoLeftMargin className="column is-1">
          <Icon
            className="is-hidden-mobile fa-2x"
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
