//@flow
import React from "react";
import { DeleteButton } from "../../../components/buttons";
import { translate } from "react-i18next";
import classNames from "classnames";
import { InputField } from "../../../components/forms";

type Props = {
  t: string => string,
  username: string,
  removeUser: string => void,
  disabled: boolean
};

type State = {};

class RemoveAdminUserButton extends React.Component<Props, State> {
  render() {
    const { t, username, removeUser, disabled } = this.props;
    return (
      <div className={classNames("is-pulled-right")}>
        <DeleteButton
          label={t("admin-settings.remove-user-button")}
          action={(event: Event) => {
            event.preventDefault();
            removeUser(username);
          }}
          disabled={disabled}
        />
      </div>
    );
  }
}

export default translate("config")(RemoveAdminUserButton);
