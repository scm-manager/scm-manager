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
import React from "react";
import { connect } from "react-redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { RepositoryRole } from "@scm-manager/ui-types";
import { InputField, Level, SubmitButton } from "@scm-manager/ui-components";
import { getRepositoryRolesLink, getRepositoryVerbsLink } from "../../../modules/indexResource";
import { fetchAvailableVerbs, getFetchVerbsFailure, getVerbsFromState, isFetchVerbsPending } from "../modules/roles";
import PermissionsWrapper from "../../../permissions/components/PermissionsWrapper";
import { compose } from "redux";

type Props = WithTranslation & {
  role?: RepositoryRole;
  loading?: boolean;
  availableVerbs: string[];
  verbsLink: string;
  submitForm: (p: RepositoryRole) => void;

  // dispatch functions
  fetchAvailableVerbs: (link: string) => void;
};

type State = {
  role: RepositoryRole;
};

class RepositoryRoleForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      role: {
        name: "",
        verbs: [],
        system: false,
        _links: {}
      }
    };
  }

  componentDidMount() {
    const { fetchAvailableVerbs, verbsLink } = this.props;
    fetchAvailableVerbs(verbsLink);
    if (this.props.role) {
      this.setState({
        role: this.props.role
      });
    }
  }

  isFalsy(value) {
    return !value;
  }

  isValid = () => {
    const { role } = this.state;
    return !(this.isFalsy(role) || this.isFalsy(role.name) || this.isFalsy(role.verbs.length > 0));
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
    const { role } = this.state;

    const newVerbs = value ? [...role.verbs, name] : role.verbs.filter(v => v !== name);

    this.setState({
      ...this.state,
      role: {
        ...role,
        verbs: newVerbs
      }
    });
  };

  submit = (event: Event) => {
    event.preventDefault();
    if (this.isValid()) {
      this.props.submitForm(this.state.role);
    }
  };

  render() {
    const { loading, availableVerbs, t } = this.props;
    const { role } = this.state;

    const selectedVerbs = availableVerbs
      ? availableVerbs.reduce((obj, verb) => ({ ...obj, [verb]: role.verbs.includes(verb) }), {})
      : {};

    return (
      <form onSubmit={this.submit}>
        <InputField
          name="name"
          label={t("repositoryRole.form.name")}
          onChange={this.handleNameChange}
          value={role.name ? role.name : ""}
          disabled={!!this.props.role}
        />
        <div className="field">
          <label className="label">{t("repositoryRole.form.permissions")}</label>
          <PermissionsWrapper
            permissions={selectedVerbs}
            onChange={this.handleVerbChange}
            disabled={false}
            role={true}
          />
        </div>
        <hr />
        <Level
          right={<SubmitButton loading={loading} label={t("repositoryRole.form.submit")} disabled={!this.isValid()} />}
        />
      </form>
    );
  }
}

const mapStateToProps = (state: any) => {
  const loading = isFetchVerbsPending(state);
  const error = getFetchVerbsFailure(state);
  const verbsLink = getRepositoryVerbsLink(state);
  const availableVerbs = getVerbsFromState(state);
  const repositoryRolesLink = getRepositoryRolesLink(state);

  return {
    loading,
    error,
    verbsLink,
    availableVerbs,
    repositoryRolesLink
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchAvailableVerbs: (link: string) => {
      dispatch(fetchAvailableVerbs(link));
    }
  };
};

export default compose(connect(mapStateToProps, mapDispatchToProps), withTranslation("admin"))(RepositoryRoleForm);
