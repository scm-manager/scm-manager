//@flow
import React from "react";
import type { Tag } from "@scm-manager/ui-types";
import ChangesetTagBase from "./ChangesetTagBase";
import { translate } from "react-i18next";
import Tooltip from "../../Tooltip";

type Props = {
  tags: Tag[],

  // context props
  t: (string) => string
};

class ChangesetTagsCollapsed extends React.Component<Props> {
  render() {
    const { tags, t } = this.props;
    const message = tags.map((tag) => tag.name).join(", ");
    return (
      <Tooltip location="top" message={message}>
        <ChangesetTagBase icon={"fa-tags"} label={ tags.length + " " + t("changeset.tags") } />
      </Tooltip>
    );
  }
}

export default translate("repos")(ChangesetTagsCollapsed);
