import sys
from mercurial import util
from xml.dom.minidom import Document

pyVersion = sys.version_info
pyVersion = str(pyVersion.major) + "." + str(pyVersion.minor) + "." + str(pyVersion.micro)
hgVersion = util.version()

doc = Document()
root = doc.createElement('verion')

pyNode = doc.createElement('python')
pyNode.appendChild(doc.createTextNode(pyVersion))
root.appendChild(pyNode)

hgNode = doc.createElement('mercurial')
hgNode.appendChild(doc.createTextNode(hgVersion))
root.appendChild(hgNode)

doc.appendChild(root)
doc.writexml(sys.stdout, encoding='UTF-8')