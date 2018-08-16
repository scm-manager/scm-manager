//@flow
import React from "react";
import { DeleteButton } from "../../../components/buttons";
import { translate } from "react-i18next";
import classNames from "classnames";

type Props = {
  t: string => string,
  groupname: string,
  removeGroup: string => void
};

type State = {};



class RemoveAdminGroupButton extends React.Component<Props, State> {
  render() {
    const { t , groupname, removeGroup} = this.props;
    return (
      <div className={classNames("is-pulled-right")}>
        <DeleteButton
          label={t("admin-settings.remove-group-button")}
          action={(event: Event) => {
            event.preventDefault();
            removeGroup(groupname);
          }}
        />
      </div>
    );
  }
}

export default translate("config")(RemoveAdminGroupButton);
