/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.upnp.annotation;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vavi.net.upnp.Action;
import vavi.net.upnp.Argument;
import vavi.net.upnp.Device;
import vavi.net.upnp.Service;
import vavi.net.upnp.StateVariable;
import vavi.util.Debug;


/**
 * UPnPFactory. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060908 nsano initial version <br>
 */
public final class UPnPFactory {

    /** */
    private UPnPFactory() {
    }

    /** ActionListener for getter */
    private static class GetterActionListener implements vavi.net.upnp.event.ActionListener {
        String name;
        String argName;
        StateVariable stateVariable;
        Object bean;
        Method method;
        GetterActionListener(String name, String argName, StateVariable stateVariable, Object bean, Method method) {
            this.name = name;
            this.argName = argName;
            this.stateVariable = stateVariable;
            this.bean = bean;
            this.method = method;
Debug.println("getter: " + name + ", " + argName + ", " + stateVariable.getName());
        }
        public boolean actionControlReceived(Action action) {
            String actionName = action.getName();
    
            boolean result = false;

            try {
                if (actionName.equals(name)) {
                    Argument arg = action.getArgument(argName);
                    stateVariable.setValue(String.valueOf(method.invoke(bean)));
                    arg.setValue(stateVariable.getValue());
                    result = true;
                }
            } catch (IOException e) {
e.printStackTrace(System.err);
                result = false;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
    
            return result;
        }
    }

    /** ActionListener for setter */
    private static class SetterActionListener implements vavi.net.upnp.event.ActionListener {
        String name;
        String argName;
        String resultName;
        StateVariable stateVariable;
        Object bean;
        Method method;
        SetterActionListener(String name, String argName, String resultName, StateVariable stateVariable, Object bean, Method method) {
            this.name = name;
            this.argName = argName;
            this.resultName = resultName; 
            this.stateVariable = stateVariable;
            this.bean = bean;
            this.method = method;
Debug.println("setter: " + name + ", " + argName + ", " + resultName + ", " + stateVariable.getName());
        }
        public boolean actionControlReceived(Action action) {
            String actionName = action.getName();
    
            boolean result = false;
    
            try {
                if (actionName.equals(name)) {
                    Argument arg = action.getArgument(argName);
                    stateVariable.setValue(arg.getValue());
                    invokeSetterMethodByString(method, bean, stateVariable.getValue());
        
                    Argument resultArg = action.getArgument(resultName);
                    resultArg.setValue(stateVariable.getValue());
                    result = true;
                }
            } catch (IOException e) {
e.printStackTrace(System.err);
                result = false;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
    
            return result;
        }
    }
    
    /** QueryListener */
    private static class QueryListener implements vavi.net.upnp.event.QueryListener {
        StateVariable stateVariable;
        QueryListener(StateVariable stateVariable) {
            this.stateVariable = stateVariable;
Debug.println("query: " + stateVariable.getName());
        }
        public boolean queryControlReceived(StateVariable stateVar) {
            try {
                stateVar.setValue(stateVariable.getValue());
                return true;
            } catch (IOException e) {
e.printStackTrace(System.err);
                return false;
            }
        }
    }

