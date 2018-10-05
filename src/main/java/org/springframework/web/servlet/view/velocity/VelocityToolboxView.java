/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.view.velocity;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.view.ToolboxManager;
import org.apache.velocity.tools.view.ViewToolContext;
import org.apache.velocity.tools.view.context.ChainedContext;
import org.apache.velocity.tools.view.servlet.ServletToolboxManager;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * {@link VelocityView} subclass which adds support for Velocity Tools toolboxes
 * and Velocity Tools ViewTool callbacks / Velocity Tools 1.3 init methods.
 * <p>
 * <p>Specify a "toolboxConfigLocation", for example "/WEB-INF/toolbox.xml",
 * to automatically load a Velocity Tools toolbox definition file and expose
 * all defined tools in the specified scopes. If no config location is
 * specified, no toolbox will be loaded and exposed.
 * <p>
 * <p>This view will always create a special Velocity context, namely an
 * instance of the ChainedContext class which is part of the view package
 * of Velocity tools. This allows to use tools from the view package of
 * Velocity Tools, like LinkTool, which need to be initialized with a special
 * context that implements the ViewContext interface (i.e. a ChainedContext).
 * <p>
 * <p>This view also checks tools that are specified as "toolAttributes":
 * If they implement the ViewTool interface, they will get initialized with
 * the Velocity context. This allows tools from the view package of Velocity
 * Tools, such as LinkTool, to be defined as
 * {@link #setToolAttributes "toolAttributes"} on a VelocityToolboxView,
 * instead of in a separate toolbox XML file.
 * <p>
 * <p>This is a separate class mainly to avoid a required dependency on
 * the view package of Velocity Tools in {@link VelocityView} itself.
 * As of Spring 3.0, this class requires Velocity Tools 1.3 or higher.
 *
 * @author Juergen Hoeller
 * @see #setToolboxConfigLocation
 * @see #initTool
 * @see org.apache.velocity.tools.view.context.ViewContext
 * @see ChainedContext
 * @since 1.1.3
 * @deprecated as of Spring 4.3, in favor of FreeMarker
 */
@Deprecated
public class VelocityToolboxView extends VelocityView {

    private String toolboxConfigLocation;


    /**
     * Set a Velocity Toolbox config location, for example "/WEB-INF/toolbox.xml",
     * to automatically load a Velocity Tools toolbox definition file and expose
     * all defined tools in the specified scopes. If no config location is
     * specified, no toolbox will be loaded and exposed.
     * <p>The specified location string needs to refer to a ServletContext
     * resource, as expected by ServletToolboxManager which is part of
     * the view package of Velocity Tools.
     *
     * @see ServletToolboxManager#getInstance
     */
    public void setToolboxConfigLocation(String toolboxConfigLocation) {
        this.toolboxConfigLocation = toolboxConfigLocation;
    }

    /**
     * Return the Velocity Toolbox config location, if any.
     */
    protected String getToolboxConfigLocation() {
        return this.toolboxConfigLocation;
    }


    /**
     * Overridden to create a ChainedContext, which is part of the view package
     * of Velocity Tools, as special context. ChainedContext is needed for
     * initialization of ViewTool instances.
     *
     * @see #initTool
     * <p>
     * support velocity tool 2.0
     */
    @Override
    protected Context createVelocityContext(Map<String, Object> model,
                                            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ViewToolContext ctx;

        ctx = new ViewToolContext(getVelocityEngine(), request, response,
                getServletContext());

        ctx.putAll(model);

        if (this.getToolboxConfigLocation() != null) {
            ToolManager tm = new ToolManager();
            tm.setVelocityEngine(getVelocityEngine());
            tm.configure(getServletContext().getRealPath(
                    getToolboxConfigLocation()));
            if (tm.getToolboxFactory().hasTools(Scope.REQUEST)) {
                ctx.addToolbox(tm.getToolboxFactory().createToolbox(
                        Scope.REQUEST));
            }
            if (tm.getToolboxFactory().hasTools(Scope.APPLICATION)) {
                ctx.addToolbox(tm.getToolboxFactory().createToolbox(
                        Scope.APPLICATION));
            }
            if (tm.getToolboxFactory().hasTools(Scope.SESSION)) {
                ctx.addToolbox(tm.getToolboxFactory().createToolbox(
                        Scope.SESSION));
            }
        }
        return ctx;
    }


    /**
     * Overridden to check for the ViewContext interface which is part of the
     * view package of Velocity Tools. This requires a special Velocity context,
     * like ChainedContext as set up by {@link #createVelocityContext} in this class.
     */
    @Override
    protected void initTool(Object tool, Context velocityContext) throws Exception {
        // Velocity Tools 1.3: a class-level "init(Object)" method.
        Method initMethod = ClassUtils.getMethodIfAvailable(tool.getClass(), "init", Object.class);
        if (initMethod != null) {
            ReflectionUtils.invokeMethod(initMethod, tool, velocityContext);
        }
    }

}
