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
import { WithTranslation, withTranslation } from "react-i18next";
import { Button, ButtonGroup, Modal, SubmitButton } from "@scm-manager/ui-components";
import PermissionCheckbox from "../../../permissions/components/PermissionCheckbox";

type Props = WithTranslation & {
  readOnly: boolean;
  availableVerbs: string[];
  selectedVerbs: string[];
  onSubmit: (p: string[]) => void;
  onClose: () => void;
};

type State = {
  verbs: any;
};

class AdvancedPermissionsDialog extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    const verbs = {};
    props.availableVerbs.forEach(
      verb => (verbs[verb] = props.selectedVerbs ? props.selectedVerbs.includes(verb) : false)
    );
    this.state = {
      verbs
    };
  }

  render() {
    const { t, onClose, readOnly } = this.props;
    const { verbs } = this.state;

    const verbSelectBoxes = Object.entries(verbs).map(e => (
      <PermissionCheckbox
        key={e[0]}
        name={e[0]}
        checked={e[1]}
        onChange={this.handleChange}
        disabled={readOnly}
        role={true}
      />
    ));

    const submitButton = !readOnly ? <SubmitButton label={t("permission.advanced.dialog.submit")} /> : null;

    const body = <>{verbSelectBoxes}</>;

    const footer = (
      <form onSubmit={this.onSubmit}>
        <ButtonGroup>
          {submitButton}
          <Button label={t("permission.advanced.dialog.abort")} action={onClose} />
        </ButtonGroup>
      </form>
    );

    return (
      <Modal
        title={t("permission.advanced.dialog.title")}
        closeFunction={() => onClose()}
        body={body}
        footer={footer}
        active={true}
      />
    );
  }

  handleChange = (value: boolean, name: string) => {
    const { verbs } = this.state;
    const newVerbs = {
      ...verbs,
      [name]: value
    };
    this.setState({
      verbs: newVerbs
    });
  };

  onSubmit = () => {
    this.props.onSubmit(
      Object.entries(this.state.verbs)
        .filter(e => e[1])
        .map(e => e[0])
    );
  };
}

export default withTranslation("repos")(AdvancedPermissionsDialog);