    /** */
    public static Device getDevice(Object bean) throws IOException {
        //
        UPnPDevice deviceAnnotation = bean.getClass().getAnnotation(UPnPDevice.class);
        if (deviceAnnotation == null) {
            throw new IllegalArgumentException("bean is not annotated with @UPnPDevice");
        }
        String description = UPnPDevice.Util.getDescription(bean);
Debug.println("description: " + description);

        Device device = new Device(UPnPFactory.class.getResource(description));

        Map<String, StateVariable> stateValiavles = new HashMap<>();
        //
        for (Field field : bean.getClass().getDeclaredFields()) {
            UPnPStateVariable stateVariableAnnotation = field.getAnnotation(UPnPStateVariable.class);
            if (stateVariableAnnotation == null) {
                continue;
            }
Debug.println("stateVariable: " + stateVariableAnnotation.name());
            StateVariable stateVariable = device.getStateVariable(stateVariableAnnotation.name());
            stateValiavles.put(stateVariableAnnotation.name(), stateVariable);
            if (stateVariableAnnotation.query()) {
                List<Service> serviceList = device.getServiceList();
                Service service = serviceList.get(0); // TODO 0
                service.setQueryListener(new QueryListener(stateVariable));
            }            
        }

        //
        for (Method method : bean.getClass().getDeclaredMethods()) {
            UPnPAction actionAnnotation = method.getAnnotation(UPnPAction.class);
            if (actionAnnotation == null) {
                continue;
            }
Debug.println("action: " + actionAnnotation.name());

            // TODO is~
            if (method.getName().startsWith("get")) {
                Action getterAction = device.getAction(actionAnnotation.name());
                getterAction.setActionListener(new GetterActionListener(actionAnnotation.name(), actionAnnotation.arg(), stateValiavles.get(actionAnnotation.arg()), bean, method));
            } else if (method.getName().startsWith("set")) {
                Action setterAction = device.getAction(actionAnnotation.name());
                setterAction.setActionListener(new SetterActionListener(actionAnnotation.name(), actionAnnotation.arg(), actionAnnotation.result(), stateValiavles.get(actionAnnotation.arg()), bean, method));
            } else {
Debug.println("@UPnPAction: not setter/getter" + method.getName());
            }
        }
        
        return device;
    }

    /**
     * String to Object conversion on setter method.
     *
     * @param bean bean
     * @param value value が null or empty の場合、
     *        設定先がプリミティブなら 0, false、ラッパークラスならば null
     */
    private static void invokeSetterMethodByString(Method method, Object bean, String value) throws Exception {
        Class<?> fieldClass = method.getParameterTypes()[0];
        if (fieldClass.equals(Boolean.class)) {
            method.invoke(bean, value == null || value.isEmpty() ? null : Boolean.parseBoolean(value));
        } else if (fieldClass.equals(Boolean.TYPE)) {
            method.invoke(bean, value == null || value.isEmpty() ? false : Boolean.parseBoolean(value));
        } else if (fieldClass.equals(Integer.class)) {
            method.invoke(bean, value == null || value.isEmpty() ? null : Integer.parseInt(value));
        } else if (fieldClass.equals(Integer.TYPE)) {
            method.invoke(bean, value == null || value.isEmpty() ? 0 : Integer.parseInt(value));
        } else if (fieldClass.equals(Short.class)) {
            method.invoke(bean, value == null || value.isEmpty() ? null : Short.parseShort(value));
        } else if (fieldClass.equals(Short.TYPE)) {
            method.invoke(bean, value == null || value.isEmpty() ? 0 : Short.parseShort(value));
        } else if (fieldClass.equals(Byte.class)) {
            method.invoke(bean, value == null || value.isEmpty() ? null : Byte.parseByte(value));
        } else if (fieldClass.equals(Byte.TYPE)) {
            method.invoke(bean, value == null || value.isEmpty() ? 0 : Byte.parseByte(value));
        } else if (fieldClass.equals(Long.class)) {
            method.invoke(bean, value == null || value.isEmpty() ? null : Long.parseLong(value));
        } else if (fieldClass.equals(Long.TYPE)) {
            method.invoke(bean, value == null || value.isEmpty() ? 0 : Long.parseLong(value));
        } else if (fieldClass.equals(Float.class)) {
            method.invoke(bean, value == null || value.isEmpty() ? null : Float.parseFloat(value));
        } else if (fieldClass.equals(Float.TYPE)) {
            method.invoke(bean, value == null || value.isEmpty() ? 0 : Float.parseFloat(value));
        } else if (fieldClass.equals(Double.class)) {
            method.invoke(bean, value == null || value.isEmpty() ? null : Double.parseDouble(value));
        } else if (fieldClass.equals(Double.TYPE)) {
            method.invoke(bean, value == null || value.isEmpty() ? 0 : Double.parseDouble(value));
        } else if (fieldClass.equals(Character.class)) {
            method.invoke(bean, value == null || value.isEmpty() ? null : Character.valueOf(value.charAt(0))); // TODO ???
        } else if (fieldClass.equals(Character.TYPE)) {
            method.invoke(bean, value == null || value.isEmpty() ? 0 : Character.valueOf(value.charAt(0))); // TODO ???
        } else {
            method.invoke(bean, value);
        }
    }
}

/* */
