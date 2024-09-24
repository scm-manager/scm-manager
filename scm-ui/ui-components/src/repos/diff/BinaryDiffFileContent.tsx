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

import React, { FC } from "react";
import { ContentType } from "@scm-manager/ui-types";
import styled from "styled-components";
import { useTranslation } from "react-i18next";

const CompareContainer = styled.div`
  gap: 1rem;
  padding: 1rem;
`;

const CompareImage = styled.img`
  border-width: 3px;
  border-style: solid;
  width: 100%;
`;

const CompareImageContainer = styled.div`
  flex: 1;
  width: 50%;
`;

const CompareImageLabel = styled.div`
  text-transform: capitalize;
`;

const isImageMediaType = (mediaType?: string) => (mediaType ? mediaType.match(/^image\/.+/g) : false);

export const canDisplayBinaryFile = (oldContentType?: ContentType, newContentType?: ContentType) =>
  isImageMediaType(newContentType?.type) || isImageMediaType(oldContentType?.type);

type Props = {
  oldContentType?: ContentType;
  newContentType?: ContentType;
  oldFileLink?: string;
  newFileLink?: string;
  sideBySide?: boolean;
};

const BinaryDiffFileContent: FC<Props> = ({ oldContentType, newContentType, oldFileLink, newFileLink, sideBySide }) => {
  const isNewFileImage = isImageMediaType(newContentType?.type);
  const isOldFileImage = isImageMediaType(oldContentType?.type);
  const isImage = isNewFileImage || isOldFileImage;
  const isChangedImage = isNewFileImage && isOldFileImage;
  const [t] = useTranslation("repos");

  if (isChangedImage && oldFileLink && newFileLink) {
    return (
      <CompareContainer className="is-flex">
        <CompareImageContainer>
          <CompareImageLabel className="has-text-danger">{t("diff.changes.delete")}</CompareImageLabel>
          <CompareImage className="has-border-danger" src={oldFileLink} alt="" />
        </CompareImageContainer>
        <CompareImageContainer>
          <CompareImageLabel className="has-text-success">{t("diff.changes.add")}</CompareImageLabel>
          <CompareImage className="has-border-success" src={newFileLink} alt="" />
        </CompareImageContainer>
      </CompareContainer>
    );
  } else if (isImage) {
    return <img src={newFileLink || oldFileLink} alt="" />;
  }
  return null;
};

export default BinaryDiffFileContent;
