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
import React, { FC } from "react";
import styled from "styled-components";
import { Link, Plugin, PluginCenterAuthenticationInfo } from "@scm-manager/ui-types";
import { CardColumn, Icon } from "@scm-manager/ui-components";
import { PluginAction, PluginModalContent } from "../containers/PluginsOverview";
import { useTranslation } from "react-i18next";
import PluginAvatar from "./PluginAvatar";
import classNames from "classnames";
import MyCloudoguTag from "./MyCloudoguTag";
import { useKeyboardIteratorTarget } from "@scm-manager/ui-shortcuts";

type Props = {
  plugin: Plugin;
  openModal: (content: PluginModalContent) => void;
  pluginCenterAuthInfo?: PluginCenterAuthenticationInfo;
};

const ActionbarWrapper = styled.div`
  & span + span {
    margin-left: 0.5rem;
  }
`;

const IconWrapperStyle = styled.span.attrs((props) => ({
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

const PluginEntry: FC<Props> = ({ plugin, openModal, pluginCenterAuthInfo }) => {
  const [t] = useTranslation("admin");
  const isInstallable = plugin._links.install && (plugin._links.install as Link).href;
  const isUpdatable = plugin._links.update && (plugin._links.update as Link).href;
  const isUninstallable = plugin._links.uninstall && (plugin._links.uninstall as Link).href;
  const isCloudoguPlugin = plugin.type === "CLOUDOGU";
  const isDefaultPluginCenterLoginAvailable = pluginCenterAuthInfo?.default && !!pluginCenterAuthInfo?._links?.login;
  const ref = useKeyboardIteratorTarget();

  const evaluateAction = () => {
    if (isInstallable) {
      return () => openModal({ plugin, action: PluginAction.INSTALL });
    }

    if (isCloudoguPlugin && isDefaultPluginCenterLoginAvailable) {
      return () => openModal({ plugin, action: PluginAction.CLOUDOGU });
    }

    return undefined;
  };

  const pendingInfo = () => (
    <>
      <Icon
      className="fa-lg"
      name="check"
      color="info"
      alt={t("plugins.markedAsPending")}
    /></>
  );
  const actionBar = () => (
    <ActionbarWrapper className="is-flex">
      {isCloudoguPlugin && isDefaultPluginCenterLoginAvailable && (
        <IconWrapper action={() => openModal({ plugin, action: PluginAction.CLOUDOGU })}>
          <Icon title={t("plugins.modal.cloudoguInstall")} name="link" color="success-dark" />
        </IconWrapper>
      )}
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
        footerRight={null}
      />
      <div
        className={classNames("is-flex", {
          "is-justify-content-space-between": isCloudoguPlugin,
          "is-justify-content-end": !isCloudoguPlugin,
        })}
      >
        {isCloudoguPlugin ? <MyCloudoguTag /> : null}
        <small className="level-item is-block shorten-text is-align-self-flex-end">{plugin.author}</small>
      </div>
    </>
  );
};

export default PluginEntry;
