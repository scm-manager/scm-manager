//@flow
import React from "react";
import { translate } from "react-i18next";
import { confirmAlert } from "../../components/modals/ConfirmAlert";
import { NavAction } from "../../components/navigation";
import type { Repository } from "../types/Repositories";

type Props = {
  repository: Repository,
  confirmDialog?: boolean,
  delete: Repository => void,

  // context props
  t: string => string
};

class DeleteNavAction extends React.Component<Props> {
  static defaultProps = {
    confirmDialog: true
  };

  delete = () => {
    this.props.delete(this.props.repository);
  };

  confirmDelete = () => {
    const { t } = this.props;
    confirmAlert({
      title: t("delete-nav-action.confirm-alert.title"),
      message: t("delete-nav-action.confirm-alert.message"),
      buttons: [
        {
          label: t("delete-nav-action.confirm-alert.submit"),
          onClick: () => this.delete()
        },
        {
          label: t("delete-nav-action.confirm-alert.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  isDeletable = () => {
    return this.props.repository._links.delete;
  };

  render() {
    const { confirmDialog, t } = this.props;
    const action = confirmDialog ? this.confirmDelete : this.delete();

    if (!this.isDeletable()) {
      return null;
    }
    return <NavAction label={t("delete-nav-action.label")} action={action} />;
  }
}

export default translate("repos")(DeleteNavAction);
