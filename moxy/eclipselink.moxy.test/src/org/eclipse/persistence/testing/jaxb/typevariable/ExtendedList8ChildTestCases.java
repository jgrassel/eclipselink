/*******************************************************************************
 * Copyright (c) 2013, 2015  Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Blaise Doughan - 2.5.1 - initial implementation
 ******************************************************************************/
package org.eclipse.persistence.testing.jaxb.typevariable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.eclipse.persistence.testing.jaxb.JAXBWithJSONTestCases;

public class ExtendedList8ChildTestCases extends JAXBWithJSONTestCases {

    private static final String XML = "org/eclipse/persistence/testing/jaxb/typevariable/extendedList.xml";
    private static final String JSON = "org/eclipse/persistence/testing/jaxb/typevariable/extendedList.json";

    public ExtendedList8ChildTestCases(String name) throws Exception {
        super(name);
        setControlDocument(XML);
        setControlJSON(JSON);
        setClasses(new Class[] {ExtendedList8ChildRoot.class});
    }

    @Override
    protected ExtendedList8ChildRoot getControlObject() {
        ExtendedList8Child list = new ExtendedList8Child();
        list.add(new ExtendedList8Impl());
        list.add(new ExtendedList8Impl());
        ExtendedList8ChildRoot control = new ExtendedList8ChildRoot();
        control.foo = list;
        return control;
    }

    public void testRI() throws Exception{
        JAXBContext ctx = JAXBContext.newInstance(new Class[]{ExtendedList8ChildRoot.class});
        System.out.println(ctx.getClass());
        Marshaller m = ctx.createMarshaller();
        m.marshal(getControlObject(), System.out);
    }

}
