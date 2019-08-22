// @flow
import React from "react";
import { Button } from "@scm-manager/ui-components";
import type { PluginCollection } from "@scm-manager/ui-types";
import { translate } from "react-i18next";
import InstallPendingModal from "./InstallPendingModal";

type Props = {
  collection: PluginCollection,

  // context props
  t: string => string
};

type State = {
  showModal: boolean
};

class InstallPendingAction extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      showModal: false
    };
  }

  openModal = () => {
    this.setState({
      showModal: true
    });
  };

  closeModal = () => {
    this.setState({
      showModal: false
    });
  };

  renderModal = () => {
    const { showModal } = this.state;
    const { collection } = this.props;
    if (showModal) {
      return (
        <InstallPendingModal
          collection={collection}
          onClose={this.closeModal}
        />
      );
    }
    return null;
  };

  render() {
    const { t } = this.props;
    return (
      <>
        {this.renderModal()}
        <Button
          color="primary"
          label={t("plugins.installPending")}
          action={this.openModal}
        />
      </>
    );
  }
}

export default translate("admin")(InstallPendingAction);
