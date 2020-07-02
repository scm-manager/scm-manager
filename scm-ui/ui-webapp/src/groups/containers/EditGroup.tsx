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
import GroupForm from "../components/GroupForm";
import { getModifyGroupFailure, isModifyGroupPending, modifyGroup, modifyGroupReset } from "../modules/groups";
import { History } from "history";
import { withRouter } from "react-router-dom";
import { DisplayedUser, Group } from "@scm-manager/ui-types";
import { ErrorNotification } from "@scm-manager/ui-components";
import { getUserAutoCompleteLink } from "../../modules/indexResource";
import DeleteGroup from "./DeleteGroup";
import { apiClient } from "@scm-manager/ui-components";
import { compose } from "redux";

type Props = {
  group: Group;
  fetchGroup: (name: string) => void;
  modifyGroup: (group: Group, callback?: () => void) => void;
  modifyGroupReset: (p: Group) => void;
  autocompleteLink: string;
  history: History;
  loading?: boolean;
  error: Error;
};

class EditGroup extends React.Component<Props> {
  componentDidMount() {
    const { group, modifyGroupReset } = this.props;
    modifyGroupReset(group);
  }

  groupModified = (group: Group) => () => {
    this.props.history.push(`/group/${group.name}`);
  };

  modifyGroup = (group: Group) => {
    this.props.modifyGroup(group, this.groupModified(group));
  };

  loadUserAutocompletion = (inputValue: string) => {
    const url = this.props.autocompleteLink + "?q=";
    return apiClient
      .get(url + inputValue)
      .then(response => response.json())
      .then(json => {
        return json.map((element: DisplayedUser) => {
          return {
            value: element,
            label: `${element.displayName} (${element.id})`
          };
        });
      });
  };

  render() {
    const { loading, error, group } = this.props;
    return (
      <div>
        <ErrorNotification error={error} />
        <GroupForm
          group={group}
          submitForm={group => {
            this.modifyGroup(group);
          }}
          loading={loading}
          loadUserSuggestions={this.loadUserAutocompletion}
        />
        <DeleteGroup group={group} />
      </div>
    );
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const loading = isModifyGroupPending(state, ownProps.group.name);
  const error = getModifyGroupFailure(state, ownProps.group.name);
  const autocompleteLink = getUserAutoCompleteLink(state);
  return {
    loading,
    error,
    autocompleteLink
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    modifyGroup: (group: Group, callback?: () => void) => {
      dispatch(modifyGroup(group, callback));
    },
    modifyGroupReset: (group: Group) => {
      dispatch(modifyGroupReset(group));
    }
  };
};

export default compose(connect(mapStateToProps, mapDispatchToProps), withRouter)(EditGroup);
