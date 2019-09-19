//@flow
import React from "react";
import { translate } from "react-i18next";
import RemoveEntryOfTableButton from "../buttons/RemoveEntryOfTableButton";

type Props = {
  members: string[],
  t: string => string,
  memberListChanged: (string[]) => void
};

type State = {};

class MemberNameTable extends React.Component<Props, State> {
  render() {
    const { t } = this.props;
    return (
      <table className="table is-hoverable is-fullwidth">
        <tbody>
          {this.props.members.map(member => {
            return (
              <tr key={member}>
                <td key={member}>{member}</td>
                <td>
                  <RemoveEntryOfTableButton
                    entryname={member}
                    removeEntry={this.removeEntry}
                    disabled={false}
                    label={t("remove-member-button.label")}
                  />
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    );
  }

  removeEntry = (membername: string) => {
    const newMembers = this.props.members.filter(name => name !== membername);
    this.props.memberListChanged(newMembers);
  };
}

export default translate("groups")(MemberNameTable);
