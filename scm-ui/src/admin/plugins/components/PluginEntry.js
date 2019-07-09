//@flow
import React from "react";
import type { Plugin } from "@scm-manager/ui-types";
import { CardColumn } from "@scm-manager/ui-components";
import PluginAvatar from "./PluginAvatar";

type Props = {
  plugin: Plugin
};

class PluginEntry extends React.Component<Props> {
  createAvatar = (plugin: Plugin) => {
    return <PluginAvatar plugin={plugin} />;
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
    const footerLeft = this.createFooterLeft(plugin);
    const footerRight = this.createFooterRight(plugin);

    // TODO: Add link to plugin page below
    return (
      <CardColumn
        link="#"
        avatar={avatar}
        title={plugin.name}
        description={plugin.description}
        footerLeft={footerLeft}
        footerRight={footerRight}
      />
    );
  }
}

export default PluginEntry;
