/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

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

/**
 * @deprecated
 */
class MemberNameTagGroup extends React.Component<Props> {
  render() {
    const { members, label, helpText, t } = this.props;
    const membersExtended = members.map((id) => {
      return {
        id,
        displayName: id,
        mail: "",
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
    const members = membersExtended.map(function (item) {
      return item["id"];
    });
    this.props.memberListChanged(members);
  };
}

/**
 * @deprecated
 */
export default withTranslation("groups")(MemberNameTagGroup);
