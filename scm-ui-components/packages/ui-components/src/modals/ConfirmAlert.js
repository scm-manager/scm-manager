// @flow
import * as React from "react";
import ReactDOM from "react-dom";
import Modal from "./Modal";

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

    const body= (
      <>
        {message}
        <div className="buttons is-right">
          {buttons.map((button, i) => (
            <a className="button is-info is-right"
               key={i}
               onClick={() => this.handleClickButton(button)}
            >
              {button.label}
            </a>
          ))}
        </div>
        </>
    );


    return (
      <Modal title={title} closeFunction={() => this.close()} body={body} active={true}/>
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
