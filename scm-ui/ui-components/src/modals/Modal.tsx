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
import React, { FC, MutableRefObject, ReactNode, useRef } from "react";
import classNames from "classnames";
import styled from "styled-components";
import { Dialog } from "@headlessui/react";
import { devices } from "../devices";

type ModalSize = "S" | "M" | "L";

const modalSizes: { [key in ModalSize]: number } = { S: 33, M: 50, L: 66 };

type Props = {
  title: string;
  closeFunction: () => void;
  body?: ReactNode;
  footer?: ReactNode;
  active: boolean;
  className?: string;
  headColor?: string;
  headTextColor?: string;
  size?: ModalSize;
  initialFocusRef?: MutableRefObject<HTMLElement | null>;
  overflowVisible?: boolean;
};

const SizedModal = styled.div<{ size?: ModalSize; overflow: string }>`
  width: ${(props) => (props.size ? `${modalSizes[props.size]}%` : "640px")};
  overflow: ${(props) => props.overflow};
  @media screen and (max-width: ${devices.mobile.width}px) {
    width: ${(props) => (props.size ? `${modalSizes[props.size]}%` : "320px")};
  }
`;

export const Modal: FC<Props> = ({
  title,
  closeFunction,
  body,
  children,
  footer,
  active,
  className,
  headColor = "secondary-less",
  headTextColor = "secondary-most",
  size,
  initialFocusRef,
  overflowVisible,
}) => {
  const closeButtonRef = useRef<HTMLButtonElement | null>(null);
  let showFooter = null;

  if (footer) {
    showFooter = <footer className="modal-card-foot">{footer}</footer>;
  }

  const overflowAttribute = overflowVisible ? "visible" : "auto";
  const borderBottomRadiusAttribute = overflowVisible && !footer ? "inherit" : "unset";

  return (
    <Dialog
      open={active}
      onClose={closeFunction}
      className={classNames(
        "modal",
        { "is-active": active },
        `is-overflow-${overflowAttribute}`,
        `is-border-bottom-radius-${borderBottomRadiusAttribute}`,
        className
      )}
      initialFocus={initialFocusRef ?? closeButtonRef}
    >
      <Dialog.Overlay className="modal-background" />
      <SizedModal className="modal-card" size={size} overflow={overflowAttribute}>
        <Dialog.Title as="header" className={classNames("modal-card-head", `has-background-${headColor}`)}>
          <h2 className={`modal-card-title m-0 has-text-${headTextColor}`}>{title}</h2>
          <button
            className="delete"
            aria-label="close"
            onClick={closeFunction}
            ref={!initialFocusRef ? closeButtonRef : undefined}
          />
        </Dialog.Title>
        <section
          className={classNames(
            "modal-card-body",
            `is-overflow-${overflowAttribute}`,
            `is-border-bottom-radius-${borderBottomRadiusAttribute}`
          )}
        >
          {body || children}
        </section>
        {showFooter}
      </SizedModal>
    </Dialog>
  );
};

export default Modal;
