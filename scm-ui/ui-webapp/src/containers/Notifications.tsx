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

import React, { FC} from "react";
import { Icon } from "@scm-manager/ui-components";
import styled from "styled-components";
import { useNotifications } from "@scm-manager/ui-api";
import { Notification, NotificationCollection } from "@scm-manager/ui-types";
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
  word-wrap: inherit;
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
  let color: string = notification.type.toLowerCase();
  if (color === "error") {
    color = "danger";
  }
  return <Icon name="bell" color={color} />;
};

const NotificationEntry: FC<EntryProps> = ({ notification }) => (
  <DropDownItem to="/" className="dropdown-item">
    <NotificationIcon notification={notification} /> {notification.message}
  </DropDownItem>
);

const NotificationList: FC<Props> = ({ data }) => (
  <div className="dropdown-content">
    {data._embedded.notifications.map((n, i) => (
      <NotificationEntry key={i} notification={n} />
    ))}
  </div>
);

const Notifications: FC = ({}) => {
  const { data } = useNotifications();

  return (
    <div className="dropdown is-right is-hoverable">
      <Container className="dropdown-trigger">
        <Bell name="bell" color="white" />
        <Count>
          <span className="tag is-rounded">{data?._embedded.notifications.length || 0}</span>
        </Count>
      </Container>
      <DropDownMenu className="dropdown-menu" id="dropdown-menu" role="menu">
        {data ? <NotificationList data={data} /> : null}
      </DropDownMenu>
    </div>
  );
};

export default Notifications;
