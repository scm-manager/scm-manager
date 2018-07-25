//modified from https://github.com/GA-MO/react-confirm-alert

import React from "react";
import { render, unmountComponentAtNode } from "react-dom";

type Props = {
  title:string,
  message: string,
  buttons: array,
}

class ConfirmAlert extends React.Component<Props> {
 
  handleClickButton = button => {
    if (button.onClick) button.onClick()
    this.close()
  }

  close = () => {
    removeElementReconfirm()
  }

  render () {
    const { title, message, buttons } = this.props;

    return (
      <div className="react-confirm-alert-overlay">
        <div className="react-confirm-alert">
          {<div className="react-confirm-alert-body">
              {title && <h1>{title}</h1>}
              {message}
              <div className="react-confirm-alert-button-group">
                {buttons.map((button, i) => (
                  <button key={i} onClick={() => this.handleClickButton(button)}>
                    {button.label}
                  </button>
                ))}
              </div>
            </div>}
        </div>
      </div>
    )
  }
}

function createElementReconfirm (properties) {
  const divTarget = document.createElement('div')
  divTarget.id = 'react-confirm-alert'
  document.body.appendChild(divTarget)
  render(<ConfirmAlert {...properties} />, divTarget)
}

function removeElementReconfirm () {
  const target = document.getElementById('react-confirm-alert')
  unmountComponentAtNode(target)
  target.parentNode.removeChild(target)
}

export function confirmAlert (properties) {
  createElementReconfirm(properties)
}

export default ConfirmAlert;
