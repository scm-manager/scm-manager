//@flow
import React from "react";
import { translate } from "react-i18next";
import { Subtitle, DeleteButton, confirmAlert } from "@scm-manager/ui-components";
import type { Repository } from "@scm-manager/ui-types";

type Props = {
  repository: Repository,
  confirmDialog?: boolean,

  // context props
  t: string => string
};

class DeleteRepo extends React.Component<Props> {
  static defaultProps = {
    confirmDialog: true
  };

  delete = () => {
    //this.props.delete(this.props.repository);
  };

  confirmDelete = () => {
    const { t } = this.props;
    confirmAlert({
      title: t("delete.confirmAlert.title"),
      message: t("delete.confirmAlert.message"),
      buttons: [
        {
          label: t("delete.confirmAlert.submit"),
          onClick: () => this.delete()
        },
        {
          label: t("delete.confirmAlert.cancel"),
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
        <Subtitle subtitle={t("delete.subtitle")} />
        <div className="columns">
          <div className="column">
            <DeleteButton
              label={t("delete.button")}
              action={action}
            />
          </div>
        </div>
      </>
    );
  }
}

export default translate("repos")(DeleteRepo);
