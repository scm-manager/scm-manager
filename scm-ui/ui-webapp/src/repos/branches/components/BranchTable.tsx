import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import BranchRow from "./BranchRow";
import { Branch } from "@scm-manager/ui-types";

type Props = WithTranslation & {
  baseUrl: string;
  branches: Branch[];
};

class BranchTable extends React.Component<Props> {
  render() {
    const { t } = this.props;
    return (
      <table className="card-table table is-hoverable is-fullwidth">
        <thead>
          <tr>
            <th>{t("branches.table.branches")}</th>
          </tr>
        </thead>
        <tbody>{this.renderRow()}</tbody>
      </table>
    );
  }

  renderRow() {
    const { baseUrl, branches } = this.props;
    let rowContent = null;
    if (branches) {
      rowContent = branches.map((branch, index) => {
        return <BranchRow key={index} baseUrl={baseUrl} branch={branch} />;
      });
    }
    return rowContent;
  }
}

export default withTranslation("repos")(BranchTable);
