import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Tag } from "@scm-manager/ui-types";
import Tooltip from "../../Tooltip";
import ChangesetTagBase from "./ChangesetTagBase";

type Props = WithTranslation & {
  tags: Tag[];
};

class ChangesetTagsCollapsed extends React.Component<Props> {
  render() {
    const { tags, t } = this.props;
    const message = tags.map(tag => tag.name).join(", ");
    return (
      <Tooltip location="top" message={message}>
        <ChangesetTagBase icon="tags" label={tags.length + " " + t("changeset.tags")} />
      </Tooltip>
    );
  }
}

export default withTranslation("repos")(ChangesetTagsCollapsed);
