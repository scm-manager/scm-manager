// @flow
import React from "react";
import Dropzone from "react-dropzone";
import { translate } from "react-i18next";
import injectSheet from "react-jss";
import classNames from "classnames";

const styles = {
  dropzone: {
    width: "100%",
    height: "20rem",
    border: "solid 1px #eeeeee",
    borderRadius: "2px"
  },
  innerBorder: {
    margin: "2rem",
    height: "16rem",
    alignSelf: "center",
    border: "dashed 3px #f5f5f5",
    display: "flex",
    justifyContent: "center"
  },
  description : {
    display: "flex",
    flexDirection: "column",
    justifyContent: "center",
    alignItems: "center"
  },
  icon : {
    margin: "1rem 0rem"
  }
};

type Props = {
  // context props
  t: string => string,
  classes: any
}

type State = {
  acceptedFiles: any
}

class FileUploadDropzone extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {
      acceptedFiles: null
    };
  }

   onDrop = (acceptedFiles ) => {
    this.setState({ acceptedFiles });
  };

  render() {
    const { t, classes } = this.props;
    const { acceptedFiles } = this.state;

    return (
      <>
        <Dropzone onDrop={this.onDrop}>
          {({getRootProps, getInputProps}) => (
            <section>
              <div {...getRootProps()}>
                <input {...getInputProps()} />
                <div className={classes.dropzone}>
                  <div className={classes.innerBorder}>
                    <div className={classNames(classes.description, "has-text-grey-light")}>
                      {t("fileUpload.clickHere")}
                      <i className={classNames("fas fa-plus-circle fa-2x has-text-grey-lighter", classes.icon)}/>
                      {t("fileUpload.dragAndDrop")}
                    </div>
                  </div>
                </div>
              </div>
            </section>
          )}
        </Dropzone>
        <div>
          <p>{acceptedFiles && acceptedFiles[0].name}</p>
        </div>
      </>
    );
  }
}

export default injectSheet(styles)(translate("repos")(FileUploadDropzone));
