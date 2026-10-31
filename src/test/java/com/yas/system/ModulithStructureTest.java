package com.yas.system;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithStructureTest {

    @Test
    void verifyModulithStructure() {
        ApplicationModules modules = ApplicationModules.of(Application.class);
        System.out.println(modules);
        modules.verify();
    }
}

