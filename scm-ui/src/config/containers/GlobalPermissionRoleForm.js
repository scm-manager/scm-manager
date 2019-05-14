// @flow
import React from "react";
import { InputField, SubmitButton } from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import PermissionCheckbox from "../../repos/permissions/components/PermissionCheckbox";
import {
  fetchAvailableVerbs,
  getFetchVerbsFailure,
  getVerbsFromState,
  isFetchVerbsPending
} from "../modules/roles";
import { getRepositoryVerbsLink } from "../../modules/indexResource";
import { connect } from "react-redux";

type Props = {
  submitForm: CustomRoleRequest => void,
  transmittedName?: string,
  loading?: boolean,
  availableVerbs: string[],
  selectedVerbs: string[],
  verbsLink: string,
  t: string => string,

  // dispatch functions
  fetchAvailableVerbs: (link: string) => void
};

type State = {
  name?: string,
  verbs: string[]
};

class GlobalPermissionRoleForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      name: props.transmittedName ? props.transmittedName : "",
      verbs: props.availableVerbs
    };
  }

  componentWillMount() {
    const { fetchAvailableVerbs, verbsLink } = this.props;
    fetchAvailableVerbs(verbsLink);
  }

  componentDidMount() {
    const {
      fetchAvailableVerbs,
      verbsLink,
    } = this.props;
    fetchAvailableVerbs(verbsLink);

  }

  isValid = () => {
    const { name, verbs } = this.state;
    return !(this.isFalsy(name) || this.isFalsy(verbs) || verbs.isEmpty());
  };

  isFalsy(value) {
    return !value;
  }

  handleNameChange = (name: string) => {
    this.setState({
      ...this.state,
      name
    });
  };

  handleVerbChange = (value: boolean, name: string) => {
    const { selectedVerbs } = this.state;
    const newVerbs = { ...selectedVerbs, [name]: value };
    this.setState({ selectedVerbs: newVerbs });
  };

  render() {
    const {
      t,
      transmittedName,
      loading,
      disabled,
      availableVerbs
    } = this.props;
    const { verbs } = this.state;

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
      <div>
        <form onSubmit={this.submit}>
          <div className="columns">
            <div className="column">
              <InputField
                name="name"
                label={t("roles.create.name")}
                onChange={this.handleNameChange}
                value={name ? name : ""}
                disabled={!!transmittedName || disabled}
              />
            </div>
          </div>
          <>{verbSelectBoxes}</>
          <div className="columns">
            <div className="column">
              <SubmitButton
                disabled={disabled || !this.isValid()}
                loading={loading}
                label={t("roles.create.submit")}
              />
            </div>
          </div>
        </form>
      </div>
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
