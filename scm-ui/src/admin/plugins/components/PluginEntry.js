//@flow
import React from "react";
import injectSheet from "react-jss";
import type { Plugin } from "@scm-manager/ui-types";
import { CardColumn } from "@scm-manager/ui-components";
import PluginAvatar from "./PluginAvatar";

type Props = {
  plugin: Plugin,

  // context props
  classes: any
};

const styles = {
  link: {
    pointerEvents: "all"
  }
};

class PluginEntry extends React.Component<Props> {
  createAvatar = (plugin: Plugin) => {
    return <PluginAvatar plugin={plugin} />;
  };

  createContentRight = (plugin: Plugin) => {
    const { classes } = this.props;
    if (plugin._links && plugin._links.install && plugin._links.install.href) {
      return (
        <div className={classes.link} onClick={() => console.log(plugin._links.install.href) /*TODO trigger plugin installation*/}>
          <i className="fas fa-cloud-download-alt fa-2x has-text-info" />
        </div>
      );
    }
  };

  createFooterLeft = (plugin: Plugin) => {
    return <small className="level-item">{plugin.author}</small>;
  };

  createFooterRight = (plugin: Plugin) => {
    return <p className="level-item">{plugin.version}</p>;
  };

  render() {
    const { plugin } = this.props;
    const avatar = this.createAvatar(plugin);
    const contentRight = this.createContentRight(plugin);
    const footerLeft = this.createFooterLeft(plugin);
    const footerRight = this.createFooterRight(plugin);

    // TODO: Add link to plugin page below
    return (
      <CardColumn
        link="#"
        avatar={avatar}
        title={plugin.name}
        description={plugin.description}
        contentRight={contentRight}
        footerLeft={footerLeft}
        footerRight={footerRight}
      />
    );
  }
}

export default injectSheet(styles)(PluginEntry);
