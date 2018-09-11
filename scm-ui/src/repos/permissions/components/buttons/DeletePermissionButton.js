// @flow
import React from "react";
import { translate } from "react-i18next";
import type { Permission } from "../../types/Permissions";
import {
  confirmAlert,
  DeleteButton
} from "../../../../../../scm-ui-components/packages/ui-components/src/index";

type Props = {
  permission: Permission,
  namespace: string,
  repoName: string,
  confirmDialog?: boolean,
  t: string => string,
  deletePermission: (
    permission: Permission,
    namespace: string,
    repoName: string
  ) => void,
  loading: boolean
};

class DeletePermissionButton extends React.Component<Props> {
  static defaultProps = {
    confirmDialog: true
  };

  deletePermission = () => {
    this.props.deletePermission(
      this.props.permission,
      this.props.namespace,
      this.props.repoName
    );
  };

  confirmDelete = () => {
    const { t } = this.props;
    confirmAlert({
      title: t("delete-permission-button.confirm-alert.title"),
      message: t("delete-permission-button.confirm-alert.message"),
      buttons: [
        {
          label: t("delete-permission-button.confirm-alert.submit"),
          onClick: () => this.deletePermission()
        },
        {
          label: t("delete-permission-button.confirm-alert.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  isDeletable = () => {
    return this.props.permission._links.delete;
  };

  render() {
    const { confirmDialog, loading, t } = this.props;
    const action = confirmDialog ? this.confirmDelete : this.deletePermission;

    if (!this.isDeletable()) {
      return null;
    }
    return (
      <DeleteButton
        label={t("delete-permission-button.label")}
        action={action}
        loading={loading}
      />
    );
  }
}

export default translate("permissions")(DeletePermissionButton);
