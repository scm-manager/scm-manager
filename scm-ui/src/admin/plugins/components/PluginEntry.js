//@flow
import React from "react";
import injectSheet from "react-jss";
import type { Plugin } from "@scm-manager/ui-types";
import { CardColumn } from "@scm-manager/ui-components";
import PluginAvatar from "./PluginAvatar";
import PluginModal from "./PluginModal";

type Props = {
  plugin: Plugin,

  // context props
  classes: any
};

type State = {
  showModal: boolean
};

const styles = {
  link: {
    pointerEvents: "all"
  }
};

class PluginEntry extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      showModal: false
    };
  }

  createAvatar = (plugin: Plugin) => {
    return <PluginAvatar plugin={plugin} />;
  };

  toggleModal = () => {
    this.setState(prevState => ({
      showModal: !prevState.showModal
    }));
  };

  createContentRight = (plugin: Plugin) => {
    const { classes } = this.props;
    if (plugin._links && plugin._links.install && plugin._links.install.href) {
      return (
        <div className={classes.link} onClick={this.toggleModal}>
          <i className="fas fa-download fa-2x has-text-info" />
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
    const { showModal } = this.state;
    const avatar = this.createAvatar(plugin);
    const contentRight = this.createContentRight(plugin);
    const footerLeft = this.createFooterLeft(plugin);
    const footerRight = this.createFooterRight(plugin);

    const modal = showModal ? <PluginModal plugin={plugin} onSubmit={this.toggleModal} onClose={this.toggleModal} /> : null;

    // TODO: Add link to plugin page below
    return (
      <>
        <CardColumn
          link="#"
          avatar={avatar}
          title={plugin.displayName ? plugin.displayName : plugin.name}
          description={plugin.description}
          contentRight={contentRight}
          footerLeft={footerLeft}
          footerRight={footerRight}
        />
        {modal}
      </>
    );
  }
}

export default injectSheet(styles)(PluginEntry);
