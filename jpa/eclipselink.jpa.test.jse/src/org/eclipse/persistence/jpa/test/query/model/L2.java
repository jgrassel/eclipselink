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

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class L2 {
    @Id @Column(name="ID")
    private Integer id;
    
    @Column(name="NAME")
    private String name;
    
    @OneToMany
    private List<L1> l1;
    
    public L2() {
        
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<L1> getL1() {
        return l1;
    }

    public void setL1(List<L1> l1) {
        this.l1 = l1;
    }

    @Override
    public String toString() {
        String L1Result = "<null>";
        if (l1 != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            if (l1.size() != 0) {
                for (L1 anL1: l1) {
                    sb.append("L1 [id=" + anL1.getId() + 
                            ", name=" + anL1.getName() + 
                            ", date=" + anL1.getDate() +
                            "] ");
                }
            }
            sb.append("]");
        }
        return "L2 [id=" + id + ", name=" + name + 
                ", l1 = " + L1Result + 
                "]";
    }
    
    
}
