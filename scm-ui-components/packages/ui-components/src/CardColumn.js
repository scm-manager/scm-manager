//@flow
import * as React from "react";
import injectSheet from "react-jss";
import classNames from "classnames";

import { Link } from "react-router-dom";

const styles = {
  inner: {
    position: "relative",
    pointerEvents: "none",
    zIndex: 1
  },
  innerLink: {
    pointerEvents: "all"
  },
  centerImage: {
    marginTop: "0.8em",
    marginLeft: "1em !important"
  },
  flexFullHeight: {
    display: "flex",
    flexDirection: "column",
    alignSelf: "stretch"
  },
  content: {
    display: "flex",
    flexGrow: 1,
    alignItems: "center",
    justifyContent: "space-between"
  },
  footer: {
    display: "flex",
    marginTop: "auto",
    paddingBottom: "1.5rem"
  },
  noBottomMargin: {
    marginBottom: "0 !important"
  }
};

type Props = {
  title: string,
  description: string,
  avatar: React.Node,
  contentRight?: React.Node,
  footerLeft: React.Node,
  footerRight: React.Node,
  link?: string,
  action?: () => void,

  // context props
  classes: any
};

class CardColumn extends React.Component<Props> {
  createLink = () => {
    const { link, action } = this.props;
    if (link) {
      return <Link className="overlay-column" to={link} />;
    } else if (action) {
      return <a className="overlay-column" onClick={e => {e.preventDefault(); action();}} href="#" />;
    }
    return null;
  };

  render() {
    const {
      avatar,
      title,
      description,
      contentRight,
      footerLeft,
      footerRight,
      classes
    } = this.props;
    const link = this.createLink();
    return (
      <>
        {link}
        <article className={classNames("media", classes.inner)}>
          <figure className={classNames(classes.centerImage, "media-left")}>
            {avatar}
          </figure>
          <div
            className={classNames(
              "media-content",
              "text-box",
              classes.flexFullHeight
            )}
          >
            <div className={classes.content}>
              <div
                className={classNames(
                  "content",
                  "shorten-text",
                  classes.noBottomMargin
                )}
              >
                <p className="is-marginless">
                  <strong>{title}</strong>
                </p>
                <p className="shorten-text">{description}</p>
              </div>
              {contentRight && contentRight}
            </div>
            <div className={classNames("level", classes.footer)}>
              <div className="level-left is-hidden-mobile">{footerLeft}</div>
              <div className="level-right is-mobile">{footerRight}</div>
            </div>
          </div>
        </article>
      </>
    );
  }
}

export default injectSheet(styles)(CardColumn);
