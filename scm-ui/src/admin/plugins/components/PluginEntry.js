//@flow
import React from "react";
import { Link } from "react-router-dom";
import injectSheet from "react-jss";
import classNames from "classnames";
import type { Plugin } from "@scm-manager/ui-types";
import PluginAvatar from "./PluginAvatar";

const styles = {
  inner: {
    position: "relative",
    pointerEvents: "none",
    zIndex: 1
  },
  centerImage: {
    marginTop: "0.8em",
    marginLeft: "1em !important"
  },
  marginBottom: {
    marginBottom: "0.75rem !important"
  }
};

type Props = {
  plugin: Plugin,
  fullColumnWidth?: boolean,

  // context props
  classes: any
};

class PluginEntry extends React.Component<Props> {
  render() {
    const { plugin, classes, fullColumnWidth } = this.props;
    const halfColumn = fullColumnWidth ? "is-full" : "is-half";
    const overlayLinkClass = fullColumnWidth
      ? "overlay-full-column"
      : "overlay-half-column";
    // TODO: Add link to plugin page below
    return (
      <div
        className={classNames(
          "box",
          "box-link-shadow",
          "column",
          "is-clipped",
          halfColumn
        )}
      >
        <Link
          className={classNames(overlayLinkClass, "is-plugin-page")}
          to="#"
        />
        <article className={classNames("media", classes.inner)}>
          <figure className={classNames(classes.centerImage, "media-left")}>
            <PluginAvatar plugin={plugin} />
          </figure>
          <div className={classNames("media-content", "text-box")}>
            <div className="content">
              <nav
                className={classNames(
                  "level",
                  "is-mobile",
                  classes.marginBottom
                )}
              >
                <div className="level-left">
                  <strong>{plugin.name}</strong>
                </div>
                <div className="level-right is-hidden-mobile">
                  {plugin.version}
                </div>
              </nav>
              <p className="shorten-text is-marginless">{plugin.description}</p>
              <p>
                <small>{plugin.author}</small>
              </p>
            </div>
          </div>
        </article>
      </div>
    );
  }
}

export default injectSheet(styles)(PluginEntry);
