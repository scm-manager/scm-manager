//@flow
import React from "react";
import { translate } from "react-i18next";
import { RemoveEntryOfTableButton } from "../../../components/buttons";

type Props = {
  proxyExcludes: string[],
  t: string => string,
  onChange: (boolean, any, string) => void,
  disabled: boolean
};

type State = {};

class ProxyExcludesTable extends React.Component<Props, State> {
  render() {
    const { t } = this.props;
    return (
      <div>
        <label className="label">{t("proxy-settings.proxy-excludes")}</label>
        <table className="table is-hoverable is-fullwidth">
          <tbody>
            {this.props.proxyExcludes.map(excludes => {
              return (
                <tr key={excludes}>
                  <td key={excludes}>{excludes}</td>
                  <td>
                    <RemoveEntryOfTableButton
                      entryname={excludes}
                      removeEntry={this.removeEntry}
                      disabled={this.props.disabled}
                      label={t("proxy-settings.remove-proxy-exclude-button")}
                    />
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    );
  }

  removeEntry = (excludename: string) => {
    const newExcludes = this.props.proxyExcludes.filter(
      name => name !== excludename
    );
    this.props.onChange(true, newExcludes, "proxyExcludes");
  };
}

export default translate("config")(ProxyExcludesTable);
