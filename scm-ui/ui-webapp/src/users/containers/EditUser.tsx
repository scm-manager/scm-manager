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
import DeleteUser from "./DeleteUser";
import { User } from "@scm-manager/ui-types";
import UserConverter from "../components/UserConverter";
import { Form, useUpdateResource } from "@scm-manager/ui-forms";
import * as userValidator from "../components/userValidation";
import { Subtitle } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  user: User;
};

const EditUser: FC<Props> = ({ user }) => {
  const [t] = useTranslation("users");
  const { submit } = useUpdateResource<User>(user, (user) => user.name, {
    contentType: "application/vnd.scmm-user+json;v=2",
    collectionName: ["user", "users"],
  });

  return (
    <>
      <Subtitle subtitle={t("userForm.subtitle")} />
      <Form translationPath={["users", "createUser.form"]} defaultValues={user} onSubmit={submit}>
        <Form.Row>
          <Form.Input
            name="displayName"
            rules={{
              validate: userValidator.isDisplayNameValid,
            }}
          />
          <Form.Input
            name="mail"
            className="is-half"
            rules={{
              validate: (email) => !email || userValidator.isMailValid(email),
            }}
          />
        </Form.Row>
        <Form.Row hidden={user.external}>
          <Form.Checkbox name="active" />
        </Form.Row>
      </Form>
      <hr />
      <UserConverter user={user} />
      <DeleteUser user={user} />
    </>
  );
};

export default EditUser;
