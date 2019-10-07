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
    justifyContent: "space-around",
    alignSelf: "stretch"
  },
  footer: {
    display: "flex",
    paddingBottom: "1rem",
  },
  topPart: {
    display: "flex"
  },
  contentRight: {
    marginLeft: "auto"
  },
  contentLeft: {
    marginBottom: "0 !important",
    overflow: "hidden"
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
  className?: string,

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
      classes,
      className
    } = this.props;
    const link = this.createLink();
    return (
      <>
        {link}
        <article className={classNames("media", className, classes.inner)}>
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
            <div className={classes.topPart}>
              <div
                className={classNames(
                  "content",
                  classes.contentLeft
                )}
              >
                <p className="shorten-text is-marginless">
                  <strong>{title}</strong>
                </p>
                <p className="shorten-text">{description}</p>
              </div>
              <div className={classes.contentRight}>
              {contentRight && contentRight}
              </div>
            </div>
              <div className={classNames("level", classes.footer)}>
                <div className="level-left is-hidden-mobile">{footerLeft}</div>
                <div className="level-right is-mobile is-marginless">{footerRight}</div>
              </div>
          </div>
        </article>
      </>
    );
  }
}

export default injectSheet(styles)(CardColumn);
