// @flow

import React from "react";
import { Button, SubmitButton } from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import PermissionCheckbox from "../components/PermissionCheckbox";

type Props = {
  readOnly: boolean,
  availableVerbs: string[],
  selectedVerbs: string[],
  onSubmit: (string[]) => void,
  onClose: () => void,

  // context props
  t: string => string
};

type State = {
  verbs: any
};

class AdvancedPermissionsDialog extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    const verbs = {};
    props.availableVerbs.forEach(
      verb => (verbs[verb] = props.selectedVerbs.includes(verb))
    );

    this.state = { verbs };
  }

  render() {
    const { t, onClose, readOnly } = this.props;
    const { verbs } = this.state;

    const verbSelectBoxes = Object.entries(verbs).map(e => (
      <PermissionCheckbox
        disabled={readOnly}
        name={e[0]}
        checked={e[1]}
        onChange={this.handleChange}
      />
    ));

    const submitButton = !readOnly ? (
      <SubmitButton label={t("permission.advanced.dialog.submit")} />
    ) : null;

    return (
      <div className={"modal is-active"}>
        <div className="modal-background" />
        <div className="modal-card">
          <header className="modal-card-head">
            <p className="modal-card-title">
              {t("permission.advanced.dialog.title")}
            </p>
            <button
              className="delete"
              aria-label="close"
              onClick={() => onClose()}
            />
          </header>
          <section className="modal-card-body">
            <div className="content">{verbSelectBoxes}</div>
            <form onSubmit={this.onSubmit}>
              {submitButton}
              <Button
                label={t("permission.advanced.dialog.abort")}
                action={onClose}
              />
            </form>
          </section>
        </div>
      </div>
    );
  }

  handleChange = (value: boolean, name: string) => {
    const { verbs } = this.state;
    const newVerbs = { ...verbs, [name]: value };
    this.setState({ verbs: newVerbs });
  };

  onSubmit = () => {
    this.props.onSubmit(
      Object.entries(this.state.verbs)
        .filter(e => e[1])
        .map(e => e[0])
    );
  };
}

export default translate("repos")(AdvancedPermissionsDialog);
