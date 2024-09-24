/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import * as React from "react";
import { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import { Plugin } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, Notification, Subtitle, Title } from "@scm-manager/ui-components";
import PluginsList from "../components/PluginList";
import PluginTopActions from "../components/PluginTopActions";
import ExecutePendingActionModal from "../components/ExecutePendingActionModal";
import CancelPendingActionModal from "../components/CancelPendingActionModal";
import UpdateAllActionModal from "../components/UpdateAllActionModal";
import ShowPendingModal from "../components/ShowPendingModal";
import {
  useAvailablePlugins,
  useInstalledPlugins,
  usePendingPlugins,
  usePluginCenterAuthInfo,
} from "@scm-manager/ui-api";
import PluginModal from "../components/PluginModal";
import CloudoguPlatformBanner from "../components/CloudoguPlatformBanner";
import PluginCenterAuthInfo from "../components/PluginCenterAuthInfo";
import styled from "styled-components";
import { Button } from "@scm-manager/ui-buttons";

export enum PluginAction {
  INSTALL = "install",
  UPDATE = "update",
  UNINSTALL = "uninstall",
  CLOUDOGU = "cloudoguInstall",
}

export type PluginModalContent = {
  plugin: Plugin;
  action: PluginAction;
};

type Props = {
  installed: boolean;
};

const StickyHeader = styled.div`
  position: sticky;
  top: var(--scm-navbar-main-height);
  z-index: 10;
  margin-bottom: 1rem;
  margin-top: -1rem;
  border-bottom: solid 2px var(--scm-border-color);
  padding-bottom: 1rem;
  padding-top: 1rem;

  @media screen and (max-width: 1215px) {
    flex-direction: column !important;
  }
`;

const PluginsOverview: FC<Props> = ({ installed }) => {
  const [t] = useTranslation("admin");
  const {
    data: availablePlugins,
    isLoading: isLoadingAvailablePlugins,
    error: availablePluginsError,
  } = useAvailablePlugins({ enabled: !installed });
  const {
    data: installedPlugins,
    isLoading: isLoadingInstalledPlugins,
    error: installedPluginsError,
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

  const renderHeader = (actions: React.ReactElement) => {
    return (
      <StickyHeader className="has-background-secondary-least is-flex is-justify-content-space-between is-align-items-baseline has-gap-2">
        <div className="is-flex-shrink-0">
          <Title className="is-flex">
            {t("plugins.title")} <PluginCenterAuthInfo {...pluginCenterAuthInfo} />
          </Title>
          <Subtitle subtitle={installed ? t("plugins.installedSubtitle") : t("plugins.availableSubtitle")} />
        </div>
        <PluginTopActions>{actions}</PluginTopActions>
      </StickyHeader>
    );
  };

  const createActions = () => {
    const buttons = [];

    if (pendingPlugins && pendingPlugins._links) {
      if (pendingPlugins._links.execute) {
        buttons.push(
          <Button variant="primary" key={"executePending"} onClick={() => setShowExecutePendingModal(true)}>
            {t("plugins.executePending")}
          </Button>
        );
      }

      if (pendingPlugins._links.cancel && !pendingPlugins._links.execute) {
        buttons.push(
          <Button variant="primary" key={"showPending"} onClick={() => setShowPendingModal(true)}>
            {t("plugins.showPending")}
          </Button>
        );
      }
    }

    if (collection && collection._links && collection._links.update) {
      buttons.push(
        <Button variant="secondary" key={"updateAll"} onClick={() => setShowUpdateAllModal(true)}>
          {computeUpdateAllSize()}
        </Button>
      );
    }

    if (pendingPlugins && pendingPlugins._links && pendingPlugins._links.cancel) {
      buttons.push(
        <Button key={"cancelPending"} onClick={() => setShowCancelModal(true)}>
          {t("plugins.cancelPending")}
        </Button>
      );
    }

    return <>{buttons.length > 0 ? buttons : null}</>;
  };

  const computeUpdateAllSize = () => {
    const outdatedPlugins = collection?._embedded?.plugins.filter((p: Plugin) => p._links.update).length;
    return t("plugins.outdatedPlugins", {
      count: outdatedPlugins,
    });
  };

  const renderPluginsList = () => {
    let pluginCenterStatusNotification: React.ReactNode;
    if (collection && collection.pluginCenterStatus !== "OK") {
      const type = collection.pluginCenterStatus === "DEACTIVATED" ? "info" : "danger";
      pluginCenterStatusNotification = (
        <Notification type={type}>{t(`plugins.pluginCenterStatus.${collection.pluginCenterStatus}`)}</Notification>
      );
    }
    if (collection?._embedded && collection._embedded.plugins.length > 0) {
      return (
        <>
          {pluginCenterStatusNotification}
          <PluginsList
            plugins={collection._embedded.plugins}
            openModal={setPluginModalContent}
            pluginCenterAuthInfo={pluginCenterAuthInfo.data}
          />
        </>
      );
    }
    return pluginCenterStatusNotification ?? <Notification type="info">{t("plugins.noPlugins")}</Notification>;
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
      {pluginCenterAuthInfo.data?.default ? <CloudoguPlatformBanner info={pluginCenterAuthInfo.data} /> : null}
      {renderPluginsList()}
      {renderModals()}
    </>
  );
};

export default PluginsOverview;
