// @flow
import * as React from "react";
import classNames from "classnames";

type Props = {
  title: string,
  closeButton: any,
  body: any,
  active: boolean
};

class Modal extends React.Component<Props> {

  render() {
    const { title, closeButton, body, active } = this.props;

    const isActive = active ? "is-active" : null;

    return (
      <div className={classNames(
        "modal",
        isActive
      )}>
        <div className="modal-background" />
        <div className="modal-card">

          <header className="modal-card-head">
            <p className="modal-card-title">
              {title}
            </p>
            {closeButton}
          </header>
          <section className="modal-card-body">
            {body}
          </section>

        </div>
      </div>
    );
  }
}


export default Modal;
