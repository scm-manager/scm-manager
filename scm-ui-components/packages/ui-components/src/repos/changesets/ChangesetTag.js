//@flow
import React from "react";
import type { Tag } from "@scm-manager/ui-types";
import ChangesetTagBase from "./ChangesetTagBase";

type Props = {
  tag: Tag
};

class ChangesetTag extends React.Component<Props> {
  render() {
    const { tag } = this.props;
    return <ChangesetTagBase icon={"fa-tag"} label={tag.name} />;
  }
}

export default ChangesetTag;
