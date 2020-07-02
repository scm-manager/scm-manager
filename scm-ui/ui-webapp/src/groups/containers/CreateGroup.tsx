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
import { compose } from "redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { History } from "history";
import { DisplayedUser, Group } from "@scm-manager/ui-types";
import { Page } from "@scm-manager/ui-components";
import { getGroupsLink, getUserAutoCompleteLink } from "../../modules/indexResource";
import { createGroup, createGroupReset, getCreateGroupFailure, isCreateGroupPending } from "../modules/groups";
import GroupForm from "../components/GroupForm";
import { apiClient } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  createGroup: (link: string, group: Group, callback?: () => void) => void;
  history: History;
  loading?: boolean;
  error?: Error;
  resetForm: () => void;
  createLink: string;
  autocompleteLink: string;
};

class CreateGroup extends React.Component<Props> {
  componentDidMount() {
    this.props.resetForm();
  }

  render() {
    const { t, loading, error } = this.props;
    return (
      <Page title={t("add-group.title")} subtitle={t("add-group.subtitle")} error={error}>
        <div>
          <GroupForm
            submitForm={group => this.createGroup(group)}
            loading={loading}
            loadUserSuggestions={this.loadUserAutocompletion}
          />
        </div>
      </Page>
    );
  }

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
  groupCreated = (group: Group) => {
    this.props.history.push("/group/" + group.name);
  };
  createGroup = (group: Group) => {
    this.props.createGroup(this.props.createLink, group, () => this.groupCreated(group));
  };
}

const mapDispatchToProps = (dispatch: any) => {
  return {
    createGroup: (link: string, group: Group, callback?: () => void) => dispatch(createGroup(link, group, callback)),
    resetForm: () => {
      dispatch(createGroupReset());
    }
  };
};

const mapStateToProps = (state: any) => {
  const loading = isCreateGroupPending(state);
  const error = getCreateGroupFailure(state);
  const createLink = getGroupsLink(state);
  const autocompleteLink = getUserAutoCompleteLink(state);
  return {
    createLink,
    loading,
    error,
    autocompleteLink
  };
};

export default compose(connect(mapStateToProps, mapDispatchToProps), withTranslation("groups"))(CreateGroup);
