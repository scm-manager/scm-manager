// @flow
import * as React from "react";
import ReactDOM from "react-dom";

type ButtonType = {
  label: string,
  onClick: () => void | null
};

type Props = {
  title: string,
  message: string,
  buttons: ButtonType[]
};

class ConfirmAlert extends React.Component<Props> {
  handleClickButton = (button: ButtonType) => {
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
              <div className="buttons">
                {buttons.map((button, i) => (
                  <a className="button"
                    key={i}
                    onClick={() => this.handleClickButton(button)}
                  >
                    {button.label}
                  </a>
                ))}
              </div>
            </section>

        </div>
      </div>
    );
  }
}

export function confirmAlert(properties: Props) {
  const root = document.getElementById("modalRoot");
  if(root){
    ReactDOM.render(<ConfirmAlert {...properties}/>, root);
  }
}

export default ConfirmAlert;
