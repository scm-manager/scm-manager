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
import { FC, useRef, useState } from "react";
import ReactDOM from "react-dom";
import Modal from "./Modal";
import classNames from "classnames";

type Button = {
  className?: string;
  label: string;
  isLoading?: boolean;
  onClick?: () => void | null;
  autofocus?: boolean;
};

type Props = {
  title: string;
  message: string;
  buttons: Button[];
  close?: () => void;
};

export const ConfirmAlert: FC<Props> = ({ title, message, buttons, close }) => {
  const [showModal, setShowModal] = useState(true);
  const initialFocusButton = useRef<HTMLButtonElement>(null);

  const onClose = () => {
    if (typeof close === "function") {
      close();
    } else {
      setShowModal(false);
    }
  };

  const handleClickButton = (button: Button) => {
    if (button.onClick) {
      button.onClick();
    }
    onClose();
  };

  const body = <>{message}</>;

  const footer = (
    <div className="field is-grouped">
      {buttons.map((button, index) => (
        <p className="control" key={index}>
          <button
            className={classNames("button", button.className, button.isLoading ? "is-loading" : "")}
            key={index}
            onClick={() => handleClickButton(button)}
            onKeyDown={e => e.key === "Enter" && handleClickButton(button)}
            tabIndex={0}
            ref={button.autofocus ? initialFocusButton : undefined}
          >
            {button.label}
          </button>
        </p>
      ))}
    </div>
  );

  return (
    <Modal
      title={title}
      closeFunction={onClose}
      body={body}
      active={showModal}
      footer={footer}
      initialFocusRef={initialFocusButton}
    />
  );
};

/**
 * @deprecated Please use {@link ConfirmAlert} directly.
 */
export function confirmAlert(properties: Props) {
  const root = document.getElementById("modalRoot");
  if (root) {
    const close = () => {
      const container = document.getElementById("modalRoot");
      if (container) {
        ReactDOM.unmountComponentAtNode(container);
      }
    };
    const props = { ...properties, close };
    ReactDOM.render(<ConfirmAlert {...props} />, root);
  }
}

export default ConfirmAlert;
