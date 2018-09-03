// @flow
import React from "react";
import type { Permission } from "../types/Permissions";
import { Checkbox } from "../../components/forms/index";
import { translate } from "react-i18next";
import {
  modifyPermission,
  isModifyPermissionPending,
  getModifyPermissionFailure,
  modifyPermissionReset,
  deletePermission,
  getDeletePermissionFailure,
  isDeletePermissionPending,
  deletePermissionReset
} from "../modules/permissions";
import connect from "react-redux/es/connect/connect";
import { withRouter } from "react-router-dom";
import type { History } from "history";
import ErrorNotification from "../../components/ErrorNotification";
import DeletePermissionButton from "../components/buttons/DeletePermissionButton";
import TypeSelector from "../components/TypeSelector";

type Props = {
  submitForm: Permission => void,
  modifyPermission: (Permission, string, string) => void,
  permission: Permission,
  t: string => string,
  namespace: string,
  repoName: string,
  match: any,
  history: History,
  loading: boolean,
  error: Error,
  permissionReset: (string, string, string) => void,
  deletePermissionReset: (string, string, string) => void,
  deletePermission: (Permission, string, string) => void,
  deleteLoading: boolean
};

type State = {
  permission: Permission
};

class SinglePermission extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      permission: {
        name: "",
        type: "READ",
        groupPermission: false,
        _links: {}
      }
    };
  }

  componentDidMount() {
    const { permission } = this.props;
    this.props.permissionReset(
      this.props.namespace,
      this.props.repoName,
      permission.name
    );
    this.props.deletePermissionReset(
      this.props.namespace,
      this.props.repoName,
      permission.name
    );
    if (permission) {
      this.setState({
        permission: {
          name: permission.name,
          type: permission.type,
          groupPermission: permission.groupPermission,
          _links: permission._links
        }
      });
    }
  }

  deletePermission = () => {
    this.props.deletePermission(
      this.props.permission,
      this.props.namespace,
      this.props.repoName
    );
  };

  render() {
    const { permission } = this.state;
    const { loading, error, namespace, repoName } = this.props;
    const typeSelector = this.props.permission._links.update ? (
      <td>
        <TypeSelector
          handleTypeChange={this.handleTypeChange}
          type={permission.type ? permission.type : "READ"}
          loading={loading}
        />
      </td>
    ) : (
      <td>{permission.type}</td>
    );

    const errorNotification = error ? (
      <ErrorNotification error={error} />
    ) : null;

    return (
      <tr>
        <td>{permission.name}</td>
        <td>
          <Checkbox checked={permission ? permission.groupPermission : false} />
        </td>
        {typeSelector}
        <td>
          <DeletePermissionButton
            permission={permission}
            namespace={namespace}
            repoName={repoName}
            deletePermission={this.deletePermission}
            loading={this.props.deleteLoading}
          />
          {errorNotification}
        </td>
      </tr>
    );
  }

  handleTypeChange = (type: string) => {
    this.setState({
      permission: {
        ...this.state.permission,
        type: type
      }
    });
    this.modifyPermission(type);
  };

  modifyPermission = (type: string) => {
    let permission = this.state.permission;
    permission.type = type;
    this.props.modifyPermission(
      permission,
      this.props.namespace,
      this.props.repoName
    );
  };

  createSelectOptions(types: string[]) {
    return types.map(type => {
      return {
        label: type,
        value: type
      };
    });
  }
}

const mapStateToProps = (state, ownProps) => {
  const permission = ownProps.permission;
  const loading = isModifyPermissionPending(
    state,
    ownProps.namespace,
    ownProps.repoName,
    permission.name
  );
  const error =
    getModifyPermissionFailure(
      state,
      ownProps.namespace,
      ownProps.repoName,
      permission.name
    ) ||
    getDeletePermissionFailure(
      state,
      ownProps.namespace,
      ownProps.repoName,
      permission.name
    );
  const deleteLoading = isDeletePermissionPending(
    state,
    ownProps.namespace,
    ownProps.repoName,
    permission.name
  );

  return { loading, error, deleteLoading };
};

const mapDispatchToProps = dispatch => {
  return {
    modifyPermission: (
      permission: Permission,
      namespace: string,
      repoName: string
    ) => {
      dispatch(modifyPermission(permission, namespace, repoName));
    },
    permissionReset: (
      namespace: string,
      repoName: string,
      permissionname: string
    ) => {
      dispatch(modifyPermissionReset(namespace, repoName, permissionname));
    },
    deletePermission: (
      permission: Permission,
      namespace: string,
      repoName: string
    ) => {
      dispatch(deletePermission(permission, namespace, repoName));
    },
    deletePermissionReset: (
      namespace: string,
      repoName: string,
      permissionname: string
    ) => {
      dispatch(deletePermissionReset(namespace, repoName, permissionname));
    }
  };
};
export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("permissions")(withRouter(SinglePermission)));
