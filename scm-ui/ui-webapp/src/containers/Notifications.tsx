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
import {
  Button,
  Notification as InfoNotification,
  ErrorNotification,
  Icon,
  ToastArea,
  ToastNotification,
  ToastType,
  Loading,
  DateFromNow
} from "@scm-manager/ui-components";
import styled from "styled-components";
import { useClearNotifications, useNotifications, useNotificationSubscription } from "@scm-manager/ui-api";
import { Notification, NotificationCollection, Link as LinkType } from "@scm-manager/ui-types";
import { Link } from "react-router-dom";

const Bell = styled(Icon)`
  font-size: 1.5rem;
`;

const Container = styled.div`
  display: flex;
  cursor: pointer;
`;

const DropDownItem = styled(Link)`
  word-wrap: break-word;
`;

const DropDownMenu = styled.div`
  min-width: 20rem;

  &:before {
    position: absolute;
    content: "";
    border-style: solid;
    pointer-events: none;
    height: 0;
    width: 0;
    top: 0;
    right: 0.9rem;
    border-color: transparent;
    border-bottom-color: white;
    border-left-color: white;
    border-width: 0.4rem;
    transform-origin: center;
    transform: rotate(135deg);
  }
`;

const IconColumn = styled.span`
  width: 1.5rem;
  display: inline-block;
`;

const DateColumn = styled.span`
  width: 8rem;
  display: inline-block;
`;

const MessageColumn = styled.span`
  display: inline-block;
  overflow: auto;
`;

type Props = {
  data: NotificationCollection;
};

type EntryProps = {
  notification: Notification;
};

const NotificationIcon: FC<EntryProps> = ({ notification }) => {
  return <Icon name="bell" color={color(notification)} />;
};

const NotificationEntry: FC<EntryProps> = ({ notification }) => (
  <DropDownItem to="/" className="dropdown-item">
    <IconColumn className="is-ellipsis-overflow">
      <NotificationIcon notification={notification} />
    </IconColumn>
    <DateColumn className="is-ellipsis-overflow">
      <DateFromNow date={notification.createdAt} />
    </DateColumn>
    <MessageColumn title={notification.message}>{notification.message}</MessageColumn>
  </DropDownItem>
);

type ClearEntryProps = {
  notifications: NotificationCollection;
};

const ClearEntry: FC<ClearEntryProps> = ({ notifications }) => {
  const { isLoading, error, clear } = useClearNotifications(notifications);
  return (
    <div className="dropdown-item">
      <ErrorNotification error={error} />
      <Button fullWidth={true} loading={isLoading} action={clear}>
        Clear all
      </Button>
    </div>
  );
};

const NotificationList: FC<Props> = ({ data }) => {
  const clearLink = data._links.clear;
  return (
    <div className="dropdown-content">
      {data._embedded.notifications.map((n, i) => (
        <NotificationEntry key={i} notification={n} />
      ))}
      {clearLink ? <ClearEntry notifications={data} /> : null}
    </div>
  );
};

const DropdownMenuContainer: FC = ({ children }) => <div className="dropdown-content p-4">{children}</div>;

const NoNotifications: FC = () => (
  <DropdownMenuContainer>
    <InfoNotification type="info">No notifications</InfoNotification>
  </DropdownMenuContainer>
);

const NotificationDropDown: FC<Props> = ({ data }) => (
  <>{data._embedded.notifications.length > 0 ? <NotificationList data={data} /> : <NoNotifications />}</>
);

type SubscriptionProps = {
  data: NotificationCollection;
  refetch: () => Promise<NotificationCollection | undefined>;
};

const color = (notification: Notification) => {
  let c: string = notification.type.toLowerCase();
  if (c === "error") {
    c = "danger";
  }
  return c;
};

const NotificationSubscription: FC<SubscriptionProps> = ({ data, refetch }) => {
  const { notifications, remove } = useNotificationSubscription(data, refetch);
  return (
    <ToastArea>
      {notifications.map((notification, i) => (
        <ToastNotification
          key={i}
          type={color(notification) as ToastType}
          title="Notification"
          close={() => remove(notification)}
        >
          <p>{notification.message}</p>
        </ToastNotification>
      ))}
    </ToastArea>
  );
};

const BellNotificationContainer = styled.div`
  position: relative;
  width: 2rem;
  height: 2rem;
`;

const NotificationCounter = styled.span`
  position: absolute;
  top: -0.5rem;
  right: 0;
`;

type BellNotificationIconProps = {
  data?: NotificationCollection;
};

const BellNotificationIcon: FC<BellNotificationIconProps> = ({ data }) => {
  const counter = data?._embedded.notifications.length || 0;

  return (
    <BellNotificationContainer>
      <Bell iconStyle={counter === 0 ? "far" : "fas"} name="bell" color="white" />
      <NotificationCounter>{counter}</NotificationCounter>
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

const Notifications: FC = () => {
  const { data, isLoading, error, refetch } = useNotifications();
  const subscribeLink = (data?._links["subscribe"] as LinkType)?.href;
  return (
    <>
      {data && subscribeLink ? <NotificationSubscription data={data} refetch={refetch} /> : null}
      <div className="dropdown is-right is-hoverable">
        <Container className="dropdown-trigger">
          <BellNotificationIcon data={data} />
        </Container>
        <DropDownMenu className="dropdown-menu" id="dropdown-menu" role="menu">
          <ErrorBox error={error} />
          {isLoading ? <LoadingBox /> : null}
          {data ? <NotificationDropDown data={data} /> : null}
        </DropDownMenu>
      </div>
    </>
  );
};

export default Notifications;
