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

import sys
from mercurial import util
from xml.dom.minidom import Document

pyVersion = sys.version_info
pyVersion = str(pyVersion.major) + "." + str(pyVersion.minor) + "." + str(pyVersion.micro)
hgVersion = util.version()

doc = Document()
root = doc.createElement('version')

pyNode = doc.createElement('python')
pyNode.appendChild(doc.createTextNode(pyVersion))
root.appendChild(pyNode)

hgNode = doc.createElement('mercurial')
hgNode.appendChild(doc.createTextNode(hgVersion))
root.appendChild(hgNode)

doc.appendChild(root)
doc.writexml(sys.stdout, encoding='UTF-8')