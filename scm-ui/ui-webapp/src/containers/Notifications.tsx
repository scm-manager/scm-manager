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

import React, { FC, useCallback, useEffect, useState } from "react";
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
import { apiClient, useClearNotifications, useNotifications } from "@scm-manager/ui-api";
import { Notification, NotificationCollection, Link as LinkType } from "@scm-manager/ui-types";
import { Link } from "react-router-dom";

const Bell = styled(Icon)`
  font-size: 1.5rem;
`;

const Container = styled.div`
  display: flex;
  cursor: pointer;
`;

const Count = styled.span`
  margin-left: 0.5rem;
`;

const DropDownItem = styled(Link)`
  word-wrap: break-word;
`;

const DropDownMenu = styled.div`
  min-width: 20rem;
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
    <>
      {data._embedded.notifications.map((n, i) => (
        <NotificationEntry key={i} notification={n} />
      ))}
      {clearLink ? <ClearEntry notifications={data} /> : null}
    </>
  );
};

const NoNotifications: FC = () => (
  <div className="m-4">
    <InfoNotification type="info">No notifications</InfoNotification>
  </div>
);

const NotificationDropDown: FC<Props> = ({ data }) => (
  <div className="dropdown-content">
    {data._embedded.notifications.length > 0 ? <NotificationList data={data} /> : <NoNotifications />}
  </div>
);

type SubscriptionProps = {
  link: string;
  refetch: () => Promise<NotificationCollection | undefined>;
};

const color = (notification: Notification) => {
  let c: string = notification.type.toLowerCase();
  if (c === "error") {
    c = "danger";
  }
  return c;
};

const useSuspendingNotification = (link: string, refetch: () => Promise<NotificationCollection | undefined>) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [disconnectedAt, setDisconnectedAt] = useState<Date>();

  const onVisible = useCallback(() => {
    return refetch().then(collection => {
      if (collection) {
        const received = collection._embedded.notifications.filter(n => {
          return disconnectedAt && disconnectedAt < new Date(n.createdAt);
        });
        if (received.length > 0) {
          setNotifications(previous => [...previous, ...received]);
        }
        setDisconnectedAt(undefined);
      }
    });
  }, [disconnectedAt, refetch]);

  const onHide = useCallback(() => {
    setDisconnectedAt(new Date());
    return Promise.resolve();
  }, []);

  const received = useCallback(
    (notification: Notification) => {
      setNotifications(previous => [...previous, notification]);
      refetch();
    },
    [refetch]
  );

  useEffect(() => {
    if (link) {
      let cancel: () => void;

      const disconnect = () => {
        if (cancel) {
          cancel();
        }
      };

      const connect = () => {
        disconnect();
        cancel = apiClient.subscribe(link, {
          notification: event => {
            received(JSON.parse(event.data));
          }
        });
      };

      const handleVisibilityChange = () => {
        if (document.visibilityState === "visible") {
          onVisible();
        } else {
          onHide();
        }
      };

      if (document.visibilityState === "visible") {
        connect();
      }

      document.addEventListener("visibilitychange", handleVisibilityChange);

      return () => {
        disconnect();
        document.removeEventListener("visibilitychange", handleVisibilityChange);
      };
    }
  }, [link, onVisible, onHide, received]);

  return {
    notifications,
    remove: (notification: Notification) => {
      setNotifications([...notifications.filter(n => n !== notification)]);
    },
    clear: () => setNotifications([])
  };
};

const NotificationSubscription: FC<SubscriptionProps> = ({ link, refetch }) => {
  const { notifications, remove } = useSuspendingNotification(link, refetch);
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

const Notifications: FC = () => {
  const { data, isLoading, error, refetch } = useNotifications();
  const subscribeLink = (data?._links["subscribe"] as LinkType)?.href;
  return (
    <>
      {subscribeLink ? <NotificationSubscription link={subscribeLink} refetch={refetch} /> : null}
      <div className="dropdown is-right is-hoverable">
        <Container className="dropdown-trigger">
          <Bell name="bell" color="white" />
          <Count>
            <span className="tag is-rounded">{data?._embedded.notifications.length || 0}</span>
          </Count>
        </Container>
        <DropDownMenu className="dropdown-menu" id="dropdown-menu" role="menu">
          <ErrorNotification error={error} />
          {isLoading ? <Loading /> : null}
          {data ? <NotificationDropDown data={data} /> : null}
        </DropDownMenu>
      </div>
    </>
  );
};

export default Notifications;
