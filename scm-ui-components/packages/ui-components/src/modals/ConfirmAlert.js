// @flow
//modified from https://github.com/GA-MO/react-confirm-alert

import * as React from "react";
import { render, unmountComponentAtNode } from "react-dom";
import "./ConfirmAlert.css";

type Button = {
  label: string,
  onClick: () => void | null
};

type Props = {
  title: string,
  message: string,
  buttons: Button[]
};

class ConfirmAlert extends React.Component<Props> {
  handleClickButton = (button: Button) => {
    if (button.onClick) {
      button.onClick();
    }
    this.close();
  };

  close = () => {
    removeElementReconfirm();
  };

  render() {
    const { title, message, buttons } = this.props;

    return (
      <div className="modal is-active">
        <div className="modal-background" />
        <div className="modal-card">

            <header className="modal-card-head">
              <p className="modal-card-title">
                {title}
              </p>
              <button
                className="delete"
                aria-label="close"
                onClick={() => this.close()}
              />
            </header>
            <section className="modal-card-body">
              {message}
              <div className="react-confirm-alert-button-group">
                {buttons.map((button, i) => (
                  <button
                    key={i}
                    onClick={() => this.handleClickButton(button)}
                    href="javascript:void(0);"
                  >
                    {button.label}
                  </button>
                ))}
              </div>
            </section>

        </div>
      </div>
    );
  }
}

function createElementReconfirm(properties: Props) {
  const divTarget = document.createElement("div");
  divTarget.id = "react-confirm-alert";
  if (document.body) {
    document.body.appendChild(divTarget);
    render(<ConfirmAlert {...properties} />, divTarget);
  }
}

function removeElementReconfirm() {
  const target = document.getElementById("react-confirm-alert");
  if (target) {
    unmountComponentAtNode(target);
    if (target.parentNode) {
      target.parentNode.removeChild(target);
    }
  }
}

export function confirmAlert(properties: Props) {
  createElementReconfirm(properties);
}

export default ConfirmAlert;
