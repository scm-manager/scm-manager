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
