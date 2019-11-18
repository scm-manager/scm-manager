import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { DisplayedUser } from "@scm-manager/ui-types";
import TagGroup from "./TagGroup";

type Props = WithTranslation & {
  members: string[];
  memberListChanged: (p: string[]) => void;
  label?: string;
  helpText?: string;
};

class MemberNameTagGroup extends React.Component<Props> {
  render() {
    const { members, label, helpText, t } = this.props;
    const membersExtended = members.map(id => {
      return {
        id,
        displayName: id,
        mail: ""
      };
    });
    return (
      <TagGroup
        items={membersExtended}
        label={label ? label : t("group.members")}
        helpText={helpText ? helpText : t("groupForm.help.memberHelpText")}
        onRemove={this.removeEntry}
      />
    );
  }

  removeEntry = (membersExtended: DisplayedUser[]) => {
    const members = membersExtended.map(function(item) {
      return item["id"];
    });
    this.props.memberListChanged(members);
  };
}

export default withTranslation("groups")(MemberNameTagGroup);
