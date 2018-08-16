//@flow
import React from "react";
import {DeleteButton} from "../../../components/buttons";
import { translate } from "react-i18next";
import classNames from "classnames";

type Props = {
  t: string => string,
  proxyExcludeName: string,
  removeProxyExclude: string => void,
  disable: boolean
};

type State = {};



class RemoveProxyExcludeButton extends React.Component<Props, State> {
  render() {
    const { t , proxyExcludeName, removeProxyExclude} = this.props;
    return (
      <div className={classNames("is-pulled-right")}>
        <DeleteButton
          label={t("proxy-settings.remove-proxy-exclude-button")}
          action={(event: Event) => {
            event.preventDefault();
            removeProxyExclude(proxyExcludeName);
          }}
          disabled={this.props.disable}
        />
      </div>
    );
  }
}

export default translate("config")(RemoveProxyExcludeButton);
