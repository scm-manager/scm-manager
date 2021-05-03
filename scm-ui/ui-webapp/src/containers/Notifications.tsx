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
import {
  useClearNotifications,
  useDismissNotification,
  useNotifications,
  useNotificationSubscription
} from "@scm-manager/ui-api";
import { Notification, NotificationCollection, Link as LinkType } from "@scm-manager/ui-types";
import { useHistory, Link } from "react-router-dom";

const Bell = styled(Icon)`
  font-size: 1.5rem;
`;

const Container = styled.div`
  display: flex;
  cursor: pointer;
`;

const DropDownMenu = styled.div`
  min-width: 50vw;

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

type Props = {
  data: NotificationCollection;
};

type EntryProps = {
  notification: Notification;
};

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

const NotificationEntry: FC<EntryProps> = ({ notification }) => {
  const history = useHistory();
  const { isLoading, error, dismiss } = useDismissNotification(notification);
  if (error) {
    return <ErrorNotification error={error} />;
  }
  return (
    <tr onClick={() => history.push(notification.link)} className={`has-cursor-pointer is-${color(notification)}`}>
      <VerticalCenteredTd className="">{notification.message}</VerticalCenteredTd>
      <DateColumn className="has-text-right">
        <DateFromNow date={notification.createdAt} />
      </DateColumn>
      <DismissColumn className="is-darker">
        {isLoading ? (
          <div className="small-loading-spinner" />
        ) : (
          <Icon name="trash" color="black" className="has-cursor-pointer" onClick={dismiss} />
        )}
      </DismissColumn>
    </tr>
  );
};

type ClearEntryProps = {
  notifications: NotificationCollection;
};

const DismissAllButton = styled(Button)`
  &:hover > * {
    color: white !important;
  }
`;

const ClearEntry: FC<ClearEntryProps> = ({ notifications }) => {
  const { isLoading, error, clear } = useClearNotifications(notifications);
  return (
    <div className="dropdown-item has-text-centered	">
      <ErrorNotification error={error} />
      <DismissAllButton className="is-outlined" color="link" loading={isLoading} action={clear}>
        <Icon color="link" name="trash" className="mr-1" /> Dismiss all messages
      </DismissAllButton>
    </div>
  );
};

const NotificationList: FC<Props> = ({ data }) => {
  const clearLink = data._links.clear;
  return (
    <div className="dropdown-content p-0">
      <table className="table mb-0 card-table">
        <tbody>
          {data._embedded.notifications.map((n, i) => (
            <NotificationEntry key={i} notification={n} />
          ))}
        </tbody>
      </table>

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
          <p>
            <Link to={notification.link}>{notification.message}</Link>
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
      {counter > 0 ? <NotificationCounter>{counter}</NotificationCounter> : null}
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
