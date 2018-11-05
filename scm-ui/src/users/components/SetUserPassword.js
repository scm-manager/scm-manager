// @flow
import React from "react";
import type { User } from "@scm-manager/ui-types";

type Props = {
  user: User
};

export default class SetUserPassword extends React.Component<Props> {

  render() {

    return (
      "Hey, Change Password!"
    );
  }
}
