// @flow
import * as React from "react";
import classNames from "classnames";
import injectSheet from "react-jss";

type Props = {
  title: string,
  closeFunction: () => void,
  body: any,
  footer?: any,
  active: boolean,
  classes: any
};

const styles = {
  resize: {
    maxWidth: "100%",
    width: "auto !important",
    display: "inline-block"
  }
};

class Modal extends React.Component<Props> {
  render() {
    const { title, closeFunction, body, footer, active, classes } = this.props;

    const isActive = active ? "is-active" : null;

    let showFooter = null;
    if (footer) {
      showFooter = <footer className="modal-card-foot">{footer}</footer>;
    }

    return (
      <div className={classNames("modal", isActive)}>
        <div className="modal-background" />
        <div className={classNames("modal-card", classes.resize)}>
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

export default injectSheet(styles)(Modal);
