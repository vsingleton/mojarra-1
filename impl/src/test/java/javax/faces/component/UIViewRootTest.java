/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package javax.faces.component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.faces.application.Application;
import javax.faces.application.ProjectStage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PostConstructViewMapEvent;
import javax.faces.event.PreDestroyViewMapEvent;
import javax.servlet.http.HttpSession;
import org.easymock.EasyMock;
import org.junit.Test;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class UIViewRootTest {

    @Test
    public void testViewMapPostConstructViewMapEvent() {
        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        Application application = EasyMock.createMock(Application.class);
        ExternalContext externalContext = EasyMock.createMock(ExternalContext.class);
        HttpSession httpSession = EasyMock.createMock(HttpSession.class);
        
        setFacesContext(facesContext);

        expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        expect(externalContext.getApplicationMap()).andReturn(null).anyTimes();
        replay(facesContext, externalContext);
        UIViewRoot viewRoot = new UIViewRoot();
        verify(facesContext, externalContext);
        
        reset(facesContext, externalContext);
        expect(facesContext.getApplication()).andReturn(application).anyTimes();
        expect(application.getProjectStage()).andReturn(ProjectStage.UnitTest);
        application.publishEvent(facesContext, PostConstructViewMapEvent.class, UIViewRoot.class, viewRoot);
        replay(facesContext, application, externalContext, httpSession);
        Map<String, Object> viewMap = viewRoot.getViewMap();
        assertNotNull(viewMap);
        verify(facesContext, application, externalContext, httpSession);

        setFacesContext(null);
    }

    @Test
    public void testViewMapPreDestroyViewMapEvent() {
        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        Application application = EasyMock.createMock(Application.class);
        ExternalContext externalContext = EasyMock.createMock(ExternalContext.class);
        HttpSession httpSession = EasyMock.createMock(HttpSession.class);
        
        setFacesContext(facesContext);
        
        expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        expect(externalContext.getApplicationMap()).andReturn(null).anyTimes();
        replay(facesContext, externalContext);
        UIViewRoot viewRoot = new UIViewRoot();
        verify(facesContext, externalContext);
        
        reset(facesContext, externalContext);
        expect(facesContext.getApplication()).andReturn(application).anyTimes();
        expect(application.getProjectStage()).andReturn(ProjectStage.UnitTest);
        application.publishEvent(facesContext, PostConstructViewMapEvent.class, UIViewRoot.class, viewRoot);
        expect(facesContext.getViewRoot()).andReturn(viewRoot);
        application.publishEvent(facesContext, PreDestroyViewMapEvent.class, UIViewRoot.class,  viewRoot);
        expect(facesContext.getViewRoot()).andReturn(viewRoot);
        application.publishEvent(facesContext, PreDestroyViewMapEvent.class, UIViewRoot.class,  viewRoot);

        replay(facesContext, application, externalContext, httpSession);
        
        Map<String, Object> viewMap = viewRoot.getViewMap();
        assertNotNull(viewMap);
        viewRoot.getViewMap().clear();
        viewRoot.getViewMap().clear();
        
        verify(facesContext, application, externalContext, httpSession);
        
        setFacesContext(null);
    }
    
    @Test
    public void testViewMapSaveAndRestoreState() {
        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        Application application = EasyMock.createMock(Application.class);
        ExternalContext externalContext = EasyMock.createMock(ExternalContext.class);
        HttpSession httpSession = EasyMock.createMock(HttpSession.class);
        HashMap<Object, Object> attributes = new HashMap<Object, Object>();
        HashMap<String, Object> sessionMap = new HashMap<String, Object>();

        setFacesContext(facesContext);
                
        expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        expect(externalContext.getApplicationMap()).andReturn(null).anyTimes();
        replay(facesContext, externalContext);
        UIViewRoot viewRoot1 = new UIViewRoot();
        verify(facesContext, externalContext);        
        reset(facesContext, externalContext);
        
        expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        expect(externalContext.getApplicationMap()).andReturn(null).anyTimes();
        replay(facesContext, externalContext);
        UIViewRoot viewRoot2 = new UIViewRoot();
        verify(facesContext, externalContext);        
        reset(facesContext, externalContext);
                
        expect(facesContext.getAttributes()).andReturn(attributes).anyTimes();
        expect(facesContext.getApplication()).andReturn(application).anyTimes();
        expect(application.getProjectStage()).andReturn(ProjectStage.UnitTest).anyTimes();
        expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        expect(externalContext.getSessionMap()).andReturn(sessionMap).anyTimes();
        application.publishEvent(facesContext, PostConstructViewMapEvent.class, UIViewRoot.class,  viewRoot1);
        replay(facesContext, application, externalContext, httpSession);
        Map<String, Object> viewMap = viewRoot1.getViewMap();
        viewMap.put("one", "one");
        Object saved = viewRoot1.saveState(facesContext);
        
        /*
         * Simulate our ViewMapListener.
         */
        Map<String, Object> viewMaps = new HashMap<String, Object>();
        viewMaps.put((String) viewRoot1.getTransientStateHelper().getTransient("com.sun.faces.application.view.viewMapId"), viewMap);
        sessionMap.put("com.sun.faces.application.view.activeViewMaps", viewMaps);
        
        viewRoot2.restoreState(facesContext, saved);
        viewMap = viewRoot2.getViewMap();
        assertEquals("one", viewMap.get("one"));
        verify(facesContext, application, externalContext, httpSession);
        
        setFacesContext(null);
    }
    
    private void setFacesContext(FacesContext facesContext) {
        try {
            Method method = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
            method.setAccessible(true);
            method.invoke(null, facesContext);
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
    }
}
