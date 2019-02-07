// @flow
import * as React from "react";
import ReactDOM from "react-dom";
import Modal from "./Modal";

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
    ReactDOM.unmountComponentAtNode(document.getElementById("modalRoot"));
  };

  render() {
    const { title, message, buttons } = this.props;

    const body = <>{message}</>;

    const footer = (
      <div className="field is-grouped">
        {buttons.map((button, i) => (
          <p className="control">
            <a
              className="button is-info"
              key={i}
              onClick={() => this.handleClickButton(button)}
            >
              {button.label}
            </a>
          </p>
        ))}
      </div>
    );

    return (
      <Modal
        title={title}
        closeFunction={() => this.close()}
        body={body}
        active={true}
        footer={footer}
      />
    );
  }
}

export function confirmAlert(properties: Props) {
  const root = document.getElementById("modalRoot");
  if (root) {
    ReactDOM.render(<ConfirmAlert {...properties} />, root);
  }
}

export default ConfirmAlert;
