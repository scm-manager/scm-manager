// @flow
import React from "react";
import { Subtitle } from "@scm-manager/ui-components";

class CreateBranch extends React.Component<> {
  render() {
    const { t } = this.props;

    return (
      <>
        <Subtitle subtitle={t("branches.create.title")} />
        <p>Create placeholder</p>
      </>
    );
  }
}

export default CreateBranch;
