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
import { FC, MutableRefObject, ReactNode } from "react";
import { useTranslation } from "react-i18next";
import { Modal } from "./Modal";
import Button from "../buttons/Button";
import styled from "styled-components";

type Props = {
  title: string;
  closeFunction: () => void;
  body: ReactNode;
  active: boolean;
  closeButtonLabel?: string;
  initialFocusRef?: MutableRefObject<HTMLElement | null>;
};

const FullSizedModal = styled(Modal)`
  & .modal-card {
    width: 98%;
    max-height: 97vh;
  }
`;

const FullscreenModal: FC<Props> = ({ title, closeFunction, body, active, initialFocusRef, closeButtonLabel }) => {
  const [t] = useTranslation("repos");
  const footer = (
    <Button label={closeButtonLabel || t("diff.fullscreen.close")} action={closeFunction} color="secondary" />
  );

  return (
    <FullSizedModal
      title={title}
      closeFunction={closeFunction}
      body={body}
      footer={footer}
      active={active}
      initialFocusRef={initialFocusRef}
    />
  );
};

export default FullscreenModal;
