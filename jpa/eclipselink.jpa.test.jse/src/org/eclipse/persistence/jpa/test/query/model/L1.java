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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class L1 {
    @Id @Column(name="ID")
    private Integer id;
    
    private Date date;
    
    @Column(name="NAME")
    private String name;
    
    @ManyToOne
    @JoinColumn(name="ID_L2", referencedColumnName="ID")
    private L2 l2;
    
    public L1() {
        
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

    public L2 getL2() {
        return l2;
    }

    public void setL2(L2 l2) {
        this.l2 = l2;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "L1 [id=" + id + ", date=" + date + ", name=" + name + 
                ", l2 = " + l2 +
                "]";
    }
    
    
}
