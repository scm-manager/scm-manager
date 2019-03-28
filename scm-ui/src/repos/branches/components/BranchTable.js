// @flow
import React from "react";
import { translate } from "react-i18next";
import type { Branch } from "@scm-manager/ui-types";
import { Link } from "react-router-dom";

type Props = {
  baseUrl: string,
  t: string => string,
  branches: Branch[]
};

class UserTable extends React.Component<Props> {
  render() {
    const { baseUrl, branches, t } = this.props;
    return (
      <table className="card-table table is-hoverable is-fullwidth">
        <thead>
        <tr>
          <th>{t("branches.table.branches")}</th>
        </tr>
        </thead>
        <tbody>
          {branches.map((branch, index) => {
            const to = `${baseUrl}/${encodeURIComponent(branch.name)}/info`;
            return (
              <tr>
                <td key={index}>
                  <Link to={to}>{branch.name}</Link>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    );
  }
}

export default translate("users")(UserTable);
