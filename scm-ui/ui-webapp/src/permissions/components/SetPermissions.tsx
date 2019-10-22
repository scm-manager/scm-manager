import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { Link } from "@scm-manager/ui-types";
import { Notification, ErrorNotification, SubmitButton } from "@scm-manager/ui-components";
import { getLink } from "../../modules/indexResource";
import { loadPermissionsForEntity, setPermissions } from "./handlePermissions";
import PermissionCheckbox from "./PermissionCheckbox";

type Props = {
  availablePermissionLink: string;
  selectedPermissionsLink: Link;

  // context props
  t: (p: string) => string;
};

type State = {
  permissions: {
    [key: string]: boolean;
  };
  loading: boolean;
  error?: Error;
  permissionsChanged: boolean;
  permissionsSubmitted: boolean;
  overwritePermissionsLink?: Link;
};

const PermissionsWrapper = styled.div`
  padding-bottom: 0;

  & .field .control {
    width: 100%;
    word-wrap: break-word;
  }
`;

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
      loading: true
    });
  };

  setErrorState = (error: Error) => {
    this.setState({
      error: error,
      loading: false
    });
  };

  setSuccessfulState = () => {
    this.setState({
      loading: false,
      error: undefined,
      permissionsSubmitted: true,
      permissionsChanged: false
    });
  };

  componentDidMount(): void {
    loadPermissionsForEntity(this.props.availablePermissionLink, this.props.selectedPermissionsLink.href).then(
      response => {
        const { permissions, overwriteLink } = response;
        this.setState({
          permissions: permissions,
          loading: false,
          overwritePermissionsLink: overwriteLink
        });
      }
    );
  }

  submit = (event: Event) => {
    event.preventDefault();
    if (this.state.permissions) {
      const { permissions } = this.state;
      this.setLoadingState();
      const selectedPermissions = Object.entries(permissions)
        .filter(e => e[1])
        .map(e => e[0]);
      if (this.state.overwritePermissionsLink) {
        setPermissions(this.state.overwritePermissionsLink.href, selectedPermissions)
          .then(result => {
            this.setSuccessfulState();
          })
          .catch(err => {
            this.setErrorState(err);
          });
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
          children={t("setPermissions.setPermissionsSuccessful")}
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
        <SubmitButton disabled={!this.state.permissionsChanged} loading={loading} label={t("setPermissions.button")} />
      </form>
    );
  }

  renderPermissions = () => {
    const { overwritePermissionsLink, permissions } = this.state;
    const permissionArray = Object.keys(permissions);
    return (
      <div className="columns">
        <PermissionsWrapper className={classNames("column", "is-half")}>
          {permissionArray.slice(0, permissionArray.length / 2 + 1).map(p => (
            <PermissionCheckbox
              key={p}
              permission={p}
              checked={permissions[p]}
              onChange={this.valueChanged}
              disabled={!overwritePermissionsLink}
            />
          ))}
        </PermissionsWrapper>
        <PermissionsWrapper className={classNames("column", "is-half")}>
          {permissionArray.slice(permissionArray.length / 2 + 1, permissionArray.length).map(p => (
            <PermissionCheckbox
              key={p}
              permission={p}
              checked={permissions[p]}
              onChange={this.valueChanged}
              disabled={!overwritePermissionsLink}
            />
          ))}
        </PermissionsWrapper>
      </div>
    );
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

export default connect(mapStateToProps)(translate("permissions")(SetPermissions));
