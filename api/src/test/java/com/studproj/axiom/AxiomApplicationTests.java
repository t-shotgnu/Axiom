package com.studproj.axiom;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AxiomApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void applicationEntryPointIsSpringBootApplication() {
        assertThat(AxiomApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
    }

}
