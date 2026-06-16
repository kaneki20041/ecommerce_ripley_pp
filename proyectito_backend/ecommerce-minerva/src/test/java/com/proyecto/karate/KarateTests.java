package com.proyecto.karate;

import com.intuit.karate.junit5.Karate;

class KarateTests {

    @Karate.Test
    Karate testAll() {
        return Karate.run("classpath:categories.feature");
    }
}
