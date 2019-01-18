//@flow
import React from "react";
import { translate } from "react-i18next";
import { Subtitle, DeleteButton, confirmAlert } from "@scm-manager/ui-components";
import type { Repository } from "@scm-manager/ui-types";

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
      title: t("repository.delete.confirm-alert.title"),
      message: t("repository.delete.confirm-alert.message"),
      buttons: [
        {
          label: t("repository.delete.confirm-alert.submit"),
          onClick: () => this.delete()
        },
        {
          label: t("repository.delete.confirm-alert.cancel"),
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

    return (
      <>
        <Subtitle subtitle={t("repository.delete.subtitle")} />
        <div className="columns">
          <div className="column">
            <DeleteButton
              label={t("repository.delete.button")}
              action={action}
            />
          </div>
        </div>
      </>
    );
  }
}

export default translate("repos")(DeleteNavAction);
