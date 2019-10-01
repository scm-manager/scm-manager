// @flow

import React from "react";
import MultiPluginActionModal from "./MultiPluginActionModal";
import type {PendingPlugins} from "@scm-manager/ui-types";
import {apiClient} from "@scm-manager/ui-components";
import {translate} from "react-i18next";

type Props = {
  onClose: () => void,
  refresh: () => void,
  pendingPlugins: PendingPlugins,

  // context props
  t: string => string
};


class CancelPendingActionModal extends React.Component<Props> {

  render() {
    const {onClose, pendingPlugins, t} = this.props;

    return <MultiPluginActionModal
      description={t("plugins.modal.cancelPending")} label={t("plugins.cancelPending")}
      onClose={onClose} pendingPlugins={pendingPlugins} execute={this.cancelPending}>
    </MultiPluginActionModal>;
  }

  cancelPending = () => {
    const { pendingPlugins, refresh, onClose } = this.props;
    return apiClient
      .post(pendingPlugins._links.cancel.href)
      .then(refresh)
      .then(onClose);
  };
}

export default translate("admin")(CancelPendingActionModal);
