// @flow
import React from "react";
import { translate } from "react-i18next";
import type { Group } from "@scm-manager/ui-types";
import { Subtitle, DeleteButton, confirmAlert } from "@scm-manager/ui-components";

type Props = {
  group: Group,
  confirmDialog?: boolean,
  deleteGroup: (group: Group) => void,
  t: string => string
};

export class DeleteGroup extends React.Component<Props> {
  static defaultProps = {
    confirmDialog: true
  };

  deleteGroup = () => {
    this.props.deleteGroup(this.props.group);
  };

  confirmDelete = () => {
    const { t } = this.props;
    confirmAlert({
      title: t("deleteGroup.confirmAlert.title"),
      message: t("deleteGroup.confirmAlert.message"),
      buttons: [
        {
          label: t("deleteGroup.confirmAlert.submit"),
          onClick: () => this.deleteGroup()
        },
        {
          label: t("deleteGroup.confirmAlert.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  isDeletable = () => {
    return this.props.group._links.delete;
  };

  render() {
    const { confirmDialog, t } = this.props;
    const action = confirmDialog ? this.confirmDelete : this.deleteGroup;

    if (!this.isDeletable()) {
      return null;
    }

    return (
      <>
        <Subtitle subtitle={t("deleteGroup.subtitle")} />
        <div className="columns">
          <div className="column">
            <DeleteButton
              label={t("deleteGroup.button")}
              action={action}
            />
          </div>
        </div>
      </>
    );
  }
}

export default translate("groups")(DeleteGroup);
