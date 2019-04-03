//@flow
import React from "react";
import { Subtitle } from "@scm-manager/ui-components";
import {translate} from "react-i18next";

type Props = {
  t: string => string
};

class CreateBranch extends React.Component<Props> {
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

export default translate("repos")(CreateBranch);
