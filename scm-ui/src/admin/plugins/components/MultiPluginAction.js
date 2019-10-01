// @flow
import React from "react";
import { Button } from "@scm-manager/ui-components";
import type { PendingPlugins, PluginCollection } from "@scm-manager/ui-types";
import { translate } from "react-i18next";

export const MultiPluginActionType = {
  UPDATE_ALL: "updateAll",
  CANCEL_PENDING: "cancelPending",
  EXECUTE_PENDING: "executePending"
};

type Props = {
  icon: string,
  label: string,
  onClick: () => void,
};

class MultiPluginAction extends React.Component<Props> {
  render() {
    const { onClick, icon, label } = this.props;
    return (
      <>
        <Button
          color="primary"
          reducedMobile={true}
          icon={icon}
          label={label}
          action={() => onClick()}
        />
      </>
    );
  }
}

export default MultiPluginAction;
