//@flow
import React from "react";
import { DeleteButton } from "../../../components/buttons";
import { translate } from "react-i18next";
import classNames from "classnames";

type Props = {
  t: string => string,
  membername: string,
  removeMember: string => void
};

type State = {};



class RemoveMemberButton extends React.Component<Props, State> {
  render() {
    const { t , membername, removeMember} = this.props;
    return (
      <div className={classNames("is-pulled-right")}>
        <DeleteButton
          label={t("remove-member-button.label")}
          action={(event: Event) => {
            event.preventDefault();
            removeMember(membername);
          }}
        />
      </div>
    );
  }
}

export default translate("groups")(RemoveMemberButton);
