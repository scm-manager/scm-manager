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

import React, { FC, MutableRefObject, useRef } from "react";
import classNames from "classnames";
import styled from "styled-components";
import { Dialog } from "@headlessui/react";
import { devices } from "../devices";
import useRegisterModal from "./useRegisterModal";

type ModalSize = "S" | "M" | "L";

const modalSizes: { [key in ModalSize]: number } = { S: 33, M: 50, L: 66 };

type Props = {
  title: string;
  closeFunction: () => void;
  body?: any;
  footer?: any;
  active: boolean;
  className?: string;
  headColor?: string;
  headTextColor?: string;
  size?: ModalSize;
  initialFocusRef?: MutableRefObject<HTMLElement | null>;
  overflowVisible?: boolean;
};

const SizedModal = styled.div<{ size?: ModalSize; overflow: string }>`
  width: ${props => (props.size ? `${modalSizes[props.size]}%` : "640px")};
  overflow: ${props => props.overflow};
  @media screen and (max-width: ${devices.mobile.width}px) {
    width: ${props => (props.size ? `${modalSizes[props.size]}%` : "320px")};
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
  overflowVisible
}) => {
  useRegisterModal(active);
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
