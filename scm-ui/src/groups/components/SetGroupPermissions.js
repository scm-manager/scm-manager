// @flow
import React from "react";
import type { Group } from "@scm-manager/ui-types";
import {
  Notification,
  ErrorNotification,
  SubmitButton
} from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import { setPermissions } from "./setPermissions";
import { apiClient } from "@scm-manager/ui-components";
import PermissionCheckbox from "../../users/components/PermissionCheckbox";
import { connect } from "react-redux";
import { getLink } from "../../modules/indexResource";

type Props = {
  group: Group,
  t: string => string,
  permissionLink: string
};

type State = {
  permissions: { [string]: boolean },
  loading: boolean,
  error?: Error,
  permissionsChanged: boolean,
  permissionsSubmitted: boolean,
  modifiable: boolean
};

class SetGroupPermissions extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      permissions: { perm1: false, perm2: false },
      loading: true,
      permissionsChanged: false,
      permissionsSubmitted: false,
      modifiable: false
    };
  }

  setLoadingState = () => {
    this.setState({
      ...this.state,
      loading: true
    });
  };

  setErrorState = (error: Error) => {
    this.setState({
      ...this.state,
      error: error,
      loading: false
    });
  };

  setSuccessfulState = () => {
    this.setState({
      ...this.state,
      loading: false,
      permissionsSubmitted: true,
      permissionsChanged: false
    });
  };

  componentDidMount(): void {
    apiClient
      .get(this.props.permissionLink)
      .then(response => {
        return response.json();
      })
      .then(response => {
        const availablePermissions = response.permissions;
        const permissions = {};
        availablePermissions.forEach(p => {
          permissions[p] = false;
        });
        this.setState({ permissions }, this.loadPermissionsForGroup);
      });
  }

  loadPermissionsForGroup = () => {
    apiClient
      .get(this.props.group._links.permissions.href)
      .then(response => {
        return response.json();
      })
      .then(response => {
        const checkedPermissions = response.permissions;
        const modifiable = !!response._links.overwrite;
        this.setState(state => {
          const newPermissions = state.permissions;
          checkedPermissions.forEach(name => (newPermissions[name] = true));
          return {
            loading: false,
            modifiable: modifiable,
            permissions: newPermissions
          };
        });
      });
  };

  submit = (event: Event) => {
    event.preventDefault();
    if (this.state.permissions) {
      const { group } = this.props;
      const { permissions } = this.state;
      this.setLoadingState();
      const selectedPermissions = Object.entries(permissions)
        .filter(e => e[1])
        .map(e => e[0]);
      setPermissions(group._links.permissions.href, selectedPermissions)
        .then(result => {
          if (result.error) {
            this.setErrorState(result.error);
          } else {
            this.setSuccessfulState();
          }
        })
        .catch(err => {});
    }
  };

  render() {
    const { t } = this.props;
    const { loading, permissionsSubmitted, error } = this.state;

    let message = null;

    if (permissionsSubmitted) {
      message = (
        <Notification
          type={"success"}
          children={t("permissions.set-permissions-successful")}
          onClose={() => this.onClose()}
        />
      );
    } else if (error) {
      message = <ErrorNotification error={error} />;
    }

    return (
      <form onSubmit={this.submit}>
        {message}
        {this.renderPermissions()}
        <SubmitButton
          disabled={!this.state.permissionsChanged}
          loading={loading}
          label={t("group-form.submit")}
        />
      </form>
    );
  }

  renderPermissions = () => {
    const { modifiable, permissions } = this.state;
    return Object.keys(permissions).map(p => (
      <div key={p}>
        <PermissionCheckbox
          permission={p}
          checked={permissions[p]}
          onChange={this.valueChanged}
          disabled={!modifiable}
        />
      </div>
    ));
  };

  valueChanged = (value: boolean, name: string) => {
    this.setState(state => {
      const newPermissions = state.permissions;
      newPermissions[name] = value;
      return {
        permissions: newPermissions,
        permissionsChanged: true
      };
    });
  };

  onClose = () => {
    this.setState({
      permissionsSubmitted: false
    });
  };
}

const mapStateToProps = state => {
  const permissionLink = getLink(state, "permissions");
  return {
    permissionLink
  };
};

export default connect(mapStateToProps)(
  translate("groups")(SetGroupPermissions)
);
