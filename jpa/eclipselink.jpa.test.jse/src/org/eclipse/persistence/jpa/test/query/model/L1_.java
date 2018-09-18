/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2018 IBM Corporation. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     MM/DD/2018-master Joe Grassel
//       - ??????: ???


package org.eclipse.persistence.jpa.test.query.model;

import java.util.Date;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(L1.class)
public class L1_ {
    public static volatile SingularAttribute<L1, Integer> id;
    public static volatile SingularAttribute<L1, Date> date;
    public static volatile SingularAttribute<L1, String> name;
    public static volatile SingularAttribute<L1, L2> l2;
}
