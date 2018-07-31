//@flow
import React from "react";
import type { Group } from "../../types/Group";
import { translate } from "react-i18next";
import { Checkbox } from "../../../components/forms";

type Props = {
  group: Group,
  t: string => string
};

class Details extends React.Component<Props> {
  render() {
    const { group, t } = this.props;
    return (
      <table className="table">
        <tbody>
          <tr>
            <td>{t("group.name")}</td>
            <td>{group.name}</td>
          </tr>
          <tr>
            <td>{t("group.description")}</td>
            <td>{group.description}</td>
          </tr>
        </tbody>
      </table>
    );
  }
}

export default translate("groups")(Details);
