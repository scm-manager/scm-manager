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
import UserForm from "../components/UserForm";
import DeleteUser from "./DeleteUser";
import { User } from "@scm-manager/ui-types";
import { ErrorNotification } from "@scm-manager/ui-components";
import UserConverter from "../components/UserConverter";
import { useUpdateUser } from "@scm-manager/ui-api";
import UpdateNotification from "../../components/UpdateNotification";

type Props = {
  user: User;
};

const EditUser: FC<Props> = ({ user }) => {
  const { error, isLoading, update, isUpdated } = useUpdateUser();

  return (
    <div>
      <UpdateNotification isUpdated={isUpdated} />
      <ErrorNotification error={error || undefined} />
      <UserForm submitForm={update} user={user} loading={isLoading} />
      <hr />
      <UserConverter user={user} />
      <DeleteUser user={user} />
    </div>
  );
};

export default EditUser;
