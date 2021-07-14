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

import React, { FC, useEffect, useState } from "react";
import {
  Button,
  Notification as InfoNotification,
  ErrorNotification,
  Icon,
  ToastArea,
  ToastNotification,
  ToastType,
  Loading,
  DateFromNow,
  devices,
} from "@scm-manager/ui-components";
import styled from "styled-components";
import {
  useClearNotifications,
  useDismissNotification,
  useNotifications,
  useNotificationSubscription,
} from "@scm-manager/ui-api";
import { Notification, NotificationCollection } from "@scm-manager/ui-types";
import { useHistory, Link } from "react-router-dom";
import classNames from "classnames";
import { useTranslation } from "react-i18next";

const Bell = styled(Icon)`
  font-size: 1.5rem;
`;

const Container = styled.div`
  display: flex;
  cursor: pointer;
`;

const DropDownMenu = styled.div`
  min-width: 35rem;

  @media screen and (max-width: ${devices.mobile.width}px) {
    min-width: 20rem;
  }

  @media screen and (max-width: ${devices.desktop.width}px) {
    margin-right: 1rem;
  }

  @media screen and (min-width: ${devices.desktop.width}px) {
    right: 0;
    left: auto;
  }

  &:before {
    position: absolute;
    content: "";
    border-style: solid;
    pointer-events: none;
    height: 0;
    width: 0;
    top: 0;
    border-color: transparent;
    border-bottom-color: white;
    border-left-color: white;
    border-width: 0.4rem;
    transform-origin: center;
    transform: rotate(135deg);

    @media screen and (max-width: ${devices.desktop.width - 1}px) {
      left: 1.3rem;
    }

    @media screen and (min-width: ${devices.desktop.width}px) {
      right: 1.3rem;
    }
  }
`;

const VerticalCenteredTd = styled.td`
  vertical-align: middle !important;
`;

const DateColumn = styled(VerticalCenteredTd)`
  white-space: nowrap;
`;

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
      <VerticalCenteredTd onClick={() => history.push(notification.link)} className="has-cursor-pointer">
        <NotificationMessage message={notification.message} />
      </VerticalCenteredTd>
      <DateColumn className="has-text-right">
        <DateFromNow date={notification.createdAt} />
      </DateColumn>
      <DismissColumn className="is-darker">
        {isLoading ? (
          <div className="small-loading-spinner" />
        ) : (
          <Icon
            name="trash"
            color="black"
            className="has-cursor-pointer"
            title={t("notifications.dismiss")}
            onClick={remove}
          />
        )}
      </DismissColumn>
    </tr>
  );
};

const DismissAllButton = styled(Button)`
  &:hover > * {
    color: white !important;
  }
`;

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
    <div className="dropdown-item has-text-centered">
      <ErrorNotification error={error} />
      <DismissAllButton className="is-outlined" color="link" loading={isLoading} action={clear}>
        <Icon color="link" name="trash" className="mr-1" /> {t("notifications.dismissAll")}
      </DismissAllButton>
    </div>
  );
};

const NotificationList: FC<Props> = ({ data, clear, remove }) => {
  const [t] = useTranslation("commons");
  const clearLink = data._links.clear;

  const all = [...data._embedded.notifications].reverse();
  const top = all.slice(0, 6);

  return (
    <div className="dropdown-content p-0">
      <table className="table mb-0 card-table">
        <tbody>
          {top.map((n, i) => (
            <NotificationEntry key={i} notification={n} removeToast={remove} />
          ))}
        </tbody>
      </table>
      {all.length > 6 ? (
        <p className="has-text-centered has-text-grey">{t("notifications.xMore", { count: all.length - 6 })}</p>
      ) : null}
      {clearLink ? <ClearEntry notifications={data} clearToasts={clear} /> : null}
    </div>
  );
};

const DropdownMenuContainer: FC = ({ children }) => <div className="dropdown-content p-4">{children}</div>;

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
    {data._embedded.notifications.length > 0 ? (
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

const NotificationMessage: FC<{ message: string }> = ({ message }) => {
  const [t] = useTranslation("plugins");
  return t("notifications." + message, message);
};

type SubscriptionProps = {
  notifications: Notification[];
  remove: (notification: Notification) => void;
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
            <Link to={notification.link}>
              <NotificationMessage message={notification.message} />
            </Link>
          </p>
        </ToastNotification>
      ))}
    </ToastArea>
  );
};

const BellNotificationContainer = styled.div`
  position: relative;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
`;

type NotificationCounterProps = {
  count: number;
};

const NotificationCounter = styled.span<NotificationCounterProps>`
  position: absolute;
  top: -0.5rem;
  right: ${(props) => (props.count < 10 ? "0" : "-0.25")}rem;
`;

type BellNotificationIconProps = {
  data?: NotificationCollection;
  onClick: () => void;
};

const BellNotificationIcon: FC<BellNotificationIconProps> = ({ data, onClick }) => {
  const counter = data?._embedded.notifications.length || 0;
  return (
    <BellNotificationContainer onClick={onClick}>
      <Bell iconStyle={counter === 0 ? "far" : "fas"} name="bell" color="white" />
      {counter > 0 ? <NotificationCounter count={counter}>{counter < 100 ? counter : "∞"}</NotificationCounter> : null}
    </BellNotificationContainer>
  );
};

const LoadingBox: FC = () => (
  <div className="box">
    <Loading />
  </div>
);

const ErrorBox: FC<{ error: Error | null }> = ({ error }) => {
  if (!error) {
    return null;
  }
  return (
    <DropdownMenuContainer>
      <ErrorNotification error={error} />
    </DropdownMenuContainer>
  );
};

type NotificationProps = {
  className?: string;
};

const Notifications: FC<NotificationProps> = ({ className }) => {
  const { data, isLoading, error, refetch } = useNotifications();
  const { notifications, remove, clear } = useNotificationSubscription(refetch, data);

  const [open, setOpen] = useState(false);
  useEffect(() => {
    const close = () => setOpen(false);
    window.addEventListener("click", close);
    return () => window.removeEventListener("click", close);
  }, []);

  return (
    <>
      <NotificationSubscription notifications={notifications} remove={remove} />
      <div
        className={classNames(
          "dropdown",
          "is-hoverable",
          {
            "is-active": open,
          },
          className
        )}
        onClick={(e) => e.stopPropagation()}
      >
        <Container className="dropdown-trigger">
          <BellNotificationIcon data={data} onClick={() => setOpen((o) => !o)} />
        </Container>
        <DropDownMenu className="dropdown-menu" id="dropdown-menu" role="menu">
          <ErrorBox error={error} />
          {isLoading ? <LoadingBox /> : null}
          {data ? <NotificationDropDown data={data} remove={remove} clear={clear} /> : null}
        </DropDownMenu>
      </div>
    </>
  );
};

export default Notifications;
