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
import * as React from "react";
import { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import { Plugin } from "@scm-manager/ui-types";
import {
  Button,
  ButtonGroup,
  ErrorNotification,
  Loading,
  Notification,
  Subtitle,
  Title
} from "@scm-manager/ui-components";
import PluginsList from "../components/PluginList";
import PluginTopActions from "../components/PluginTopActions";
import PluginBottomActions from "../components/PluginBottomActions";
import ExecutePendingActionModal from "../components/ExecutePendingActionModal";
import CancelPendingActionModal from "../components/CancelPendingActionModal";
import UpdateAllActionModal from "../components/UpdateAllActionModal";
import ShowPendingModal from "../components/ShowPendingModal";
import {
  useAvailablePlugins,
  useInstalledPlugins,
  usePendingPlugins,
  usePluginCenterAuthInfo
} from "@scm-manager/ui-api";
import PluginModal from "../components/PluginModal";
import MyCloudoguBanner from "../components/MyCloudoguBanner";
import PluginCenterAuthInfo from "../components/PluginCenterAuthInfo";

export enum PluginAction {
  INSTALL = "install",
  UPDATE = "update",
  UNINSTALL = "uninstall",
  CLOUDOGU = "cloudoguInstall"
}

export type PluginModalContent = {
  plugin: Plugin;
  action: PluginAction;
};

type Props = {
  installed: boolean;
};

const PluginsOverview: FC<Props> = ({ installed }) => {
  const [t] = useTranslation("admin");
  const {
    data: availablePlugins,
    isLoading: isLoadingAvailablePlugins,
    error: availablePluginsError
  } = useAvailablePlugins({ enabled: !installed });
  const {
    data: installedPlugins,
    isLoading: isLoadingInstalledPlugins,
    error: installedPluginsError
  } = useInstalledPlugins({ enabled: installed });
  const { data: pendingPlugins, isLoading: isLoadingPendingPlugins, error: pendingPluginsError } = usePendingPlugins();
  const pluginCenterAuthInfo = usePluginCenterAuthInfo();
  const [showPendingModal, setShowPendingModal] = useState(false);
  const [showExecutePendingModal, setShowExecutePendingModal] = useState(false);
  const [showUpdateAllModal, setShowUpdateAllModal] = useState(false);
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [pluginModalContent, setPluginModalContent] = useState<PluginModalContent | null>(null);
  const collection = installed ? installedPlugins : availablePlugins;
  const error = (installed ? installedPluginsError : availablePluginsError) || pendingPluginsError;
  const loading = (installed ? isLoadingInstalledPlugins : isLoadingAvailablePlugins) || isLoadingPendingPlugins;

  const renderHeader = (actions: React.ReactNode) => {
    return (
      <div className="columns">
        <div className="column">
          <Title className="is-flex">
            {t("plugins.title")} <PluginCenterAuthInfo {...pluginCenterAuthInfo} />
          </Title>
          <Subtitle subtitle={installed ? t("plugins.installedSubtitle") : t("plugins.availableSubtitle")} />
        </div>
        <PluginTopActions>{actions}</PluginTopActions>
      </div>
    );
  };

  const renderFooter = (actions: React.ReactNode) => {
    if (actions) {
      return <PluginBottomActions>{actions}</PluginBottomActions>;
    }
    return null;
  };

  const createActions = () => {
    const buttons = [];

    if (pendingPlugins && pendingPlugins._links) {
      if (pendingPlugins._links.execute) {
        buttons.push(
          <Button
            color="primary"
            reducedMobile={true}
            key={"executePending"}
            icon={"arrow-circle-right"}
            label={t("plugins.executePending")}
            action={() => setShowExecutePendingModal(true)}
          />
        );
      }

      if (pendingPlugins._links.cancel) {
        if (!pendingPlugins._links.execute) {
          buttons.push(
            <Button
              color="primary"
              reducedMobile={true}
              key={"showPending"}
              icon={"info"}
              label={t("plugins.showPending")}
              action={() => setShowPendingModal(true)}
            />
          );
        }

        buttons.push(
          <Button
            color="primary"
            reducedMobile={true}
            key={"cancelPending"}
            icon={"times"}
            label={t("plugins.cancelPending")}
            action={() => setShowCancelModal(true)}
          />
        );
      }
    }

    if (collection && collection._links && collection._links.update) {
      buttons.push(
        <Button
          color="primary"
          reducedMobile={true}
          key={"updateAll"}
          icon={"sync-alt"}
          label={computeUpdateAllSize()}
          action={() => setShowUpdateAllModal(true)}
        />
      );
    }

    if (buttons.length > 0) {
      return <ButtonGroup>{buttons}</ButtonGroup>;
    }
    return null;
  };

  const computeUpdateAllSize = () => {
    const outdatedPlugins = collection?._embedded?.plugins.filter((p: Plugin) => p._links.update).length;
    return t("plugins.outdatedPlugins", {
      count: outdatedPlugins
    });
  };

  const renderPluginsList = () => {
    if (collection?._embedded && collection._embedded.plugins.length > 0) {
      return (
        <PluginsList
          plugins={collection._embedded.plugins}
          openModal={setPluginModalContent}
          pluginCenterAuthInfo={pluginCenterAuthInfo.data}
        />
      );
    }
    return <Notification type="info">{t("plugins.noPlugins")}</Notification>;
  };

  const renderModals = () => {
    if (showPendingModal && pendingPlugins) {
      return <ShowPendingModal onClose={() => setShowPendingModal(false)} pendingPlugins={pendingPlugins} />;
    }
    if (showExecutePendingModal && pendingPlugins) {
      return (
        <ExecutePendingActionModal onClose={() => setShowExecutePendingModal(false)} pendingPlugins={pendingPlugins} />
      );
    }
    if (showCancelModal && pendingPlugins) {
      return <CancelPendingActionModal onClose={() => setShowCancelModal(false)} pendingPlugins={pendingPlugins} />;
    }
    if (showUpdateAllModal && collection) {
      return <UpdateAllActionModal onClose={() => setShowUpdateAllModal(false)} installedPlugins={collection} />;
    }
    if (pluginModalContent) {
      const { action, plugin } = pluginModalContent;
      return <PluginModal plugin={plugin} pluginAction={action} onClose={() => setPluginModalContent(null)} />;
    }
    return null;
  };

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (!collection || loading) {
    return <Loading />;
  }

  const actions = createActions();
  return (
    <>
      {renderHeader(actions)}
      <hr className="header-with-actions" />
      {pluginCenterAuthInfo.data?.default ? <MyCloudoguBanner info={pluginCenterAuthInfo.data} /> : null}
      {renderPluginsList()}
      {renderFooter(actions)}
      {renderModals()}
    </>
  );
};

export default PluginsOverview;
