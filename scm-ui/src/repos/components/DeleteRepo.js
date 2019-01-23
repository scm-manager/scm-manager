//@flow
import React from "react";
import { translate } from "react-i18next";
import type { Repository } from "@scm-manager/ui-types";
import { Subtitle, DeleteButton, confirmAlert } from "@scm-manager/ui-components";

type Props = {
  repository: Repository,
  confirmDialog?: boolean,

  // dispatcher functions
  delete: Repository => void,

  // context props
  t: string => string
};

class DeleteRepo extends React.Component<Props> {
  static defaultProps = {
    confirmDialog: true
  };

  deleteRepo = () => {
    this.props.delete(this.props.repository);
  };

  confirmDelete = () => {
    const { t } = this.props;
    confirmAlert({
      title: t("deleteRepo.confirmAlert.title"),
      message: t("deleteRepo.confirmAlert.message"),
      buttons: [
        {
          label: t("deleteRepo.confirmAlert.submit"),
          onClick: () => this.deleteRepo()
        },
        {
          label: t("deleteRepo.confirmAlert.cancel"),
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
    const action = confirmDialog ? this.confirmDelete : this.deleteRepo;

    if (!this.isDeletable()) {
      return null;
    }

    return (
      <>
        <Subtitle subtitle={t("deleteRepo.subtitle")} />
        <div className="columns">
          <div className="column">
            <DeleteButton
              label={t("deleteRepo.button")}
              action={action}
            />
          </div>
        </div>
      </>
    );
  }
}

export default translate("repos")(DeleteRepo);
