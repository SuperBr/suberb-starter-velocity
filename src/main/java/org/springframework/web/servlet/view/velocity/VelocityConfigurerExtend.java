package org.springframework.web.servlet.view.velocity;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.core.io.ClassPathResource;

public class VelocityConfigurerExtend extends VelocityConfigurer {
    /**
     * custom library
     */
    private static final String SPRING_MACRO_LIBRARY = "macros.vm";

    @Override
    protected void postProcessVelocityEngine(VelocityEngine velocityEngine) {
        super.postProcessVelocityEngine(velocityEngine);
        if (new ClassPathResource("macros.vm").exists()) {
            velocityEngine.addProperty(VelocityEngine.VM_LIBRARY, SPRING_MACRO_LIBRARY);
        }
    }
}
