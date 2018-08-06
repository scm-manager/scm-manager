//@flow
import React from "react";
import { DeleteButton } from "../../../components/buttons";
import { translate } from "react-i18next";
import classNames from "classnames";

type Props = {
  t: string => string,
  username: string,
  removeUser: string => void
};

type State = {};



class RemoveUserButton extends React.Component<Props, State> {
  render() {
    const { t , username, removeUser} = this.props;
    return (
      <div className={classNames("is-pulled-right")}>
        <DeleteButton
          label={t("remove-user-button.label")}
          action={(event: Event) => {
            event.preventDefault();
            removeUser(username);
          }}
        />
      </div>
    );
  }
}

export default translate("groups")(RemoveUserButton);
