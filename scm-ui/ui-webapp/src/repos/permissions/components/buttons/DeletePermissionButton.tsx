/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Permission } from "@scm-manager/ui-types";
import { confirmAlert } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  permission: Permission;
  namespace: string;
  repoName: string;
  confirmDialog?: boolean;
  deletePermission: (permission: Permission, namespace: string, repoName: string) => void;
  loading: boolean;
};

class DeletePermissionButton extends React.Component<Props> {
  static defaultProps = {
    confirmDialog: true
  };

  deletePermission = () => {
    this.props.deletePermission(this.props.permission, this.props.namespace, this.props.repoName);
  };

  confirmDelete = () => {
    const { t } = this.props;
    confirmAlert({
      title: t("permission.delete-permission-button.confirm-alert.title"),
      message: t("permission.delete-permission-button.confirm-alert.message"),
      buttons: [
        {
          className: "is-outlined",
          label: t("permission.delete-permission-button.confirm-alert.submit"),
          onClick: () => this.deletePermission()
        },
        {
          label: t("permission.delete-permission-button.confirm-alert.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  isDeletable = () => {
    return this.props.permission._links.delete;
  };

  render() {
    const { confirmDialog } = this.props;
    const action = confirmDialog ? this.confirmDelete : this.deletePermission;

    if (!this.isDeletable()) {
      return null;
    }
    return (
      <a className="level-item" onClick={action}>
        <span className="icon is-small">
          <i className="fas fa-trash" />
        </span>
      </a>
    );
  }
}

export default withTranslation("repos")(DeletePermissionButton);
