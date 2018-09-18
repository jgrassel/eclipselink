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

package org.eclipse.persistence.jpa.test.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.SingularAttribute;

import org.eclipse.persistence.jpa.test.framework.DDLGen;
import org.eclipse.persistence.jpa.test.framework.Emf;
import org.eclipse.persistence.jpa.test.framework.EmfRunner;
import org.eclipse.persistence.jpa.test.framework.Property;
import org.eclipse.persistence.jpa.test.query.model.L1;
import org.eclipse.persistence.jpa.test.query.model.L1_;
import org.eclipse.persistence.jpa.test.query.model.L2;
import org.eclipse.persistence.jpa.test.query.model.TOut;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EmfRunner.class)
public class TestCriteriaAPI {
    @Emf(name = "defaultEMF", 
            classes = { L1.class, L2.class }, 
            createTables = DDLGen.DROP_CREATE,
            properties = { 
                    @Property(name="eclipselink.logging.level", value="FINE"),
                    @Property(name="eclipselink.logging.parameters", value="true"),
                    @Property(name = "eclipselink.cache.shared.default", value = "false"),
            })
    private EntityManagerFactory emf;
    
    private static boolean setup = false;
    
    @Before
    public void setup() {
        System.out.println("****************************************");
        System.out.flush();
        if (setup) return;
        
        System.out.println("Begin setup...");
        System.out.flush();
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            L1 l1 = new L1(); // Will link l1.l2 -> L2(id=1)
            l1.setId(1);
            l1.setDate(new java.util.Date());
            l1.setName("Name");
            em.persist(l1);
            
            L1 l1_2 = new L1(); // Will NOT link l1.l2 -> L2(id=1)
            l1_2.setId(2);
            l1_2.setDate(new java.util.Date());
            l1_2.setName("Name");
            em.persist(l1_2);
            
            L2 l2 = new L2();
            l2.setId(1);
            l2.setName("EMAN");
            l2.setL1(new ArrayList<L1>());
            l2.getL1().add(l1);
            em.persist(l2);
            
            l1.setL2(l2);
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getLocalizedMessage());
        } finally {
            System.out.println("End Setup.");
            System.out.println("##############################");
            System.out.flush();
            setup = true;
            if (em != null) {
                em.close();
            }
        }
    }
    
    @Test
    public void testSimpleL1Fetch_JPQL() throws Exception {
        System.out.println("Running testSimpleL1Fetch_JPQL ...");
        System.out.flush();
        
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            String jpql = 
                    "SELECT "
                    + " e "
                    + " FROM L1 e";
            System.out.println("Executing JPQL: " + jpql);
            TypedQuery<L1> query = em.createQuery(jpql, L1.class);           
            List<L1> resultList = query.getResultList();
            System.out.println("Executed Query.");
            System.out.flush();
            
            Assert.assertNotNull(resultList);
            for (L1 i : resultList) {
                System.out.println(i);
            }
            System.out.flush();
        } finally {
            System.out.println("End testSimpleL1Fetch_JPQL.");
            System.out.flush();
            if (em != null) {
                em.close();
            }
        }
    }
    
    @Test
    public void testSimpleL1Fetch_CAPI() throws Exception {
        System.out.println("Running testSimpleL1Fetch_CAPI...");
        System.out.flush();
        
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            final CriteriaBuilder builder = em.getCriteriaBuilder();
            final CriteriaQuery<L1> query = builder.createQuery(L1.class);
            final Root<L1> root = query.from(L1.class);
            List<L1> resultList = em.createQuery(query).getResultList();
            System.out.println("Executed Query.");
            System.out.flush();
            
            Assert.assertNotNull(resultList);
            for (L1 i : resultList) {
                System.out.println(i);
            }
            System.out.flush();
        } finally {
            System.out.println("End testSimpleL1Fetch_CAPI.");
            System.out.flush();
            if (em != null) {
                em.close();
            }
        }
    }
    
    @Test
    public void testSimpleL1FetchWithLeftJoinFetch_JPQL() throws Exception {
        System.out.println("Running testSimpleL1FetchWithLeftJoinFetch_JPQL ...");
        System.out.flush();
        
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            String jpql = 
                    "SELECT "
                    + " e "
                    + " FROM L1 e " 
                    + " LEFT JOIN FETCH e.l2 f";
            System.out.println("Executing JPQL: " + jpql);
            TypedQuery<L1> query = em.createQuery(jpql, L1.class);
            List<L1> resultList = query.getResultList();
            System.out.println("Executed Query.");
            System.out.flush();
            
            Assert.assertNotNull(resultList);
            for (L1 i : resultList) {
                System.out.println(i);
            }
            System.out.flush();
        } finally {
            System.out.println("End testSimpleL1FetchWithLeftJoinFetch_JPQL.");
            System.out.flush();
            if (em != null) {
                em.close();
            }
        }
    }
    
    @Test
    public void testSimpleL1FetchWithLeftJoinFetch_CAPI() throws Exception {
        System.out.println("Running testSimpleL1FetchWithLeftJoinFetch_CAPI...");
        System.out.flush();
        
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            final CriteriaBuilder builder = em.getCriteriaBuilder();
            final CriteriaQuery<L1> query = builder.createQuery(L1.class);
            final Root<L1> root = query.from(L1.class);
            root.fetch(L1_.l2, JoinType.LEFT);
            List<L1> resultList = em.createQuery(query).getResultList();
            System.out.println("Executed Query.");
            System.out.flush();
            
            Assert.assertNotNull(resultList);
            for (L1 i : resultList) {
                System.out.println(i);
            }
            System.out.flush();
        } finally {
            System.out.println("End testSimpleL1FetchWithLeftJoinFetch_CAPI.");
            System.out.flush();
            if (em != null) {
                em.close();
            }
        }
    }
    
    
    @Test
    public void test001_JAG1_JPQL() throws Exception {
        System.out.println("Running test001_JAG1_JPQL...");
        System.out.flush();
        
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            String jpql = 
                    "SELECT "
                    + " e.l2 "
                    + " FROM L1 e";
            System.out.println("Executing JPQL: " + jpql);
            TypedQuery<L2> query = em.createQuery(jpql, L2.class);
            List<L2> resultList = query.getResultList();
            System.out.println("Executed Query.");
            System.out.flush();
            
            Assert.assertNotNull(resultList);
            for (L2 i : resultList) {
                System.out.println(i);
            }
            System.out.flush();
        } finally {
            System.out.println("End test001_JAG1_JPQL.");
            System.out.flush();
            if (em != null) {
                em.close();
            }
        }
    }
    
    
    
    @Test
    public void test001_JAG1_CAPI() throws Exception {
        System.out.println("Running test001_JAG1_CAPI...");
        System.out.flush();
        
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            final CriteriaBuilder builder = em.getCriteriaBuilder();
            final CriteriaQuery<L2> query = builder.createQuery(L2.class);
            final Root<L1> root = query.from(L1.class);
//            root.fetch(L1_.l2, JoinType.LEFT);
//            final Join<L1, L2> join = root.join(L1_.l2);
            final Join<L1, L2> join = root.join(L1_.l2, JoinType.LEFT);
            CriteriaQuery<L2> cq = query.select(root.get(L1_.l2));
//            final Join<L1, L2> join = root.join(L1_.l2, JoinType.LEFT);
            List<L2> resultList = em.createQuery(cq).getResultList();
            System.out.println("Executed Query.");
            System.out.flush();
       
      /*
       ############################## Join before CriteriaQuery<L2> cq = query.select(root.get(L1_.l2));
Running test001_JAG1_CAPI...
[EL Fine]: sql: 2018-08-16 16:18:21.125--ServerSession(1908153060)--Connection(1707582034)--Thread(Thread[main,5,main])--SELECT t0.ID, t0.NAME FROM L2 t0, L2 t2, L1 t1 WHERE (t0.ID = t1.ID_L2)
Executed Query.
[EL Fine]: sql: 2018-08-16 16:18:21.159--ServerSession(1908153060)--Connection(1707582034)--Thread(Thread[main,5,main])--SELECT t1.ID, t1.DATE, t1.NAME, t1.ID_L2 FROM L2_L1 t0, L1 t1 WHERE ((t0.L2_ID = ?) AND (t1.ID = t0.l1_ID))
    bind => [1]
L2 [id=1, name=EMAN, l1 = <null>]
End test001_JAG1_CAPI.
****************************************
*
       ############################## Join after CriteriaQuery<L2> cq = query.select(root.get(L1_.l2));
Running test001_JAG1_CAPI...
[EL Fine]: sql: 2018-08-16 16:15:51.37--ServerSession(1908153060)--Connection(1707582034)--Thread(Thread[main,5,main])--SELECT t0.ID, t0.NAME FROM L1 t1 LEFT OUTER JOIN L2 t2 ON (t2.ID = t1.ID_L2), L2 t3, L2 t0 WHERE (t0.ID = t1.ID_L2)
Executed Query.
[EL Fine]: sql: 2018-08-16 16:15:51.417--ServerSession(1908153060)--Connection(1707582034)--Thread(Thread[main,5,main])--SELECT t1.ID, t1.DATE, t1.NAME, t1.ID_L2 FROM L2_L1 t0, L1 t1 WHERE ((t0.L2_ID = ?) AND (t1.ID = t0.l1_ID))
    bind => [1]
L2 [id=1, name=EMAN, l1 = <null>]
End test001_JAG1_CAPI.
       * 
       */
            Assert.assertNotNull(resultList);
            for (L2 i : resultList) {
                System.out.println(i);
            }
            System.out.flush();
        } finally {
            System.out.println("End test001_JAG1_CAPI.");
            System.out.flush();
            if (em != null) {
                em.close();
            }
        }
    }
    
    @Test
    public void test002_JAG1_CAPI() throws Exception {
        System.out.println("Running test002_JAG1_CAPI...");
        System.out.flush();
        
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            final CriteriaBuilder builder = em.getCriteriaBuilder();
            final CriteriaQuery<L1> query = builder.createQuery(L1.class);
            final Root<L1> root = query.from(L1.class);
            root.fetch(L1_.l2, JoinType.LEFT);
            List<L1> resultList = em.createQuery(query).getResultList();
            System.out.println("Executed Query.");
            System.out.flush();
            
            Assert.assertNotNull(resultList);
            for (L1 i : resultList) {
                System.out.println(i);
            }
            System.out.flush();
        } finally {
            System.out.println("End test002_JAG1_CAPI.");
            System.out.flush();
            if (em != null) {
                em.close();
            }
        }
    }
    
    @Test
    public void test001_JPQL() throws Exception {
        System.out.println("Running test001_JPQL...");
        System.out.flush();
        
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            String jpql = 
                    "SELECT "
                    + " NEW org.eclipse.persistence.jpa.test.query.model.TOut "
                    + " ("
                      + "e.id, "
                      + "e.l2"
                    + " ) "
                    + " FROM L1 e";
            TypedQuery<TOut> query = em.createQuery(jpql, TOut.class);
            List<TOut> resultList = query.getResultList();
            System.out.println("Executed Query.");
            System.out.flush();
            
            Assert.assertNotNull(resultList);
            for (TOut i : resultList) {
                System.out.println(i);
            }
            System.out.flush();
        } finally {
            System.out.println("End test001_JPQL.");
            System.out.flush();
            if (em != null) {
                em.close();
            }
        }
    }
    
    @Test
    public void test001_CAPI() throws Exception {
        System.out.println("Running test001_CAPI...");
        System.out.flush();
        
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            final CriteriaBuilder builder = em.getCriteriaBuilder();
            final CriteriaQuery<TOut> query = builder.createQuery(TOut.class);
            final Root<L1> root = query.from(L1.class);
//            final Join<L1, L2> join = root.join(L1_.l2, JoinType.LEFT);  // Placement here doesn't seem to work
//            final Fetch<L1, L2> fetch = root.fetch(L1_.l2, JoinType.LEFT);
            final CompoundSelection<TOut> selection = builder.construct(TOut.class, root.get(L1_.id), root.get(L1_.l2));
//            final Join<L1, L2> join = root.join(L1_.l2, JoinType.LEFT);
            
            CriteriaQuery<TOut> cq = query.select(selection);
            final Fetch<L1, L2> fetch = root.fetch(L1_.l2, JoinType.LEFT);

//            final Join<L1, L2> join = root.join(L1_.l2, JoinType.LEFT);  // Placement here works (sorta)
//            final Fetch<L1, L2> fetch = root.fetch(L1_.l2); //, JoinType.LEFT);
//            cq.getRoots().iterator().next().join("l2", JoinType.LEFT);
            
            List<TOut> resultList = em.createQuery(cq).getResultList();
            System.out.println("Executed Query.");
            System.out.flush();
            
            Assert.assertNotNull(resultList);
            for (TOut i : resultList) {
                System.out.println(i);
            }
            System.out.flush();
        } finally {
            System.out.println("End test001_CAPI.");
            System.out.flush();
            if (em != null) {
                em.close();
            }
        }
    }
    
}


//{
//System.out.println("(Before) Roots: ");
//for (Root r : cq.getRoots()) {
//  System.out.println("  " + r + " alias = " + r.getAlias() + " java-type: " + r.getJavaType());
//  System.out.println("  Model: " + r.getModel());
//  System.out.println("  Fetches:");
//  Iterator<Fetch> i = r.getFetches().iterator();
//  while (i.hasNext()) {
//      Fetch f = i.next();
//      System.out.println("    " + f + " / " + f.getJoinType());
//  }
//  System.out.println("  Joins:");
//  Iterator<Join> k = r.getJoins().iterator();
//  while (i.hasNext()) {
//      Join j = k.next();
//      System.out.println("    " + j + " / " + j.getJoinType());
//  }
//}
//}
