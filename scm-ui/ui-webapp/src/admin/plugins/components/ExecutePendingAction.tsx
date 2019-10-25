import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { PendingPlugins } from "@scm-manager/ui-types";
import { Button } from "@scm-manager/ui-components";
import ExecutePendingModal from "./ExecutePendingModal";

type Props = WithTranslation & {
  pendingPlugins: PendingPlugins;
};

type State = {
  showModal: boolean;
};

class ExecutePendingAction extends React.Component<Props, State> {
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
    const { pendingPlugins } = this.props;
    if (showModal) {
      return <ExecutePendingModal pendingPlugins={pendingPlugins} onClose={this.closeModal} />;
    }
    return null;
  };

  render() {
    const { t } = this.props;
    return (
      <>
        {this.renderModal()}
        <Button color="primary" label={t("plugins.executePending")} action={this.openModal} />
      </>
    );
  }
}

export default withTranslation("admin")(ExecutePendingAction);
