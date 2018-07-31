//@flow
import React from "react";
import injectSheet from "react-jss";
import { translate } from "react-i18next";
import { AddButton } from "../../../components/buttons";
import classNames from "classnames";

const styles = {
  spacing: {
    margin: "1em 0 0 1em"
  }
};

type Props = {
  t: string => string,
  classes: any
};

class CreateGroupButton extends React.Component<Props> {
  render() {
    const { classes, t } = this.props;
    return (
      <div className={classNames("is-pulled-right", classes.spacing)}>
        <AddButton label={t("create-group-button.label")} link="/groups/add" />
      </div>
    );
  }
}

export default translate("groups")(injectSheet(styles)(CreateGroupButton));
