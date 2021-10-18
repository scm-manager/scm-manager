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
import React, { FC, KeyboardEvent, useRef } from "react";
import classNames from "classnames";
import usePortalRootElement from "../usePortalRootElement";
import ReactDOM from "react-dom";
import styled from "styled-components";
import { useTrapFocus } from "../useTrapFocus";

type ModalSize = "S" | "M" | "L";

const modalSizes: { [key in ModalSize]: number } = { S: 33, M: 50, L: 66 };

type Props = {
  title: string;
  closeFunction: () => void;
  body: any;
  footer?: any;
  active: boolean;
  className?: string;
  headColor?: string;
  headTextColor?: string;
  size?: ModalSize;
};

const SizedModal = styled.div<{ size?: ModalSize }>`
  width: ${(props) => (props.size ? `${modalSizes[props.size]}%` : "640px")};
`;

export const Modal: FC<Props> = ({
  title,
  closeFunction,
  body,
  footer,
  active,
  className,
  headColor = "light",
  headTextColor = "black",
  size,
}) => {
  const portalRootElement = usePortalRootElement("modalsRoot");
  const initialFocusRef = useRef(null);
  const trapRef = useTrapFocus({
    includeContainer: true,
    initialFocus: initialFocusRef.current,
    returnFocus: true,
    updateNodes: false,
  });

  if (!portalRootElement) {
    return null;
  }

  const isActive = active ? "is-active" : null;

  let showFooter = null;
  if (footer) {
    showFooter = <footer className="modal-card-foot">{footer}</footer>;
  }

  const onKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
    if (closeFunction && "Escape" === event.key) {
      closeFunction();
    }
  };

  const modalElement = (
    <div className={classNames("modal", className, isActive)} ref={trapRef} onKeyDown={onKeyDown}>
      <div className="modal-background" onClick={closeFunction} />
      <SizedModal className="modal-card" size={size}>
        <header className={classNames("modal-card-head", `has-background-${headColor}`)}>
          <p className={`modal-card-title m-0 has-text-${headTextColor}`}>{title}</p>
          <button
            className="delete"
            aria-label="close"
            onClick={closeFunction}
            ref={initialFocusRef}
            autoFocus={true}
          />
        </header>
        <section className="modal-card-body">{body}</section>
        {showFooter}
      </SizedModal>
    </div>
  );

  return ReactDOM.createPortal(modalElement, portalRootElement);
};

export default Modal;
