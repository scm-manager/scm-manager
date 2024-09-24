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
import { Link, useHistory } from "react-router-dom";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import {
  useClearNotifications,
  useDismissNotification,
  useNotifications,
  useNotificationSubscription,
} from "@scm-manager/ui-api";
import { Notification, NotificationCollection } from "@scm-manager/ui-types";
import {
  Button,
  DateFromNow,
  ErrorNotification,
  Icon,
  Notification as InfoNotification,
  ToastArea,
  ToastNotification,
  ToastType,
} from "@scm-manager/ui-components";
import HeaderDropDown, { Column, OnlyMobileWrappingColumn, Table } from "../components/HeaderDropDown";

const DismissColumn = styled.td`
  vertical-align: middle !important;
  width: 2rem;
`;

type EntryProps = {
  notification: Notification;
  removeToast: (notification: Notification) => void;
};

const NotificationEntry: FC<EntryProps> = ({ notification, removeToast }) => {
  const history = useHistory();
  const { isLoading, error, dismiss } = useDismissNotification(notification);
  const [t] = useTranslation("commons");

  const remove = () => {
    removeToast(notification);
    dismiss();
  };

  if (error) {
    return <ErrorNotification error={error} />;
  }
  return (
    <tr className={`is-${color(notification)}`}>
      <Column onClick={notification.link ? () => history.push(notification.link) : undefined} className="is-clickable">
        <NotificationMessage message={notification.message} parameters={notification.parameters} />
      </Column>
      <OnlyMobileWrappingColumn className="has-text-right">
        <DateFromNow date={notification.createdAt} />
      </OnlyMobileWrappingColumn>
      <DismissColumn>
        {isLoading ? (
          <div className="small-loading-spinner" aria-label={t("notifications.loading")} />
        ) : (
          <Button color="text" icon="trash" action={remove} title={t("notifications.dismiss")} className="px-1" />
        )}
      </DismissColumn>
    </tr>
  );
};

type ClearEntryProps = {
  notifications: NotificationCollection;
  clearToasts: () => void;
};

const ClearEntry: FC<ClearEntryProps> = ({ notifications, clearToasts }) => {
  const { isLoading, error, clear: clearStore } = useClearNotifications(notifications);
  const [t] = useTranslation("commons");
  const clear = () => {
    clearToasts();
    clearStore();
  };
  return (
    <div className={classNames("dropdown-item", "has-text-centered")}>
      <ErrorNotification error={error} />
      <Button className="is-outlined m-3" color="link" loading={isLoading} action={clear}>
        <Icon color="inherit" name="trash" className="mr-1" alt="" /> {t("notifications.dismissAll")}
      </Button>
    </div>
  );
};

const NotificationList: FC<Props> = ({ data, clear, remove }) => {
  const [t] = useTranslation("commons");
  const clearLink = data._links.clear;

  const all = [...(data._embedded?.notifications || [])].reverse();
  const top = all.slice(0, 6);

  return (
    <div className={classNames("dropdown-content", "p-0")}>
      <Table className={classNames("table", "card-table", "mb-0")}>
        <tbody>
          {top.map((n, i) => (
            <NotificationEntry key={i} notification={n} removeToast={remove} />
          ))}
        </tbody>
      </Table>
      {all.length > 6 ? (
        <p className={classNames("has-text-centered", "has-text-secondary")}>
          {t("notifications.xMore", { count: all.length - 6 })}
        </p>
      ) : null}
      {clearLink ? <ClearEntry notifications={data} clearToasts={clear} /> : null}
    </div>
  );
};

const DropdownMenuContainer: FC = ({ children }) => (
  <div className={classNames("dropdown-content", "p-4")}>{children}</div>
);

const NoNotifications: FC = () => {
  const [t] = useTranslation("commons");
  return (
    <DropdownMenuContainer>
      <InfoNotification type="info">{t("notifications.empty")}</InfoNotification>
    </DropdownMenuContainer>
  );
};

type Props = {
  data: NotificationCollection;
  remove: (notification: Notification) => void;
  clear: () => void;
};

const NotificationDropDown: FC<Props> = ({ data, remove, clear }) => (
  <>
    {(data._embedded?.notifications.length ?? 0) > 0 ? (
      <NotificationList data={data} remove={remove} clear={clear} />
    ) : (
      <NoNotifications />
    )}
  </>
);

const color = (notification: Notification) => {
  let c: string = notification.type.toLowerCase();
  // We use the color danger for an error.
  // All other notification types are matching a color, except error which must be mapped to danger.
  if (c === "error") {
    c = "danger";
  }
  return c;
};

const NotificationMessage: FC<{ message: string; parameters?: Record<string, string> }> = ({ message, parameters }) => {
  const [t] = useTranslation("plugins");
  if (parameters) {
    return t("notifications." + message, message, parameters);
  } else {
    return t("notifications." + message, message);
  }
};

type SubscriptionProps = {
  notifications: Notification[];
  remove: (notification: Notification) => void;
};

const PotentialLink: FC<{ link?: string }> = ({ link, children }) => {
  if (link) {
    return <Link to={link}>{children}</Link>;
  } else {
    return <>{children}</>;
  }
};

const NotificationSubscription: FC<SubscriptionProps> = ({ notifications, remove }) => {
  const [t] = useTranslation("commons");

  const top = [...notifications].slice(-3);

  return (
    <ToastArea>
      {top.map((notification, i) => (
        <ToastNotification
          key={i}
          type={color(notification) as ToastType}
          title={t("notifications.toastTitle")}
          close={() => remove(notification)}
        >
          <p>
            <PotentialLink link={notification.link}>
              <NotificationMessage message={notification.message} parameters={notification.parameters} />
            </PotentialLink>
          </p>
        </ToastNotification>
      ))}
    </ToastArea>
  );
};

type BellNotificationIconProps = {
  data?: NotificationCollection;
};

const BellNotificationIcon: FC<BellNotificationIconProps> = ({ data }) => {
  const [t] = useTranslation("commons");
  const counter = data?._embedded?.notifications.length || 0;
  return (
    <Icon
      className="is-size-4"
      iconStyle={counter === 0 ? "far" : "fas"}
      name="bell"
      color="inherit"
      alt={t("notifications.bellTitle")}
    />
  );
};

const count = (data?: NotificationCollection) => {
  const counter = data?._embedded?.notifications.length || 0;
  if (counter !== 0) {
    return counter < 100 ? counter.toString() : "∞";
  }
};

type NotificationProps = React.PropsWithChildren<{
  className?: string;
}>;

const Notifications = React.forwardRef<HTMLButtonElement, NotificationProps>(({ className }, ref) => {
  const { data, isLoading, error, refetch } = useNotifications();
  const { notifications, remove, clear } = useNotificationSubscription(refetch, data);

  return (
    <>
      <NotificationSubscription notifications={notifications} remove={remove} />
      <HeaderDropDown
        className={className}
        error={error}
        isLoading={isLoading}
        icon={<BellNotificationIcon data={data} />}
        count={count(data)}
        mobilePosition="left"
        ref={ref}
      >
        {data ? <NotificationDropDown data={data} remove={remove} clear={clear} /> : null}
      </HeaderDropDown>
    </>
  );
});

export default Notifications;
