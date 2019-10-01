// @flow
import * as React from "react";
import classNames from "classnames";

type Props = {
  title: string,
  closeFunction: () => void,
  body: any,
  footer?: any,
  active: boolean,
  className?: string,
  headColor: string
};

class Modal extends React.Component<Props> {
  static defaultProps = {
    headColor: "light"
  };

  render() {
    const {
      title,
      closeFunction,
      body,
      footer,
      active,
      className,
      headColor
    } = this.props;

    const isActive = active ? "is-active" : null;

    let showFooter = null;
    if (footer) {
      showFooter = <footer className="modal-card-foot">{footer}</footer>;
    }

    return (
      <div className={classNames("modal", className, isActive)}>
        <div className="modal-background" />
        <div className="modal-card">
          <header
            className={classNames(
              "modal-card-head",
              `has-background-${headColor}`
            )}
          >
            <p className="modal-card-title is-marginless">{title}</p>
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
