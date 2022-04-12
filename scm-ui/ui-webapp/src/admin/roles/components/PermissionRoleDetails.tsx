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
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { RepositoryRole } from "@scm-manager/ui-types";
import { Button, Level } from "@scm-manager/ui-components";
import PermissionRoleDetailsTable from "./PermissionRoleDetailsTable";

type Props = WithTranslation & {
  role: RepositoryRole;
  url: string;
};

class PermissionRoleDetails extends React.Component<Props> {
  renderEditButton() {
    const { t, url } = this.props;
    if (!!this.props.role._links.update) {
      return (
        <>
          <hr />
          <Level right={<Button label={t("repositoryRole.editButton")} link={`${url}/edit`} color="primary" />} />
        </>
      );
    }
    return null;
  }

  render() {
    const { role } = this.props;

    return (
      <>
        <PermissionRoleDetailsTable role={role} />
        {this.renderEditButton()}
        <ExtensionPoint<extensionPoints.RepositoryRoleDetailsInformation>
          name="repositoryRole.role-details.information"
          renderAll={true}
          props={{
            role
          }}
        />
      </>
    );
  }
}

export default withTranslation("admin")(PermissionRoleDetails);
