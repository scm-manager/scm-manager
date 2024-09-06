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
import { Group } from "@scm-manager/ui-types";
import { useUpdateGroup, useUserSuggestions } from "@scm-manager/ui-api";
import { ErrorNotification } from "@scm-manager/ui-components";
import GroupForm from "../components/GroupForm";
import DeleteGroup from "./DeleteGroup";
import UpdateNotification from "../../components/UpdateNotification";

type Props = {
  group: Group;
};

const EditGroup: FC<Props> = ({ group }) => {
  const { error, isLoading, update, isUpdated } = useUpdateGroup();

  return (
    <div>
      <UpdateNotification isUpdated={isUpdated} />
      <ErrorNotification error={error || undefined} />
      <GroupForm group={group} submitForm={update} loading={isLoading} />
      <DeleteGroup group={group} />
    </div>
  );
};

export default EditGroup;
