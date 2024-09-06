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

import React, { FC, MouseEvent } from "react";
import { useTranslation } from "react-i18next";
import { useSearchHelpContent } from "@scm-manager/ui-api";
import { ErrorNotification, Loading, MarkdownView, Modal } from "@scm-manager/ui-components";

type Props = {
  close: () => void;
};

const SyntaxModalContent: FC<Props> = ({ close }) => {
  const { i18n } = useTranslation("commons");
  const { isLoading, data, error } = useSearchHelpContent(i18n.languages[0]);

  const handleClickEvent = (e: MouseEvent<HTMLElement>) => {
    const target = e.target as HTMLElement;
    if (target.tagName === "A") {
      close();
    }
  };

  if (error) {
    return <ErrorNotification error={error} />;
  } else if (isLoading || !data) {
    return <Loading />;
  } else {
    return (
      <div onClick={handleClickEvent}>
        <MarkdownView content={data} basePath="/" />
      </div>
    );
  }
};

const SyntaxModal: FC<Props> = ({ close }) => {
  const [t] = useTranslation("commons");
  return (
    <Modal
      active={true}
      title={t("search.quickSearch.hints")}
      body={<SyntaxModalContent close={close} />}
      closeFunction={close}
    />
  );
};

export default SyntaxModal;
