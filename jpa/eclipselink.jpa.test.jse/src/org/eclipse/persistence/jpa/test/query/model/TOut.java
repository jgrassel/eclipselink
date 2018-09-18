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

public class TOut {
    private Integer id;
    private L2 l2;
    
    public TOut(Integer id, L2 l2) {
        super();
        this.id = id;
        this.l2 = l2;
    }
    
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public L2 getL2() {
        return l2;
    }
    public void setL2(L2 l2) {
        this.l2 = l2;
    }

    @Override
    public String toString() {
        return "TOut [id=" + id + ", l2=" + l2 + "]";
    }
    
    
}
