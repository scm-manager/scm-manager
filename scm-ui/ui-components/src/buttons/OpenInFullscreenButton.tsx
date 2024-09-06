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
import { FC, ReactNode, useState } from "react";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import FullscreenModal from "../modals/FullscreenModal";
import { Tooltip } from "@scm-manager/ui-overlays";

type Props = {
  modalTitle: string;
  modalBody: ReactNode;
  tooltipStyle?: "tooltipComponent" | "htmlTitle";
};

const Button = styled.button`
  width: 50px;
  &:hover {
    color: var(--scm-info-color);
  }
`;

/**
 * @deprecated
 */
const OpenInFullscreenButton: FC<Props> = ({ modalTitle, modalBody, tooltipStyle = "tooltipComponent" }) => {
  const [t] = useTranslation("repos");
  const [showModal, setShowModal] = useState(false);

  const tooltip = t("diff.fullscreen.open");
  const button = (
    <Button
      title={tooltipStyle === "htmlTitle" ? tooltip : undefined}
      className="button"
      onClick={() => setShowModal(true)}
      aria-label={tooltip}
    >
      <i className="fas fa-search-plus" />
    </Button>
  );
  const modal = showModal && (
    <FullscreenModal title={modalTitle} closeFunction={() => setShowModal(false)} body={modalBody} active={showModal} />
  );

  if (tooltipStyle === "htmlTitle") {
    return (
      <>
        {button}
        {modal}
      </>
    );
  }

  return (
    <>
      {modal}
      <Tooltip message={tooltip} side="top">
        {button}
      </Tooltip>
    </>
  );
};

export default OpenInFullscreenButton;
