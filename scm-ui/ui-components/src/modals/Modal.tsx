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
  overflowVisible?: boolean;
};

const SizedModal = styled.div<{ size?: ModalSize; overflow: string }>`
  width: ${props => (props.size ? `${modalSizes[props.size]}%` : "640px")};
  overflow: ${props => props.overflow};
`;

const DivWithOptionalOverflow = styled.div<{ overflow: string; borderBottomRadius: string }>`
  overflow: ${props => props.overflow};
  border-bottom-left-radius: ${props => props.borderBottomRadius};
  border-bottom-right-radius: ${props => props.borderBottomRadius};
`;

const SectionWithOptionalOverflow = styled.section<{ overflow: string; borderBottomRadius: string }>`
  overflow: ${props => props.overflow};
  border-bottom-left-radius: ${props => props.borderBottomRadius};
  border-bottom-right-radius: ${props => props.borderBottomRadius};
`;

export const Modal: FC<Props> = ({
  title,
  closeFunction,
  body,
  footer,
  active,
  className,
  headColor = "secondary-less",
  headTextColor = "secondary-most",
  size,
  overflowVisible
}) => {
  const portalRootElement = usePortalRootElement("modalsRoot");
  const initialFocusRef = useRef(null);
  const trapRef = useTrapFocus({
    includeContainer: true,
    initialFocus: initialFocusRef.current,
    returnFocus: true,
    updateNodes: false
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

  const overflowAttribute = overflowVisible ? "visible" : "auto";
  const borderBottomRadiusAttribute = overflowVisible && !footer ? "inherit" : "unset";

  const modalElement = (
    <DivWithOptionalOverflow
      className={classNames("modal", className, isActive)}
      ref={trapRef}
      onKeyDown={onKeyDown}
      overflow={overflowAttribute}
      borderBottomRadius={borderBottomRadiusAttribute}
    >
      <div className="modal-background" onClick={closeFunction} />
      <SizedModal className="modal-card" size={size} overflow={overflowAttribute}>
        <header className={classNames("modal-card-head", `has-background-${headColor}`)}>
          <h2 className={`modal-card-title m-0 has-text-${headTextColor}`}>{title}</h2>
          <button className="delete" aria-label="close" onClick={closeFunction} ref={initialFocusRef} autoFocus />
        </header>
        <SectionWithOptionalOverflow className="modal-card-body" overflow={overflowAttribute} borderBottomRadius={borderBottomRadiusAttribute}>
          {body}
        </SectionWithOptionalOverflow>
        {showFooter}
      </SizedModal>
    </DivWithOptionalOverflow>
  );

  return ReactDOM.createPortal(modalElement, portalRootElement);
};

export default Modal;
