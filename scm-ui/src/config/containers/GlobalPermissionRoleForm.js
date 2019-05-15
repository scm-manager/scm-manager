// @flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import type { Role } from "@scm-manager/ui-types";
import { InputField, SubmitButton } from "@scm-manager/ui-components";
import PermissionCheckbox from "../../repos/permissions/components/PermissionCheckbox";
import {
  fetchAvailableVerbs,
  getFetchVerbsFailure,
  getVerbsFromState,
  isFetchVerbsPending
} from "../modules/roles";
import { getRepositoryVerbsLink } from "../../modules/indexResource";

type Props = {
  submitForm: CustomRoleRequest => void,
  role?: Role,
  loading?: boolean,
  availableVerbs: string[],
  selectedVerbs: string[],
  verbsLink: string,

  // context objects
  t: string => string,

  // dispatch functions
  fetchAvailableVerbs: (link: string) => void
};

type State = {
  role: Role,
  verbs: string[]
};

class GlobalPermissionRoleForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      role: {
        name: "",
        verbs: [],
        system: false,
        _links: {}
      },
      verbs: props.availableVerbs
    };
  }

  componentDidMount() {
    const { fetchAvailableVerbs, verbsLink, role } = this.props;
    fetchAvailableVerbs(verbsLink);

    if (role) {
      this.setState({ role: { ...role } });
    }
  }

  isFalsy(value) {
    return !value;
  }

  isValid = () => {
    const { role } = this.state;
    return !(
      this.isFalsy(role) ||
      this.isFalsy(role.name) ||
      this.isFalsy(role.verbs)
    );
  };

  handleNameChange = (name: string) => {
    this.setState({
      role: {
        ...this.state.role,
        name
      }
    });
  };

  handleVerbChange = (value: boolean, name: string) => {
    const { selectedVerbs } = this.props;
    const newVerbs = { ...selectedVerbs, [name]: value };
    this.setState({ verbs: newVerbs });
  };

  render() {
    const { loading, availableVerbs, t } = this.props;
    const { role, verbs } = this.state;

    const verbSelectBoxes =
      !!availableVerbs &&
      Object.entries(availableVerbs).map(e => (
        <PermissionCheckbox
          key={e[0]}
          // disabled={readOnly}
          name={e[0]}
          checked={e[1]}
          onChange={this.handleVerbChange}
        />
      ));

    return (
      <form onSubmit={this.submit}>
        <div className="columns">
          <div className="column">
            <InputField
              name="name"
              label={t("roles.create.name")}
              onChange={this.handleNameChange}
              value={role.name ? role.name : ""}
              disabled={!!role.name} // || disabled
            />
          </div>
        </div>
        <>{verbSelectBoxes}</>
        <div className="columns">
          <div className="column">
            <SubmitButton
              loading={loading}
              label={t("roles.create.submit")}
              //disabled={disabled || !this.isValid()}
            />
          </div>
        </div>
      </form>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const loading = isFetchVerbsPending(state);
  const error = getFetchVerbsFailure(state);
  const verbsLink = getRepositoryVerbsLink(state);
  const availableVerbs = getVerbsFromState(state);

  return {
    loading,
    error,
    verbsLink,
    availableVerbs
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchAvailableVerbs: (link: string) => {
      dispatch(fetchAvailableVerbs(link));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("roles")(GlobalPermissionRoleForm));
