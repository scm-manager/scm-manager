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
