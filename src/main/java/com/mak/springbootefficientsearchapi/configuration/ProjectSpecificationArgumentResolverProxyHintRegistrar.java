package com.mak.springbootefficientsearchapi.configuration;

import net.kaczmarzyk.spring.data.jpa.nativeimage.SpecificationArgumentResolverProxyHintRegistrar;

class ProjectSpecificationArgumentResolverProxyHintRegistrar extends SpecificationArgumentResolverProxyHintRegistrar {
    protected  ProjectSpecificationArgumentResolverProxyHintRegistrar() {
        super(
                "com.mak.springbootefficientsearchapi.controller" // the name of package containing the interfaces with specification definitions
        );
    }
}
