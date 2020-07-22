/*******************************************************************************
 * Copyright (c) 1998, 2015 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.indirection;

import java.beans.PropertyChangeListener;

import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.indirection.WeavedAttributeValueHolderInterface;
import org.eclipse.persistence.internal.helper.Helper;
import org.eclipse.persistence.internal.security.PrivilegedAccessHelper;
import org.eclipse.persistence.internal.security.PrivilegedMethodInvoker;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;

import org.eclipse.persistence.descriptors.changetracking.ChangeTracker;

/**
 * A WeavedObjectBasicIndirectionPolicy is used by OneToOne mappings that are LAZY through weaving
 * and which use Property(method) access.
 * 
 * It extends BasicIndirection by providing the capability of calling the set method that was initially
 * mapped in addition to the set method for the weaved valueholder in order to coordinate the value of the
 * underlying property with the value stored in the valueholder
 * 
 * @author Tom Ware
 */
public class WeavedObjectBasicIndirectionPolicy extends BasicIndirectionPolicy {  
    /** Name of the initial set method. */
    protected String setMethodName = null;
    /** Lazily initialized set method based on the set method name. */
    protected transient Method setMethod = null;
    /** Name of the initial get method. */
    protected String getMethodName;
    /** indicates whether the mapping has originally used method access */
    protected boolean hasUsedMethodAccess;
    /** Stores the actual type of the mapping if different from the reference type.  Used for set method invocation*/
    protected String actualTypeClassName = null;
    
    public WeavedObjectBasicIndirectionPolicy(String getMethodName, String setMethodName, String actualTypeClassName, boolean hasUsedMethodAccess) {
        super();
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("JAG: WeavedObjectBasicIndirectionPolicy.ctor() entry/exit:\n");
            sb.append("   arguments:\n");
            sb.append("      getMethodName = ").append(getMethodName).append("\n");
            sb.append("      setMethodName = ").append(setMethodName).append("\n");
            sb.append("      actualTypeClassName = ").append(actualTypeClassName).append("\n");
            sb.append("      hasUsedMethodAccess = ").append(hasUsedMethodAccess).append("\n");
            sb.append("   jagIdent = ").append(jagIdent).append("\n");
        } catch (Throwable t) {} finally {
            System.out.println(sb);
            sb.setLength(0);
        }
        
