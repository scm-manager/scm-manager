// @flow
import React from "react";
import type { Link } from "@scm-manager/ui-types";
import {
  Notification,
  ErrorNotification,
  SubmitButton
} from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import { setPermissions } from "./setPermissions";
import { apiClient } from "@scm-manager/ui-components";
import PermissionCheckbox from "./PermissionCheckbox";
import { connect } from "react-redux";
import { getLink } from "../../modules/indexResource";

type Props = {
  t: string => string,
  availablePermissionLink: string,
  selectedPermissionsLink: Link
};

type State = {
  permissions: { [string]: boolean },
  loading: boolean,
  error?: Error,
  permissionsChanged: boolean,
  permissionsSubmitted: boolean,
  overwritePermissionsLink?: Link
};

class SetPermissions extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      permissions: {},
      loading: true,
      permissionsChanged: false,
      permissionsSubmitted: false,
      modifiable: false,
      overwritePermissionsLink: undefined
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
      .get(this.props.availablePermissionLink)
      .then(response => {
        return response.json();
      })
      .then(response => {
        const availablePermissions = response.permissions;
        const permissions = {};
        availablePermissions.forEach(p => {
          permissions[p] = false;
        });
        this.setState({ permissions }, this.loadPermissionsForUser);
      });
  }

  loadPermissionsForUser = () => {
    apiClient
      .get(this.props.selectedPermissionsLink.href)
      .then(response => {
        return response.json();
      })
      .then(response => {
        const checkedPermissions = response.permissions;
        this.setState(state => {
          const newPermissions = state.permissions;
          checkedPermissions.forEach(name => (newPermissions[name] = true));
          return {
            loading: false,
            permissions: newPermissions,
            overwritePermissionsLink: response._links.overwrite
          };
        });
      });
  };

  submit = (event: Event) => {
    event.preventDefault();
    if (this.state.permissions) {
      const { permissions } = this.state;
      this.setLoadingState();
      const selectedPermissions = Object.entries(permissions)
        .filter(e => e[1])
        .map(e => e[0]);
      if (this.state.overwritePermissionsLink) {
        setPermissions(
          this.state.overwritePermissionsLink.href,
          selectedPermissions
        )
          .then(result => {
            if (result.error) {
              this.setErrorState(result.error);
            } else {
              this.setSuccessfulState();
            }
          })
          .catch(err => {});
      }
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
          children={t("form.set-permissions-successful")}
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
          label={t("form.submit-button.label")}
        />
      </form>
    );
  }

  renderPermissions = () => {
    const { overwritePermissionsLink, permissions } = this.state;
    return Object.keys(permissions).map(p => (
      <div key={p}>
        <PermissionCheckbox
          permission={p}
          checked={permissions[p]}
          onChange={this.valueChanged}
          disabled={!overwritePermissionsLink}
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
  const availablePermissionLink = getLink(state, "permissions");
  return {
    availablePermissionLink
  };
};

export default connect(mapStateToProps)(
  translate("permissions")(SetPermissions)
);
