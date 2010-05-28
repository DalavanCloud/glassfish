/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.deployment.node.ws;

import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.node.*;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * This node represents port-component in weblogic-webservices.xml
 *
 * @author Rama Pulavarthi
 */
class WLWebServiceEndpointNode extends DeploymentDescriptorNode {
    private WebServiceEndpoint descriptor = null;

    private final static XMLElement tag =
            new XMLElement(WebServicesTagNames.PORT_COMPONENT);

    public WLWebServiceEndpointNode() {
        registerElementHandler(new XMLElement(WLWebServicesTagNames.WSDL),
                        WSDLNode.class);

    }

    @Override
    protected XMLElement getXMLRootTag() {
        return tag;
    }

    @Override
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(WLWebServicesTagNames.STREAM_ATTACHMENTS, "setStreamAttachments");
        table.put(WLWebServicesTagNames.VALIDATE_REQUEST, "setValidateRequest");
        return table;
    }

    @Override
    public void setElementValue(XMLElement element, String value) {
        String elementName = element.getQName();
        if (WebServicesTagNames.PORT_COMPONENT_NAME.equals(elementName)) {
            WebService webservice = (WebService) getParentNode().getDescriptor();
            descriptor = webservice.getEndpointByName(value);
        } else super.setElementValue(element, value);
    }

    @Override
    public XMLNode getHandlerFor(XMLElement element) {
        String elementName = element.getQName();
        DeploymentDescriptorNode node = null;
        if (WLWebServicesTagNames.WSDL.equals(elementName)) {
            node = new WSDLNode(descriptor);
            node.setParentNode(this);
        }
        return node;

    }

    @Override
    public Object getDescriptor() {
        return descriptor;
    }

    public Node writeDescriptor(Node parent, String nodeName,
                                WebServiceEndpoint descriptor) {
        Node wseNode = super.writeDescriptor(parent, nodeName, descriptor);

        appendTextChild(wseNode,
                WebServicesTagNames.PORT_COMPONENT_NAME,
                descriptor.getEndpointName());

        if (descriptor.getWsdlExposed() != null) {
            new WSDLNode(descriptor).writeDescriptor(wseNode, descriptor);
        }
        
        if (descriptor.getStreamAttachments() != null) {
            appendTextChild(wseNode,
                    WLWebServicesTagNames.STREAM_ATTACHMENTS,
                    descriptor.getStreamAttachments());
        }

        if (descriptor.getValidateRequest() != null) {
            appendTextChild(wseNode,
                    WLWebServicesTagNames.VALIDATE_REQUEST,
                    descriptor.getValidateRequest());

        }
        return wseNode;
    }

    /**
     * This node represents
     * <wsdl>
     * <exposed/>
     * </wsdl>
     * <p/>
     * inside port-component
     */
    public static class WSDLNode extends DeploymentDescriptorNode {
        private final XMLElement tag =
                new XMLElement(WLWebServicesTagNames.WSDL);
        WebServiceEndpoint descriptor;

        public WSDLNode(WebServiceEndpoint descriptor) {
            this.descriptor = descriptor;
        }


        protected XMLElement getXMLRootTag() {
            return tag;
        }

        public Object getDescriptor() {
            return descriptor;
        }

        protected Map getDispatchTable() {
            Map table = super.getDispatchTable();
            table.put(WLWebServicesTagNames.WSDL_EXPOSED, "setWsdlExposed");
            return table;
        }

        public Node writeDescriptor(Node parent, WebServiceEndpoint descriptor) {
            if (descriptor.getWsdlExposed() != null) {
                Document doc = getOwnerDocument(parent);
                Element wsdl = doc.createElement(WLWebServicesTagNames.WSDL);
                Element exposed = doc.createElement(WLWebServicesTagNames.WSDL_EXPOSED);
                exposed.appendChild(doc.createTextNode(descriptor.getWsdlExposed()));
                wsdl.appendChild(exposed);
                parent.appendChild(wsdl);
                return wsdl;
            }
            return null;
        }

    }
}