        this.setMethodName = setMethodName;
        this.getMethodName = getMethodName;
        this.hasUsedMethodAccess = hasUsedMethodAccess;
        this.actualTypeClassName = actualTypeClassName;
    }    
    

    public String getActualTypeClassName() {
        return actualTypeClassName;
    }

    /**
     * Return the "real" attribute value, as opposed to any wrapper.
     * This will trigger the wrapper to instantiate the value. In a weaved policy, this will
     * also call the initial setter method to coordinate the values of the valueholder with
     * the underlying data.
     */
    public Object getRealAttributeValueFromObject(Object object, Object attribute) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("JAG: WeavedObjectBasicIndirectionPolicy.getRealAttributeValueFromObject() entry:\n");
            sb.append("   arguments:\n");
            sb.append("      object = ").append(object).append("\n");
            sb.append("      attribute = ").append(attribute).append("\n");
            sb.append("   jagIdent = ").append(jagIdent).append("\n");
        } catch (Throwable t) {} finally {
            System.out.println(sb);
            sb.setLength(0);
        }
        Object retVal = null;
        try {
            boolean wasInstantiated = attribute != null && attribute instanceof ValueHolderInterface && ((ValueHolderInterface)attribute).isInstantiated();
            Object value = super.getRealAttributeValueFromObject(object, attribute);
            // Provide the indirection policy with a callback that allows it to do any updates it needs as the result of getting the value.
            if (!wasInstantiated && (value != attribute)) {
                //if the attribute was already unwrapped then do not call this method
                updateValueInObject(object, value, attribute);
            }
            retVal = value;
            return value;           
        } finally {
            sb.setLength(0);
            sb.append("JAG: WeavedObjectBasicIndirectionPolicy.getRealAttributeValueFromObject() exit:\n");
            sb.append("      retVal = ").append(retVal).append("\n");
            sb.append("   jagIdent = ").append(jagIdent).append("\n");
            System.out.println(sb);
        }
    }

    /**
     * This method will lazily initialize the set method
     * Lazy initialization occurs to that we are not required to have a handle on
     * the actual class that we are using until runtime.  This helps to satisfy the 
     * weaving requirement that demands that we avoid loading domain classes into
     * the main class loader until after weaving occurs.
     */
    protected Method getSetMethod() {
        if (setMethod == null) {
            ForeignReferenceMapping sourceMapping = (ForeignReferenceMapping)mapping;
            // The parameter type for the set method must always be the return type of the get method.
            Class[] parameterTypes = new Class[1];
            parameterTypes[0] = sourceMapping.getReferenceClass();

            try {
                setMethod = Helper.getDeclaredMethod(sourceMapping.getDescriptor().getJavaClass(), setMethodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                if (actualTypeClassName != null) {
                    try {
                        // try the actual class of the field or property
                        parameterTypes[0] = Helper.getClassFromClasseName(actualTypeClassName, sourceMapping.getReferenceClass().getClassLoader());
                        setMethod = Helper.getDeclaredMethod(sourceMapping.getDescriptor().getJavaClass(), setMethodName, parameterTypes);
                    } catch (NoSuchMethodException nsme) {}
                    if (setMethod != null) {
                        return setMethod;
                    }
                }
                try {
                    // Get the set method type from the get method.
                    Method getMethod = Helper.getDeclaredMethod(sourceMapping.getDescriptor().getJavaClass(), getGetMethodName(), new Class[0]);
                    parameterTypes[0] = getMethod.getReturnType();
                    setMethod = Helper.getDeclaredMethod(sourceMapping.getDescriptor().getJavaClass(), setMethodName, parameterTypes);
                } catch (NoSuchMethodException e2) {                
                    // As a last ditch effort, change the parameter type to Object.class. 
                    // If the model uses generics:
                    //   public T getStuntDouble()
                    //   public void setStuntDouble(T)
                    // The weaved methods will be:
                    //   public Object getStuntDouble() and 
                    //   public void setStuntDouble(Object)
                    try {
                        parameterTypes[0] = Object.class;
                        setMethod = Helper.getDeclaredMethod(sourceMapping.getDescriptor().getJavaClass(), setMethodName, parameterTypes);    
                    } catch (NoSuchMethodException e3) {
                        // Throw the original exception.
                        throw DescriptorException.errorAccessingSetMethodOfEntity(sourceMapping.getDescriptor().getJavaClass(), setMethodName, sourceMapping.getDescriptor(), e);
                    }
                }
            }
        }
        return setMethod;
    }

    
    /**
     * Coordinate the valueholder for this mapping with the underlying property by calling the
     * initial setter method.
     */
    public void updateValueInObject(Object object, Object value, Object attributeValue){
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("JAG: WeavedObjectBasicIndirectionPolicy.updateValueInObject() entry:\n");
            sb.append("   arguments:\n");
            sb.append("      object = ").append(object).append("\n");
            sb.append("      value = ").append(value).append("\n");
            sb.append("      attributeValue = ").append(attributeValue).append("\n");
            sb.append("   jagIdent = ").append(jagIdent).append("\n");
        } catch (Throwable t) {} finally {
            System.out.println(sb);
            sb.setLength(0);
        }
        try {
            setRealAttributeValueInObject(object, value);
            ((WeavedAttributeValueHolderInterface)attributeValue).setIsCoordinatedWithProperty(true);       
        } finally {
            sb.setLength(0);
            sb.append("JAG: WeavedObjectBasicIndirectionPolicy.updateValueInObject() exit:\n");
            sb.append("   jagIdent = ").append(jagIdent).append("\n");
            System.out.println(sb);
        }
        
        
    }
    
    /**
     * Set the value of the appropriate attribute of target to attributeValue.
     * In this case, place the value inside the target's ValueHolder.
     * Change tracking will be turned off when this method is called
     */
    public void setRealAttributeValueInObject(Object target, Object attributeValue) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("JAG: WeavedObjectBasicIndirectionPolicy.setRealAttributeValueInObject() entry:\n");
            sb.append("   arguments:\n");
            sb.append("      target = ").append(target).append("\n");
            sb.append("      attributeValue = ").append(attributeValue).append("\n");
            sb.append("   jagIdent = ").append(jagIdent).append("\n");
        } catch (Throwable t) {} finally {
            System.out.println(sb);
            sb.setLength(0);
        }
        try {
            setRealAttributeValueInObject(target, attributeValue, false);      
        } finally {
            sb.setLength(0);
            sb.append("JAG: WeavedObjectBasicIndirectionPolicy.setRealAttributeValueInObject() exit:\n");
            sb.append("   jagIdent = ").append(jagIdent).append("\n");
            System.out.println(sb);
        }
    }

    /**
     * Set the value of the appropriate attribute of target to attributeValue.
     * In this case, place the value inside the target's ValueHolder.
     * if trackChanges is true, set the value in the object as if the user was setting it.  Allow change tracking to pick up the change.
     */
    public void setRealAttributeValueInObject(Object target, Object attributeValue, boolean trackChanges) {
        final StringBuilder sb = new StringBuilder();
        try {
            sb.append("JAG: WeavedObjectBasicIndirectionPolicy.setRealAttributeValueInObject() entry:\n");
            sb.append("   arguments:\n");
            sb.append("      target = ").append(target).append("\n");
            sb.append("      attributeValue = ").append(attributeValue).append("\n");
            sb.append("      trackChanges = ").append(trackChanges).append("\n");
            sb.append("   jagIdent = ").append(jagIdent).append("\n");
            
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    AccessController.doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            StackTraceElement[] steArr = Thread.currentThread().getStackTrace();
                            sb.append("   Stack Trace:\n");
                            for (StackTraceElement ste : steArr) {
                                sb.append("     ").append(ste).append("\n");
                            }
                            return null;
                        }
                    });
                } catch (Throwable t) {}
            } else {
                StackTraceElement[] steArr = Thread.currentThread().getStackTrace();
                sb.append("   Stack Trace:\n");
                for (StackTraceElement ste : steArr) {
                    sb.append("     ").append(ste).append("\n");
                }
            }
        } catch (Throwable t) {} finally {
            System.out.println(sb);
            sb.setLength(0);
        }
        try {
         // If the target object is using change tracking, it must be disable first to avoid thinking the value changed.
            PropertyChangeListener listener = null;
            ChangeTracker trackedObject = null;
            if (!trackChanges && target instanceof ChangeTracker) {
                trackedObject = (ChangeTracker)target;
                listener = trackedObject._persistence_getPropertyChangeListener();
                trackedObject._persistence_setPropertyChangeListener(null);
            }
            Object[] parameters = new Object[1];
            parameters[0] = attributeValue;
            try {
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()) {
                    try {
                        AccessController.doPrivileged(new PrivilegedMethodInvoker(getSetMethod(), target, parameters));
                    } catch (PrivilegedActionException exception) {
                        Exception throwableException = exception.getException();
                        if (throwableException instanceof IllegalAccessException) {
                            throw DescriptorException.illegalAccessWhileSettingValueThruMethodAccessor(setMethod.getName(), attributeValue, throwableException);
                        } else {
                            throw DescriptorException.targetInvocationWhileSettingValueThruMethodAccessor(setMethod.getName(), attributeValue, throwableException);
                         }
                    }
                } else {
                    PrivilegedAccessHelper.invokeMethod(getSetMethod(), target, parameters);
                }
            } catch (IllegalAccessException exception) {
                throw DescriptorException.illegalAccessWhileSettingValueThruMethodAccessor(setMethod.getName(), attributeValue, exception);
            } catch (IllegalArgumentException exception) {
                  throw DescriptorException.illegalArgumentWhileSettingValueThruMethodAccessor(setMethod.getName(), attributeValue, exception);
            } catch (InvocationTargetException exception) {
                throw DescriptorException.targetInvocationWhileSettingValueThruMethodAccessor(setMethod.getName(), attributeValue, exception);
            } finally {
                if (!trackChanges && trackedObject != null) {
                    trackedObject._persistence_setPropertyChangeListener(listener);
                }
            }
        } finally {
            sb.setLength(0);
            sb.append("JAG: WeavedObjectBasicIndirectionPolicy.setRealAttributeValueInObject() exit:\n");
            sb.append("   jagIdent = ").append(jagIdent).append("\n");
            System.out.println(sb);
        }
    }
    
    public String getGetMethodName() {
        return this.getMethodName;
    }

    public String getSetMethodName() {
        return this.setMethodName;
    }
    
    public boolean hasUsedMethodAccess() {
        return this.hasUsedMethodAccess;
    }
    
    public boolean isWeavedObjectBasicIndirectionPolicy() {
        return true;
    }
}
