// @flow

import React from "react";
import MultiPluginActionModal from "./MultiPluginActionModal";
import type {PluginCollection} from "@scm-manager/ui-types";
import {apiClient} from "@scm-manager/ui-components";
import {translate} from "react-i18next";

type Props = {
  onClose: () => void,
  refresh: () => void,
  installedPlugins: PluginCollection,

  // context props
  t: string => string
};


class UpdateAllActionModal extends React.Component<Props> {

  render() {
    const {onClose, installedPlugins, t} = this.props;

    return <MultiPluginActionModal
      description={t("plugins.modal.updateAll")} label={t("plugins.updateAll")}
      onClose={onClose} installedPlugins={installedPlugins} execute={this.updateAll}>
    </MultiPluginActionModal>;
  }

  updateAll = () => {
    const { installedPlugins, refresh, onClose } = this.props;
    return apiClient
      .post(installedPlugins._links.update.href)
      .then(refresh)
      .then(onClose);
  };
}

export default translate("admin")(UpdateAllActionModal);
