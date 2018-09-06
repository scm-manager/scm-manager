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
      <div className="react-confirm-alert-overlay">
        <div className="react-confirm-alert">
          {
            <div className="react-confirm-alert-body">
              {title && <h1>{title}</h1>}
              {message}
              <div className="react-confirm-alert-button-group">
                {buttons.map((button, i) => (
                  <button
                    key={i}
                    onClick={() => this.handleClickButton(button)}
                  >
                    {button.label}
                  </button>
                ))}
              </div>
            </div>
          }
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
