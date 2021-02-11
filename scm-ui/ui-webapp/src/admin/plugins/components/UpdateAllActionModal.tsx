/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React, {FC} from "react";
import {useTranslation, WithTranslation, withTranslation} from "react-i18next";
import { PluginCollection } from "@scm-manager/ui-types";
import { apiClient } from "@scm-manager/ui-components";
import PluginActionModal from "./PluginActionModal";
import {useUpdatePlugins} from "@scm-manager/ui-api";

type Props = WithTranslation & {
  onClose: () => void;
  installedPlugins: PluginCollection;
};

const UpdateAllActionModal: FC<Props> = ({ installedPlugins, onClose }) => {
  const [t] = useTranslation("admin");
  const { update, isLoading, error, isUpdated } = useUpdatePlugins();

  return (
    <PluginActionModal
      description={t("plugins.modal.updateAll")}
      label={t("plugins.updateAll")}
      onClose={onClose}
      installedPlugins={installedPlugins}
      execute={() => update(installedPlugins)}
    />
  );
}

class UpdateAllActionModalCmp extends React.Component<Props> {
  render() {
    const { onClose, installedPlugins, t } = this.props;

    return (
      <PluginActionModal
        description={t("plugins.modal.updateAll")}
        label={t("plugins.updateAll")}
        onClose={onClose}
        installedPlugins={installedPlugins}
        execute={this.updateAll}
      />
    );
  }

  updateAll = () => {
    const { installedPlugins, onClose } = this.props;
    return apiClient
      .post(installedPlugins._links.update.href)
      .then(refresh)
      .then(onClose);
  };
}

export default withTranslation("admin")(UpdateAllActionModal);
