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

import React, { FC } from "react";
import styled from "styled-components";
import { Link, Plugin } from "@scm-manager/ui-types";
import { CardColumn, Icon } from "@scm-manager/ui-components";
import { PluginAction, PluginModalContent } from "../containers/PluginsOverview";
import { useTranslation } from "react-i18next";
import PluginAvatar from "./PluginAvatar";
import { useKeyboardIteratorTarget } from "@scm-manager/ui-shortcuts";

type Props = {
  plugin: Plugin;
  openModal: (content: PluginModalContent) => void;
};

const ActionbarWrapper = styled.div`
  & span + span {
    margin-left: 0.5rem;
  }
`;

const IconWrapperStyle = styled.span.attrs(() => ({
  className: "level-item mb-0 p-2 is-clickable",
}))`
  border: 1px solid #cdcdcd; // $dark-25
  border-radius: 4px;

  &:hover {
    border-color: #9a9a9a; // $dark-50
  }
`;

const IconWrapper: FC<{ action: () => void }> = ({ action, children }) => {
  return (
    <IconWrapperStyle onClick={action} onKeyDown={(e) => e.key === "Enter" && action()} tabIndex={0}>
      {children}
    </IconWrapperStyle>
  );
};

const PluginEntry: FC<Props> = ({ plugin, openModal }) => {
  const [t] = useTranslation("admin");
  const isInstallable = plugin._links.install && (plugin._links.install as Link).href;
  const isUpdatable = plugin._links.update && (plugin._links.update as Link).href;
  const isUninstallable = plugin._links.uninstall && (plugin._links.uninstall as Link).href;
  const ref = useKeyboardIteratorTarget();

  const evaluateAction = () => {
    if (isInstallable) {
      return () => openModal({ plugin, action: PluginAction.INSTALL });
    }

    return undefined;
  };

  const pendingInfo = () => (
    <>
      <Icon className="fa-lg" name="check" color="info" alt={t("plugins.markedAsPending")} />
    </>
  );
  const actionBar = () => (
    <ActionbarWrapper className="is-flex">
      {isInstallable && (
        <IconWrapper action={() => openModal({ plugin, action: PluginAction.INSTALL })}>
          <Icon title={t("plugins.modal.install")} name="download" color="info" />
        </IconWrapper>
      )}
      {isUninstallable && (
        <IconWrapper action={() => openModal({ plugin, action: PluginAction.UNINSTALL })}>
          <Icon title={t("plugins.modal.uninstall")} name="trash" color="info" />
        </IconWrapper>
      )}
      {isUpdatable && (
        <IconWrapper action={() => openModal({ plugin, action: PluginAction.UPDATE })}>
          <Icon title={t("plugins.modal.update")} name="sync-alt" color="info" />
        </IconWrapper>
      )}
    </ActionbarWrapper>
  );

  return (
    <>
      <CardColumn
        ref={ref}
        action={evaluateAction()}
        avatar={<PluginAvatar plugin={plugin} />}
        title={plugin.displayName ? <strong>{plugin.displayName}</strong> : <strong>{plugin.name}</strong>}
        description={plugin.description}
        contentRight={plugin.pending || plugin.markedForUninstall ? pendingInfo() : actionBar()}
        footerLeft={<small>{plugin.version}</small>}
        footerRight={<small className="level-item is-block shorten-text">{plugin.author}</small>}
      />
    </>
  );
};

export default PluginEntry;
