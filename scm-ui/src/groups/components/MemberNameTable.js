//@flow
import React from "react";
import { translate } from "react-i18next";
import RemoveMemberButton from "./buttons/RemoveMemberButton";

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
      <div>
        <label className="label">{t("group.members")}</label>
        <table className="table is-hoverable is-fullwidth">
          <tbody>
            {this.props.members.map((member, index) => {
              return (
                <tr key={member}>
                  <td key={member}>{member}</td>
                  <td>
                    <RemoveMemberButton
                      membername={member}
                      removeMember={this.removeMember}
                    />
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    );
  }

  removeMember = (membername: string) => {
    const newMembers = this.props.members.filter(name => name !== membername);
    this.props.memberListChanged(newMembers);
  };
}

export default translate("groups")(MemberNameTable);
