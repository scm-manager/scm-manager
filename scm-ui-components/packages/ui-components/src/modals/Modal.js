// @flow
import * as React from "react";
import classNames from "classnames";

type Props = {
  title: string,
  closeFunction: () => void,
  body: any,
  footer?: any,
  active: boolean,
};



class Modal extends React.Component<Props> {
  render() {
    const { title, closeFunction, body, footer, active } = this.props;

    const isActive = active ? "is-active" : null;

    let showFooter = null;
    if (footer) {
      showFooter = <footer className="modal-card-foot">{footer}</footer>;
    }

    return (
      <div className={classNames("modal", isActive)}>
        <div className="modal-background" />
        <div className="modal-card">
          <header className="modal-card-head">
            <p className="modal-card-title">{title}</p>
            <button
              className="delete"
              aria-label="close"
              onClick={closeFunction}
            />
          </header>
          <section className="modal-card-body">{body}</section>
          {showFooter}
        </div>
      </div>
    );
  }
}

export default Modal;
