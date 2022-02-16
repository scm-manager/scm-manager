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
