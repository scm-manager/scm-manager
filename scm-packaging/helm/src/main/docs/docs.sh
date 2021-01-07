#!/bin/bash
#
# MIT License
#
# Copyright (c) 2020-present Cloudogu GmbH and Contributors
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#

set -euo pipefail
IFS=$'\n\t'

if helm-docs --version > /dev/null 2>&1; then
  SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
  MODULE_DIR="$( cd "${SCRIPT_DIR}/../../../" >/dev/null 2>&1 && pwd )"

  CHARTDIR="${MODULE_DIR}/target/chart"
  if [ ! -d "${CHARTDIR}" ]; then
    echo "chart directory ${CHARTDIR} does not exists"
    echo "please run maven package before"
    echo "https://github.com/norwoodj/helm-docs"
    exit 1
  fi

  cd "${CHARTDIR}"
  PAGE="docs/en/installation/k8s.md"
  echo helm-docs --template-files="${MODULE_DIR}/src/main/docs/helm.md.gotmpl" --output-file="${MODULE_DIR}/../../${PAGE}"
  helm-docs --template-files="../../src/main/docs/helm.md.gotmpl" --output-file="../../../../${PAGE}"
  echo "The helm documentation was successfully generated"
  echo "Please check the results at ${PAGE}"
  echo "And don't forget to commit ;)"

else
  echo "helm-docs not found"
  echo "please install helm-docs, before running this script"
  echo "https://github.com/norwoodj/helm-docs"
  exit 1
fi
